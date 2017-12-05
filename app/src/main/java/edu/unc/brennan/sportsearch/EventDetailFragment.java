package edu.unc.brennan.sportsearch;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelAddMemberTask;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelRemoveMemberTask;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.unc.brennan.sportsearch.dummy.DummyContent;

/**
 * A fragment representing a single Event detail screen.
 * This fragment is either contained in a {@link EventListActivity}
 * in two-pane mode (on tablets) or a {@link EventDetailActivity}
 * on handsets.
 */
public class EventDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Event event;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventDetailFragment() {
    }
    int current;
    LayoutInflater in;
    ViewGroup con;
    TextView text;
    DatabaseReference db;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            db = database.getReference("SportSearch");
            event = EventListActivity.map.get(getArguments().get(ARG_ITEM_ID));
            EventDetailActivity.clicked=false;
            db.child("userEvents").child(EventListActivity.uid).child(event.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null) {
                        EventDetailActivity.clicked = true;
                        EventDetailActivity.fab.setImageResource(R.drawable.done);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            current = Integer.parseInt(event.getParticipants());
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(event.getName());
            }
        }
    }

    public void increment(){
        current++;
        db.child("eventGM").child(event.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    ApplozicChannelAddMemberTask.ChannelAddMemberListener channelAddMemberListener =  new ApplozicChannelAddMemberTask.ChannelAddMemberListener() {
                        @Override
                        public void onSuccess(String response, Context context) {
                            System.out.println("Adding was a success");
                        }

                        @Override
                        public void onFailure(String response, Exception e, Context context) {
                            System.out.println("Adding was a failure");
                        }
                    };
                    ApplozicChannelAddMemberTask applozicChannelAddMemberTask =  new ApplozicChannelAddMemberTask(getContext(),dataSnapshot.getValue(Integer.class),EventListActivity.uid,channelAddMemberListener);//pass channel key and userId whom you want to add to channel
                    applozicChannelAddMemberTask.execute((Void)null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        db.child("userEvents").child(EventListActivity.uid).child(event.getId()).setValue(event);
        System.out.println(event.getId());
        db.child("events").orderByChild("id").equalTo(event.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Event e = dataSnapshot.getValue(Event.class);
                db.child("events").child(event.getId()).child("participants").setValue(String.valueOf(Integer.parseInt(e.getParticipants())+1));
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
        setText();
    }

    public void decrement(){
        current--;

        db.child("eventGM").child(event.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    ApplozicChannelRemoveMemberTask.ChannelRemoveMemberListener channelRemoveMemberListener =  new ApplozicChannelRemoveMemberTask.ChannelRemoveMemberListener() {
                        @Override
                        public void onSuccess(String response, Context context) {
                            System.out.println("Removing was a success");
                        }

                        @Override
                        public void onFailure(String response, Exception e, Context context) {
                            System.out.println("Removing was a failure");
                        }
                    };
                    ApplozicChannelRemoveMemberTask applozicChannelRemoveMemberTask =  new ApplozicChannelRemoveMemberTask(getContext(),dataSnapshot.getValue(Integer.class),EventListActivity.uid,channelRemoveMemberListener);//pass channel key and userId whom you want to add to channel
                    applozicChannelRemoveMemberTask.execute((Void)null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        db.child("userEvents").child(EventListActivity.uid).child(event.getId()).removeValue();
        db.child("events").orderByChild("id").equalTo(event.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                Event e = dataSnapshot.getValue(Event.class);
                db.child("events").child(e.getId()).child("participants").setValue(String.valueOf(Integer.parseInt(e.participants)-1));
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
        setText();
    }

    public String getKey(){
        return event.getId();
    }

    public void setText(){
        text.setText("Event information:"+
                "\nSport: "+event.getSport()+
                "\nDate: "+event.getDate()+
                "\nLocation: "+event.getLocation()+
                "\nParticipants: "+current);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        in = inflater;
        con = container;
        View rootView = inflater.inflate(R.layout.event_detail, container, false);

        if (event != null) {
            text=((TextView) rootView.findViewById(R.id.event_detail));
            text.setText("Event information:"+
                            "\nSport: "+event.getSport()+
                            "\nDate: "+event.getDate()+
                            "\nLocation: "+event.getLocation()+
                            "\nParticipants: "+current);
        }

        return rootView;
    }
}
