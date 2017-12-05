package edu.unc.brennan.sportsearch;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    //private FusedLocationProviderClient mFusedLocationClient;
    Location loc;
    GeoFire geoFire;
    GeoQuery geoQuery;
    DatabaseReference db;
    Date now;
    protected GoogleApiClient c;
    protected LocationRequest req;
    protected Location lastLocation;
    protected String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        buildApiClient();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db = database.getReference("SportSearch");
        now = new Date();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void map(View v){
//        Intent x = new Intent(MapsActivity.this, MapsActivity.class);
//        startActivity(x);
    }

    public void events(View v){
        NavUtils.navigateUpTo(this, new Intent(this, EventListActivity.class));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }else {
            mMap.setMyLocationEnabled(true);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SportSearch/geofire");
            geoFire = new GeoFire(ref);
            //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            checkPermission();

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(35.9092167, -79.047465);
//        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.soccer);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").icon(BitmapDescriptorFactory.fromBitmap(largeIcon)));
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    LinearLayout info = new LinearLayout(MapsActivity.this);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(MapsActivity.this);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(MapsActivity.this);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15.0f));
        }
//
// mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney), 12f);
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
//            Intent x = new Intent(MapsActivity.this, MapsActivity.class);
//            startActivity(x);
        }else{
            //buildApiClient();
//            mFusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            if(location!=null){
//                                System.out.println("LOCATION IS NOT NULL");
//                                loc = location;
//                                geoQuery = geoFire.queryAtLocation(new GeoLocation(loc.getLatitude(),loc.getLongitude()), 10);
//                                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//
//                                    @Override
//                                    public void onKeyEntered(String key, GeoLocation location) {
//                                        final GeoLocation loc = location;
//                                        db.child("events").orderByChild("id").equalTo(key).addChildEventListener(new ChildEventListener() {
//                                            @Override
//                                            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
//                                                System.out.println("HERE WE GO");
//                                                System.out.println(String.format("Key entered the search area at [%f,%f]", loc.latitude, loc.longitude));
//                                                Event e = dataSnapshot.getValue(Event.class);
//                                                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                                Date event=null;
//                                                try {
//                                                    event = sdfDate.parse(e.getDate());
//                                                } catch (ParseException e1) {
//                                                    e1.printStackTrace();
//                                                }
//                                                System.out.println("event date: "+event);
//                                                System.out.println("current date: "+ now);
//                                                System.out.println("Comparison: "+event.after(now));
//                                                if(event.after(now)){
//                                                    setMarker(e, new LatLng(loc.latitude,loc.longitude));
//                                                }
//                                               }
//
//                                            @Override
//                                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                                            }
//
//                                            @Override
//                                            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                                            }
//
//                                            @Override
//                                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                                            }
//
//                                            @Override
//                                            public void onCancelled(DatabaseError databaseError) {
//
//                                            }
//                                        });
//                                    }
//
//                                    @Override
//                                    public void onKeyExited(String key) {
//                                        System.out.println(String.format("Key %s is no longer in the search area", key));
//                                    }
//
//                                    @Override
//                                    public void onKeyMoved(String key, GeoLocation location) {
//                                        System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
//                                    }
//
//                                    @Override
//                                    public void onGeoQueryReady() {
//                                        System.out.println("EVERYTHING IS DONE AND YOUR SHIT IS BROKE");
//                                    }
//
//                                    @Override
//                                    public void onGeoQueryError(DatabaseError error) {
//                                        System.err.println("There was an error with this query: " + error);
//                                    }
//                                });
//                            }else{
//                                System.out.println("LOCATION IS NULL");
//                            }
//                        }
//                    });
        }
    }

    // Establish the Google API client
    public void buildApiClient(){
        if(c==null){
            c = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(Bundle bundle){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
//            Intent x = new Intent(MapsActivity.this, MapsActivity.class);
//            startActivity(x);
        }else {
            System.out.println("CONNECTED");
            try {
                // Don't really need to get the last location; not using it right now
                Location location = LocationServices.FusedLocationApi.getLastLocation(c);
                System.out.println("Location is null = " + (location == null));
                if (location != null) {
                    loc = location;
                    updateMap();
                }
            } catch (SecurityException ex) {
                ex.printStackTrace();
            }
            req = new LocationRequest();
            req.setInterval(10000);
            req.setFastestInterval(5000);
            req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Make sure location permission has been granted
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(c, req, this);
            } else {
                System.out.println("LOCATION NOT GRANTED");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location){
        loc = location;
        updateMap();
    }

    public void updateMap(){
        geoQuery = geoFire.queryAtLocation(new GeoLocation(loc.getLatitude(),loc.getLongitude()), 10);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                final GeoLocation loc = location;
                db.child("events").orderByChild("id").equalTo(key).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                        System.out.println("HERE WE GO");
                        System.out.println(String.format("Key entered the search area at [%f,%f]", loc.latitude, loc.longitude));
                        Event e = dataSnapshot.getValue(Event.class);
                        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date event=null;
                        try {
                            event = sdfDate.parse(e.getDate());
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        System.out.println("event date: "+event);
                        System.out.println("current date: "+ now);
                        System.out.println("Comparison: "+event.after(now));
                        if(event.after(now)){
                            setMarker(e, new LatLng(loc.latitude,loc.longitude));
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("EVERYTHING IS DONE AND YOUR SHIT IS BROKE");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }

    public void setMarker(Event e, LatLng loc){
        Bitmap largeIcon=null;
        switch (e.getSport()){
            case "Soccer":
                largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.soccer);
                break;
            case "Basketball":
                largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.basketball_ball);
                break;
            case "Football":
                largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.football);
                break;
            case "Tennis":
                largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.tennis);
                break;
        }
        String title = e.getName().length()>0 ? e.getName() : e.getSport() + " Game";
        String description = "Date: "+e.getDate()+"\nParticipants: "+e.getParticipants()+"\nLocation: "+e.getLocation();
        mMap.addMarker(new MarkerOptions().position(loc).snippet(description).title(title).icon(BitmapDescriptorFactory.fromBitmap(largeIcon)));

    }

    @Override
    public void onConnectionSuspended(int i){
        c.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    protected void onStart(){
        super.onStart();
        c.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        c.disconnect();
    }

}
