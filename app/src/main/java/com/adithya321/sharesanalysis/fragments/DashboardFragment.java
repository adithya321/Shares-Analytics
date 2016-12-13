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

package com.adithya321.sharesanalysis.fragments;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.activities.DetailActivity;
import com.adithya321.sharesanalysis.adapters.ShareHoldingsAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.recyclerviewdrag.SimpleItemTouchHelperCallback;
import com.adithya321.sharesanalysis.utils.AndroidUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

import io.realm.Realm;

public class DashboardFragment extends Fragment {

    private DatabaseHandler databaseHandler;
    private List<Share> sharesList;
    private ShareHoldingsAdapter mDashboardAdapter;
    private RecyclerView sharesRecyclerView;
    private ItemTouchHelper mItemTouchHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);

        Window window = getActivity().getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        databaseHandler = new DatabaseHandler(getContext());
        sharesRecyclerView = (RecyclerView) root.findViewById(R.id.shares_recycler_view);
        setRecyclerViewAdapter();

        return root;
    }

    private void setRecyclerViewAdapter() {
        sharesList = databaseHandler.getShares();
        mDashboardAdapter = new ShareHoldingsAdapter(getContext(), sharesList);
        mDashboardAdapter.setOnItemClickListener(new ShareHoldingsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("pos", position);
                startActivity(intent);
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

        if (AndroidUtils.isNetworkConnected(getContext())) new CurrentShareValue().execute();
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
            mDashboardAdapter.notifyDataSetChanged();
        }
    }
}
