package com.vinwil.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Payment implements Parcelable {

    private String id;
    private String createdAt;
    private String amount;

    public Payment(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getAmount() {
        return amount;
    }

    public Payment(Parcel in){
        id = in.readString();
        createdAt = in.readString();
        amount = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public Payment createFromParcel(Parcel in) {
            return new Payment(in);
        }
        public Payment[] newArray(int size) {
            return new Payment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(createdAt);
        dest.writeString(amount);
    }

}
