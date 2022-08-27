package com.vinwil.data;

import android.content.Context;

import com.vinwil.data.model.Stop;

import org.json.JSONObject;

public class DataManager {

    private Context context;

    public DataManager(Context context) {
        this.context = context;
    }

    public Stop getStop(JSONObject stopData){
        try {
            Stop stop = new Stop();
            stop.setId(stopData.getString("stops_id"));
            stop.setName(stopData.getString("stop_name"));
            stop.setLatitude(stopData.getDouble("stop_lat"));
            stop.setLongitude(stopData.getDouble("stop_lon"));
            return stop;
        } catch (Exception e) {
            return null;
        }
    }

}
