package com.adithya321.sharesanalysis.adapters;

import android.graphics.RectF;

import com.robinhood.spark.SparkAdapter;

public class SparkViewAdapter extends SparkAdapter {
    private final float[] yData;
    int i = 0;

    public SparkViewAdapter() {
        yData = new float[10];
    }

    public void add(Float y) {
        yData[i] = y;
        i = (i < 9) ? i + 1 : 0;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return yData.length;
    }

    @Override
    public Object getItem(int index) {
        return yData[index];
    }

    @Override
    public float getY(int index) {
        return yData[index];
    }

    @Override
    public float getX(int index) {
        return super.getX(index);
    }

    @Override
    public RectF getDataBounds() {
        return super.getDataBounds();
    }

    @Override
    public boolean hasBaseLine() {
        return false;
    }

    @Override
    public float getBaseLine() {
        return super.getBaseLine();
    }
}
