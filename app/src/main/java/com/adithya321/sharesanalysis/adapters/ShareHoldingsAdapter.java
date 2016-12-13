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
import com.adithya321.sharesanalysis.recyclerviewdrag.ItemTouchHelperAdapter;
import com.adithya321.sharesanalysis.recyclerviewdrag.ItemTouchHelperViewHolder;
import com.adithya321.sharesanalysis.utils.DateUtils;
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;

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

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public final TextView name;
        public final TextView currentNoOfShares;
        public final TextView currentShareValue;
        public final TextView percentChange;
        public final TextView noOfDays;
        public final TextView reward;

        public ViewHolder(final View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.holdings_share_name);
            currentNoOfShares = (TextView) view.findViewById(R.id.holdings_current_no_of_shares);
            noOfDays = (TextView) view.findViewById(R.id.holdings_days);
            reward = (TextView) view.findViewById(R.id.holdings_reward);
            currentShareValue = (TextView) view.findViewById(R.id.holdings_current_price);
            percentChange = (TextView) view.findViewById(R.id.holdings_percent_change);

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

    public ShareHoldingsAdapter(Context context, List<Share> shares) {
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
        double percentChange = 0;
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

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("prefs", 0);

        int currentNoOfShares = totalSharesPurchased - totalSharesSold;
        totalProfit = totalValueSold - totalValuePurchased;
        double target = sharedPreferences.getFloat("target", 0);
        targetTotalProfit = (target / 100) * totalValuePurchased * ((double) noOfDays / 365);
        reward = totalProfit - targetTotalProfit;

        viewHolder.name.setText(StringUtils.getCode(share.getName()));
        viewHolder.currentNoOfShares.setText(currentNoOfShares + " shares");
        viewHolder.noOfDays.setText(noOfDays + " days");
        viewHolder.reward.setText(String.valueOf(NumberUtils.round(reward, 2)));
        if (reward < 0)
            viewHolder.reward.setTextColor(getContext().getResources().getColor((R.color.red_500)));
        else
            viewHolder.reward.setTextColor(getContext().getResources().getColor((R.color.colorPrimary)));
        if (share.getCurrentShareValue() == 0.0)
            viewHolder.currentShareValue.setText("NA");
        else
            viewHolder.currentShareValue.setText(String.valueOf(NumberUtils.round(share.getCurrentShareValue(), 2)));
        viewHolder.percentChange.setText(String.valueOf(NumberUtils.round(percentChange, 2)));
        if (percentChange < 0)
            viewHolder.percentChange.setTextColor(getContext().getResources().getColor((R.color.red_500)));
        else if (percentChange >= target)
            viewHolder.percentChange.setTextColor(getContext().getResources().getColor((R.color.colorAccent)));
        else
            viewHolder.percentChange.setTextColor(getContext().getResources().getColor((R.color.colorPrimary)));
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
