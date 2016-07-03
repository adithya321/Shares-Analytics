package com.adithya321.sharesanalysis.fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.adithya321.sharesanalysis.R;

public class ChartsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_charts, container, false);

        Window window = getActivity().getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

        return root;
    }
}
