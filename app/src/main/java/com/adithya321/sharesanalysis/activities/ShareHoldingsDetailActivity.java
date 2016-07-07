package com.adithya321.sharesanalysis.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.utils.DateUtils;
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.RealmList;

public class ShareHoldingsDetailActivity extends AppCompatActivity {
    private DatabaseHandler databaseHandler;
    private TextView mainRewardTV;
    private TextView noOfDaysTV;
    private TextView currentValueTV;
    private TextView percentChangeTV;
    private TextView noOfSharesTV;
    private TextView totalValueTV;
    private TextView profitTV;
    private TextView targetProfitTV;
    private TextView rewardTV;
    private Share share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView();
        setValues();
    }

    private void setView() {
        setContentView(R.layout.activity_share_holdings_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.shd_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(getResources().getColor(R.color.blue_700));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        getSupportActionBar().setTitle(StringUtils.getName(getIntent().getStringExtra("name")));

        mainRewardTV = (TextView) findViewById(R.id.shd_main_reward);
        noOfDaysTV = (TextView) findViewById(R.id.shd_no_of_days);
        currentValueTV = (TextView) findViewById(R.id.shd_current_value);
        percentChangeTV = (TextView) findViewById(R.id.shd_percent_change);
        noOfSharesTV = (TextView) findViewById(R.id.shd_no_of_shares);
        totalValueTV = (TextView) findViewById(R.id.shd_total_value);
        profitTV = (TextView) findViewById(R.id.shd_profit);
        targetProfitTV = (TextView) findViewById(R.id.shd_target_profit);
        rewardTV = (TextView) findViewById(R.id.shd_reward);
    }

    private void setValues() {
        databaseHandler = new DatabaseHandler(ShareHoldingsDetailActivity.this);
        List<Share> shareList = databaseHandler.getShares();
        share = new Share();
        for (Share s : shareList) {
            if (s.getName().equals(getIntent().getStringExtra("name"))) {
                share = s;
                break;
            }
        }

        int totalSharesPurchased = 0;
        int totalSharesSold = 0;
        double totalValuePurchased = 0;
        double totalValueSold = 0;
        double averageShareValue = 0;
        double percentChange = 0;
        double currentTotalValue = 0;
        double totalProfit = 0;
        double targetTotalProfit = 0;
        double reward = 0;

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

        if (averageShareValue != 0)
            percentChange = ((share.getCurrentShareValue() - averageShareValue) / averageShareValue) * 100;
        Date today = new Date();
        Date start = share.getDateOfInitialPurchase();
        long noOfDays = DateUtils.getDateDiff(start, today, TimeUnit.DAYS);

        SharedPreferences sharedPreferences = getSharedPreferences("prefs", 0);

        int currentNoOfShares = totalSharesPurchased - totalSharesSold;
        totalProfit = totalValueSold - totalValuePurchased;
        double target = sharedPreferences.getFloat("target", 0);
        targetTotalProfit = (target / 100) * totalValuePurchased * ((double) noOfDays / 365);
        reward = totalProfit - targetTotalProfit;
        currentTotalValue = currentNoOfShares * share.getCurrentShareValue();

        noOfSharesTV.setText(currentNoOfShares + " shares");
        noOfDaysTV.setText(noOfDays + " days");
        mainRewardTV.setText(String.valueOf(NumberUtils.round(reward, 2)));
        rewardTV.setText(String.valueOf(NumberUtils.round(reward, 2)));
        if (reward < 0) {
            mainRewardTV.setTextColor(getResources().getColor((R.color.red_500)));
            rewardTV.setTextColor(getResources().getColor((R.color.red_500)));
        } else {
            mainRewardTV.setTextColor(getResources().getColor((R.color.colorPrimary)));
            rewardTV.setTextColor(getResources().getColor((R.color.colorPrimary)));
        }
        currentValueTV.setText(String.valueOf(NumberUtils.round(share.getCurrentShareValue(), 2)));
        percentChangeTV.setText(String.valueOf(NumberUtils.round(percentChange, 2)));
        if (percentChange < 0)
            percentChangeTV.setTextColor(getResources().getColor((R.color.red_500)));
        else if (percentChange >= target)
            percentChangeTV.setTextColor(getResources().getColor((R.color.colorAccent)));
        else
            percentChangeTV.setTextColor(getResources().getColor((R.color.colorPrimary)));
        totalValueTV.setText(String.valueOf(NumberUtils.round(currentTotalValue, 2)));
        profitTV.setText(String.valueOf(NumberUtils.round(totalProfit, 2)));
        if (totalProfit < 0) {
            profitTV.setTextColor(getResources().getColor((R.color.red_500)));
        } else {
            profitTV.setTextColor(getResources().getColor((R.color.colorPrimary)));
        }
        targetProfitTV.setText(String.valueOf(NumberUtils.round(targetTotalProfit, 2)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_delete:
                new AlertDialog.Builder(this).setTitle("Delete Share")
                        .setMessage("Are you sure you want to delete this share?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                databaseHandler.deleteShare(share);
                                onBackPressed();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Do Nothing
                            }
                        }).show();
                return true;
        }
        return false;
    }
}
