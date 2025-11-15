package com.example.myapplication.ui.server;

import android.util.Log;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public class Employee {
    public ObjectId _id;
    public String username;
    public ArrayList<String> skills;

    public int rank;

    public Employee(Document doc) {
        _id = doc.getObjectId("_id");
        username = doc.getString("username");
        skills = (ArrayList<String>) doc.get("skills");
    }

    public boolean hasSkill(ArrayList<String> required_skills) {
        for (String s : required_skills) {
            if (!skills.contains(s)) return false;
        }
        return true;
    }

    public int getAssignedWork() {
        if (!Server.employees_assigned.containsKey(_id)) return 0;
        return Server.employees_assigned.get(_id);
    }

    public int getPending() {
        if (!Server.employees_pending.containsKey(_id)) return 0;
        return Server.employees_pending.get(_id);
    }

    public String getAvgCompletionTime(){
        long completionTime_sum;
        int completionTime_N;

        if(!Server.employees_assigned.containsKey(_id)) return "N/A";

        try{
            completionTime_N = Server.employees_completionN.get(_id);
            completionTime_sum = Server.employees_completionTime.get(_id);
        } catch(Exception e){
            return "N/A";
        }
        if(completionTime_N == 0) return "N/A";

        int avg_ms = (int) ((completionTime_sum)/completionTime_N);
        double days = 1.0*avg_ms/1000/3600/24;
        double hours = 1.0*avg_ms/1000/3600;
        if(hours < 24) return String.format("%.2f hours/task", hours);
        return String.format("%.2f days/task", days);

    }

    public int getAvgCompletionTime_ms(){
        long completionTime_sum = 0;
        int completionTime_N = 0;

        if(!Server.employees_assigned.containsKey(_id)) return 2147483646;
        try{
            completionTime_N = Server.employees_completionN.get(_id);
            completionTime_sum = Server.employees_completionTime.get(_id);
        } catch(Exception e){
            return 2147483646;
        }

        if(completionTime_N == 0) return 2147483646;

        return (int) ((completionTime_sum)/completionTime_N);

    }


}
