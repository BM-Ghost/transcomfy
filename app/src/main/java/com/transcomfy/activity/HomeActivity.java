package com.transcomfy.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.transcomfy.R;
import com.transcomfy.fragment.BillingFragment;
import com.transcomfy.fragment.HistoryFragment;
import com.transcomfy.fragment.RequestFragment;

public class HomeActivity extends AppCompatActivity {

    private TextView tvName;
    private TextView tvEmail;
    private DrawerLayout dlHome;
    private NavigationView nvHome;

    private boolean isDashboardSelected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        nvHome = findViewById(R.id.nv_home);
        tvName = nvHome.getHeaderView(0).findViewById(R.id.tv_name);
        tvEmail = nvHome.getHeaderView(0).findViewById(R.id.tv_email);
        dlHome = findViewById(R.id.dl_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        request(); // request is the default fragment

        setHome(); // home UI

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                HomeActivity.this, dlHome, R.string.drawer_layout_open, R.string.drawer_layout_closed
        );
        dlHome.addDrawerListener(toggle);
        toggle.syncState();

        nvHome.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                isDashboardSelected = false;
                switch (item.getItemId()){

                    case R.id.action_request:
                        request();
                        break;

                    case R.id.action_billing:
                        billing();
                        break;

                    case R.id.action_history:
                        history();
                        break;

                    default:
                        request();

                }
                dlHome.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                logOut();
                return true;
            default:
                return false;
        }
    }

    private void request() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.ll_home, new RequestFragment());
        transaction.commit();
        nvHome.setCheckedItem(R.id.action_request);
        setDashboardSelected(true);
    }

    private void billing() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.ll_home, new BillingFragment());
        transaction.commit();
        nvHome.setCheckedItem(R.id.action_billing);
        setDashboardSelected(false);
    }

    private void history() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.ll_home, new HistoryFragment());
        transaction.commit();
        nvHome.setCheckedItem(R.id.action_history);
        setDashboardSelected(false);
    }

    private void setHome() {
        tvName.setText(R.string.msg_loading);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(auth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tvName.setText(dataSnapshot.child("name").getValue(String.class));
                        tvEmail.setText(dataSnapshot.child("email").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void logOut() {
        // Get Firebase Authentication service and Sign Out user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        // Go to Log in page and clear activity history
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        HomeActivity.this.finish();
    }

    public void setDashboardSelected(boolean dashboardSelected) {
        isDashboardSelected = dashboardSelected;
    }

    public DrawerLayout getDlHome() {
        return dlHome;
    }

}
