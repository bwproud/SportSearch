package edu.unc.brennan.sportsearch;

import java.io.Serializable;

/**
 * Created by Brennan on 11/26/17.
 */

public class Event implements Serializable{
    public final String id;
    public final String sport;
    public final String date;
    public final String participants;
    public final String location;
    public final String name;

    public Event(String id, String sport, String date, String participants, String location, String name) {
        this.id = id;
        this.sport = sport;
        this.date = date;
        this.participants = participants;
        this.location = location;
        this.name = name;
    }
    public Event() {
        this.id = null;
        this.sport = null;
        this.date = null;
        this.participants = null;
        this.location = null;
        this.name = null;
    }

    public String getId(){
        return id;
    }

    public String getSport(){
        return sport;
    }

    public String getParticipants(){
        return participants;
    }

    public String getLocation(){
        return location;
    }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }
}
