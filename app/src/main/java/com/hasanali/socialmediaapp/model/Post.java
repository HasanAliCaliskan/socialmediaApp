package com.hasanali.socialmediaapp.model;

public class Post {
    public String comment;
    public String date;
    public String image;
    public String pphoto;
    public String username;
    public String fullname;

    public Post (String comment, String image, String pphoto, String username, String fullname, String date) {
        this.comment = comment;
        this.date = date;
        this.image = image;
        this.pphoto = pphoto;
        this.username = username;
        this.fullname = fullname;
    }
}
