package com.transcomfy.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Request implements Parcelable {

    private String id;
    private String name;
    private String status;
    private Location location;

    public Request(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public Location getLocation() {
        return location;
    }

    public Request(Parcel in){
        id = in.readString();
        name = in.readString();
        status = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Creator CREATOR = new Creator() {
        public Request createFromParcel(Parcel in) {
            return new Request(in);
        }
        public Request[] newArray(int size) {
            return new Request[size];
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
        dest.writeString(status);
        dest.writeParcelable(location, flags);
    }

}
