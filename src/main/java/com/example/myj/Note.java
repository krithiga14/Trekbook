package com.example.myj;

import com.google.firebase.Timestamp;

public class Note {
    public String title;
    public String content;
    public Timestamp timestamp;
    public String imageUrl; // New field for storing image URL or image data

    public Note() {
        // Default constructor required for Firebase
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
