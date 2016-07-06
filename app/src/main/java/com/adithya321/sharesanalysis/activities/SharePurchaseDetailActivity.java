package com.adithya321.sharesanalysis.activities;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.PurchaseShareAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;

import java.util.Calendar;
import java.util.List;

import io.realm.RealmList;

public class SharePurchaseDetailActivity extends AppCompatActivity {
    private DatabaseHandler databaseHandler;
    private TextView mainTotalValueTV;
    private TextView totalSharesTV;
    private TextView totalValueTV;
    private TextView averageValueTV;
    private TextView dateOfInitialPurchaseTV;
    private RecyclerView purchasesRecyclerView;
    private Share share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView();
        setValues();
    }

    private void setView() {
        setContentView(R.layout.activity_share_purchase_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(getResources().getColor(R.color.red_700));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.red_500)));
        getSupportActionBar().setTitle(StringUtils.getName(getIntent().getStringExtra("name")));

        mainTotalValueTV = (TextView) findViewById(R.id.spd_main_total_value);
        totalSharesTV = (TextView) findViewById(R.id.spd_total_shares);
        totalValueTV = (TextView) findViewById(R.id.spd_total_value);
        averageValueTV = (TextView) findViewById(R.id.spd_average_value);
        dateOfInitialPurchaseTV = (TextView) findViewById(R.id.spd_date);
        purchasesRecyclerView = (RecyclerView) findViewById(R.id.spd_purchase_recycler_view);
    }

    private void setValues() {
        databaseHandler = new DatabaseHandler(getApplicationContext());
        List<Share> shareList = databaseHandler.getShares();
        share = new Share();
        for (Share s : shareList) {
            if (s.getName().equals(getIntent().getStringExtra("name"))) {
                share = s;
                break;
            }
        }

        int totalSharesPurchased = 0;
        double totalValue = 0;
        double averageShareValue = 0;

        RealmList<Purchase> purchases = share.getPurchases();
        for (Purchase purchase : purchases) {
            if (purchase.getType().equals("buy")) {
                totalSharesPurchased += purchase.getQuantity();
                totalValue += (purchase.getQuantity() * purchase.getPrice());
            }
        }
        if (totalSharesPurchased != 0)
            averageShareValue = totalValue / totalSharesPurchased;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(share.getDateOfInitialPurchase());
        String date = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1)
                + "/" + calendar.get(Calendar.YEAR);

        mainTotalValueTV.setText("₹" + NumberUtils.round(totalValue, 2));
        totalSharesTV.setText(totalSharesPurchased + " shares");
        totalValueTV.setText("₹" + NumberUtils.round(totalValue, 2));
        averageValueTV.setText(String.valueOf(NumberUtils.round(averageShareValue, 2)));
        dateOfInitialPurchaseTV.setText(date);

        PurchaseShareAdapter purchaseAdapter = new PurchaseShareAdapter(getApplicationContext(), purchases);
        purchaseAdapter.setOnItemClickListener(new PurchaseShareAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {

            }
        });
        purchasesRecyclerView.setHasFixedSize(true);
        purchasesRecyclerView.setAdapter(purchaseAdapter);
        purchasesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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
