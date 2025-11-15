package com.example.myapplication.ui.reservation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentMyReservationBinding;
import com.example.myapplication.ui.server.Server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MyReservationFragment extends Fragment {

    private FragmentMyReservationBinding binding;

    public MyReservationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Extract the views from this fragment and display it
        binding = FragmentMyReservationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        for(Reservation r:Server.reservations){
            addReservationView(r);
        }

        binding.refreshLayoutMyReservation.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Server.readAll();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.linearLayoutToday.removeAllViews();
                        binding.linearLayoutUpcoming.removeAllViews();
                        for(Reservation r:Server.reservations){
                            addReservationView(r);
                        }
                        binding.refreshLayoutMyReservation.setRefreshing(false);
                    }
                },2000);
            }
        });

        return root;
    }

    public void addReservationView(Reservation r){
        final View r_view = getLayoutInflater().inflate(R.layout.temporary_item, null, false);
        ((TextView) r_view.findViewById(R.id.textView_title)).setText(r.room);
        ((TextView) r_view.findViewById(R.id.textView_description)).setText(r.getSummary());
        ((CheckBox) r_view.findViewById(R.id.checkBox)).setVisibility(View.GONE);

        ImageView delete = r_view.findViewById(R.id.imageView_delete);
        if(!r.createdBy.equals(Server.user_id)) delete.setVisibility(View.GONE);

        Date date = Calendar.getInstance().getTime();
        final LinearLayout linearLayout;
        if(date.getYear() == r.date.getYear() && date.getMonth() == r.date.getMonth() && date.getDate() == r.date.getDate()){
            linearLayout = binding.linearLayoutToday;
        }
        else linearLayout = binding.linearLayoutUpcoming;

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.removeView(r_view);
                Server.reservations.remove(r);
                Server.deleteReservation(r);
                for(Room room:Server.rooms){
                    if(room.name.equals(r.room)){
                        int start = Integer.valueOf(r.time);
                        int dur = Integer.valueOf(r.duration);
                        ArrayList<Integer> to_release = new ArrayList<Integer>();
                        for(int i = start; i < start+dur; i++) to_release.add(i);
                        room.updateDate(r.date);
                        room.incrementCap(to_release);
                        Server.updateRoom(room);
                        break;
                    }
                }

            }
        });

        linearLayout.addView(r_view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}