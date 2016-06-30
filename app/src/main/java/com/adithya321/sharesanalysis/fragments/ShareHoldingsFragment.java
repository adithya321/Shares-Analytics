package com.adithya321.sharesanalysis.fragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.ShareHoldingsAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.recyclerviewdrag.OnStartDragListener;
import com.adithya321.sharesanalysis.recyclerviewdrag.SimpleItemTouchHelperCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class ShareHoldingsFragment extends Fragment implements OnStartDragListener {

    private DatabaseHandler databaseHandler;
    private List<Share> sharesList;
    private ShareHoldingsAdapter shareHoldingsAdapter;
    private MenuItem actionProgressItem, actionRefreshItem;
    private RecyclerView shareHoldingsRecyclerView;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_share_holdings, container, false);

        databaseHandler = new DatabaseHandler(getContext());
        shareHoldingsRecyclerView = (RecyclerView) root.findViewById(R.id.share_holdings_recycler_view);
        setRecyclerViewAdapter();

        return root;
    }

    private void setRecyclerViewAdapter() {
        sharesList = databaseHandler.getShares();
        shareHoldingsAdapter = new ShareHoldingsAdapter(getContext(), sharesList);
        shareHoldingsAdapter.setOnItemClickListener(new ShareHoldingsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Share share = sharesList.get(position);

                RealmList<Purchase> purchases = share.getPurchases();
                String string = "";
                for (Purchase purchase : purchases) {
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
        shareHoldingsRecyclerView.setAdapter(shareHoldingsAdapter);
        shareHoldingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        shareHoldingsRecyclerView.setHasFixedSize(true);
        shareHoldingsRecyclerView.setAdapter(shareHoldingsAdapter);
        shareHoldingsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(shareHoldingsAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(shareHoldingsRecyclerView);
        new CurrentShareValue().execute();
    }

    private class CurrentShareValue extends AsyncTask<Void, Void, Void> {
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
                    String name = share.getName();
                    String code = "";
                    int j = 0;
                    while (name.charAt(j) != ' ') {
                        code += name.charAt(j);
                        j++;
                    }
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
                shareHoldingsAdapter.notifyDataSetChanged();
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
