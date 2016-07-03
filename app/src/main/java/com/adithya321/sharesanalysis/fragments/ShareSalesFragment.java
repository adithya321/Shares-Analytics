package com.adithya321.sharesanalysis.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.ShareSalesAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.recyclerviewdrag.OnStartDragListener;
import com.adithya321.sharesanalysis.recyclerviewdrag.SimpleItemTouchHelperCallback;
import com.adithya321.sharesanalysis.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmList;

public class ShareSalesFragment extends Fragment implements OnStartDragListener {

    private DatabaseHandler databaseHandler;
    private List<Share> sharesList;
    private int year_start, month_start, day_start;
    private RecyclerView salesRecyclerView;
    private ShareSalesAdapter shareSalesAdapter;
    private MenuItem actionProgressItem, actionRefreshItem;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_share_sales, container, false);

        Window window = getActivity().getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(getResources().getColor(R.color.red_700));
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.red_500)));

        databaseHandler = new DatabaseHandler(getContext());
        salesRecyclerView = (RecyclerView) root.findViewById(R.id.sales_recycler_view);
        setRecyclerViewAdapter();

        FloatingActionButton sellShareFab = (FloatingActionButton) root.findViewById(R.id.sell_share_fab);
        sellShareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setTitle("Sell Share Holdings");
                dialog.setContentView(R.layout.dialog_sell_share_holdings);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.show();

                final Spinner spinner = (Spinner) dialog.findViewById(R.id.existing_spinner);

                ArrayList<String> shares = new ArrayList<>();
                for (Share share : sharesList) {
                    shares.add(share.getName());
                }
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, shares);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

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

                Button sellShareBtn = (Button) dialog.findViewById(R.id.sell_share_btn);
                sellShareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Purchase purchase = new Purchase();

                        String stringStartDate = year_start + " " + month_start + " " + day_start;
                        DateFormat format = new SimpleDateFormat("yyyy MM dd", Locale.ENGLISH);
                        try {
                            Date date = format.parse(stringStartDate);
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

                        EditText price = (EditText) dialog.findViewById(R.id.selling_price);
                        try {
                            purchase.setPrice(Double.parseDouble(price.getText().toString()));
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Invalid Selling Price", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        purchase.setType("sell");
                        databaseHandler.addPurchase(spinner.getSelectedItem().toString(), purchase);
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
        shareSalesAdapter = new ShareSalesAdapter(getContext(), sharesList);
        shareSalesAdapter.setOnItemClickListener(new ShareSalesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Share share = sharesList.get(position);

                RealmList<Purchase> purchases = share.getPurchases();
                String string = "";
                for (Purchase purchase : purchases) {
                    if (purchase.getType().equals("sell"))
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
        salesRecyclerView.setAdapter(shareSalesAdapter);
        salesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        salesRecyclerView.setHasFixedSize(true);
        salesRecyclerView.setAdapter(shareSalesAdapter);
        salesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(shareSalesAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(salesRecyclerView);
        new CurrentShareValue().execute();
    }

    private class CurrentShareValue extends AsyncTask<Void, Double, Void> {
        private String currentShareValue;
        private DatabaseHandler db;
        private List<Share> shares;

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
            db = new DatabaseHandler(getContext());
            shares = db.getShares();
            try {
                for (int i = 0; i < shares.size(); i++) {
                    Share share = shares.get(i);
                    String code = StringUtils.getCode(share.getName());
                    String url = "https://in.finance.yahoo.com/q?s=" + code + ".NS";
                    Document document = Jsoup.connect(url).followRedirects(true).get();
                    try {
                        currentShareValue = document.getElementById("yfs_l84_" + code.toLowerCase()
                                + ".ns").html();
                        Realm realm = db.getRealmInstance();
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
                shareSalesAdapter.notifyDataSetChanged();
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            new CurrentShareValue().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}