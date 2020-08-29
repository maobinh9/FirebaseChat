package com.example.firebasechat;

public class Comments {
    String comment, date, time, username;

    public Comments(String comment, String date, String time, String username) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.username = username;
    }

    public Comments() {
    }

    public String getComment() {
        return comment;
    }

    public void setComments(String comments) {
        this.comment = comments;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
