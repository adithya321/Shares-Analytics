package com.adithya321.sharesanalysis.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;

import java.util.Calendar;
import java.util.List;

public class PurchaseShareAdapter extends RecyclerView.Adapter<PurchaseShareAdapter.ViewHolder> {

    private Context mContext;
    private List<Purchase> mPurchases;
    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView date;
        public final TextView name;
        public final TextView quantity;
        public final TextView price;
        public final TextView value;

        public ViewHolder(final View view) {
            super(view);

            date = (TextView) view.findViewById(R.id.purchase_date);
            name = (TextView) view.findViewById(R.id.purchase_name);
            quantity = (TextView) view.findViewById(R.id.purchase_quantity);
            price = (TextView) view.findViewById(R.id.purchase_price);
            value = (TextView) view.findViewById(R.id.purchase_value);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(view, getLayoutPosition());
                }
            });
        }
    }

    public PurchaseShareAdapter(Context context, List<Purchase> purchases) {
        mContext = context;
        mPurchases = purchases;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item_purchase_share, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Purchase purchase = mPurchases.get(position);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(purchase.getDate());
        String date = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1)
                + "/" + calendar.get(Calendar.YEAR);
        double value = purchase.getPrice() * purchase.getQuantity();

        viewHolder.date.setText(date);
        viewHolder.name.setText(StringUtils.getCode(purchase.getName()));
        viewHolder.quantity.setText(purchase.getQuantity() + " shares");
        viewHolder.price.setText(String.valueOf(NumberUtils.round(purchase.getPrice(), 2)));
        viewHolder.value.setText(String.valueOf(NumberUtils.round(value, 2)));
        if (purchase.getType().equals("buy"))
            viewHolder.value.setTextColor(getContext().getResources().getColor(R.color.red_500));
        else
            viewHolder.value.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public int getItemCount() {
        return mPurchases.size();
    }
}
