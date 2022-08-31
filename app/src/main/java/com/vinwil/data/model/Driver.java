package com.vinwil.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Driver implements Parcelable {

    private String id;
    private String name;
    private String email;
    private Bus bus;

    public Driver(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Bus getBus() {
        return bus;
    }

    public Driver(Parcel in){
        id = in.readString();
        name = in.readString();
        email = in.readString();
        bus = in.readParcelable(Bus.class.getClassLoader());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Driver createFromParcel(Parcel in) {
            return new Driver(in);
        }
        public Driver[] newArray(int size) {
            return new Driver[size];
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
        dest.writeString(email);
        dest.writeParcelable(bus, flags);
    }

}
