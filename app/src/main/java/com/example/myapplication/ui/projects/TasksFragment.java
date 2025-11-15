package com.example.myapplication.ui.projects;

import android.os.Bundle;

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

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentTasksBinding;
import com.example.myapplication.ui.server.Employee;
import com.example.myapplication.ui.server.Server;
import com.example.myapplication.ui.server.Task;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TasksFragment extends Fragment {

    private FragmentTasksBinding binding;

    public ArrayList<String> required_skills;
    public ArrayList<Employee> employees_with_skill = new ArrayList<>();
    public ArrayList<String> members_to_invite = new ArrayList<>();
    public ArrayList<CheckBox> checkboxes = new ArrayList<>();


    public TasksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTasksBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        for(Task t:Server.selected_project.tasks){
            addTaskView(t);
        }

        binding.refreshLayoutTask.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Server.readAll();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.refreshLayoutTask.setRefreshing(false);

                        binding.linearLayoutTasks.removeAllViews();
                        for(Task t:Server.selected_project.tasks){
                            addTaskView(t);
                        }
                    }
                }, 2000);
            }
        });


        ArrayAdapter<String> adapter_skills = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, Server.skills);
        binding.spinnerRequiredSkill1.setAdapter(adapter_skills);
        required_skills = new ArrayList<>();
        checkboxes.clear();
        updateFitEmployees();

        binding.spinnerRequiredSkill1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0) {
                    required_skills.add(parent.getItemAtPosition(position).toString());

                    View skill = getLayoutInflater().inflate(R.layout.skill_simple, null, false);

                    ((TextView) skill.findViewById(R.id.textView_skillName)).setText(parent.getItemAtPosition(position).toString());
                    ((ImageView) skill.findViewById(R.id.imageView_skillCancel)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            required_skills.remove(parent.getItemAtPosition(position).toString());
                            binding.linearLayoutSkills.removeView(skill);
                            updateFitEmployees();
                        }
                    });

                    binding.linearLayoutSkills.addView(skill);
                    updateFitEmployees();
                    binding.spinnerRequiredSkill1.setSelection(0);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                members_to_invite.clear();
                for(CheckBox box:checkboxes){
                    if(box.isChecked()) members_to_invite.add(box.getText().toString().split(" • ")[0]);
                }

                Document doc = new Document();
                doc.append("_id", new ObjectId());
                doc.append("name", binding.editTextTaskName.getText().toString());
                doc.append("isCompleted", false);
                doc.append("assignedEmployees", Server.stringtoEmployeeIDs(members_to_invite));
                doc.append("projectId", Server.selected_project._id);
                doc.append("createdAt", Calendar.getInstance().getTime());
                doc.append("completedAt", null);
                doc.append("skillsRequired", new ArrayList<>(required_skills));

                Task task = new Task(doc);
                Server.selected_project.tasks.add(task);
                Server.appendTask(task);
                addTaskView(task);
                
                binding.editTextTaskName.getText().clear();
                binding.editTextTaskName.clearFocus();
                binding.editTextMembersNum.clearFocus();

                binding.spinnerRequiredSkill1.setSelection(0);
                binding.linearLayoutSkills.removeAllViews();
                required_skills.clear();
                updateFitEmployees();
            }
        });

        binding.buttonAutoselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = Integer.valueOf(binding.editTextMembersNum.getText().toString());
                ArrayList<CheckBox> boxes = new ArrayList<>(checkboxes);

                for(CheckBox cb:boxes){
                    cb.setChecked(false);
                }

                ArrayList<CheckBox> candidate = new ArrayList<>();

                CheckBox min_box = null;
                int min_val;
                while(!boxes.isEmpty() && candidate.size() < val) {
                    min_val = 999999;
                    for (int i = 0; i < boxes.size(); i++) {
                        ObjectId memberID = Server.getEmployeeId(boxes.get(i).getText().toString().split(" • ")[0]);
                        if(!Server.employees_assigned.containsKey(memberID)){
                            min_val = 0;
                            min_box = boxes.get(i);
                            break;
                        }
                        else if(Server.employees_assigned.get(memberID) < min_val) {
                            min_val = Server.employees_assigned.get(memberID);
                            min_box = boxes.get(i);
                        }
                    }
                    candidate.add(min_box);
                    boxes.remove(min_box);
                }

                for(CheckBox cb:candidate){
                    cb.setChecked(true);
                }
            }
        });

        return root;
    }

    public void updateFitEmployees(){
        employees_with_skill = Server.filterEmployeesWithSkill(required_skills);
        binding.linearLayoutMembers.removeAllViews();
        checkboxes.clear();
        for(Employee e:employees_with_skill) addMemberView(e);
    }

    public void addMemberView(Employee employee){
        CheckBox checkBox = new CheckBox(getActivity());

        int busy = employee.getAssignedWork();

        if(busy >= 9)  checkBox.setText(String.format("%s • overloaded", employee.username));
        else if(busy >= 6)  checkBox.setText(String.format("%s • busy", employee.username));
        else if(busy >= 3)  checkBox.setText(String.format("%s • moderate", employee.username));
        else checkBox.setText(String.format("%s • free", employee.username));

        checkboxes.add(checkBox);

        binding.linearLayoutMembers.addView(checkBox);
    }

    public void addTaskView(Task task){
        final View task_view = getLayoutInflater().inflate(R.layout.temporary_item, null, false);
        ((TextView) task_view.findViewById(R.id.textView_title)).setText(task.name);

        ArrayList<String> out = new ArrayList<>();
        out.add(String.join(", ", Server.idtoEmployeeString(task.assignedEmployees)));
        out.add(String.join(", ", task.skillsRequired));
        ((TextView) task_view.findViewById(R.id.textView_description)).setText(String.join("\n", out));

        CheckBox checkBox = task_view.findViewById(R.id.checkBox);
        ImageView imageView = task_view.findViewById(R.id.imageView_delete);

        checkBox.setChecked(task.isCompleted);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                task.isCompleted = isChecked;
                if(task.isCompleted) task.completedAt = Calendar.getInstance().getTime();
                else task.completedAt = null;
                Server.updateTask(task);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Server.selected_project.tasks.remove(task);
                Server.deleteTask(task);
                binding.linearLayoutTasks.removeView(task_view);
            }
        });

        binding.linearLayoutTasks.addView(task_view);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}