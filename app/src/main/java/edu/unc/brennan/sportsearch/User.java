package edu.unc.brennan.sportsearch;

/**
 * Created by Brennan on 11/26/17.
 */

public class User {
    public final String id;
    public final String email;
    public final String firstName;
    public final String lastName;
    public final String zip;
    public final String favSport;


    public User(String id, String email, String firstName, String lastName, String zip, String favSport) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.zip = zip;
        this.favSport = favSport;
    }

    public User() {
        this.id = null;
        this.email = null;
        this.firstName = null;
        this.lastName = null;
        this.zip = null;
        this.favSport = null;
    }

    public String getId(){
        return id;
    }

    public String getEmail(){
        return email;
    }

    public String getFirstName(){ return firstName;}

    public String getLastName(){ return lastName;}

    public String getZip(){
        return zip;
    }

    public String getFavSport(){
        return favSport;
    }

}
