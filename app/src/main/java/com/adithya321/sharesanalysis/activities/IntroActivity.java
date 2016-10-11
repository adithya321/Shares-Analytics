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
