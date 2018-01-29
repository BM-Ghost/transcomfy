package com.transcomfy.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.transcomfy.R;
import com.transcomfy.activity.HomeActivity;
import com.transcomfy.activity.SearchDestinationActivity;
import com.transcomfy.data.DataManager;
import com.transcomfy.data.Keys;
import com.transcomfy.data.model.Bus;
import com.transcomfy.data.model.Stop;
import com.transcomfy.internet.Internet;
import com.transcomfy.internet.URLs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LOCATION_SERVICE;

public class RequestFragment extends Fragment implements OnMapReadyCallback {

    private View rootView;
    private Toolbar tbRequest;
    private TextView tvSetDestination;
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private TextView tvMessage;

    private DataManager manager;
    private int REQUEST_STOP = 1;
    private boolean inTransit = false;
    private boolean canCalculate = true;
    private Stop startStop;
    private Stop endStop;
    private Bus tripBus;

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
        manager = new DataManager(getContext());

        tbRequest = rootView.findViewById(R.id.tb_request);
        tvSetDestination = rootView.findViewById(R.id.tv_set_destination);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        tvMessage = rootView.findViewById(R.id.tv_message);
        tvMessage.setText(R.string.msg_set_your_destination);
        tvMessage.setOnClickListener(null);

        ((AppCompatActivity) getContext()).setSupportActionBar(tbRequest);
        ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getContext()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity) getContext()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        setHasOptionsMenu(true);

        tvSetDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RequestFragment.this.getContext(), SearchDestinationActivity.class);
                startActivityForResult(intent, REQUEST_STOP);
            }
        });

        mapFragment.getMapAsync(RequestFragment.this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                //Toast.makeText(getContext(), "foo", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(RequestFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(RequestFragment.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //checkLocationPermission();
                    return false;
                }

                LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
                String provider = locationManager.getBestProvider(new Criteria(), true);
                Location location = locationManager.getLastKnownLocation(provider);
                moveToLocation(location);
                return true;
            }
        });

        if (ActivityCompat.checkSelfPermission(RequestFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RequestFragment.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //checkLocationPermission();
        } else {
            googleMap.setMyLocationEnabled(true);
        }

        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestSingleUpdate(new Criteria(), new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                moveToLocation(location);
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
        }, Looper.getMainLooper());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_STOP) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                endStop = data.getParcelableExtra(Keys.EXTRA_STOP);
                tvSetDestination.setText(endStop.getName());
                LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
                String provider = locationManager.getBestProvider(new Criteria(), true);
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }

                Location location = locationManager.getLastKnownLocation(provider);

//                com.transcomfy.data.model.Location location = new com.transcomfy.data.model.Location(); // My location
//                location.setLatitude(-1.2654);
//                location.setLongitude(36.8045);
                moveToLocation(location);

                setStopsNearby(location, endStop);
            }
        }
    }

    private void moveToLocation(Location location) {
        if(location != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14.0f));
        }
    }

    private void setStopsNearby(final Location location, final Stop stop) {
        tvMessage.setText(R.string.msg_pick_a_bus_stop);
        tvMessage.setOnClickListener(null);
        googleMap.clear();
        RequestQueue queue = Volley.newRequestQueue(getContext());

        if(!Internet.isNetworkAvailable(getContext())){
            Toast.makeText(getContext(), R.string.msg_no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        String url = URLs.URL_STOPS
                .concat("?latitude=").concat(String.valueOf(location.getLatitude()))
                .concat("&longitude=").concat(String.valueOf(location.getLongitude()));

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray routesData) {
                        try {
                            if(routesData.length() > 0){
                                for(int i = 0; i < routesData.length(); i++){
                                    Stop stop = manager.getStop(routesData.getJSONObject(i));
                                    if(stop != null){
                                        MarkerOptions options = new MarkerOptions();
                                        options.title(stop.getName());
                                        options.position(new LatLng(stop.getLatitude(), stop.getLongitude()));
                                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bus_primary_18dp));
                                        googleMap.addMarker(options);
                                    }
                                }

                                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker marker) {
                                        googleMap.setOnMarkerClickListener(null);
                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        builder.include(new LatLng(stop.getLatitude(),  stop.getLongitude())).include(marker.getPosition());

                                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));

                                        googleMap.clear();

                                        MarkerOptions options = new MarkerOptions();
                                        options.position(marker.getPosition());
                                        options.title(marker.getTitle());
                                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bus_green_18dp));
                                        googleMap.addMarker(options);

                                        MarkerOptions optionsDestination = new MarkerOptions();
                                        optionsDestination.position(new LatLng(stop.getLatitude(), stop.getLongitude()));
                                        optionsDestination.title(stop.getName());
                                        optionsDestination.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bus_red_18dp));
                                        googleMap.addMarker(optionsDestination);

                                        // draw line - options to stop
                                        // ========
                                        //Define list to get all latlng for the route
                                        List<LatLng> path = new ArrayList<>();

                                        //Execute Directions API moveToLocation
                                        GeoApiContext context = new GeoApiContext.Builder()
                                                .apiKey("AIzaSyC7c_xTdIvyLPmJA3ZWvXhGMmBLXHoJKl4")
                                                .build();

                                        DirectionsApiRequest req = DirectionsApi.getDirections(context,
                                                String.valueOf(options.getPosition().latitude).concat(",").concat(String.valueOf(options.getPosition().longitude)),
                                                String.valueOf(stop.getLatitude()).concat(",").concat(String.valueOf(stop.getLongitude()))
                                        );


                                        try {
                                            DirectionsResult res = req.await();

                                            //Loop through legs and steps to get encoded polylines of each step
                                            if (res.routes != null && res.routes.length > 0) {
                                                DirectionsRoute route = res.routes[0];

                                                if (route.legs != null) {
                                                    for(int i=0; i<route.legs.length; i++) {
                                                        DirectionsLeg leg = route.legs[i];
                                                        if (leg.steps != null) {
                                                            for (int j=0; j<leg.steps.length;j++){
                                                                DirectionsStep step = leg.steps[j];
                                                                if (step.steps != null && step.steps.length >0) {
                                                                    for (int k=0; k<step.steps.length;k++){
                                                                        DirectionsStep step1 = step.steps[k];
                                                                        EncodedPolyline points1 = step1.polyline;
                                                                        if (points1 != null) {
                                                                            //Decode polyline and add points to list of route coordinates
                                                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                                                            }
                                                                        }
                                                                    }
                                                                } else {
                                                                    EncodedPolyline points = step.polyline;
                                                                    if (points != null) {
                                                                        //Decode polyline and add points to list of route coordinates
                                                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                                                        for (com.google.maps.model.LatLng coord : coords) {
                                                                            path.add(new LatLng(coord.lat, coord.lng));
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } catch(Exception ex) {
                                            Log.e("TAG", ex.getLocalizedMessage());
                                        }

                                        Log.e("TAG", String.valueOf(path.size()));

                                        //Draw the polyline
                                        if (path.size() > 0) {
                                            PolylineOptions opts = new PolylineOptions().addAll(path).color(ContextCompat.getColor(getContext(), R.color.color_primary_dark)).width(5);
                                            googleMap.addPolyline(opts);
                                        }

                                        confirmPickUp(stop);
                                        return true;
                                    }
                                });
                            }else{
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            // An error occurred
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 10, 10));
        queue.add(request);
    }

    private void confirmPickUp(final Stop stop) {
        tvMessage.setText(R.string.msg_confirm_pick_up);
        tvMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvMessage.setText(R.string.msg_finding_nearest_bus);
                tvSetDestination.setOnClickListener(null);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference("buses")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get a bus
                                // Make a request
                                // Use request to track bus arrival time
                                Bus bus = null;
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    Bus temp = snapshot.getValue(Bus.class);
                                    temp.setId(snapshot.getKey());

                                    if(dataSnapshot.getChildrenCount() == 0) {
                                        bus = temp;
                                    } else {
                                        // TODO
                                        // if bus distance is shorter than current
                                        bus = temp;
                                    }
                                }

                                if (bus != null && bus.getLocation() != null && bus.getAvailableSpace() > 0) {
                                    trackBus(stop, endStop, bus);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }
        });
    }

    private void trackBus(final Stop start, final Stop stop, final Bus bus) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("buses").child(bus.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Bus selectedBus = dataSnapshot.getValue(Bus.class);
                        selectedBus.setId(dataSnapshot.getKey());
                        // check distance = stop position - bus position
                        if(inTransit) {
                            calculateTimeInTransit(start, stop, selectedBus);
                        } else if(canCalculate) {
                            calculateTime(start, selectedBus);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void calculateTime(final Stop stop, final Bus bus) {
        setCanCalculate(false);
        this.startStop = stop;
        this.tripBus = bus;
        RequestQueue queue = Volley.newRequestQueue(getContext());

        if(!Internet.isNetworkAvailable(getContext())){
            Toast.makeText(getContext(), R.string.msg_no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        String url = URLs.URL_DIRECTIONS
                .concat("origin=").concat(String.valueOf(stop.getLatitude())).concat(",").concat(String.valueOf(stop.getLongitude()))
                .concat("&destination=").concat(String.valueOf(bus.getLocation().getLatitude())).concat(",").concat(String.valueOf(bus.getLocation().getLongitude()))
                .concat("&sensor=false");
        queue.add(new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        googleMap.clear();

                        MarkerOptions optionsBus = new MarkerOptions();
                        optionsBus.title(bus.getLocation().getName());
                        optionsBus.snippet(bus.getNumberPlate());
                        optionsBus.position(new LatLng(bus.getLocation().getLatitude(), bus.getLocation().getLongitude()));
                        optionsBus.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker_primary_dark_32dp));
                        googleMap.addMarker(optionsBus);

                        MarkerOptions optionsStop = new MarkerOptions();
                        optionsStop.title(stop.getName());
                        optionsStop.position(new LatLng(stop.getLatitude(), stop.getLongitude()));
                        optionsStop.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bus_green_18dp));
                        googleMap.addMarker(optionsStop);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(optionsBus.getPosition()).include(optionsStop.getPosition());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));

                        try {
                            tvMessage.setText(
                                    getString(R.string.msg_bus_mins_away_1)
                                            .concat(bus.getNumberPlate())
                                            .concat("\u0020is\u0020")
                                            .concat(response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text"))
                                            .concat(getString(R.string.msg_bus_mins_away_2))
                            );
                            // if not in transit
                            if(!inTransit) {
                                request(bus, stop);
                            }
                        } catch (Exception e) {
                            tvMessage.setText(R.string.msg_no_bus_available);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ));
        // if distance < 2km show estimate arrival time, number plate
        // else show no bus available
    }

    private void calculateTimeInTransit(final Stop start, final Stop stop, final Bus bus) {
        setCanCalculate(false);
        RequestQueue queue = Volley.newRequestQueue(getContext());

        if(!Internet.isNetworkAvailable(getContext())){
            Toast.makeText(getContext(), R.string.msg_no_network, Toast.LENGTH_SHORT).show();
            return;
        }

        String url = URLs.URL_DIRECTIONS
                .concat("origin=").concat(String.valueOf(stop.getLatitude())).concat(",").concat(String.valueOf(stop.getLongitude()))
                .concat("&destination=").concat(String.valueOf(bus.getLocation().getLatitude())).concat(",").concat(String.valueOf(bus.getLocation().getLongitude()))
                .concat("&sensor=false");
        queue.add(new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        googleMap.clear();

                        MarkerOptions optionsStart = new MarkerOptions();
                        optionsStart.title(start.getName());
                        optionsStart.position(new LatLng(start.getLatitude(), start.getLongitude()));
                        optionsStart.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bus_red_18dp));
                        googleMap.addMarker(optionsStart);

                        MarkerOptions optionsBus = new MarkerOptions();
                        optionsBus.title(bus.getLocation().getName());
                        optionsBus.snippet(bus.getNumberPlate());
                        optionsBus.position(new LatLng(bus.getLocation().getLatitude(), bus.getLocation().getLongitude()));
                        optionsBus.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker_primary_dark_32dp));
                        googleMap.addMarker(optionsBus);

                        MarkerOptions optionsStop = new MarkerOptions();
                        optionsStop.title(stop.getName());
                        optionsStop.position(new LatLng(stop.getLatitude(), stop.getLongitude()));
                        optionsStop.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bus_green_18dp));
                        googleMap.addMarker(optionsStop);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(optionsBus.getPosition()).include(optionsStop.getPosition());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));

                        try {
                            tvMessage.setText(
                                            "Your destination is\u0020"
                                            .concat(response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text"))
                                            .concat(getString(R.string.msg_bus_mins_away_2))
                            );
                        } catch (Exception e) {
                            tvMessage.setText(R.string.msg_no_bus_available);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ));
        // if distance < 2km show estimate arrival time, number plate
        // else show no bus available
    }

    private void request(final Bus bus, final Stop stop) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        com.transcomfy.data.model.Location location = new com.transcomfy.data.model.Location();
                        location.setName(stop.getName());
                        location.setLatitude(stop.getLatitude());
                        location.setLongitude(stop.getLongitude());

                        com.transcomfy.data.model.Request request = new com.transcomfy.data.model.Request();
                        request.setName(dataSnapshot.child("name").getValue(String.class));
                        request.setLocation(location);
                        request.setStatus("PENDING");

                        String id = database.getReference().push().getKey();
                        database.getReference("buses").child(bus.getId()).child("requests").child(id).setValue(request);
                        database.getReference("drivers").child(bus.getDriverId()).child("bus").child("requests").child(id).setValue(request);

                        FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                        database1.getReference("buses").child(bus.getId()).child("requests").child(id)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        com.transcomfy.data.model.Request request1 = dataSnapshot.getValue(com.transcomfy.data.model.Request.class);
                                        request1.setId(dataSnapshot.getKey());

                                        setInTransit(request1.getStatus().equalsIgnoreCase("APPROVED")
                                                || request1.getStatus().equalsIgnoreCase("TRANSIT"));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void setCanCalculate(boolean canCalculate) {
        this.canCalculate = canCalculate;

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                setCanCalculate(true);
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 15000);
    }

    public void setInTransit(boolean inTransit) {
        this.inTransit = inTransit;
        if(inTransit) {
            calculateTimeInTransit(startStop, endStop, tripBus);
        }
    }
}
