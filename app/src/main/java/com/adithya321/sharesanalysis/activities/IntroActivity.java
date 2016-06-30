package com.adithya321.sharesanalysis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.adithya321.sharesanalysis.R;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Intro", "Intro", R.mipmap.ic_launcher, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("Intro", "Intro", R.mipmap.ic_launcher, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("Intro", "Intro", R.mipmap.ic_launcher, getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance("Intro", "Intro", R.mipmap.ic_launcher, getResources().getColor(R.color.colorPrimary)));

        showStatusBar(false);
        setImageSkipButton(null);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        startActivity(new Intent(IntroActivity.this, ProfileActivity.class));
    }
}
