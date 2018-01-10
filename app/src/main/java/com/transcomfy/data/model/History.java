package com.transcomfy.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class History implements Parcelable {

    private String from;
    private String to;
    private double amount;
    private long createdAt;

    public History(){

    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getAmount() {
        return amount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public History(Parcel in){
        from = in.readString();
        to = in.readString();
        amount = in.readDouble();
        createdAt = in.readLong();
    }

    public static final Creator CREATOR = new Creator() {
        public History createFromParcel(Parcel in) {
            return new History(in);
        }
        public History[] newArray(int size) {
            return new History[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(from);
        dest.writeString(to);
        dest.writeDouble(amount);
        dest.writeLong(createdAt);
    }

}
