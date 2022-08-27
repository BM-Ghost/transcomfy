package com.vinwil.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vinwil.R;
import com.vinwil.data.model.Bus;
import com.vinwil.data.model.Request;
import com.vinwil.userinterface.recycleradapter.PendingRequestsRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PendingRequestsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar tbPendingRequests;
    private SupportMapFragment mapFragment;
    private RecyclerView rvPendingRequests;

    private GoogleMap googleMap;

    private List<Request> requests;
    private List<Request> pins;
    private Bus bus;

    private int REQUEST_CODE_LOCATION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_requests);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tbPendingRequests = findViewById(R.id.tb_pending_requests);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        rvPendingRequests = findViewById(R.id.rv_pending_requests);

        setSupportActionBar(tbPendingRequests);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapFragment.getMapAsync(PendingRequestsActivity.this);

        setPendingRequests(); // Set up data and refresh UI for pending requests page
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(PendingRequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(PendingRequestsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        } else {
            googleMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = locationManager.getBestProvider(criteria, true);
            locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(location != null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 11.0f));
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        String driverId = auth.getUid();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        com.vinwil.data.model.Location newLocation = new com.vinwil.data.model.Location();
                        newLocation.setName("Waiyaki Way");
                        newLocation.setLatitude(location.getLatitude());
                        newLocation.setLongitude(location.getLongitude());
                        database.getReference("drivers").child(driverId).child("bus").child("location").setValue(newLocation);
                        database.getReference("buses").child(bus.getBusId()).child("location").setValue(newLocation);
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
        }

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (ActivityCompat.checkSelfPermission(PendingRequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(PendingRequestsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkLocationPermission();
                    return false;
                }

                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(provider);
                if(location != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 11.0f));
                }
                return true;
            }
        });

        pins = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String driverId = auth.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("drivers").child(driverId).child("bus").child("requests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        googleMap.clear();
                        pins.clear();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Request pin = snapshot.getValue(Request.class);
                            pins.add(pin);
                            LatLng position = new LatLng(pin.getLocation().getLatitude(), pin.getLocation().getLongitude());
                            MarkerOptions options = new MarkerOptions();
                            options.title(pin.getName());
                            options.snippet(pin.getLocation().getName());
                            options.position(position);
                            if(pin.getStatus().equalsIgnoreCase("PENDING")) {
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                googleMap.addMarker(options);
                            } else if(pin.getStatus().equalsIgnoreCase("APPROVED")) {
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                googleMap.addMarker(options);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION && googleMap != null) {
            if (ActivityCompat.checkSelfPermission(PendingRequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(PendingRequestsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    private void setPendingRequests() {
        requests = new ArrayList<>();
        rvPendingRequests.setLayoutManager(new LinearLayoutManager(PendingRequestsActivity.this));
        PendingRequestsRecyclerAdapter adapter = new PendingRequestsRecyclerAdapter(PendingRequestsActivity.this, requests);
        adapter.setOnItemClicked(new PendingRequestsRecyclerAdapter.OnItemClicked() {
            @Override
            public void onItemClicked(Request request) {
                requestOptions(request);
            }
        });
        rvPendingRequests.setAdapter(adapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String driverId = auth.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("drivers").child(driverId).child("bus")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        bus = dataSnapshot.getValue(Bus.class);
                        bus.setId(dataSnapshot.getKey());
                        DataSnapshot snapshot = dataSnapshot.child("requests");
                        requests.clear();
                        rvPendingRequests.getAdapter().notifyDataSetChanged();
                        for(DataSnapshot snap : snapshot.getChildren()) {
                            Request request = snap.getValue(Request.class);
                            if(request.getStatus().equalsIgnoreCase("PENDING")) {
                                request.setId(snap.getKey());
                                requests.add(request);
                                rvPendingRequests.getAdapter().notifyDataSetChanged();
                            }
                        }

                        if(requests.size() > 0) {
                            String subtitle = String.valueOf(requests.size()).concat(" requests remaining");
                            getSupportActionBar().setSubtitle(subtitle);
                        } else {
                            getSupportActionBar().setSubtitle(null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void requestOptions(final Request request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PendingRequestsActivity.this);
        builder.setTitle("Approve request");
        String message = "Would you like to approve a pickup request from "
                .concat(request.getName()).concat(" at ").concat(request.getLocation().getName());
        builder.setMessage(message);
        builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String driverId = auth.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference("drivers").child(driverId).child("bus").child("requests").child(request.getId()).child("status").setValue("DECLINED");
                database.getReference("buses").child(bus.getBusId()).child("requests").child(request.getId()).child("status").setValue("DECLINED");
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(bus.getAvailableSpace() > 0) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String driverId = auth.getUid();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    database.getReference("drivers").child(driverId).child("bus").child("requests").child(request.getId()).child("status").setValue("APPROVED");
                    int newAvailableSpace = bus.getAvailableSpace() - 1;
                    database.getReference("drivers").child(driverId).child("bus").child("availableSpace").setValue(newAvailableSpace);
                    database.getReference("buses").child(bus.getBusId()).child("requests").child(request.getId()).child("status").setValue("APPROVED");
                    database.getReference("buses").child(bus.getBusId()).child("availableSpace").setValue(newAvailableSpace);
                } else {
                    Toast.makeText(PendingRequestsActivity.this, "You do not have enough available space", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void checkLocationPermission() {
        ActivityCompat.requestPermissions(PendingRequestsActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
    }

}
