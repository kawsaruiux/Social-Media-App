package com.kawsarit.konok.socialmediaapp.Model;

public class Friends {

    public String fullname, profileimage, date;

    public Friends() {

    }

    public Friends(String fullname, String profileimage, String date) {
        this.fullname = fullname;
        this.profileimage = profileimage;
        this.date = date;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
