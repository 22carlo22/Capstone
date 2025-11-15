package com.example.myapplication.ui.reservation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.R;

import java.util.ArrayList;

public class RoomAdapter extends ArrayAdapter<Room> {

    RoomAdapter(Context context, ArrayList<Room> filteredList) {
        super(context, 0, filteredList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //initialize view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.room_item, parent, false);
        }
        //get current object from SomeList class
        Room currentSomeList = getItem(position);


        TextView room = listItemView.findViewById(R.id.roomname_txt);
        TextView descp = listItemView.findViewById(R.id.room_descp);


        room.setText(currentSomeList.name);
        descp.setText(currentSomeList.getSummaryToday());


        return listItemView;
    }
}
