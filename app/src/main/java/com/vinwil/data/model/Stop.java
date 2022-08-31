package com.vinwil.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Stop implements Parcelable {

    private String id;
    private String name;
    private double latitude;
    private double longitude;

    public Stop(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Stop(Parcel in){
        id = in.readString();
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator CREATOR = new Creator() {
        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

}
