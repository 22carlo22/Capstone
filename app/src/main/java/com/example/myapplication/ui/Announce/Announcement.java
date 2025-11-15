package com.example.myapplication.ui.Announce;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Announcement {

    public ObjectId _id;
    public ObjectId createdBy;

    public String title;
    public String content;

    public Announcement(Document doc){
        _id = doc.getObjectId("_id");
        createdBy = doc.getObjectId("createdBy");
        title = doc.getString("title");
        content = doc.getString("content");
    }

    public Document get(){
        Document doc = new Document();
        doc.append("_id", _id);
        doc.append("title", title);
        doc.append("content", content);
        doc.append("createdBy", createdBy);
        return doc;
    }
}
