package com.example.myapplication.ui.server;

import com.example.myapplication.ui.Announce.Announcement;
import com.example.myapplication.ui.projects.Project;
import com.example.myapplication.ui.calendar.Event;
import com.example.myapplication.ui.reservation.OtherReservation;
import com.example.myapplication.ui.reservation.Reservation;
import com.example.myapplication.ui.reservation.Room;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import io.realm.mongodb.mongo.result.DeleteResult;
import io.realm.mongodb.mongo.result.InsertOneResult;
import io.realm.mongodb.mongo.result.UpdateResult;

public class Server {

    public static ArrayList<String> skills = new ArrayList<>(Arrays.asList("Select a skill", "Civil", "Software", "Computer"));

    public static User user;
    public static ObjectId user_id;
    public static String username;
    public static boolean admin;

    public static ArrayList<Project> projects = new ArrayList<>();
    public static Project selected_project;

    public static ArrayList<Employee> employees = new ArrayList<>();
    public static ArrayList<Manager> managers = new ArrayList<>();

    public static ArrayList<Event> events = new ArrayList<>();

    public static ArrayList<Announcement> announcements = new ArrayList<>();

    public static ArrayList<Reservation> reservations = new ArrayList<>();

    public static ArrayList<Room> rooms = new ArrayList<>();

    public static HashMap<ObjectId, Integer> employees_assigned = new HashMap<>();
    public static HashMap<ObjectId, Integer> employees_pending = new HashMap<>();

    public static HashMap<ObjectId, Long> employees_completionTime = new HashMap<>();
    public static HashMap<ObjectId, Integer> employees_completionN = new HashMap<>();

    public static HashMap<String, OtherReservation> other_reservations = new HashMap<>();


    public static void readAll(){
        Server.readEmployees();
        Server.readAnnouncements();
        Server.readManagers();
        Server.readMyReservations();

        if(Server.admin) {
            Server.readRooms(false);
            Server.readMyProjects();
            Server.readMyEvents();
            Server.readBusyEmployees();
        }
        else{
            Server.readRooms(true);
            Server.readInvitedProjects();
            Server.readInvitedEvents();
        }
    }

    public static Employee getEmployee(ObjectId id){
        for(Employee e:employees){
            if(e._id.equals(id)) return e;
        }
        return null;
    }

    public static void updateHashAdd(HashMap<ObjectId, Integer> myHash, ObjectId id, int val){
        if(!myHash.containsKey(id)) myHash.put(id, 0);
        myHash.put(id, myHash.get(id)+val);
    }

    public static void updateHashAdd(HashMap<ObjectId, Long> myHash, ObjectId id, long val){
        if(!myHash.containsKey(id)) myHash.put(id, (long) 0);
        myHash.put(id, myHash.get(id)+val);
    }
    public static void readBusyEmployees(){
        MongoCollection<Document> col = getCol("tasks");
        col.find().iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                employees_assigned.clear();
                employees_pending.clear();
                employees_completionN.clear();
                employees_completionTime.clear();
                while(result.get().hasNext()) {
                    Document doc = result.get().next();
                    try {
                        ArrayList<ObjectId> members = (ArrayList<ObjectId>) doc.get("assignedEmployees");
                        boolean isCompleted = doc.getBoolean("isCompleted");
                        for (ObjectId m : members) {
                            updateHashAdd(employees_assigned, m, 1);
                            updateHashAdd(employees_pending, m, 0);
                            updateHashAdd(employees_completionN, m, 0);
                            updateHashAdd(employees_completionTime, m, 0);

                            if(!isCompleted) updateHashAdd(employees_pending, m, 1);
                            else{
                                updateHashAdd(employees_completionN, m, 1);
                                updateHashAdd(employees_completionTime, m, doc.getDate("completedAt").getTime() - doc.getDate("createdAt").getTime());
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
    }


    public static void  readRooms(boolean hotdesk_only){
        MongoCollection<Document> col = getCol("reservations");
        col.find().iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                rooms.clear();
                while(result.get().hasNext()){
                    Document doc = result.get().next();
                    if(hotdesk_only) {
                        if (doc.getBoolean("hotdesk")) rooms.add(new Room(doc));
                    }
                    else rooms.add(new Room(doc));
                }
            }
        });
    }

    public static void updateRoom(Room r){
        MongoCollection<Document> col = getCol("reservations");

        col.updateOne(new Document().append("_id", r._id), r.get()).getAsync(new App.Callback<UpdateResult>() {
            @Override
            public void onResult(App.Result<UpdateResult> result) {
            }
        });
    }

    public static void readOtherReservations(){
        MongoCollection<Document> col = getCol("reservationlists");
        col.find().iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                other_reservations.clear();
                while(result.get().hasNext()){
                    Document doc = result.get().next();
                    ArrayList<String> members = (ArrayList<String>) doc.get("attendees");

                    int start = Integer.valueOf(doc.getString("time"));
                    int dur = Integer.valueOf(doc.getString("duration"));
                    String creator = getManagerName(doc.getObjectId("createdBy"));
                    Date date = doc.getDate("date");

                    for(String m:members){
                        if(!other_reservations.containsKey(m)){
                            other_reservations.put(m, new OtherReservation());
                        }
                        other_reservations.get(m).addSlots(date, start, dur);
                    }

                    if(!other_reservations.containsKey(creator)){
                        other_reservations.put(creator, new OtherReservation());
                    }
                    other_reservations.get(creator).addSlots(date, start, dur);
                }
            }
        });
    }



    public static String getManagerName(ObjectId _id){
        for(Manager m:managers){
            if(m._id.equals(_id)) return m.name;
        }
        return null;
    }

    public static void readMyReservations(){
        MongoCollection<Document> col = getCol("reservationlists");
        col.find().iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                reservations.clear();
                while(result.get().hasNext()){
                    Document doc = result.get().next();
                    if(doc.getObjectId("createdBy").equals(user_id) || ((ArrayList<String>) doc.get("attendees")).contains(username)) {
                        reservations.add(new Reservation(doc));
                    }
                }
            }
        });
    }

    public static void addReservation(Reservation r){
        MongoCollection<Document> col = getCol("reservationlists");
        col.insertOne(r.get()).getAsync(new App.Callback<InsertOneResult>() {
            @Override
            public void onResult(App.Result<InsertOneResult> result) {

            }
        });
    }

    public static void deleteReservation(Reservation r){
        MongoCollection<Document> col = getCol("reservationlists");
        col.deleteOne(new Document("_id", r._id)).getAsync(new App.Callback<DeleteResult>() {
            @Override
            public void onResult(App.Result<DeleteResult> result) {

            }
        });
    }

    public static void appendAnnouncement(Announcement a){
        MongoCollection<Document> col = getCol("announcements");
        col.insertOne(a.get()).getAsync(new App.Callback<InsertOneResult>() {
            @Override
            public void onResult(App.Result<InsertOneResult> result) {

            }
        });
    }

    public static void  readAnnouncements(){
        MongoCollection<Document> col = getCol("announcements");
        col.find().iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                announcements.clear();
                while(result.get().hasNext()){
                    Document doc = result.get().next();
                    Announcement a = new Announcement(doc);
                    announcements.add(a);
                }
            }
        });
    }

    public static void deleteAnnouncement(Announcement a){
        MongoCollection<Document> col = getCol("announcements");
        col.deleteOne(new Document("_id", a._id)).getAsync(new App.Callback<DeleteResult>() {
            @Override
            public void onResult(App.Result<DeleteResult> result) {

            }
        });
    }

    public static void appendEvent(Event event){
        MongoCollection<Document> col = getCol("calendars");
        col.insertOne(event.get()).getAsync(new App.Callback<InsertOneResult>() {
            @Override
            public void onResult(App.Result<InsertOneResult> result) {

            }
        });
    }

    public static void readInvitedEvents(){
        MongoCollection<Document> col = getCol("calendars");
        col.find().iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                events.clear();
                while(result.get().hasNext()){
                    Document doc = result.get().next();
                    ArrayList<String> members = (ArrayList<String>) doc.get("attendees");
                    if(members.contains(username)) events.add(new Event(doc));
                 }
            }
        });
    }

    public static void readMyEvents(){
        MongoCollection<Document> col = getCol("calendars");
        col.find(new Document("createdBy", user_id)).iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                events.clear();
                while(result.get().hasNext()){
                    Document doc = result.get().next();
                    events.add(new Event(doc));
                }
            }
        });
    }

    public static void deleteEvent(Event event){
        MongoCollection<Document> col = getCol("calendars");
        col.deleteOne(new Document("_id", event._id)).getAsync(new App.Callback<DeleteResult>() {
            @Override
            public void onResult(App.Result<DeleteResult> result) {

            }
        });
    }

    public static void readInvitedProjects(){
        MongoCollection<Document> col_project = getCol("projects");
        MongoCollection<Document> col_task = getCol("tasks");
        col_project.find().iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                projects.clear();
                while(result.get().hasNext()){
                    Document doc = result.get().next();
                    Project p = new Project(doc);
                    col_task.find(new Document("projectId", p._id)).iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
                        @Override
                        public void onResult(App.Result<MongoCursor<Document>> result) {
                            boolean has_project = false;
                            while(result.get().hasNext()){
                                Document doc = result.get().next();
                                if(doc.get("assignedEmployees") != null){
                                    Task t = new Task(doc);
                                    has_project |= t.assignedEmployees.contains(user_id);
                                    p.tasks.add(t);
                                }
                            }
                            if(has_project) projects.add(p);
                        }
                    });
                }
            }
        });
    }

    public static  ArrayList<ObjectId> stringtoEmployeeIDs(ArrayList<String> name){
        ArrayList<ObjectId> id = new ArrayList<>();
        for(String n:name) {
            id.add(getEmployeeId(n));
        }
        return id;
    }

    public static ArrayList<String> idtoEmployeeString(ArrayList<ObjectId> ids){
        ArrayList<String> name = new ArrayList<>();
        for(ObjectId id:ids) {
            name.add(getEmployeeName(id));
        }
        return name;
    }

    public static ObjectId getEmployeeId(String name){
        for(Employee e:employees) if(e.username.equals(name)) return e._id;
        return null;
    }

    public static String getEmployeeName(Object id){
        for(Employee e:employees) if(e._id.equals(id)) return e.username;
        return null;
    }


    public static void appendTask(Task task){
        MongoCollection<Document> col = getCol("tasks");
        col.insertOne(task.get()).getAsync(new App.Callback<InsertOneResult>() {
            @Override
            public void onResult(App.Result<InsertOneResult> result) {

            }
        });
    }

    public static void deleteTask(Task task){
        MongoCollection<Document> col = getCol("tasks");
        col.deleteOne(new Document("_id", task._id)).getAsync(new App.Callback<DeleteResult>() {
            @Override
            public void onResult(App.Result<DeleteResult> result) {

            }
        });
    }

    public static void updateTask(Task task){
        MongoCollection<Document> col = getCol("tasks");
        col.updateOne(new Document("_id", task._id), task.get()).getAsync(new App.Callback<UpdateResult>() {
            @Override
            public void onResult(App.Result<UpdateResult> result) {
            }
        });
    }

    public static void readEmployees(){
        MongoCollection<Document> col = getCol("users");
        col.find(new Document("admin", false)).iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                employees.clear();
                while(result.get().hasNext()){
                    employees.add(new Employee(result.get().next()));
                }
            }
        });
    }

    public static void readManagers(){
        MongoCollection<Document> col = getCol("users");
        col.find(new Document("admin", true)).iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                managers.clear();
                while(result.get().hasNext()){
                    managers.add(new Manager(result.get().next()));
                }
            }
        });
    }

    public static ArrayList<Employee> filterEmployeesWithSkill(ArrayList<String> required_skills){
        if(required_skills.isEmpty()) return new ArrayList<>(employees);

        ArrayList<Employee> employees_with_skill = new ArrayList<>();
        for(Employee e:employees){
            if(e.hasSkill(required_skills)) employees_with_skill.add(e);
        }
        return employees_with_skill;
    }

    public static void appendMyProjects(Project project){
        MongoCollection<Document> col = getCol("projects");
        col.insertOne(project.get()).getAsync(new App.Callback<InsertOneResult>() {
            @Override
            public void onResult(App.Result<InsertOneResult> result) {
                projects.add(project);
            }
        });
    }

    public static void deleteMyProjects(Project project){
        MongoCollection<Document> col = getCol("projects");
        col.deleteOne(new Document("_id",project._id)).getAsync(new App.Callback<DeleteResult>() {
            @Override
            public void onResult(App.Result<DeleteResult> result) {
                projects.remove(project);
            }
        });
        for(Task task:project.tasks){
            deleteTask(task);
        }
    }

    public static void readMyProjects(){
        MongoCollection<Document> col = getCol("projects");
        col.find(new Document("createdBy", user_id)).iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
            @Override
            public void onResult(App.Result<MongoCursor<Document>> result) {
                projects.clear();
                while(result.get().hasNext()){
                    Document doc = result.get().next();
                    Project project = new Project(doc);
                    projects.add(project);
                }

                MongoCollection<Document> col = getCol("tasks");
                for(Project p:projects){
                    col.find(new Document("projectId", p._id)).iterator().getAsync(new App.Callback<MongoCursor<Document>>() {
                        @Override
                        public void onResult(App.Result<MongoCursor<Document>> result) {
                            while(result.get().hasNext()){
                                Document doc = result.get().next();
                                p.tasks.add(new Task(doc));
                            }
                        }
                    });
                }
            }
        });
    }

    public static MongoCollection<Document> getCol(String collection){
        MongoCollection<Document> col = user.getMongoClient("OfficeEaseCluster").getDatabase("test").getCollection(collection);
        return col;
    }
   public static void registerAccount(String user, String pwd, ArrayList<String> mySkills, boolean admin){

       Document doc = new Document();
       doc.append("username", user);
       doc.append("password", pwd);
       if(admin) doc.append("skills", new ArrayList<String>());
       else doc.append("skills", mySkills);
       doc.append("admin", admin);
       MongoCollection<Document> col = getCol("users");
       col.insertOne(doc).getAsync(new App.Callback<InsertOneResult>() {
           @Override
           public void onResult(App.Result<InsertOneResult> result) {

           }
       });
   }


   public static void readSkills(){
       MongoCollection<Document> col = getCol("skills");
       col.findOne(new Document("_id", new ObjectId("67d21581d06cca43ecd006cc"))).getAsync(new App.Callback<Document>() {
           @Override
           public void onResult(App.Result<Document> result) {
               skills = (ArrayList<String>) result.get().get("skills");
               skills.add(0, "Select a skill");
           }
       });
   }


}
