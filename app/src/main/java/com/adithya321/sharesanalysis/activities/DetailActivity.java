package com.adithya321.sharesanalysis.activities;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.adapters.SectionsPagerAdapter;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.utils.StringUtils;

import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        int pos = getIntent().getIntExtra("pos", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
        final List<Share> sharesList = databaseHandler.getShares();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(), sharesList);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setTitle(StringUtils.getName(sharesList.get(position).getName()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        TabLayout.Tab tab = tabLayout.getTabAt(pos);
        tab.select();
        getSupportActionBar().setTitle(StringUtils.getName(sharesList.get(pos).getName()));
    }
}
