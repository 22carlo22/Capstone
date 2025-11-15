package com.example.myapplication.ui.reservation;

import org.bson.Document;

import java.util.ArrayList;

public class RoomSlot {
    public ArrayList<Integer> slots;
    public ArrayList<Integer> cap;

    public RoomSlot(ArrayList<Integer> slots, ArrayList<Integer> cap){
        this.slots = slots;
        this.cap = cap;
    }

    public Document get(){
        Document doc = new Document();
        doc.append("slots", slots);
        doc.append("cap", cap);
        return doc;
    }
}
