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
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.recyclerviewdrag.ItemTouchHelperAdapter;
import com.adithya321.sharesanalysis.recyclerviewdrag.ItemTouchHelperViewHolder;
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;
import com.robinhood.spark.SparkView;

import java.util.Collections;
import java.util.List;

import io.realm.Realm;

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
        public final SparkView mSparkView;
        public final TextView currentShareValue;

        public ViewHolder(final View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.dashboard_share_name);
            mSparkView = (SparkView) view.findViewById(R.id.spark_view);
            currentShareValue = (TextView) view.findViewById(R.id.dashboard_share_price);

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

        SparkViewAdapter mSparkViewAdapter = new SparkViewAdapter();
        if (viewHolder.mSparkView.getAdapter() != null) {
            mSparkViewAdapter = (SparkViewAdapter) viewHolder.mSparkView.getAdapter();
        }
        mSparkViewAdapter.add((float) share.getCurrentShareValue());
        viewHolder.mSparkView.setAdapter(mSparkViewAdapter);

        viewHolder.currentShareValue.setText(String.valueOf(NumberUtils.round(share.getCurrentShareValue(), 2)));
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
