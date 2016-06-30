package com.adithya321.sharesanalysis.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.utils.NumberUtils;

import java.util.Calendar;
import java.util.List;

import io.realm.RealmList;

public class SharePurchaseAdapter extends RecyclerView.Adapter<SharePurchaseAdapter.ViewHolder> {

    private Context mContext;
    private List<Share> mShares;
    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final TextView totalSharesPurchased;
        public final TextView totalValue;
        public final TextView averageShareValue;
        public final TextView dateOfInitialPurchase;

        public ViewHolder(final View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.share_name);
            totalSharesPurchased = (TextView) view.findViewById(R.id.total_shares_purchased);
            totalValue = (TextView) view.findViewById(R.id.total_value);
            averageShareValue = (TextView) view.findViewById(R.id.average_share_value);
            dateOfInitialPurchase = (TextView) view.findViewById(R.id.date_of_initial_purchase);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(view, getLayoutPosition());
                }
            });
        }
    }

    public SharePurchaseAdapter(Context context, List<Share> shares) {
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
        View view = inflater.inflate(R.layout.list_item_share_purchase, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Share share = mShares.get(position);

        int totalSharesPurchased = 0;
        double totalValue = 0;
        double averageShareValue = 0;

        RealmList<Purchase> purchases = share.getPurchases();
        for (Purchase purchase : purchases) {
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

        viewHolder.name.setText(share.getName());
        viewHolder.totalSharesPurchased.setText(String.valueOf(totalSharesPurchased));
        viewHolder.totalValue.setText(String.valueOf(NumberUtils.round(totalValue, 2)));
        viewHolder.averageShareValue.setText(String.valueOf(NumberUtils.round(averageShareValue, 2)));
        viewHolder.dateOfInitialPurchase.setText(date);
    }

    @Override
    public int getItemCount() {
        return mShares.size();
    }
}
