package com.adithya321.sharesanalysis;

import android.app.Application;
import android.support.annotation.NonNull;

import com.adithya321.sharesanalysis.database.DatabaseHandler;

import io.smooch.core.Smooch;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Smooch.init(this, getString(R.string.smooch_app_token));
    }

    @NonNull
    public DatabaseHandler getDBHandler() {
        return new DatabaseHandler(getApplicationContext());
    }
}
