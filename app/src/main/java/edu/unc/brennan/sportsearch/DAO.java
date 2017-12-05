package edu.unc.brennan.sportsearch;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;

/**
 * Created by Brennan on 11/26/17.
 */

public class DAO {
        DatabaseReference db;
        Boolean saved=null;
        ArrayList<Event> events=new ArrayList<>();

        public DAO(DatabaseReference db) {
            this.db = db;
        }

        //WRITE IF NOT NULL
        public Boolean save(Event event)
        {
            if(event==null){
                saved=false;
            }else{
                try{
                    db.child("Event").push().setValue(event);
                    saved=true;
                }catch (DatabaseException e){
                    e.printStackTrace();
                    saved=false;
                }
            }
            return saved;
        }

        //IMPLEMENT FETCH DATA AND FILL ARRAYLIST
        private void fetchData(DataSnapshot dataSnapshot){
            events.clear();
            for (DataSnapshot ds : dataSnapshot.getChildren()){
                Event event=ds.getValue(Event.class);
                events.add(event);
            }
        }

        //READ BY HOOKING ONTO DATABASE OPERATION CALLBACKS
        public ArrayList<Event> retrieve() {
            db.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    fetchData(dataSnapshot);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    fetchData(dataSnapshot);
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
            return events;
        }
}
