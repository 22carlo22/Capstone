package com.example.myapplication.ui.Announce;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentAnnouncementBinding;
import com.example.myapplication.ui.server.Server;

import org.bson.Document;
import org.bson.types.ObjectId;

public class AnnouncementFragment extends Fragment {

    private FragmentAnnouncementBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Extract the views from this fragment and display it
        binding = FragmentAnnouncementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        for(Announcement a:Server.announcements) addAnnouncementView(a);

        binding.refreshLayoutAnnouncement.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Server.readAll();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.announceList.removeAllViews();
                        for(Announcement a:Server.announcements) addAnnouncementView(a);
                        binding.refreshLayoutAnnouncement.setRefreshing(false);
                    }
                },2000);
            }
        });

        binding.buttonAddAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Document doc = new Document();
                doc.append("_id", new ObjectId());
                doc.append("createdBy", Server.user_id);
                doc.append("title", binding.editTextAnnounceTitle.getText().toString());
                doc.append("content", binding.editTextAnnounceDescp.getText().toString());

                Announcement a = new Announcement(doc);
                Server.announcements.add(a);
                Server.appendAnnouncement(a);
                addAnnouncementView(a);

                binding.editTextAnnounceTitle.getText().clear();
                binding.editTextAnnounceDescp.getText().clear();
                binding.editTextAnnounceTitle.clearFocus();
                binding.editTextAnnounceDescp.clearFocus();
            }
        });

        if(!Server.admin) binding.cardViewAddAnnouncement.setVisibility(View.GONE);

        return root;
    }

    public void addAnnouncementView(Announcement a){

        final View a_view = getLayoutInflater().inflate(R.layout.temporary_item, null, false);
        ((TextView) a_view.findViewById(R.id.textView_title)).setText(a.title);
        ((TextView) a_view.findViewById(R.id.textView_description)).setText(a.content);
        ((CheckBox) a_view.findViewById(R.id.checkBox)).setVisibility(View.GONE);

        ImageView delete = a_view.findViewById(R.id.imageView_delete);
        if(!a.createdBy.equals(Server.user_id)) delete.setVisibility(View.GONE);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.announceList.removeView(a_view);
                Server.deleteAnnouncement(a);
            }
        });

        binding.announceList.addView(a_view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}