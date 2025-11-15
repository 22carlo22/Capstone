package com.example.myapplication.ui.reservation;

import java.util.ArrayList;

public class ReservationHelper {
    public static void fragmentMode(ArrayList<Integer> arr, int len){
        if(arr.size() < len){arr.clear();}
        while(arr.size() > len){
            arr.remove(arr.size()-1);
        }
    }

    public static void strictMode(ArrayList<Integer> arr, int len){
        ArrayList<Integer> out = new ArrayList<>();
        boolean status;
        for(Integer a:arr){
            status = true;
            for(int i = 0; i < len; i++){
                status = status && arr.contains(a+i);
            }
            if(status){
                for(int i = 0; i < len; i++) out.add(a+i);
                break;
            }
        }
        arr.clear();
        arr.addAll(out);
    }

    public static void intervalFilter(ArrayList<Integer> arr, int min, int max){
        ArrayList<Integer> out = new ArrayList<>();
        for(Integer a:arr){
            if(min <= a && a <= max){
                out.add(a);
            }
        }
        arr.clear();
        arr.addAll(out);
    }

    public static ArrayList<Integer> getValidArray(ArrayList<Integer> arr, ArrayList<Integer> limit){
        ArrayList<Integer> out = new ArrayList<>();
        for(int i = 0; i < arr.size(); i++){
            if(limit.get(i) > 0){
                out.add(arr.get(i));
            }
        }
        return out;
    }

}
