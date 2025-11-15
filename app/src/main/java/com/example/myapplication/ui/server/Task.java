package com.example.myapplication.ui.server;

import android.widget.CheckBox;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Task {
    public ObjectId _id;
    public String name;
    public boolean isCompleted;
    public ArrayList<ObjectId> assignedEmployees;
    public ObjectId projectId;
    public Date createdAt;
    public Date completedAt;
    public ArrayList<String> skillsRequired;


    public Task(Document doc){
        _id = doc.getObjectId("_id");
        name = doc.getString("name");
        isCompleted = doc.getBoolean("isCompleted");
        assignedEmployees = (ArrayList<ObjectId>) doc.get("assignedEmployees");
        projectId = doc.getObjectId("projectId");
        createdAt = doc.getDate("createdAt");
        completedAt = doc.getDate("completedAt");
        skillsRequired = (ArrayList<String>) doc.get("skillsRequired");

    }

    public Document get(){
        Document doc = new Document();
        doc.append("_id", _id);
        doc.append("name", name);
        doc.append("isCompleted", isCompleted);
        doc.append("assignedEmployees", assignedEmployees);
        doc.append("projectId", projectId);
        doc.append("createdAt", createdAt);
        doc.append("completedAt", completedAt);
        doc.append("skillsRequired", skillsRequired);
        return doc;
    }

    public int getAverageCompletionTimeEmployee(Employee e){
        int avg = e.getAvgCompletionTime_ms();
        if(avg != 2147483646) return avg;

        int N = 0;
        avg = 0;
        for(Employee i:Server.employees) {
            int e_avg = i.getAvgCompletionTime_ms();
            if (e_avg != 2147483646) {
                avg += e_avg;
                N++;
            }
        }

        avg /= N;
        return avg;
    }

}
