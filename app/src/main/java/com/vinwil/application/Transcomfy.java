package com.vinwil.application;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class Transcomfy extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
