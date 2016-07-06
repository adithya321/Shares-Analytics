package com.adithya321.sharesanalysis.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ShareUtils {
    public static List<String> getNseList(Context context) {
        List<String> nseList = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open("NSE-datasets-codes.csv");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                nseList.add(line);
            }
        } catch (Exception e) {
            Log.e("getNseList()", e.toString());
        }
        return nseList;
    }
}
