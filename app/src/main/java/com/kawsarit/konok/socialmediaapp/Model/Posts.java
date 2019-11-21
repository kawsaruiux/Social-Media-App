package com.kawsarit.konok.socialmediaapp.Model;

//Make sure use the same name here which one create on firebase database under the 'Posts'.
//For a properly constructed model class, Firebase can perform automatic serialization
//in DatabaseReference#setValue() and automatic deserialization in DataSnapshot#getValue().

public class Posts {    //Posts is module class

    public String uid, time, date, description, fullname, postimage, profileimage;

    public Posts(){     //This is default constructor. Its name should be same as module class(is required for Firebase's automatic data mapping.)

    }

    public Posts(String uid, String time, String date, String description, String fullname, String postimage, String profileimage) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.description = description;
        this.fullname = fullname;
        this.postimage = postimage;
        this.profileimage = profileimage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
