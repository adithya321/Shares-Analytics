/*
 * Shares Analysis
 * Copyright (C) 2016  Adithya J
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.adithya321.sharesanalysis.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.PurchaseShareAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.utils.DateUtils;
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.RealmList;

public class ShareSalesDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private DatabaseHandler databaseHandler;
    private List<Share> shareList;
    private List<Purchase> sellList;
    private TextView mainTotalValueTV;
    private TextView totalSharesTV;
    private TextView averageValueTV;
    private TextView totalValueTV;
    private TextView targetValueTV;
    private TextView currentValueTV;
    private TextView differenceTV;
    private RecyclerView purchasesRecyclerView;
    private Share share;
    private int year_start, month_start, day_start;
    private DatePickerDialog.OnDateSetListener onDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    year_start = year;
                    month_start = monthOfYear + 1;
                    day_start = dayOfMonth;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView();
        setValues();
    }

    private void setView() {
        setContentView(R.layout.activity_share_sales_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.ssd_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        getSupportActionBar().setTitle(StringUtils.getName(getIntent().getStringExtra("name")));

        mainTotalValueTV = (TextView) findViewById(R.id.ssd_main_total_value);
        totalSharesTV = (TextView) findViewById(R.id.ssd_total_shares);
        averageValueTV = (TextView) findViewById(R.id.ssd_average_value);
        totalValueTV = (TextView) findViewById(R.id.ssd_total_value);
        targetValueTV = (TextView) findViewById(R.id.ssd_target_price);
        currentValueTV = (TextView) findViewById(R.id.ssd_current_value);
        differenceTV = (TextView) findViewById(R.id.ssd_difference);
        purchasesRecyclerView = (RecyclerView) findViewById(R.id.ssd_sales_recycler_view);
    }

    private void setValues() {
        databaseHandler = new DatabaseHandler(ShareSalesDetailActivity.this);
        shareList = databaseHandler.getShares();
        share = new Share();
        for (Share s : shareList) {
            if (s.getName().equals(getIntent().getStringExtra("name"))) {
                share = s;
                break;
            }
        }

        int totalSharesSold = 0;
        int totalSharesPurchased = 0;
        double totalValueSold = 0;
        double totalValuePurchased = 0;
        double averagePurchaseValue = 0;
        double averageSaleValue = 0;
        double targetSalePrice = 0;
        double difference = 0;

        sellList = new ArrayList<>();
        RealmList<Purchase> purchaseList = share.getPurchases();
        for (Purchase purchase : purchaseList) {
            if (purchase.getType().equals("sell")) {
                sellList.add(purchase);
                totalSharesSold += purchase.getQuantity();
                totalValueSold += (purchase.getQuantity() * purchase.getPrice());
            } else if (purchase.getType().equals("buy")) {
                totalSharesPurchased += purchase.getQuantity();
                totalValuePurchased += (purchase.getQuantity() * purchase.getPrice());
            }
        }
        if (totalSharesPurchased != 0)
            averagePurchaseValue = totalValuePurchased / totalSharesPurchased;
        if (totalSharesSold != 0)
            averageSaleValue = totalValueSold / totalSharesSold;

        Date today = new Date();
        Date start = share.getDateOfInitialPurchase();
        long noOfDays = DateUtils.getDateDiff(start, today, TimeUnit.DAYS);
        SharedPreferences sharedPreferences = getSharedPreferences("prefs", 0);
        double target = sharedPreferences.getFloat("target", 0);
        targetSalePrice = averagePurchaseValue * Math.pow((1 + (target / 100)), ((double) noOfDays / 365));
        difference = share.getCurrentShareValue() - targetSalePrice;

        mainTotalValueTV.setText(String.valueOf(NumberUtils.round(totalValueSold, 2)));
        totalSharesTV.setText(totalSharesSold + " shares");
        averageValueTV.setText(String.valueOf(NumberUtils.round(averageSaleValue, 2)));
        totalValueTV.setText(String.valueOf(NumberUtils.round(totalValueSold, 2)));
        targetValueTV.setText(String.valueOf(NumberUtils.round(targetSalePrice, 2)));
        currentValueTV.setText(String.valueOf(NumberUtils.round(share.getCurrentShareValue(), 2)));
        differenceTV.setText(String.valueOf(NumberUtils.round(difference, 2)));
        if (difference < 0)
            differenceTV.setTextColor(getResources().getColor((R.color.red_500)));
        else
            differenceTV.setTextColor(getResources().getColor((R.color.colorPrimary)));
        setRecyclerViewAdapter();
    }

    private void setRecyclerViewAdapter() {
        PurchaseShareAdapter salesAdapter = new PurchaseShareAdapter(ShareSalesDetailActivity.this, sellList);
        salesAdapter.setOnItemClickListener(new PurchaseShareAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                final Purchase purchase = sellList.get(position);

                final Dialog dialog = new Dialog(ShareSalesDetailActivity.this);
                dialog.setTitle("Edit Share Sale");
                dialog.setContentView(R.layout.dialog_sell_share_holdings);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.show();

                final Spinner spinner = (Spinner) dialog.findViewById(R.id.existing_spinner);
                ArrayList<String> shares = new ArrayList<>();
                int pos = 0;
                for (int i = 0; i < shareList.size(); i++) {
                    shares.add(shareList.get(i).getName());
                    if (shareList.get(i).getName().equals(purchase.getName()))
                        pos = i;
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(ShareSalesDetailActivity.this,
                        android.R.layout.simple_spinner_item, shares);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                spinner.setSelection(pos);
                spinner.setVisibility(View.VISIBLE);

                final EditText quantity = (EditText) dialog.findViewById(R.id.no_of_shares);
                final EditText price = (EditText) dialog.findViewById(R.id.selling_price);
                quantity.setText(String.valueOf(purchase.getQuantity()));
                price.setText(String.valueOf(purchase.getPrice()));

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(purchase.getDate());
                year_start = calendar.get(Calendar.YEAR);
                month_start = calendar.get(Calendar.MONTH) + 1;
                day_start = calendar.get(Calendar.DAY_OF_MONTH);
                final Button selectDate = (Button) dialog.findViewById(R.id.select_date);
                selectDate.setText(new StringBuilder().append(day_start).append("/")
                        .append(month_start).append("/").append(year_start));
                selectDate.setOnClickListener(ShareSalesDetailActivity.this);

                Button sellShareBtn = (Button) dialog.findViewById(R.id.sell_share_btn);
                sellShareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Purchase p = new Purchase();
                        p.setId(purchase.getId());

                        String stringStartDate = year_start + " " + month_start + " " + day_start;
                        DateFormat format = new SimpleDateFormat("yyyy MM dd", Locale.ENGLISH);
                        try {
                            Date date = format.parse(stringStartDate);
                            p.setDate(date);
                        } catch (Exception e) {
                            Toast.makeText(ShareSalesDetailActivity.this, "Invalid Date", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            p.setQuantity(Integer.parseInt(quantity.getText().toString()));
                        } catch (Exception e) {
                            Toast.makeText(ShareSalesDetailActivity.this, "Invalid Number of Shares", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            p.setPrice(Double.parseDouble(price.getText().toString()));
                        } catch (Exception e) {
                            Toast.makeText(ShareSalesDetailActivity.this, "Invalid Buying Price", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        p.setType("sell");
                        p.setName(spinner.getSelectedItem().toString());
                        databaseHandler.updatePurchase(p);
                        setValues();
                        setRecyclerViewAdapter();
                        dialog.dismiss();
                    }
                });
            }
        });
        purchasesRecyclerView.setHasFixedSize(true);
        purchasesRecyclerView.setAdapter(salesAdapter);
        purchasesRecyclerView.setLayoutManager(new LinearLayoutManager(ShareSalesDetailActivity.this));
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

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.select_date:
                Dialog dialog = new DatePickerDialog(ShareSalesDetailActivity.this, onDateSetListener,
                        year_start, month_start - 1, day_start);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ((TextView) view).setText(new StringBuilder().append(day_start).append("/")
                                .append(month_start).append("/").append(year_start));
                    }
                });
                dialog.show();
                break;
        }
    }
}
