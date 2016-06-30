package com.adithya321.sharesanalysis.database;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmObject;

public class Purchase extends RealmObject {
    private int quantity;
    private double price;
    private Date date;
    private String type;

    public Purchase() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDate());
        return new StringBuilder().append(calendar.get(Calendar.DAY_OF_MONTH)).append("/")
                .append(calendar.get(Calendar.MONTH) + 1).append("/").append(calendar.get(Calendar.YEAR))
                + ", quantity = " + quantity +
                ", price = " + price;
    }
}
