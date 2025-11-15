package com.example.myapplication.ui.calendar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentCalendarBinding;
import com.example.myapplication.ui.server.Employee;
import com.example.myapplication.ui.server.Server;
import com.example.myapplication.ui.projects.Project;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private boolean appear_buttons = true;


    public ArrayList<String> invited_name;
    public ArrayList<String> project_name;
    public ArrayList<ArrayList<String>> project_members;

    public Date selected_date;

    public ArrayList<Event> selected_events;

    public CalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Extract the views from this fragment and display
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        invited_name = new ArrayList<>();
        project_name = new ArrayList<>();
        project_members = new ArrayList<>();

        project_name.add("N/A");
        ArrayList<String> temp = new ArrayList<>();
        for(Employee e:Server.employees){
            temp.add(e.username);
        }
        project_members.add(temp);

        for(Project project:Server.projects){
            project_name.add(project.name);
            project_members.add(project.getMembers());
        }

        ArrayAdapter<String> adapter_projects = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, project_name);
        binding.spinner.setAdapter(adapter_projects);

        updateMembersView(project_members.get(0));

        binding.textViewDate.setText(convertDateToString(Calendar.getInstance().getTime()));
        binding.calendarView.setOnCalendarDayClickListener(new OnCalendarDayClickListener() {
            @Override
            public void onClick(@NonNull CalendarDay calendarDay) {
                selected_date = calendarDay.getCalendar().getTime();
                selected_events = getEventInDate(selected_date);
                binding.linearLayoutEvents.removeAllViews();


                if(selected_events.isEmpty()) binding.textView11.setVisibility(View.VISIBLE);
                else binding.textView11.setVisibility(View.GONE);
                for(Event e:selected_events) addEventView(e);
                updateCalendarIcon(getDifferentDates());
                binding.textViewDate.setText(convertDateToString(selected_date));
            }
        });

        selected_date = Calendar.getInstance().getTime();
        selected_events = getEventInDate(selected_date);
        updateCalendarIcon(getDifferentDates());

        binding.refreshLayoutCalendar.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Server.readAll();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        selected_events = getEventInDate(selected_date);
                        binding.linearLayoutEvents.removeAllViews();
                        if(selected_events.isEmpty()) binding.textView11.setVisibility(View.VISIBLE);
                        else binding.textView11.setVisibility(View.GONE);
                        for(Event e:selected_events) addEventView(e);
                        updateCalendarIcon(getDifferentDates());
                        binding.refreshLayoutCalendar.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        if(selected_events.isEmpty()) binding.textView11.setVisibility(View.VISIBLE);
        else binding.textView11.setVisibility(View.GONE);
        for(Event e:selected_events) addEventView(e);

        binding.buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Document doc = new Document();
                doc.append("_id", new ObjectId());
                doc.append("name", binding.editTextEventName.getText().toString());

                Calendar cal = Calendar.getInstance();
                cal.setTime(selected_date);
                doc.append("date", selected_date);
                doc.append("formattedDate", String.format("%1$tb %1$td, %1$tY", cal));

                doc.append("time", binding.editTextTime.getText().toString());
                doc.append("attendees", new ArrayList<String>(invited_name));
                doc.append("location", binding.editTextLocation.getText().toString());
                doc.append("description", binding.editTextEventDescription.getText().toString());
                doc.append("createdBy", Server.user_id);

                Event event = new Event(doc);
                Server.events.add(event);
                addEventView(event);
                Server.appendEvent(event);

                updateCalendarIcon(getDifferentDates());
                binding.editTextEventName.getText().clear();
                binding.editTextEventName.clearFocus();
                binding.editTextTime.getText().clear();
                binding.editTextTime.clearFocus();
                binding.editTextLocation.getText().clear();
                binding.editTextLocation.clearFocus();
                binding.editTextEventDescription.getText().clear();
                binding.editTextEventDescription.clearFocus();
                binding.spinner.setSelection(0);
            }
        });

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMembersView(project_members.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(!Server.admin) binding.cardViewAddEvent.setVisibility(View.GONE);

        return root;
    }

    public void addEventView(Event event){
        final View event_view = getLayoutInflater().inflate(R.layout.temporary_item, null, false);
        CheckBox checkBox = event_view.findViewById(R.id.checkBox);
        checkBox.setVisibility(View.GONE);

        TextView title = event_view.findViewById(R.id.textView_title);
        TextView descp = event_view.findViewById(R.id.textView_description);
        title.setText(event.name);
        descp.setText(event.getSummary());

        ImageView delete = event_view.findViewById(R.id.imageView_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.linearLayoutEvents.removeView(event_view);
                Server.events.remove(event);
                Server.deleteEvent(event);
                updateCalendarIcon(getDifferentDates());
            }
        });

        if(!Server.admin) delete.setVisibility(View.GONE);
        binding.linearLayoutEvents.addView(event_view);

    }

    public void updateMembersView(ArrayList<String> names){
        binding.linearLayoutInvited.removeAllViews();
        invited_name.clear();
        for(String name:names){
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setText(name);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        invited_name.add(name);
                    }
                    else{
                        invited_name.remove(name);
                    }
                }
            });
            binding.linearLayoutInvited.addView(checkBox);
        }
    }


    public ArrayList<Event> getEventInDate(Date d){
        ArrayList<Event> my_events = new ArrayList<>();
        for(Event e:Server.events){
            if(d.getYear() == e.date.getYear() && d.getMonth() == e.date.getMonth() && d.getDate() == e.date.getDate()){
                my_events.add(e);
            }
        }
        return my_events;
    }

    public ArrayList<Date> getDifferentDates(){
        ArrayList<Date> my_dates = new ArrayList<>();
        boolean unique;
        for(Event e:Server.events){
            unique = true;
            for(Date d:my_dates){
                if(d.getYear() == e.date.getYear() && d.getMonth() == e.date.getMonth() && d.getDate() == e.date.getDate()){
                    unique = false;
                    break;
                }
            }
            if(unique) my_dates.add(e.date);
        }
        return my_dates;
    }

    public void updateCalendarIcon(ArrayList<Date> dates){
        ArrayList<CalendarDay> calendars = new ArrayList<>();
        Calendar calendar;
        CalendarDay calendarDay;

        boolean has_set = false;
        for(Date d:dates){
            calendar = Calendar.getInstance();
            calendar.set(d.getYear()+1900, d.getMonth(), d.getDate());
            calendarDay = new CalendarDay(calendar);
            calendarDay.setImageDrawable(new CircleDrawable(getEventInDate(d).size()));


            if(!has_set && selected_date.getYear() == d.getYear() && selected_date.getMonth() == d.getMonth() && selected_date.getDate() == d.getDate()){
                calendarDay.setBackgroundResource(R.color.purple_500_light);
                has_set = true;
            }

            calendars.add(calendarDay);

        }

        if(!has_set) {
            calendar = Calendar.getInstance();
            calendar.set(selected_date.getYear() + 1900, selected_date.getMonth(), selected_date.getDate());
            calendarDay = new CalendarDay(calendar);
            calendarDay.setBackgroundResource(R.color.purple_500_light);
            calendars.add(calendarDay);
        }

        binding.calendarView.setCalendarDays(calendars);

    }

    public static String convertDateToString(Date date)
    {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date_str = simpleDateFormat.format(date);
        return date_str;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}