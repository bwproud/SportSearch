package edu.unc.brennan.sportsearch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelMetadata;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class CreateEvent extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    Button btnDatePicker, btnTimePicker;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // UI references.
    private EditText name;
    private Spinner sport;
    private View mProgressView;
    private View mLoginFormView;
    LatLng coor;
    String placeName;
    GeoFire geoFire;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        name = (EditText) findViewById(R.id.name);

        mLoginFormView = findViewById(R.id.event_form);
        mProgressView = findViewById(R.id.event_progress);
        sport = (Spinner) findViewById(R.id.spinnerSport);

        btnDatePicker = (Button) findViewById(R.id.btn_date);
        btnTimePicker = (Button) findViewById(R.id.btn_time);
        txtDate = (EditText) findViewById(R.id.in_date);
        txtTime = (EditText) findViewById(R.id.in_time);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SportSearch/geofire");
        geoFire = new GeoFire(ref);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(null, "Place: " + place.getName());
                System.out.println(place.getName());
                coor = place.getLatLng();
                placeName = (String) place.getName();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(null, "An error occurred: " + status);
            }
        });
    }

    public void map(View v) {
        Intent x = new Intent(CreateEvent.this, MapsActivity.class);
        startActivity(x);
    }

    public void events(View v) {
        NavUtils.navigateUpTo(this, new Intent(this, EventListActivity.class));
    }

    public void date(View v) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        mYear = year;
                        mMonth = monthOfYear+1;
                        mDay = dayOfMonth;
                        txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void time(View v) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        mHour = hourOfDay;
                        mMinute = minute;
                        txtTime.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void createEvent(View v) {
        if (mYear == 0 || mMonth == 0 || mDay == 0 || mHour == 0) {
            name.setError("Date and time must be filled out");
        }

        showProgress(true);
        String minute = (mMinute<10) ? "0"+mMinute : mMinute+"";
        String hour = (mHour<10) ? "0"+mHour : mHour+"";
        String month = (mMonth<10) ? "0"+mMonth : mMonth+"";
        String day = (mDay<10) ? "0"+mDay : mDay+"";
        String date_time = String.format("%s-%s-%s %s:%s:00", mYear, month, day, hour, minute);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference db = database.getReference("SportSearch");
        key = db.child("events").push().getKey();
        String location = placeName;
        geoFire.setLocation(key, new GeoLocation(coor.latitude, coor.longitude));
        String s = sport.getSelectedItem().toString();
        String n = name.getText().toString().length()>0 ? name.getText().toString() : String.valueOf(s+" game");
        Event e = new Event(key, s, date_time, 1 + "", location, n);
        db.child("events").child(key).setValue(e);
        db.child("userEvents").child(EventListActivity.uid).child(key).setValue(e);
        createGroupMessage();
        Toast.makeText(CreateEvent.this, "Pickup Game Created",
                Toast.LENGTH_SHORT).show();
        Intent x = new Intent(CreateEvent.this, EventListActivity.class);
        startActivity(x);
    }

    public void createGroupMessage(){
        final ChannelMetadata channelMetadata = new ChannelMetadata();
        channelMetadata.setCreateGroupMessage(ChannelMetadata.ADMIN_NAME + " created " + ChannelMetadata.GROUP_NAME);
        channelMetadata.setAddMemberMessage(ChannelMetadata.ADMIN_NAME + " added " + ChannelMetadata.USER_NAME);
        channelMetadata.setRemoveMemberMessage(ChannelMetadata.ADMIN_NAME + " removed " + ChannelMetadata.USER_NAME);
        channelMetadata.setGroupNameChangeMessage(ChannelMetadata.USER_NAME + " changed group name " + ChannelMetadata.GROUP_NAME);
        channelMetadata.setJoinMemberMessage(ChannelMetadata.USER_NAME + " joined");
        channelMetadata.setGroupLeftMessage(ChannelMetadata.USER_NAME + " left group " + ChannelMetadata.GROUP_NAME);
        channelMetadata.setGroupIconChangeMessage(ChannelMetadata.USER_NAME + " changed icon");
        channelMetadata.setDeletedGroupMessage(ChannelMetadata.ADMIN_NAME + " deleted group " + ChannelMetadata.GROUP_NAME);

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> channelMembersList = new ArrayList<String>();
                String s = sport.getSelectedItem().toString();
                String n = name.getText().toString().length()>0 ? name.getText().toString() : String.valueOf(s+" game");
                ChannelInfo channelInfo  = new ChannelInfo(n,channelMembersList);
                channelInfo.setType(Channel.GroupType.PUBLIC.getValue().intValue()); //group type
                //channelInfo.setImageUrl(""); //pass group image link URL
                channelInfo.setChannelMetadata(channelMetadata); //Optional option for setting group meta data
                channelInfo.setClientGroupId(key); //Optional if you have your own groupId then you can pass here
                ChannelService channelService = ChannelService.getInstance(CreateEvent.this);
                Channel channel = channelService.createChannel(channelInfo);
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SportSearch");
                ref.child("eventGM").child(key).setValue(channel.getKey());
                System.out.println("GROUP_ID "+channel.getClientGroupId());
                System.out.println("USER_COUNT "+channel.getUserCount());
                System.out.println(ChannelService.getInstance(CreateEvent.this).getChannelInfo(key));
            }
        }).start();
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}

