package com.example.myapplication.ui.reservation;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Room{
    public ArrayList<Integer> open;
    public boolean hotdesk;
    public int capacity;

    public ObjectId _id;
    public String name;

    public HashMap<String, RoomSlot> my_slot;

    public String selected_date;

    public ArrayList<Integer> slots;
    public ArrayList<Integer> cap;

    public Room(Document doc){

        open = (ArrayList<Integer>)  doc.get("open");
        hotdesk = doc.getBoolean("hotdesk");
        capacity = doc.getInteger("capacity");
        name = doc.getString("room");
        _id = doc.getObjectId("_id");

        my_slot = new HashMap<>();
        for(String s:doc.keySet()){
            if(s.replace("-","").matches("[0-9]+")){
                Document temp = (Document) doc.get(s);
                my_slot.put(s, new RoomSlot((ArrayList<Integer>) temp.get("slots"), (ArrayList<Integer>) temp.get("cap")));
            }
        }
    }

    public void updateDate(Date date){
        String selected_date = new SimpleDateFormat("yyyy-MM-dd").format(date);
        RoomSlot reserved = my_slot.get(selected_date);

        if(reserved == null){
            my_slot.put(selected_date, new RoomSlot(new ArrayList<>(), new ArrayList<>()));
            reserved = my_slot.get(selected_date);
        }
        slots = reserved.slots;
        cap = reserved.cap;
    }

    public void decrementCap(ArrayList<Integer> position){
        for(int i = 0; i < slots.size(); i++){
            if(position.contains(slots.get(i))) cap.set(i, cap.get(i)-1);
        }
    }

    public void incrementCap(ArrayList<Integer> position){
        for(int i = 0; i < slots.size(); i++){
            if(position.contains(slots.get(i))) cap.set(i, cap.get(i)+1);
        }
    }
    public String  getSummaryToday(){
        updateDate(Calendar.getInstance().getTime());
        String out = "";

        int hour = Calendar.getInstance().getTime().getHours();
        if(open.get(0) <= hour && hour < open.get(1)){
            out += "OPEN - closes "+open.get(1)+":00\n";
            float busy_rate = 0;
            for(Integer c:cap) busy_rate += capacity-c;
            busy_rate /= (capacity*cap.size());
            if(busy_rate < 0.3) out += "Not busy";
            else if (busy_rate < 0.6) out += "Busy";
            else out += "Very busy";
        }
        else{
            out += "Close - opens "+open.get(0)+":00\n";
        }
        return out;
    }

    public Document get(){
        Document doc = new Document();
        doc.append("open", open);
        doc.append("hotdesk", hotdesk);
        doc.append("capacity", capacity);
        doc.append("room", name);
        doc.append("_id", _id);
        for(String s: my_slot.keySet()) doc.append(s, my_slot.get(s).get());
        return doc;
    }

    @Override
    public String toString() {
        return name.toLowerCase();
    }

}
