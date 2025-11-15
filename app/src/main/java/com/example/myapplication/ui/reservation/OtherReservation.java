package com.example.myapplication.ui.reservation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class OtherReservation {
    public HashMap<String, ArrayList<Integer>> taken_slots;

    public OtherReservation(){
        taken_slots = new HashMap<String, ArrayList<Integer>>();
    }

    public void addSlots(Date date, int start, int dur){
        String selected_date = new SimpleDateFormat("yyyy-MM-dd").format(date);

        if(!taken_slots.containsKey(selected_date)) taken_slots.put(selected_date, new ArrayList<>());
        for(int i = start; i < start+dur; i++){
            taken_slots.get(selected_date).add(i);
        }
    }
    public ArrayList<Integer> getSlots(Date date){
        String selected_date = new SimpleDateFormat("yyyy-MM-dd").format(date);
        ArrayList<Integer> slots = taken_slots.get(selected_date);
        if(slots == null) return new ArrayList<>();
        return slots;
    }

}
