package com.adithya321.sharesanalysis.database;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
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

    public void updateShare(Share share){
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(share);
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
        RealmResults<Share> results = realm.where(Share.class).findAll();
        ArrayList<Share> shares = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            shares.add(results.get(i));
        }
        return shares;
    }

    public long getNextKey() {
        Number id = realm.where(Fund.class).max("id");
        if (id == null) return 0;
        else return Long.parseLong(id.toString()) + 1;
    }
}
