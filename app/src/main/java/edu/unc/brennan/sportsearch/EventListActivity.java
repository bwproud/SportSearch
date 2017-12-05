package edu.unc.brennan.sportsearch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import edu.unc.brennan.sportsearch.dummy.DummyContent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity representing a list of Events. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link EventDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class EventListActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public static List<Event> events = new ArrayList<>();
    public static Map<String, Event> map = new HashMap<>();
    private boolean mTwoPane;
    CoordinatorLayout myLayout;
    Location loc;
    GeoFire geoFire;
    GeoQuery geoQuery;
    DatabaseReference db;
    Date now;
    protected GoogleApiClient c;
    protected LocationRequest req;
    protected Location lastLocation;
    protected String address;
    Event notFound;
    private View mProgressView;
    private View mLoginFormView;
    private boolean rendered;
    public static String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        myLayout = (CoordinatorLayout) findViewById(R.id.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        Intent x = new Intent(EventListActivity.this, CreateEvent.class);
                        startActivity(x);
            }
        });
        rendered=false;
        mLoginFormView = findViewById(R.id.frameLayout1);
        mProgressView = findViewById(R.id.progress_bar);
        showProgress(true);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db = database.getReference("SportSearch");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SportSearch/geofire");
        geoFire = new GeoFire(ref);
        now = new Date();
        //String id, String sport, String date, String participants, String location, String name
        notFound = new Event("69", "No Events found","No Events found","No Events found","No Events found","No Events found");
        Intent i = getIntent();
        String user = i.getStringExtra("userId");
        System.out.println("USERIDKJLSDJF:LKAJF "+user);
        if(user!=null && user.length()>0) {
            uid = i.getStringExtra("userId");
            db.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User u = dataSnapshot.getValue(User.class);
                    System.out.println("Signed in as " + u.firstName + " " + u.lastName);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        buildApiClient();
    }

    public void map(View v){
        Intent x = new Intent(EventListActivity.this, MapsActivity.class);
        startActivity(x);
    }

    public void events(View v){
//        Intent x = new Intent(EventListActivity.this, EventDetailActivity.class);
//        startActivity(x);
    }

    private void setupSoccerRecyclerView(@NonNull RecyclerView recyclerView) {
        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                return e1.getSport().compareTo(e2.getSport());
            }
        });
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(events));
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
           // Intent x = new Intent(EventListActivity.this, EventListActivity.class);
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
        //updateMap();
    }

    public void updateMap(){
        map.clear();
        events.clear();
        geoQuery = geoFire.queryAtLocation(new GeoLocation(loc.getLatitude(),loc.getLongitude()), 10);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                final GeoLocation loc = location;
                Query q = db.child("events").orderByChild("id").equalTo(key);
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        showProgress(false);
                        rendered=true;
                        View recyclerView = findViewById(R.id.event_list);
                        assert recyclerView != null;
                        setupSoccerRecyclerView((RecyclerView) recyclerView);

                        if (findViewById(R.id.event_detail_container) != null) {
                            // The detail container view will be present only in the
                            // large-screen layouts (res/values-w900dp).
                            // If this view is present, then the
                            // activity should be in two-pane mode.
                            mTwoPane = true;
                        }
                    }
                    public void onCancelled(DatabaseError firebaseError) { }
                });
                q.addChildEventListener(new ChildEventListener() {
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
                            events.add(e);
                            map.put(e.getId(),e);
                            if(rendered){
                                View recyclerView = findViewById(R.id.event_list);
                                assert recyclerView != null;
                                setupSoccerRecyclerView((RecyclerView) recyclerView);

                                if (findViewById(R.id.event_detail_container) != null) {
                                    // The detail container view will be present only in the
                                    // large-screen layouts (res/values-w900dp).
                                    // If this view is present, then the
                                    // activity should be in two-pane mode.
                                    mTwoPane = true;
                                }
                            }
                            // setMarker(e, new LatLng(loc.latitude,loc.longitude));
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


    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Event> mValues;

        public SimpleItemRecyclerViewAdapter(List<Event> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.event_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            switch (mValues.get(position).getSport()){
                case "Soccer":
                    holder.mImageView.setImageResource(R.drawable.small_ball);
                    break;
                case "Basketball":
                    holder.mImageView.setImageResource(R.drawable.bball);
                    break;
                case "Football":
                    holder.mImageView.setImageResource(R.drawable.fball);
                    break;
                case "Tennis":
                    holder.mImageView.setImageResource(R.drawable.tball);
                    break;
            }
            holder.mIdView.setText(mValues.get(position).getName());
            holder.mContentView.setText("");

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(EventDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        EventDetailFragment fragment = new EventDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.event_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, EventDetailActivity.class);
                        intent.putExtra(EventDetailFragment.ARG_ITEM_ID, holder.mItem.getId());
                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final ImageView mImageView;
            public final TextView mIdView;
            public final TextView mContentView;
            public Event mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
                mImageView = (ImageView) view.findViewById(R.id.image);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
