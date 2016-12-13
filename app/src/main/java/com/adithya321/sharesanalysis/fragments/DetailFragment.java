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

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.SparkViewAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Purchase;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.utils.DateUtils;
import com.adithya321.sharesanalysis.utils.NumberUtils;
import com.adithya321.sharesanalysis.utils.StringUtils;
import com.robinhood.spark.SparkView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.RealmList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.adithya321.sharesanalysis.R.id.detail_difference;
import static com.adithya321.sharesanalysis.R.id.detail_reward;

public class DetailFragment extends Fragment {
    private static final String ARG_SHARE_NAME = "share_name";
    private DatabaseHandler databaseHandler;
    private Share share;

    public DetailFragment newInstance(String shareName) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SHARE_NAME, shareName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        final TextView date = (TextView) rootView.findViewById(R.id.detail_date);
        final TextView open = (TextView) rootView.findViewById(R.id.detail_open);
        final TextView high = (TextView) rootView.findViewById(R.id.detail_high);
        final TextView low = (TextView) rootView.findViewById(R.id.detail_low);
        final TextView last = (TextView) rootView.findViewById(R.id.detail_last);
        final TextView close = (TextView) rootView.findViewById(R.id.detail_close);
        final TextView quantity = (TextView) rootView.findViewById(R.id.detail_quantity);
        final TextView turnover = (TextView) rootView.findViewById(R.id.detail_turnover);

        final SparkView sparkView = (SparkView) rootView.findViewById(R.id.detail_spark_view);

        databaseHandler = new DatabaseHandler(getActivity());
        List<Share> shares = databaseHandler.getShares();
        share = new Share();
        for (Share s : shares) {
            if (s.getName().equals(getArguments().getString(ARG_SHARE_NAME))) {
                share = s;
                break;
            }
        }

        String BASE_URL = "https://www.quandl.com/api/v3/datasets/NSE/";
        String url = BASE_URL.concat(StringUtils.getCode(share.getName()) + ".json");
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("api_key", getString(R.string.quandl_api_key));
        url = urlBuilder.build().toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject j = new JSONObject(responseData);
                    j = j.getJSONObject("dataset");
                    final JSONArray jsonArray = j.getJSONArray("data");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                float[] yData = new float[jsonArray.length()];
                                int j = jsonArray.length() - 1;
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    try {
                                        yData[j] = Float.parseFloat(jsonArray.getJSONArray(i).get(5).toString());
                                    } catch (Exception e) {
                                        //
                                    }
                                    j--;
                                }
                                sparkView.setAdapter(new SparkViewAdapter(yData));

                                date.setText(jsonArray.getJSONArray(0).get(0).toString());
                                open.setText(jsonArray.getJSONArray(0).get(1).toString());
                                high.setText(jsonArray.getJSONArray(0).get(2).toString());
                                low.setText(jsonArray.getJSONArray(0).get(3).toString());
                                last.setText(jsonArray.getJSONArray(0).get(4).toString());
                                close.setText(jsonArray.getJSONArray(0).get(5).toString());
                                quantity.setText(jsonArray.getJSONArray(0).get(6).toString());
                                turnover.setText(jsonArray.getJSONArray(0).get(7).toString());
                            } catch (Exception e) {
                                Log.e("UI Json", e.toString());
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("DetailFragment Json", e.toString());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DetailFragment onFail", e.toString());
            }
        });

        setSharePurchases(rootView);
        setShareSales(rootView);
        setShareHoldings(rootView);

        return rootView;
    }

    private void setSharePurchases(View view) {
        TextView totalSharesPurchasedTV = (TextView) view.findViewById(R.id.detail_total_shares_purchased);
        TextView totalValueTV = (TextView) view.findViewById(R.id.detail_total_purchased_value);
        TextView averageShareValueTV = (TextView) view.findViewById(R.id.detail_average_value);
        TextView dateOfInitialPurchaseTV = (TextView) view.findViewById(R.id.detail_date_of_purchase);

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

        totalSharesPurchasedTV.setText(String.valueOf(totalSharesPurchased));
        totalValueTV.setText(String.valueOf(NumberUtils.round(totalValue, 2)));
        averageShareValueTV.setText(String.valueOf(NumberUtils.round(averageShareValue, 2)));
        dateOfInitialPurchaseTV.setText(date);
    }

    private void setShareSales(View view) {
        TextView totalSharesPurchasedTV = (TextView) view.findViewById(R.id.detail_total_shares_sold);
        TextView totalValueTV = (TextView) view.findViewById(R.id.detail_total_value_sold);
        TextView targetSalePriceTV = (TextView) view.findViewById(R.id.detail_target);
        TextView differenceTV = (TextView) view.findViewById(detail_difference);

        int totalSharesSold = 0;
        int totalSharesPurchased = 0;
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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("prefs", 0);
        double target = sharedPreferences.getFloat("target", 0);
        targetSalePrice = averageShareValue * Math.pow((1 + (target / 100)), ((double) noOfDays / 365));
        difference = share.getCurrentShareValue() - targetSalePrice;
        if (difference < 0)
            differenceTV.setTextColor(getResources().getColor((android.R.color.holo_red_dark)));
        else differenceTV.setTextColor(getResources().getColor((R.color.colorPrimary)));

        totalSharesPurchasedTV.setText(String.valueOf(totalSharesSold));
        totalValueTV.setText(String.valueOf(NumberUtils.round(totalValueSold, 2)));
        targetSalePriceTV.setText(String.valueOf(NumberUtils.round(targetSalePrice, 2)));
        differenceTV.setText(String.valueOf(NumberUtils.round(difference, 2)));
    }

    private void setShareHoldings(View view) {
        TextView percentageChangeTV = (TextView) view.findViewById(R.id.detail__percent_change);
        TextView noOfDaysTV = (TextView) view.findViewById(R.id.detail_no_of_days);
        TextView totalProfitTV = (TextView) view.findViewById(R.id.detail_total_profit);
        TextView currentNoOfSharesTV = (TextView) view.findViewById(R.id.detail_currents_no_of_shares);
        TextView currentStockValueTV = (TextView) view.findViewById(R.id.detail_current_value);
        TextView targetTotalProfitTV = (TextView) view.findViewById(R.id.detail_target_total_profit);
        TextView rewardTV = (TextView) view.findViewById(detail_reward);

        int totalSharesPurchased = 0;
        int totalSharesSold = 0;
        double totalValuePurchased = 0;
        double totalValueSold = 0;
        double averageShareValue = 0;
        double percentageChange = 0;
        double totalProfit = 0;
        double targetTotalProfit = 0;
        double reward = 0;
        double currentStockValue = 0;

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
            percentageChange = ((share.getCurrentShareValue() - averageShareValue) / averageShareValue) * 100;
        Date today = new Date();
        Date start = share.getDateOfInitialPurchase();
        long noOfDays = DateUtils.getDateDiff(start, today, TimeUnit.DAYS);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("prefs", 0);

        int currentNoOfShares = totalSharesPurchased - totalSharesSold;
        totalProfit = totalValueSold - totalValuePurchased;
        currentStockValue = currentNoOfShares * share.getCurrentShareValue();
        double target = sharedPreferences.getFloat("target", 0);
        targetTotalProfit = (target / 100) * totalValuePurchased * ((double) noOfDays / 365);
        reward = totalProfit - targetTotalProfit;
        if (reward < 0)
            rewardTV.setTextColor(getResources().getColor((android.R.color.holo_red_dark)));
        else rewardTV.setTextColor(getResources().getColor((R.color.colorPrimary)));

        currentNoOfSharesTV.setText(String.valueOf(currentNoOfShares));
        percentageChangeTV.setText(String.valueOf(NumberUtils.round(percentageChange, 2)));
        noOfDaysTV.setText(String.valueOf(noOfDays));
        totalProfitTV.setText(String.valueOf(NumberUtils.round(totalProfit, 2)));
        currentStockValueTV.setText(String.valueOf(NumberUtils.round(currentStockValue, 2)));
        targetTotalProfitTV.setText(String.valueOf(NumberUtils.round(targetTotalProfit, 2)));
        rewardTV.setText(String.valueOf(NumberUtils.round(reward, 2)));
    }
}
