package com.example.myapplication.ui.reservation;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Reservation{
    public ObjectId _id;
    public String room;
    public Date date;
    public String time;
    public String duration;
    public ArrayList<String> attendees;
    public String description;
    public  ObjectId createdBy;

    public Reservation(Document doc){
        _id = doc.getObjectId("_id");
        room = doc.getString("room");
        date = doc.getDate("date");
        time = doc.getString("time");
        duration = doc.getString("duration");
        attendees = (ArrayList<String>) doc.get("attendees");
        description = doc.getString("description");
        createdBy = doc.getObjectId("createdBy");
     }

     public Reservation(){

     }

     public String getSummary(){
        String members = String.join(", ", attendees);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String date_format = String.format("%1$tb %1$td, %1$tY", cal);
        String time_format = String.format("%s:00 to %d:00", time, (Integer.valueOf(time)+Integer.valueOf(duration))%24);
        String summary[] = {date_format, time_format,description, members};
        return String.join("\n", summary);
     }

     public Document get(){
        Document doc = new Document();
        doc.append("_id", _id);
        doc.append("room", room);
        doc.append("date", date);
        doc.append("time", time);
        doc.append("duration", duration);
        doc.append("attendees", attendees);
        doc.append("description", description);
        doc.append("createdBy", createdBy);
        return doc;
     }





}
