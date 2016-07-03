package com.adithya321.sharesanalysis.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.backup.RealmBackupRestore;
import com.adithya321.sharesanalysis.database.DatabaseHandler;
import com.adithya321.sharesanalysis.database.Share;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.List;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import io.smooch.ui.ConversationActivity;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Drawer drawer;
    private Toolbar toolbar;
    private LibsConfiguration.LibsListener libsListener = new LibsConfiguration.LibsListener() {
        @Override
        public void onIconClicked(View v) {
            Intent github = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/adithya321/Shares-Analytics"));
            startActivity(github);
        }

        @Override
        public boolean onLibraryAuthorClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onLibraryContentClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onLibraryBottomClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onExtraClicked(View v, Libs.SpecialButton specialButton) {
            switch (v.getId()) {
                case R.id.aboutSpecial1:
                    String MARKET_URL = "https://play.google.com/store/apps/details?id=";
                    String PlayStoreListing = getPackageName();
                    Intent rate = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL + PlayStoreListing));
                    startActivity(rate);
                    return true;

                case R.id.aboutSpecial2:
                    String PlayStoreDevAccount = "https://play.google.com/store/apps/developer?id=P.I.M.P.";
                    Intent devPlay = new Intent(Intent.ACTION_VIEW, Uri.parse(PlayStoreDevAccount));
                    startActivity(devPlay);
                    return true;
            }
            return false;
        }

        @Override
        public boolean onIconLongClicked(View v) {
            return false;
        }

        @Override
        public boolean onLibraryAuthorLongClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onLibraryContentLongClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onLibraryBottomLongClicked(View v, Library library) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("prefs", 0);
        if (sharedPreferences.getBoolean("first", true)) {
            startActivity(new Intent(this, IntroActivity.class));
            return;
        }

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        final DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(0).withName("Dashboard").withIcon(R.drawable.ic_timeline_gray),
                        new PrimaryDrawerItem().withIdentifier(1).withName("Fund Flow").withIcon(R.drawable.ic_compare_arrows_gray),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(2).withName("Share Purchase")
                                .withIcon(R.drawable.ic_add_red)
                                .withTextColor(getResources().getColor(R.color.red_500)),
                        new PrimaryDrawerItem().withIdentifier(3).withName("Share Sales")
                                .withIcon(R.drawable.ic_remove_green)
                                .withTextColor(getResources().getColor(R.color.colorPrimary)),
                        new PrimaryDrawerItem().withIdentifier(4).withName("Share Holdings")
                                .withIcon(R.drawable.ic_account_balance_blue)
                                .withTextColor(getResources().getColor(R.color.colorAccent)),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(5).withName("Charts").withIcon(R.drawable.ic_insert_chart_gray),
                        new PrimaryDrawerItem().withIdentifier(6).withName("Summary").withIcon(R.drawable.ic_description_gray),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(7).withName("Feedback").withIcon(R.drawable.ic_feedback_gray),
                        new PrimaryDrawerItem().withIdentifier(8).withName("Help").withIcon(R.drawable.ic_help_gray),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(10).withName("Backup").withIcon(R.drawable.ic_backup_gray),
                        new PrimaryDrawerItem().withIdentifier(11).withName("Restore").withIcon(R.drawable.ic_restore_gray),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(21).withName("Settings").withIcon(R.drawable.ic_settings_gray),
                        new PrimaryDrawerItem().withIdentifier(22).withName("About").withIcon(R.drawable.ic_info_gray)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        boolean flag;
                        List<Share> shareList = databaseHandler.getShares();
                        if (drawerItem != null) {
                            flag = true;
                            switch ((int) drawerItem.getIdentifier()) {
                                case 0:
                                    if (shareList.size() < 1)
                                        drawer.setSelection(2, true);
                                    else switchFragment("Dashboard", "Dashboard");
                                    break;
                                case 1:
                                    switchFragment("Fund Flow", "FundFlow");
                                    break;

                                case 2:
                                    switchFragment("Share Purchase", "SharePurchaseMain");
                                    break;
                                case 3:
                                    if (shareList.size() < 1)
                                        drawer.setSelection(2, true);
                                    else switchFragment("Share Sales", "SharePurchaseMain");
                                    break;
                                case 4:
                                    if (shareList.size() < 1)
                                        drawer.setSelection(2, true);
                                    else switchFragment("Share Holdings", "ShareHoldings");
                                    break;

                                case 5:
                                    if (shareList.size() < 1)
                                        drawer.setSelection(2, true);
                                    else switchFragment("Charts", "Charts");
                                    break;
                                case 6:
                                    if (shareList.size() < 1)
                                        drawer.setSelection(2, true);
                                    else switchFragment("Summary", "Summary");
                                    break;

                                case 7:
                                    ConversationActivity.show(MainActivity.this);
                                    break;
                                case 8:
                                    startActivity(new Intent(MainActivity.this, IntroActivity.class));
                                    break;

                                case 10:
                                    RealmBackupRestore backup = new RealmBackupRestore(MainActivity.this);
                                    backup.backup();
                                    break;
                                case 11:
                                    RealmBackupRestore restore = new RealmBackupRestore(MainActivity.this);
                                    restore.restore();
                                    Intent mStartActivity = new Intent(MainActivity.this, MainActivity.class);
                                    int mPendingIntentId = 123456;
                                    PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this,
                                            mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                                    AlarmManager mgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
                                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                                    System.exit(0);
                                    break;

                                case 21:
                                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                                    break;
                                case 22:
                                    new LibsBuilder()
                                            .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                            .withActivityTitle(getString(R.string.app_name))
                                            .withAboutIconShown(true)
                                            .withAboutVersionShown(true)
                                            .withVersionShown(true)
                                            .withLicenseShown(true)
                                            .withLicenseDialog(true)
                                            .withListener(libsListener)
                                            .start(MainActivity.this);
                                    break;

                                default:
                                    switchFragment("Dashboard", "Dashboard");
                                    break;
                            }
                        } else {
                            flag = false;
                        }
                        return flag;
                    }
                })
                .build();

        if (savedInstanceState == null) drawer.setSelection(0, true);
        else drawer.setSelection(savedInstanceState.getLong("drawerSelection"), true);

        CustomActivityOnCrash.install(this);
    }

    private void switchFragment(String title, String fragment) {
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
    protected void onSaveInstanceState(Bundle outState) {
        if (drawer != null)
            outState.putLong("drawerSelection", drawer.getCurrentSelection());
        super.onSaveInstanceState(outState);
    }
}
