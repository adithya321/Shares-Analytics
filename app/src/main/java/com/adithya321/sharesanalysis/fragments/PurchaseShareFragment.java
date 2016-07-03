package com.adithya321.sharesanalysis.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.PurchaseShareAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.RealmList;

public class PurchaseShareFragment extends Fragment {

    private DatabaseHandler databaseHandler;
    private int year_start, month_start, day_start;
    private List<Share> sharesList;
    private List<Purchase> purchaseList;
    private RecyclerView sharePurchasesRecyclerView;
    private ItemTouchHelper mItemTouchHelper;
    private TextView emptyTV;
    private ImageView arrow;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_share_purchase, container, false);

        Window window = getActivity().getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        databaseHandler = new DatabaseHandler(getContext());
        sharePurchasesRecyclerView = (RecyclerView) root.findViewById(R.id.share_purchases_recycler_view);
        emptyTV = (TextView) root.findViewById(R.id.empty);
        arrow = (ImageView) root.findViewById(R.id.arrow);
        setRecyclerViewAdapter();

        FloatingActionButton addPurchaseFab = (FloatingActionButton) root.findViewById(R.id.add_purchase_fab);
        addPurchaseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setTitle("Add Share Purchase");
                dialog.setContentView(R.layout.dialog_add_share_purchase);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.show();

                final AutoCompleteTextView name = (AutoCompleteTextView) dialog.findViewById(R.id.share_name);
                final List<String> nseList = new ArrayList<>();
                try {
                    InputStream inputStream = getActivity().getAssets().open("NSE-datasets-codes.csv");
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        nseList.add(line);
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_dropdown_item_1line, nseList);
                    name.setAdapter(arrayAdapter);
                } catch (Exception e) {
                    Log.e("AutoComplete", e.toString());
                }

                final Spinner spinner = (Spinner) dialog.findViewById(R.id.existing_spinner);

                ArrayList<String> shares = new ArrayList<>();
                for (Share share : sharesList) {
                    shares.add(share.getName());
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, shares);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

                final RadioButton newRB = (RadioButton) dialog.findViewById(R.id.radioBtn_new);
                RadioButton existingRB = (RadioButton) dialog.findViewById(R.id.radioBtn_existing);
                if (shares.size() == 0) existingRB.setVisibility(View.GONE);
                (newRB).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        name.setVisibility(View.VISIBLE);
                        spinner.setVisibility(View.GONE);
                    }
                });

                (existingRB).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        name.setVisibility(View.GONE);
                        spinner.setVisibility(View.VISIBLE);
                    }
                });

                Calendar calendar = Calendar.getInstance();
                year_start = calendar.get(Calendar.YEAR);
                month_start = calendar.get(Calendar.MONTH) + 1;
                day_start = calendar.get(Calendar.DAY_OF_MONTH);
                final Button selectDate = (Button) dialog.findViewById(R.id.select_date);
                selectDate.setText(new StringBuilder().append(day_start).append("/")
                        .append(month_start).append("/").append(year_start));
                selectDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog dialog = new DatePickerDialog(getActivity(), onDateSetListener,
                                year_start, month_start - 1, day_start);
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                selectDate.setText(new StringBuilder().append(day_start).append("/")
                                        .append(month_start).append("/").append(year_start));
                            }
                        });
                        dialog.show();
                    }
                });

                Button addPurchaseBtn = (Button) dialog.findViewById(R.id.add_purchase_btn);
                addPurchaseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Share share = new Share();
                        share.setId(databaseHandler.getNextKey("share"));
                        share.setPurchases(new RealmList<Purchase>());
                        Purchase purchase = new Purchase();

                        if (newRB.isChecked()) {
                            String sName = name.getText().toString().trim();
                            if (sName.equals("")) {
                                Toast.makeText(getActivity(), "Invalid Name", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                share.setName(sName);
                                purchase.setName(sName);
                            }
                        }

                        String stringStartDate = year_start + " " + month_start + " " + day_start;
                        DateFormat format = new SimpleDateFormat("yyyy MM dd", Locale.ENGLISH);
                        try {
                            Date date = format.parse(stringStartDate);
                            share.setDateOfInitialPurchase(date);
                            purchase.setDate(date);
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Invalid Date", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        EditText quantity = (EditText) dialog.findViewById(R.id.no_of_shares);
                        try {
                            purchase.setQuantity(Integer.parseInt(quantity.getText().toString()));
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Invalid Number of Shares", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        EditText price = (EditText) dialog.findViewById(R.id.buying_price);
                        try {
                            purchase.setPrice(Double.parseDouble(price.getText().toString()));
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Invalid Buying Price", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        purchase.setType("buy");
                        if (newRB.isChecked()) {
                            if (!databaseHandler.addShare(share, purchase)) {
                                Toast.makeText(getActivity(), "Share Already Exists", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            databaseHandler.addPurchase(spinner.getSelectedItem().toString(), purchase);
                        }
                        setRecyclerViewAdapter();
                        dialog.dismiss();
                    }
                });
            }
        });

        return root;
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    year_start = year;
                    month_start = monthOfYear + 1;
                    day_start = dayOfMonth;
                }
            };

    private void setRecyclerViewAdapter() {
        sharesList = databaseHandler.getShares();
        purchaseList = databaseHandler.getPurchases();

        if (sharesList.size() < 1) {
            emptyTV.setVisibility(View.VISIBLE);
            arrow.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (getResources().getConfiguration().orientation == 1) {
                    arrow.setBackground(getResources().getDrawable(R.drawable.curved_line_vertical));
                } else {
                    arrow.setBackground((getResources().getDrawable(R.drawable.curved_line_horizontal)));
                }
            }
        } else {
            emptyTV.setVisibility(View.GONE);
            arrow.setVisibility(View.GONE);
        }

        PurchaseShareAdapter purchaseAdapter = new PurchaseShareAdapter(getContext(), purchaseList);
        purchaseAdapter.setOnItemClickListener(new PurchaseShareAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Share share = sharesList.get(position);

                RealmList<Purchase> purchases = share.getPurchases();
                String string = "";
                for (Purchase purchase : purchases) {
                    if (purchase.getType().equals("buy"))
                        string = string.concat(purchase.toString() + "\n\n");
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle(share.getName())
                        .setMessage(string)
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
        sharePurchasesRecyclerView.setHasFixedSize(true);
        sharePurchasesRecyclerView.setAdapter(purchaseAdapter);
        sharePurchasesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) setRecyclerViewAdapter();
    }
}
