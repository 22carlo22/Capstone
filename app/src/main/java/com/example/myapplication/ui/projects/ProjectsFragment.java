package com.example.myapplication.ui.projects;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.ui.server.Task;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentProjectsBinding;
import com.example.myapplication.ui.server.Server;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.mongodb.mongo.MongoCollection;

public class ProjectsFragment extends Fragment {

    private FragmentProjectsBinding binding;


    public Date deadline;

    MongoCollection<Document> col;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Extract the views from this fragment
        binding = FragmentProjectsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.refreshLayoutProjects.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Server.readAll();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.refreshLayoutProjects.setRefreshing(false);

                        if(Server.admin){
                            binding.linearLayoutProjectList.removeAllViews();
                            for (Project p : Server.projects) {
                                addProjectView(p);
                            }
                        }
                        else{
                            binding.linearLayoutProjectsList.removeAllViews();
                            for(Project project:Server.projects){
                                addProjectView_Employee(project);
                            }
                        }
                    }
                }, 2000);
            }
        });

        fragmentRun();


        return root;
    }

    public void fragmentRun(){
        if(Server.admin) {
            for (Project p : Server.projects) {
                addProjectView(p);
            }

            binding.buttonAddProject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Document doc = new Document();
                    doc.append("_id", new ObjectId());
                    doc.append("name", binding.editTextProjectName.getText().toString());
                    doc.append("description", binding.editTextProjectDescription.getText().toString());
                    doc.append("deadline", deadline);
                    doc.append("createdBy", Server.user_id);
                    doc.append("createdAt", Calendar.getInstance().getTime());
                    Project project = new Project(doc);
                    Server.appendMyProjects(project);
                    addProjectView(project);
                }
            });

            deadline = Calendar.getInstance().getTime();
            binding.calendarViewProjectDeadline.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    deadline = new Date();
                    deadline.setYear(year - 1900);
                    deadline.setMonth(month);
                    deadline.setDate(dayOfMonth - 1);
                }
            });
        }
        else{
            binding.cardViewAddProject.setVisibility(View.GONE);
            for(Project project:Server.projects){
                addProjectView_Employee(project);
            }
        }
    }

    public void addProjectView_Employee(Project project){
        View project_view = getLayoutInflater().inflate(R.layout.project_item, null, false);
        TextView textView_title_project = project_view.findViewById(R.id.textView_title);
        TextView textView_descp_project = project_view.findViewById(R.id.textView_description);
        TextView textView_daysLeft_project = project_view.findViewById(R.id.textView_daysleft);
        ImageView imageView_down = project_view.findViewById(R.id.imageView_down);
        ImageView imageView_up = project_view.findViewById(R.id.imageView_up);
        CardView cardView_projectItem = project_view.findViewById(R.id.cardView_projectItem);
        LinearLayout linearLayout_tasks = project_view.findViewById(R.id.linearLayout_tasks);
        LinearLayout linearLayout_tasksList = project_view.findViewById(R.id.linearLayout_tasksList);
        linearLayout_tasks.setVisibility(View.GONE);

        textView_title_project.setText(project.name);
        textView_descp_project.setText(project.descp);

        project.setProjectStatus(Server.user_id, textView_daysLeft_project);

        cardView_projectItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imageView_down.getVisibility() == View.VISIBLE){
                    for(Task task:project.getEmployeeTasks(Server.user_id)) {
                        View task_view = getLayoutInflater().inflate(R.layout.temporary_item, null, false);
                        TextView textView_title_task = task_view.findViewById(R.id.textView_title);
                        TextView textView_descp_task = task_view.findViewById(R.id.textView_description);
                        ImageView imageView_delete = task_view.findViewById(R.id.imageView_delete);

                        textView_title_task.setText(task.name);
                        textView_title_task.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                        textView_descp_task.setText(String.join(", ", Server.idtoEmployeeString(task.assignedEmployees)));
                        imageView_delete.setVisibility(View.GONE);

                        CheckBox checkBox = task_view.findViewById(R.id.checkBox);
                        checkBox.setChecked(task.isCompleted);
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                task.isCompleted = isChecked;
                                if(task.isCompleted) task.completedAt = Calendar.getInstance().getTime();
                                else task.completedAt = null;
                                project.setProjectStatus(Server.user_id, textView_daysLeft_project);
                                Server.updateTask(task);
                            }
                        });

                        linearLayout_tasksList.addView(task_view);
                    }

                    linearLayout_tasks.setVisibility(View.VISIBLE);
                    textView_title_project.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    imageView_down.setVisibility(View.GONE);
                    imageView_up.setVisibility(View.VISIBLE);
                }
                else{
                    linearLayout_tasksList.removeAllViews();
                    linearLayout_tasks.setVisibility(View.GONE);
                    textView_title_project.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    imageView_down.setVisibility(View.VISIBLE);
                    imageView_up.setVisibility(View.GONE);
                }
            }
        });

        binding.linearLayoutProjectsList.addView(project_view);

    }

    public void addProjectView(Project project){
        final View project_view = getLayoutInflater().inflate(R.layout.temporary_item, null, false);
        TextView textView_name = project_view.findViewById(R.id.textView_title);
        TextView textView_status = project_view.findViewById(R.id.textView_daysLeftItem);
        TextView textView_descp = project_view.findViewById(R.id.textView_description);
        ImageView imageView_delete = project_view.findViewById(R.id.imageView_delete);
        CheckBox checkBox = project_view.findViewById(R.id.checkBox);

        textView_name.setText(project.name);
        textView_descp.setText("\n"+project.descp);
        project.setProjectStatus(null, textView_status);

        checkBox.setVisibility(View.GONE);
        textView_status.setVisibility(View.VISIBLE);
        textView_name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        binding.linearLayoutProjectList.addView(project_view);
        binding.editTextProjectName.getText().clear();
        binding.editTextProjectDescription.getText().clear();
        binding.editTextProjectName.clearFocus();
        binding.editTextProjectDescription.clearFocus();

        LinearLayout linearLayout_click_project = project_view.findViewById(R.id.linearLayout_texts);
        linearLayout_click_project.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MyBoardActivity.class);
                Server.selected_project = project;
                startActivity(intent);
            }
        });


        imageView_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Server.deleteMyProjects(project);
                binding.linearLayoutProjectList.removeView(project_view);
            }
        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}