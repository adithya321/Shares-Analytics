package com.adithya321.sharesanalysis.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.activities.DetailActivity;
import com.adithya321.sharesanalysis.adapters.DashboardAdapter;
import com.adithya321.sharesanalysis.adapters.SparkViewAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.recyclerviewdrag.SimpleItemTouchHelperCallback;
import com.adithya321.sharesanalysis.utils.StringUtils;
import com.robinhood.spark.SparkView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

import io.realm.Realm;

public class DashboardFragment extends Fragment {

    private DatabaseHandler databaseHandler;
    private List<Share> sharesList;
    private DashboardAdapter mDashboardAdapter;
    private RecyclerView sharesRecyclerView;
    private ItemTouchHelper mItemTouchHelper;
    private SparkViewAdapter mSparkViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);

        databaseHandler = new DatabaseHandler(getContext());
        sharesRecyclerView = (RecyclerView) root.findViewById(R.id.shares_recycler_view);
        mSparkViewAdapter = new SparkViewAdapter();
        setRecyclerViewAdapter();

        return root;
    }

    private void setRecyclerViewAdapter() {
        sharesList = databaseHandler.getShares();
        mDashboardAdapter = new DashboardAdapter(getContext(), sharesList);
        mDashboardAdapter.setOnItemClickListener(new DashboardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                SparkView sparkView = (SparkView) itemView.findViewById(R.id.dashboard_spark_view);
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("pos", position);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String transitionName = "spark_view_transition";

                    ActivityOptions transitionActivityOptions = ActivityOptions
                            .makeSceneTransitionAnimation(getActivity(), sparkView, transitionName);
                    startActivity(intent, transitionActivityOptions.toBundle());
                } else {
                    startActivity(intent);
                    getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
                }
            }
        });
        sharesRecyclerView.setAdapter(mDashboardAdapter);
        sharesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        sharesRecyclerView.setHasFixedSize(true);
        sharesRecyclerView.setAdapter(mDashboardAdapter);
        sharesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mDashboardAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(sharesRecyclerView);

        //new CurrentShareValue().execute();
    }

    private class CurrentShareValue extends AsyncTask<Void, Void, Void> {
        private String currentShareValue;
        private DatabaseHandler db;
        private List<Share> shares;

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
            super.onPostExecute(aVoid);
            mDashboardAdapter.notifyDataSetChanged();
            new CurrentShareValue().execute();
        }
    }
}
