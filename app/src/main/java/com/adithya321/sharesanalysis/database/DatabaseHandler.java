package com.adithya321.sharesanalysis.database;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class DatabaseHandler {

    private static RealmConfiguration mRealmConfig;
    private Context mContext;
    private Realm realm;

    public DatabaseHandler(Context context) {
        this.mContext = context;

        this.realm = getNewRealmInstance();
    }

    public Realm getNewRealmInstance() {
        if (mRealmConfig == null) {
            mRealmConfig = new RealmConfiguration.Builder(mContext)
                    .schemaVersion(0)
                    .migration(new Migration())
                    .build();
        }
        return Realm.getInstance(mRealmConfig);
    }

    public Realm getRealmInstance() {
        return realm;
    }

    public void addFund(Fund fund) {
        realm.beginTransaction();
        realm.copyToRealm(fund);
        realm.commitTransaction();
    }

    public void updateFund(Fund fund) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(fund);
        realm.commitTransaction();
    }

    public List<Fund> getFunds() {
        RealmResults<Fund> results = realm.where(Fund.class).findAllSorted("date", Sort.DESCENDING);
        ArrayList<Fund> funds = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            funds.add(results.get(i));
        }
        return funds;
    }

    public boolean addShare(Share share, Purchase purchase) {
        RealmResults<Share> shares = realm.where(Share.class).equalTo("name", share.getName()).findAll();
        if (!shares.isEmpty())
            return false;
        realm.beginTransaction();
        purchase = realm.copyToRealm(purchase);
        share.getPurchases().add(purchase);
        realm.copyToRealm(share);
        realm.commitTransaction();
        return true;
    }

    public void deleteShare(Share share) {
        final RealmResults<Share> shares = realm.where(Share.class).equalTo("name", share.getName()).findAll();
        final RealmList<Purchase> purchases = shares.get(0).getPurchases();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                purchases.deleteAllFromRealm();
                shares.deleteAllFromRealm();
            }
        });
    }

    public void updatePurchase(Purchase purchase) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(purchase);
        realm.commitTransaction();
    }

    public boolean addPurchase(String name, Purchase purchase) {
        RealmResults<Share> shares = realm.where(Share.class).equalTo("name", name).findAll();
        if (shares.isEmpty())
            return false;
        realm.beginTransaction();
        purchase = realm.copyToRealm(purchase);
        shares.get(0).getPurchases().add(purchase);
        realm.commitTransaction();
        return true;
    }

    public ArrayList<Share> getShares() {
        RealmResults<Share> results = realm.where(Share.class).findAllSorted("id");
        ArrayList<Share> shares = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            shares.add(results.get(i));
        }
        return shares;
    }

    public ArrayList<Purchase> getPurchases() {
        RealmResults<Purchase> results = realm.where(Purchase.class).equalTo("type", "buy")
                .findAllSorted("date", Sort.DESCENDING);
        ArrayList<Purchase> purchases = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            purchases.add(results.get(i));
        }
        return purchases;
    }

    public ArrayList<Purchase> getSales() {
        RealmResults<Purchase> results = realm.where(Purchase.class).equalTo("type", "sell")
                .findAllSorted("date", Sort.DESCENDING);
        ArrayList<Purchase> purchases = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            purchases.add(results.get(i));
        }
        return purchases;
    }

    public long getNextKey(String where) {
        Number maxId = null;
        switch (where) {
            case "fund":
                maxId = realm.where(Fund.class).max("id");
                break;
            case "share":
                maxId = realm.where(Share.class).max("id");
                break;
            case "purchase":
                maxId = realm.where(Purchase.class).max("id");

            default:
                return 0;
        }
        if (maxId == null) return 0;
        else return Long.parseLong(maxId.toString()) + 1;
    }
}
