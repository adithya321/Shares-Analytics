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

public class ShareSalesAdapter extends RecyclerView.Adapter<ShareSalesAdapter.ViewHolder>
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
        public final TextView totalSharesSold;
        public final TextView totalValue;
        public final TextView targetSalePrice;
        public final TextView currentShareValue;
        public final TextView currentNoOfShares;
        public final TextView difference;

        public ViewHolder(final View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.share_name);
            totalSharesSold = (TextView) view.findViewById(R.id.total_shares_sold);
            totalValue = (TextView) view.findViewById(R.id.total_value);
            targetSalePrice = (TextView) view.findViewById(R.id.target_sale_price);
            currentShareValue = (TextView) view.findViewById(R.id.current_share_value);
            currentNoOfShares = (TextView) view.findViewById(R.id.current_no_of_shares);
            difference = (TextView) view.findViewById(R.id.difference);

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

    public ShareSalesAdapter(Context context, List<Share> shares) {
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
        View view = inflater.inflate(R.layout.list_item_share_sales, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Share share = mShares.get(position);

        int totalSharesSold = 0;
        int totalSharesPurchased = 0;
        int currentNoOfShares = 0;
        double totalValueSold = 0;
        double totalValuePurchased = 0;
        double averageShareValue = 0;
        double targetSalePrice = 0;
        double difference = 0;

        RealmList<Purchase> purchases = share.getPurchases();
        for (Purchase purchase : purchases) {
            if (purchase.getType().equals("sell")) {
                totalSharesSold += purchase.getQuantity();
                totalValueSold += (purchase.getQuantity() * purchase.getPrice());
            } else if (purchase.getType().equals("buy")) {
                totalSharesPurchased += purchase.getQuantity();
                totalValuePurchased += (purchase.getQuantity() * purchase.getPrice());
            }
        }
        if (totalSharesPurchased != 0)
            averageShareValue = totalValuePurchased / totalSharesPurchased;

        Date today = new Date();
        Date start = share.getDateOfInitialPurchase();
        long noOfDays = DateUtils.getDateDiff(start, today, TimeUnit.DAYS);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("prefs", 0);
        double target = sharedPreferences.getFloat("target", 0);
        targetSalePrice = averageShareValue * Math.pow((1 + (target / 100)), ((double) noOfDays / 365));
        currentNoOfShares = totalSharesPurchased - totalSharesSold;
        difference = share.getCurrentShareValue() - targetSalePrice;

        viewHolder.name.setText(StringUtils.getCode(share.getName()));
        viewHolder.totalSharesSold.setText(totalSharesSold + " shares sold");
        viewHolder.totalValue.setText(String.valueOf(NumberUtils.round(totalValueSold, 2)));
        viewHolder.targetSalePrice.setText(String.valueOf(NumberUtils.round(targetSalePrice, 2)));
        if (share.getCurrentShareValue() == 0.0)
            viewHolder.currentShareValue.setText("NA");
        else
            viewHolder.currentShareValue.setText(String.valueOf(NumberUtils.round(share.getCurrentShareValue(), 2)));
        viewHolder.currentNoOfShares.setText(currentNoOfShares + " shares left");
        viewHolder.difference.setText(String.valueOf(NumberUtils.round(difference, 2)));
        if (difference < 0)
            viewHolder.difference.setTextColor(getContext().getResources().getColor((R.color.red_500)));
        else
            viewHolder.difference.setTextColor(getContext().getResources().getColor((R.color.colorPrimary)));
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