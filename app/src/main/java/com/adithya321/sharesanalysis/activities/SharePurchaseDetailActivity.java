package com.adithya321.sharesanalysis.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.PurchaseShareAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.RealmList;

public class SharePurchaseDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private DatabaseHandler databaseHandler;
    private List<Share> shareList;
    private RealmList<Purchase> purchaseList;
    private TextView mainTotalValueTV;
    private TextView totalSharesTV;
    private TextView totalValueTV;
    private TextView averageValueTV;
    private TextView dateOfInitialPurchaseTV;
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
        setContentView(R.layout.activity_share_purchase_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.spd_toolbar);
        setSupportActionBar(toolbar);
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
        databaseHandler = new DatabaseHandler(SharePurchaseDetailActivity.this);
        shareList = databaseHandler.getShares();
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

        purchaseList = share.getPurchases();
        for (Purchase purchase : purchaseList) {
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
        setRecyclerViewAdapter();
    }

    private void setRecyclerViewAdapter() {
        PurchaseShareAdapter purchaseAdapter = new PurchaseShareAdapter(SharePurchaseDetailActivity.this, purchaseList);
        purchaseAdapter.setOnItemClickListener(new PurchaseShareAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Purchase purchase = purchaseList.get(position);

                final Dialog dialog = new Dialog(SharePurchaseDetailActivity.this);
                dialog.setTitle("Edit Share Purchase");
                dialog.setContentView(R.layout.dialog_add_share_purchase);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.show();

                RadioButton newRB = (RadioButton) dialog.findViewById(R.id.radioBtn_new);
                RadioButton existingRB = (RadioButton) dialog.findViewById(R.id.radioBtn_existing);
                AutoCompleteTextView name = (AutoCompleteTextView) dialog.findViewById(R.id.share_name);
                newRB.setVisibility(View.GONE);
                existingRB.setChecked(true);
                name.setVisibility(View.GONE);

                final Spinner spinner = (Spinner) dialog.findViewById(R.id.existing_spinner);
                ArrayList<String> shares = new ArrayList<>();
                int pos = 0;
                for (int i = 0; i < shareList.size(); i++) {
                    shares.add(shareList.get(i).getName());
                    if (shareList.get(i).getName().equals(purchase.getName()))
                        pos = i;
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(SharePurchaseDetailActivity.this,
                        android.R.layout.simple_spinner_item, shares);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                spinner.setSelection(pos);
                spinner.setVisibility(View.VISIBLE);

                final EditText quantity = (EditText) dialog.findViewById(R.id.no_of_shares);
                final EditText price = (EditText) dialog.findViewById(R.id.buying_price);
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
                selectDate.setOnClickListener(SharePurchaseDetailActivity.this);

                Button addPurchaseBtn = (Button) dialog.findViewById(R.id.add_purchase_btn);
                addPurchaseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Share share = new Share();
                        share.setId(databaseHandler.getNextKey("share"));
                        share.setPurchases(new RealmList<Purchase>());
                        Purchase purchase = new Purchase();
                        purchase.setId(databaseHandler.getNextKey("purchase"));

                        String stringStartDate = year_start + " " + month_start + " " + day_start;
                        DateFormat format = new SimpleDateFormat("yyyy MM dd", Locale.ENGLISH);
                        try {
                            Date date = format.parse(stringStartDate);
                            share.setDateOfInitialPurchase(date);
                            purchase.setDate(date);
                        } catch (Exception e) {
                            Toast.makeText(SharePurchaseDetailActivity.this, "Invalid Date", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            purchase.setQuantity(Integer.parseInt(quantity.getText().toString()));
                        } catch (Exception e) {
                            Toast.makeText(SharePurchaseDetailActivity.this, "Invalid Number of Shares", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            purchase.setPrice(Double.parseDouble(price.getText().toString()));
                        } catch (Exception e) {
                            Toast.makeText(SharePurchaseDetailActivity.this, "Invalid Buying Price", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        purchase.setType("buy");
                        purchase.setName(spinner.getSelectedItem().toString());
                        databaseHandler.updatePurchase(purchase);
                        setRecyclerViewAdapter();
                        dialog.dismiss();
                    }
                });
            }
        });
        purchasesRecyclerView.setHasFixedSize(true);
        purchasesRecyclerView.setAdapter(purchaseAdapter);
        purchasesRecyclerView.setLayoutManager(new LinearLayoutManager(SharePurchaseDetailActivity.this));
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
                Dialog dialog = new DatePickerDialog(SharePurchaseDetailActivity.this, onDateSetListener,
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
