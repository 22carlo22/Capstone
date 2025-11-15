package com.example.myapplication.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.ui.login.LoginActivity;
import com.example.myapplication.databinding.FragmentSettingsBinding;
import com.example.myapplication.ui.projects.Project;
import com.example.myapplication.ui.server.Server;
import com.example.myapplication.ui.server.Task;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if(Server.admin){
            binding.cardViewOverview.setVisibility(View.GONE);
            binding.textViewMyAccount.setText(String.format("Username: %s\nRole: Manager\n", Server.username));
        }
        else{
            binding.textViewMyAccount.setText(String.format("Username: %s\nRole: Employee\nSkills: %s", Server.username, String.join(", ", Server.getEmployee(Server.user_id).skills)));

            int w = 0;
            int p = 0;
            for(Project project:Server.projects){
                for(Task task:project.getEmployeeTasks(Server.user_id)){
                    w++;
                    if(!task.isCompleted) p++;
                }
            }

            String status;
            if(w >= 9) status = "Overloaded";
            else if(w >= 6) status = "Busy";
            else if(w >= 3) status = "Moderate";
            else status = "Free";

            binding.textViewOverview.setText(String.format("Status: %s\nPending tasks: %d\nAvg completion time: %s", status, p, Server.getEmployee(Server.user_id).getAvgCompletionTime()));
        }

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
//                Intent intent = new Intent(getActivity(), LoginActivity.class);
//                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}