package com.example.myapplication.ui.calendar;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Event {
    public ObjectId _id;
    public String formattedDate;
    public String name;
    public Date date;
    public String time;
    public ArrayList<String> attendees;
    public String location;
    public String description;
    public ObjectId createdBy;


    public Event(Document doc){
        _id = doc.getObjectId("_id");
        name = doc.getString("name");
        date = doc.getDate("date");
        formattedDate = doc.getString("formattedDate");
        time = doc.getString("time");
        attendees = (ArrayList<String>) doc.get("attendees");
        location = doc.getString("location");
        description = doc.getString("description");
        createdBy = doc.getObjectId("createdBy");
    }

    public Document get(){
        Document doc = new Document();
        doc.append("_id", _id);
        doc.append("name", name);
        doc.append("date", date);
        doc.append("formattedDate", formattedDate);
        doc.append("time", time);
        doc.append("attendees", attendees);
        doc.append("location", location);
        doc.append("description", description);
        doc.append("createdBy", createdBy);
        return doc;
    }

    public String getSummary(){
        String members = String.join(", ", attendees);
        ArrayList<String> summary = new ArrayList<String>(Arrays.asList(description, location, time, members));
        while(summary.contains("")) summary.remove("");

        return String.join("\n", summary);
    }
}

