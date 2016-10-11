package com.adithya321.sharesanalysis.fragments;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.utils.AndroidUtils;
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class SummaryFragment extends Fragment {

    private MenuItem actionProgressItem, actionRefreshItem;
    private DatabaseHandler databaseHandler;
    private List<Share> sharesList;
    private TickerView currentInvestmentsTV, netWorthTV, potentialProfitTV, targetProfitTV;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_summary, container, false);

        Window window = getActivity().getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        currentInvestmentsTV = (TickerView) root.findViewById(R.id.current_investments);
        netWorthTV = (TickerView) root.findViewById(R.id.net_worth);
        potentialProfitTV = (TickerView) root.findViewById(R.id.potential_profit);
        targetProfitTV = (TickerView) root.findViewById(R.id.target_profit);

        currentInvestmentsTV.setCharacterList(TickerUtils.getDefaultListForUSCurrency());
        netWorthTV.setCharacterList(TickerUtils.getDefaultListForUSCurrency());
        potentialProfitTV.setCharacterList(TickerUtils.getDefaultListForUSCurrency());
        targetProfitTV.setCharacterList(TickerUtils.getDefaultListForUSCurrency());

        currentInvestmentsTV.setText("0");
        netWorthTV.setText("0");
        potentialProfitTV.setText("0");
        targetProfitTV.setText("0");

        databaseHandler = new DatabaseHandler(getContext());
        sharesList = databaseHandler.getShares();
        calculateValues();

        return root;
    }

    private void calculateValues() {
        double currentInvestment = 0;
        double netWorth = 0;

        for (Share share : sharesList) {
            int totalSharesPurchased = 0;
            int totalSharesSold = 0;
            double totalValuePurchased = 0;
            double totalValueSold = 0;
            int currentNoOfShares = 0;
            RealmList<Purchase> purchases = share.getPurchases();
            for (Purchase purchase : purchases) {
                if (purchase.getType().equals("buy")) {
                    totalSharesPurchased += purchase.getQuantity();
                    totalValuePurchased += (purchase.getQuantity() * purchase.getPrice());
                } else if (purchase.getType().equals("sell")) {
                    totalSharesSold += purchase.getQuantity();
                    totalValueSold += (purchase.getQuantity() * purchase.getPrice());
                }
            }
            double averageShareValue = totalValuePurchased / totalSharesPurchased;
            currentNoOfShares += (totalSharesPurchased - totalSharesSold);
            currentInvestment += (currentNoOfShares * averageShareValue);
            netWorth += (currentNoOfShares * share.getCurrentShareValue());
        }

        double potentialProfit = netWorth - currentInvestment;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("prefs", 0);
        double target = sharedPreferences.getFloat("target", 0);
        double targetProfit = currentInvestment * target / 100;

        currentInvestmentsTV.setText(String.valueOf(NumberUtils.round(currentInvestment, 2)));
        netWorthTV.setText(String.valueOf(NumberUtils.round(netWorth, 2)));
        potentialProfitTV.setText(String.valueOf(NumberUtils.round(potentialProfit, 2)));
        targetProfitTV.setText(String.valueOf(NumberUtils.round(targetProfit, 2)));
    }

    private class CurrentShareValue extends AsyncTask<Void, Void, Void> {
        private String currentShareValue;

        @Override
        protected void onPreExecute() {
            try {
                actionProgressItem.setVisible(true);
                actionRefreshItem.setVisible(false);
            } catch (Exception e) {
                Log.e("CurrentShareValue Pre", e.toString());
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (Share share : sharesList) {
                    String code = StringUtils.getCode(share.getName());
                    String url = "https://in.finance.yahoo.com/q?s=" + code + ".NS";
                    Document document = Jsoup.connect(url).followRedirects(true).get();
                    try {
                        currentShareValue = document.getElementById("yfs_l84_" + code.toLowerCase()
                                + ".ns").html();
                        Realm realm = databaseHandler.getRealmInstance();
                        realm.beginTransaction();
                        share.setCurrentShareValue(Double.parseDouble(currentShareValue));
                        realm.copyToRealmOrUpdate(share);
                        realm.commitTransaction();
                    } catch (Exception e) {
                        Log.e("CurrentShareValue do1", e.toString());
                    }
                }
            } catch (Exception e) {
                Log.e("CurrentShareValue do2", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                actionProgressItem.setVisible(false);
                actionRefreshItem.setVisible(true);
            } catch (Exception e) {
                Log.e("CurrentShareValue Post", e.toString());
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.share_sales, menu);
        actionProgressItem = menu.findItem(R.id.action_progress);
        actionRefreshItem = menu.findItem(R.id.action_refresh);
        actionProgressItem.setVisible(true);
        actionRefreshItem.setVisible(false);

        ProgressBar progressBar = (ProgressBar) actionProgressItem.getActionView()
                .findViewById(R.id.pbProgressAction);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(android.R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            if (AndroidUtils.isNetworkConnected(getContext())) new CurrentShareValue().execute();
        }
        return super.onOptionsItemSelected(item);
    }
}
