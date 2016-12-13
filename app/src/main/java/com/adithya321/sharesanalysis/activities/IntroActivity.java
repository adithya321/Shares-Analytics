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

package com.adithya321.sharesanalysis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.utils.CustomIntroSlide;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(CustomIntroSlide.newInstance(R.layout.app_intro));
        addSlide(AppIntroFragment.newInstance("Dashboard", "Keep track of all your share details.", R.drawable.drawer, getResources().getColor(R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance("Share Purchase", "Tap the + and enter the purchase details about your new or existing share.", R.drawable.purchase_dialog, getResources().getColor(R.color.red_500)));
        addSlide(AppIntroFragment.newInstance("Share Sales", "Tap the - and enter the sale details about your existing share.", R.drawable.sell_dialog, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("Share Holdings", "Tap any card to see the performance of individual shares. You can also drag and drop to rearrange.", R.drawable.holdings, getResources().getColor(R.color.colorAccent)));

        showStatusBar(false);
        setImageSkipButton(null);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        startActivity(new Intent(IntroActivity.this, ProfileActivity.class));
    }
}
