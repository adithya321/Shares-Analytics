package com.adithya321.sharesanalysis.database;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Share extends RealmObject {
    @PrimaryKey
    private long id;
    private String name;
    private Date dateOfInitialPurchase;
    private double currentShareValue;
    private RealmList<Purchase> purchases;

    public Share() {
    }

    public Share(long id, String name, Date dateOfInitialPurchase, double currentShareValue,
                 RealmList<Purchase> purchases) {
        this.id = id;
        this.name = name;
        this.dateOfInitialPurchase = dateOfInitialPurchase;
        this.currentShareValue = currentShareValue;
        this.purchases = purchases;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateOfInitialPurchase() {
        return dateOfInitialPurchase;
    }

    public void setDateOfInitialPurchase(Date dateOfInitialPurchase) {
        this.dateOfInitialPurchase = dateOfInitialPurchase;
    }

    public double getCurrentShareValue() {
        return currentShareValue;
    }

    public void setCurrentShareValue(double currentShareValue) {
        this.currentShareValue = currentShareValue;
    }

    public RealmList<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(RealmList<Purchase> purchases) {
        this.purchases = purchases;
    }

    @Override
    public String toString() {
        return "Share{" +
                ", name='" + name + '\'' +
                ", dateOfInitialPurchase=" + dateOfInitialPurchase +
                '}';
    }
}
