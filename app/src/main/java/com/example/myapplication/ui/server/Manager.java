package com.example.myapplication.ui.server;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Manager {
    public ObjectId _id;
    public String name;

    public Manager(Document doc){
        _id = doc.getObjectId("_id");
        name = doc.getString("username");
    }
}
