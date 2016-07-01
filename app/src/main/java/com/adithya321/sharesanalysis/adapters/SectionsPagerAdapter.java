package com.adithya321.sharesanalysis.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.adithya321.sharesanalysis.database.Share;
import com.adithya321.sharesanalysis.fragments.DetailFragment;
import com.adithya321.sharesanalysis.utils.StringUtils;

import java.util.List;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private List<Share> shares;

    public SectionsPagerAdapter(FragmentManager fm, List<Share> shareList) {
        super(fm);
        shares = shareList;
    }

    @Override
    public Fragment getItem(int position) {
        return new DetailFragment().newInstance(shares.get(position).getName());
    }

    @Override
    public int getCount() {
        return shares.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return StringUtils.getCode(shares.get(position).getName());
    }
}