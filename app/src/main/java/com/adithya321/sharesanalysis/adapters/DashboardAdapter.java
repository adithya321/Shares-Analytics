package com.adithya321.sharesanalysis.adapters;

import android.content.Context;
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
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private Context mContext;
    private List<Share> mShares;
    private static DashboardAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(DashboardAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public final TextView name;
        public final TextView currentShareValue;
        public final TextView currentnoOfShares;
        public final TextView shareChange;

        public ViewHolder(final View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.dashboard_share_name);
            currentShareValue = (TextView) view.findViewById(R.id.dashboard_share_price);
            currentnoOfShares = (TextView) view.findViewById(R.id.dashboard_current_no_of_shares);
            shareChange = (TextView) view.findViewById(R.id.dashboard_share_change);

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

    public DashboardAdapter(Context context, List<Share> shares) {
        mContext = context;
        mShares = shares;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item_dashboard, parent, false);
        DashboardAdapter.ViewHolder viewHolder = new DashboardAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DashboardAdapter.ViewHolder viewHolder, int position) {
        Share share = mShares.get(position);
        viewHolder.name.setText(StringUtils.getCode(share.getName()));
        viewHolder.currentShareValue.setText(String.valueOf(NumberUtils.round(share.getCurrentShareValue(), 2)));

        int totalSharesPurchased = 0;
        int totalSharesSold = 0;
        double totalValue = 0;
        int currentNoOfShares = 0;
        double averageShareValue = 0;

        RealmList<Purchase> purchases = share.getPurchases();
        for (Purchase purchase : purchases) {
            if (purchase.getType().equals("buy")) {
                totalSharesPurchased += purchase.getQuantity();
                totalValue += (purchase.getQuantity() * purchase.getPrice());
            } else if (purchase.getType().equals("sell")) {
                totalSharesSold += purchase.getQuantity();
            }
        }
        if (totalSharesPurchased != 0)
            averageShareValue = totalValue / totalSharesPurchased;

        Double percentChange = (share.getCurrentShareValue() - averageShareValue) / averageShareValue * 100;
        currentNoOfShares = totalSharesPurchased - totalSharesSold;
        viewHolder.currentnoOfShares.setText(currentNoOfShares + " shares");
        viewHolder.shareChange.setText(NumberUtils.round(percentChange, 2) + "%");
        if (percentChange < 0)
            viewHolder.shareChange.setTextColor(getContext().getResources().getColor((android.R.color.holo_red_dark)));
        else
            viewHolder.shareChange.setTextColor(getContext().getResources().getColor((R.color.colorPrimary)));
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
