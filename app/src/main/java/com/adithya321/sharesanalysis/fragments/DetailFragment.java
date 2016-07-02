package com.adithya321.sharesanalysis.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.SparkViewAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.utils.StringUtils;
import com.robinhood.spark.SparkView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailFragment extends Fragment {
    private static final String ARG_SHARE_NAME = "share_name";
    private DatabaseHandler databaseHandler;

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
        Share share = new Share();
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

        return rootView;
    }
}
