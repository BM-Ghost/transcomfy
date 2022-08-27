package com.vinwil.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Request implements Parcelable {

    private String id;
    private String name;
    private String status;
    private Location location;
    private double fare;
    private double currentBalance;
    private String from;
    private String to;

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

    public void setFare(double fare) {
        this.fare = fare;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
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

    public double getFare() {
        return fare;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Request(Parcel in){
        id = in.readString();
        name = in.readString();
        status = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
        fare = in.readDouble();
        currentBalance = in.readDouble();
        from = in.readString();
        to = in.readString();
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
        dest.writeDouble(fare);
        dest.writeDouble(currentBalance);
        dest.writeString(from);
        dest.writeString(to);
    }

}
