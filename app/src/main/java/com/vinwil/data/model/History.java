package com.vinwil.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class History implements Parcelable {

    private String title;
    private String description;
    private String status;
    private long createdAt;

    public History(){

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public History(Parcel in){
        title = in.readString();
        description = in.readString();
        status = in.readString();
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
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(status);
        dest.writeLong(createdAt);
    }

}
