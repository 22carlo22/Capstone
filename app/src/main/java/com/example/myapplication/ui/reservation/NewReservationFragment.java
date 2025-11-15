package com.example.myapplication.ui.reservation;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import android.widget.CalendarView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentNewReservationBinding;
import com.example.myapplication.ui.server.Employee;
import com.example.myapplication.ui.server.Server;
import com.example.myapplication.ui.projects.Project;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NewReservationFragment extends Fragment {

    private FragmentNewReservationBinding binding;

    public int default_duration = 1;
    public static ArrayList<Room> room_reservations_hotdesk = new ArrayList<>();
    public static ArrayList<Room> room_reservations_meeting = new ArrayList<>();

    public static RoomAdapter adapter_all, adapter_hotdesk, adapter_meeting;;

    public Date selected_date;
    public ArrayList<String> selected_members;

    public Room selected_room;
    public ArrayList<String> project_names;
    public ArrayList<ArrayList<String>> project_members;

    public NewReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Extract the views from this fragment and display it
        binding = FragmentNewReservationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_room);
        TextView dialog_roomname = dialog.findViewById(R.id.dialog_roomname);
        Button dialog_cancel = dialog.findViewById(R.id.cancel_room);
        Button dialog_submit = dialog.findViewById(R.id.submit_room);
        CardView dialog_members = dialog.findViewById(R.id.dialog_attendees);
        Spinner dialog_spinner = dialog.findViewById(R.id.room_spinner);
        CalendarView dialog_calendar = dialog.findViewById(R.id.calendar_room);
        Button dialog_durSub = dialog.findViewById(R.id.duration_sub);
        Button dialog_durAdd = dialog.findViewById(R.id.duration_add);
        TextView dialog_durText = dialog.findViewById(R.id.duration_text);
        EditText dialog_note = dialog.findViewById(R.id.note_editText);
        EditText dialog_textStart = dialog.findViewById(R.id.editText_start);
        EditText dialog_textEnd = dialog.findViewById(R.id.editText_end);
        LinearLayout linearLayout_members = dialog.findViewById(R.id.linearLayout_roomMembers);

        project_members = new ArrayList<>();
        project_names = new ArrayList<>();
        selected_members = new ArrayList<>();

        project_names.add("N/A");
        ArrayList<String> temp = new ArrayList<>();
        for(Employee e:Server.employees){
            temp.add(e.username);
        }
        project_members.add(temp);

        for(Project project:Server.projects){
            project_names.add(project.name);
            project_members.add(project.getMembers());
        }

        ArrayAdapter<String> adapter_projects = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, project_names);
        dialog_spinner.setAdapter(adapter_projects);
        selected_date = Calendar.getInstance().getTime();

        adapter_all = new RoomAdapter(getActivity(), Server.rooms);
        ArrayList<Room> room_hotdesk = new ArrayList<>();
        ArrayList<Room> room_meeting = new ArrayList<>();
        for(Room r:Server.rooms){
            if(r.hotdesk) room_hotdesk.add(r);
            else room_meeting.add(r);
        }
        adapter_hotdesk = new RoomAdapter(getActivity(), room_hotdesk);
        adapter_meeting = new RoomAdapter(getActivity(), room_meeting);

        dialog_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMembersView(project_members.get(position), linearLayout_members);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dialog_durSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_durText.setText((--default_duration)+" hours");
            }
        });

        dialog_durAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_durText.setText((++default_duration)+" hours");
            }
        });

        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                selected_room.updateDate(selected_date);
                ArrayList<Integer> slots = new ArrayList<>(selected_room.slots);
                ArrayList<Integer> caps = new ArrayList<>(selected_room.cap);
                ArrayList<Integer> in_conflict = new ArrayList<>();

                ReservationHelper.getValidArray(slots, caps);

                ArrayList<String> check_members = new ArrayList<>(selected_members);
                check_members.add(Server.username);

                for(String m:check_members){
                    if(Server.other_reservations.containsKey(m)){
                        for(Integer i:Server.other_reservations.get(m).getSlots(selected_date)){
                            in_conflict.add(i);
                        }
                    }
                }

                slots.removeAll(in_conflict);

                int start = Integer.valueOf(dialog_textStart.getText().toString());
                int end = Integer.valueOf(dialog_textEnd.getText().toString());

                ReservationHelper.intervalFilter(slots, start, end);
                ReservationHelper.strictMode(slots, default_duration);

                if(!slots.isEmpty()){
                    Document doc = new Document();
                    doc.append("_id", new ObjectId());
                    doc.append("room", selected_room.name);
                    doc.append("date", selected_date);
                    doc.append("time", String.valueOf(slots.get(0)));
                    doc.append("duration", String.valueOf(default_duration));
                    doc.append("attendees", selected_members);
                    doc.append("description", dialog_note.getText().toString());
                    doc.append("createdBy", Server.user_id);

                    Reservation r = new Reservation(doc);
                    Server.reservations.add(r);
                    Server.addReservation(r);
                    selected_room.decrementCap(slots);
                    Server.updateRoom(selected_room);
                    Toast.makeText(getActivity(), "Reserving "+slots.get(0)+":00 to "+(slots.get(0)+default_duration)%24+":00", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
                else{
                    Toast.makeText(getActivity(), "Unsuccessful!", Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog_calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                selected_date = cal.getTime();
            }
        });


        binding.roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected_room =  (Room) parent.getItemAtPosition(position);

                if(selected_room.hotdesk) dialog_members.setVisibility(View.GONE);
                else dialog_members.setVisibility(View.VISIBLE);

                dialog_roomname.setText("Reserving "+selected_room.name);
                dialog_textStart.setText(String.valueOf(selected_room.open.get(0)));
                dialog_textEnd.setText(String.valueOf(selected_room.open.get(1)));
                dialog.show();

                dialog_spinner.setSelection(0);
                dialog_note.clearFocus();
                dialog_note.getText().clear();

                Server.readOtherReservations();

            }
        });

        binding.reservetypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).toString().equals("All"))
                    binding.roomList.setAdapter(adapter_all);
                else if(parent.getItemAtPosition(position).toString().equals("Hotdesk"))
                    binding.roomList.setAdapter(adapter_hotdesk);
                else
                    binding.roomList.setAdapter(adapter_meeting);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.reserveSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter_all.getFilter().filter(newText.toLowerCase());
                adapter_hotdesk.getFilter().filter(newText.toLowerCase());
                adapter_meeting.getFilter().filter(newText.toLowerCase());
                return false;
            }
        });

        return root;
    }

    public void updateMembersView(ArrayList<String> names, LinearLayout layout){
        layout.removeAllViews();
        selected_members.clear();
        for(String name:names){
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setText(name);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        selected_members.add(name);
                    }
                    else{
                        selected_members.remove(name);
                    }
                }
            });
            layout.addView(checkBox);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}