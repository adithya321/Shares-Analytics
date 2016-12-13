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