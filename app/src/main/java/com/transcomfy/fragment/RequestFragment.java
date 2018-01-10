package com.transcomfy.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.transcomfy.R;
import com.transcomfy.activity.HomeActivity;

import static android.content.Context.LOCATION_SERVICE;

public class RequestFragment extends Fragment implements OnMapReadyCallback {

    private View rootView;
    private Toolbar tbRequest;
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    
    public RequestFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_request, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tbRequest = rootView.findViewById(R.id.tb_request);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        ((AppCompatActivity) getContext()).setSupportActionBar(tbRequest);
        ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity) getContext()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        setHasOptionsMenu(true);

        mapFragment.getMapAsync(RequestFragment.this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                ((HomeActivity) getContext()).getDlHome().openDrawer(GravityCompat.START, true);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (ActivityCompat.checkSelfPermission(RequestFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RequestFragment.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //checkLocationPermission();
        } else {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Toast.makeText(getContext(), "foo", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(RequestFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(RequestFragment.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //checkLocationPermission();
                    return false;
                }

                LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(provider);
                request(location);
                return true;
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("buses")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        googleMap.clear();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if(snapshot.child("location").getValue() != null) {
                                double latitude = snapshot.child("location").child("latitude").getValue(Double.class);
                                double longitude = snapshot.child("location").child("longitude").getValue(Double.class);
                                String name = snapshot.child("location").child("name").getValue(String.class);
                                String numberPlate = snapshot.child("numberPlate").getValue(String.class);

                                MarkerOptions options = new MarkerOptions();
                                options.title(name);
                                options.snippet(numberPlate);
                                options.position(new LatLng(latitude, longitude));
                                googleMap.addMarker(options);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void request(Location location) {
        if(location != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16.0f));
        }
    }

}
