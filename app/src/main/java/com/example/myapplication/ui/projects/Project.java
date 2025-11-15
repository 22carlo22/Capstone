package com.example.myapplication.ui.projects;

import android.graphics.Color;
import android.widget.TextView;

import com.example.myapplication.ui.server.Server;
import com.example.myapplication.ui.server.Task;

import org.bson.types.ObjectId;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class Project {
    public ObjectId _id;
    public ObjectId createdBy;
    public String name, descp;
    public Date deadline;
    public Date createdAt;
    public ArrayList<Task> tasks = new ArrayList<>();

    public Project(Document doc){
        _id = doc.getObjectId("_id");
        createdBy = doc.getObjectId("createdBy");
        name = doc.getString("name");
        descp = doc.getString("description");
        deadline = doc.getDate("deadline");
        createdAt = doc.getDate("createdAt");
    }

    public Document get(){
        Document doc = new Document();
        doc.append("_id", _id);
        doc.append("name", name);
        doc.append("description", descp);
        doc.append("deadline", deadline);
        doc.append("createdBy", createdBy);
        doc.append("createdAt", createdAt);
        return doc;
    }

    public float getDeadlineRatio(){
        long now = Calendar.getInstance().getTime().getTime();
        long start = createdAt.getTime();
        long end = deadline.getTime();
        if(now >= end) return 1;
        return 1.0f*(now-start)/(end-start);
    }

    public ArrayList<String> getMembers(){
        ArrayList<String> members = new ArrayList<>();
        for(Task task: tasks){
            for(String name:Server.idtoEmployeeString(task.assignedEmployees)){
                if(!members.contains(name)) members.add(name);
            }
        }
        return members;
    }

    public void setProjectStatus(ObjectId employee, TextView textView){
        long days_left = (deadline.getTime()- Calendar.getInstance().getTime().getTime());
        days_left = days_left/1000/86400;

        ArrayList<Task> myTasks;
        if(employee == null) myTasks = tasks;
        else myTasks = getEmployeeTasks(employee);

        int completed = 0;
        for(Task task:myTasks) {
            if(task.isCompleted) completed++;
        }

        if(completed == myTasks.size()){
            textView.setText("Completed");
            textView.setTextColor(Color.rgb(0, 150, 0));
        }
        else if(days_left < 0){
            textView.setText(String.format("Due %d days ago • %d pending tasks", -days_left, myTasks.size()-completed));
            textView.setTextColor(Color.rgb(200,0,0));
        }
        else{
            textView.setText(String.format("%d days left • %d pending tasks", days_left, myTasks.size()-completed));
            textView.setTextColor(Color.GRAY);
        }
    }


    public ArrayList<Task> getEmployeeTasks(ObjectId employee){
        ArrayList<Task> myTasks = new ArrayList<>();
        for(Task task:tasks){
            if(!task.assignedEmployees.contains(employee)) continue;
            myTasks.add(task);
        }

        return myTasks;
    }

    public float getCompletion(){
        if(tasks.isEmpty()) return 0;

        float sum = 0;
        for(Task t:tasks){
            if(t.isCompleted) sum++;
        }

        return sum/tasks.size();
    }

}
