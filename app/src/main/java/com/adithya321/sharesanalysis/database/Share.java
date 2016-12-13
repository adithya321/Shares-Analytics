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
