package com.adithya321.sharesanalysis.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.recyclerViewDrag.ItemTouchHelperAdapter;
import com.adithya321.sharesanalysis.recyclerViewDrag.ItemTouchHelperViewHolder;
import com.adithya321.sharesanalysis.recyclerViewDrag.OnStartDragListener;
import com.adithya321.sharesanalysis.utils.DateUtils;
import com.adithya321.sharesanalysis.utils.NumberUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmList;

public class ShareHoldingsAdapter extends RecyclerView.Adapter<ShareHoldingsAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private Context mContext;
    private List<Share> mShares;
    private static OnItemClickListener listener;
    private final OnStartDragListener mDragStartListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public final TextView name;
        public final TextView averageShareValue;
        public final TextView currentShareValue;
        public final TextView percentageChange;
        public final TextView noOfDays;
        public final TextView valueSold;
        public final TextView totalProfit;
        public final TextView currentStockValue;
        public final TextView targetTotalProfit;
        public final TextView reward;

        public ViewHolder(final View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.share_name);
            averageShareValue = (TextView) view.findViewById(R.id.average_share_value);
            currentShareValue = (TextView) view.findViewById(R.id.current_share_value);
            percentageChange = (TextView) view.findViewById(R.id.percentage_change);
            noOfDays = (TextView) view.findViewById(R.id.no_of_days);
            valueSold = (TextView) view.findViewById(R.id.value_sold);
            totalProfit = (TextView) view.findViewById(R.id.total_profit);
            currentStockValue = (TextView) view.findViewById(R.id.current_stock_value);
            targetTotalProfit = (TextView) view.findViewById(R.id.target_total_profit);
            reward = (TextView) view.findViewById(R.id.reward);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(view, getLayoutPosition());
                }
            });
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(Color.WHITE);
        }
    }

    public ShareHoldingsAdapter(Context context, List<Share> shares, OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;
        mContext = context;
        mShares = shares;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item_share_holdings, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Share share = mShares.get(position);

        int totalSharesPurchased = 0;
        int totalSharesSold = 0;
        double totalValuePurchased = 0;
        double totalValueSold = 0;
        double averageShareValue = 0;
        double percentageChange = 0;
        double totalProfit = 0;
        double targetTotalProfit = 0;
        double reward = 0;
        double currentStockValue = 0;

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
        if (totalSharesPurchased != 0)
            averageShareValue = totalValuePurchased / totalSharesPurchased;

        percentageChange = ((share.getCurrentShareValue() - averageShareValue) / averageShareValue) * 100;
        Date today = new Date();
        Date start = share.getDateOfInitialPurchase();
        long noOfDays = DateUtils.getDateDiff(start, today, TimeUnit.DAYS);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("prefs", 0);

        int currentNoOfShares = totalSharesPurchased - totalSharesSold;
        totalProfit = totalValueSold - totalValuePurchased;
        currentStockValue = currentNoOfShares * share.getCurrentShareValue();
        double target = sharedPreferences.getFloat("target", 0);
        targetTotalProfit = (target / 100) * totalValuePurchased * ((double) noOfDays / 365);
        reward = totalProfit - targetTotalProfit;

        viewHolder.name.setText(share.getName());
        viewHolder.averageShareValue.setText(String.valueOf(NumberUtils.round(averageShareValue, 2)));
        if (share.getCurrentShareValue() == 0.0)
            viewHolder.currentShareValue.setText("NA");
        else
            viewHolder.currentShareValue.setText(String.valueOf(NumberUtils.round(share.getCurrentShareValue(), 2)));
        viewHolder.percentageChange.setText(String.valueOf(NumberUtils.round(percentageChange, 2)));
        viewHolder.noOfDays.setText(String.valueOf(noOfDays));
        viewHolder.valueSold.setText(String.valueOf(NumberUtils.round(totalValueSold, 2)));
        viewHolder.totalProfit.setText(String.valueOf(NumberUtils.round(totalProfit, 2)));
        viewHolder.currentStockValue.setText(String.valueOf(NumberUtils.round(currentStockValue, 2)));
        viewHolder.targetTotalProfit.setText(String.valueOf(NumberUtils.round(targetTotalProfit, 2)));
        viewHolder.reward.setText(String.valueOf(NumberUtils.round(reward, 2)));
    }

    @Override
    public int getItemCount() {
        return mShares.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        DatabaseHandler databaseHandler = new DatabaseHandler(getContext());
        Realm realm = databaseHandler.getRealmInstance();
        realm.beginTransaction();
        long temp = 999;
        mShares.get(fromPosition).setId(temp);
        mShares.get(toPosition).setId(fromPosition);
        mShares.get(fromPosition).setId(toPosition);
        realm.commitTransaction();
        Collections.swap(mShares, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
}
