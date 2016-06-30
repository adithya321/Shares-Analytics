package com.adithya321.sharesanalysis.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.backup.RealmBackupRestore;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import io.smooch.ui.ConversationActivity;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("prefs", 0);
        if (sharedPreferences.getBoolean("first", true)) {
            startActivity(new Intent(this, IntroActivity.class));
            return;
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withHeaderBackground(R.drawable.header)
                .withCompactStyle(true)
                .withActivity(this)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(sharedPreferences.getString("name", "Name"))
                                .withEmail("Target : " + sharedPreferences.getFloat("target", 20) + "%")
                                .withIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName("Fund Flow"),
                        new PrimaryDrawerItem().withIdentifier(2).withName("Share Purchase"),
                        new PrimaryDrawerItem().withIdentifier(3).withName("Share Sales"),
                        new PrimaryDrawerItem().withIdentifier(4).withName("Share Holdings"),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(5).withName("Charts"),
                        new PrimaryDrawerItem().withIdentifier(6).withName("Summary"),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(10).withName("Backup & Restore"),
                        new PrimaryDrawerItem().withIdentifier(11).withName("Help"),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(21).withName("Settings").withIcon(R.drawable.ic_settings_gray),
                        new PrimaryDrawerItem().withIdentifier(22).withName("About").withIcon(R.drawable.ic_info_gray)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        boolean flag;
                        if (drawerItem != null) {
                            flag = true;
                            switch ((int) drawerItem.getIdentifier()) {
                                case 10:
                                    final RealmBackupRestore backupRestore = new RealmBackupRestore(MainActivity.this);
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setPositiveButton("Backup", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    backupRestore.backup();
                                                }
                                            })
                                            .setNeutralButton("Restore", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    backupRestore.restore();
                                                    Intent mStartActivity = new Intent(MainActivity.this,
                                                            MainActivity.class);
                                                    int mPendingIntentId = 123456;
                                                    PendingIntent mPendingIntent = PendingIntent
                                                            .getActivity(MainActivity.this,
                                                                    mPendingIntentId, mStartActivity,
                                                                    PendingIntent.FLAG_CANCEL_CURRENT);
                                                    AlarmManager mgr = (AlarmManager) MainActivity.this
                                                            .getSystemService(Context.ALARM_SERVICE);
                                                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                                                            mPendingIntent);
                                                    System.exit(0);
                                                }
                                            }).show();
                                    break;
                                case 11:
                                    ConversationActivity.show(MainActivity.this);
                                    break;

                                case 1:
                                    switchFragment("Fund Flow", "FundFlow");
                                    break;
                                case 2:
                                    switchFragment("Share Purchase", "SharePurchase");
                                    break;
                                case 3:
                                    switchFragment("Share Sales", "ShareSales");
                                    break;
                                case 4:
                                    switchFragment("Share Holdings", "ShareHoldings");
                                    break;
                                case 5:
                                    switchFragment("Charts", "Charts");
                                    break;
                                case 6:
                                    switchFragment("Summary", "Summary");
                                    break;

                                case 21:
                                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                                    break;
                                case 22:
                                    new LibsBuilder()
                                            .withAboutIconShown(true)
                                            .withAboutVersionShown(true)
                                            .withAboutDescription("Developed By Adithya J")
                                            .start(MainActivity.this);
                                    break;

                                default:
                                    switchFragment("Fund Flow", "FundFlow");
                                    break;
                            }
                        } else {
                            flag = false;
                        }
                        return flag;
                    }
                })
                .build();

        if (savedInstanceState == null) {
            drawer.setSelection(1);
        } else {
            drawer.setSelection(savedInstanceState.getInt("drawerSelection"));
        }

        CustomActivityOnCrash.install(this);
    }

    public void switchFragment(String title, String fragment) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.main, Fragment.instantiate(MainActivity.this,
                        "com.adithya321.sharesanalysis.fragments." + fragment + "Fragment"))
                .commit();

        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0) {
            recreate();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt("drawerSelection", drawer.getCurrentSelectedPosition());
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
