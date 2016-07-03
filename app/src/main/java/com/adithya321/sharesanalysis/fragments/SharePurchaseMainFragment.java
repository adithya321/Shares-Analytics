package com.adithya321.sharesanalysis.fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.TabsAdapter;

public class SharePurchaseMainFragment extends Fragment {

    private ActionBar actionBar;
    private String title;
    private TabLayout tabLayout;
    private Window window;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_share_purchase, container, false);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        title = actionBar.getTitle().toString();
        tabLayout = (TabLayout) root.findViewById(R.id.tabs);
        window = getActivity().getWindow();

        ViewPager viewPager = (ViewPager) root.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setColors();

        return root;
    }

    private void setColors() {
        if (title.equals("Share Purchase")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setStatusBarColor(getResources().getColor(R.color.red_700));
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.red_500)));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.red_500));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        TabsAdapter adapter = new TabsAdapter(getChildFragmentManager());
        if (title.equals("Share Purchase")) {
            adapter.addFragment(new SharePurchaseFragment(), "BY NAME");
            adapter.addFragment(new PurchaseShareFragment(), "BY DATE");
        } else {
            adapter.addFragment(new ShareSalesFragment(), "BY NAME");
            adapter.addFragment(new SalesShareFragment(), "BY DATE");
        }
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(0);
    }
}
