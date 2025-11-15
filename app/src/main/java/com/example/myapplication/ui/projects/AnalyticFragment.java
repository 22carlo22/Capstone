package com.example.myapplication.ui.projects;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentAnalyticBinding;
import com.example.myapplication.ui.server.Server;
import com.example.myapplication.ui.server.Task;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class AnalyticFragment extends Fragment {

    private FragmentAnalyticBinding binding;

    public AnalyticFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAnalyticBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        binding.textViewProjectCompletion.setText("Due: "+String.format("%tB %<te", Server.selected_project.deadline.getTime()));
        binding.progressDeadline.setProgress((int) (100*Server.selected_project.getDeadlineRatio()), true);

        ArrayList<String> members_name = new ArrayList<>();
        ArrayList<Integer> members_val = new ArrayList<>();
        int progress = 0;

        for(Task task: Server.selected_project.tasks){
            if(task.isCompleted) progress++;
            for(String name:Server.idtoEmployeeString(task.assignedEmployees)){
                if(!members_name.contains(name)){
                    members_name.add(name);
                    members_val.add(1);
                }
                else{
                    int i = members_name.indexOf(name);
                    members_val.set(i, members_val.get(i)+1);
                }
            }
        }


        ArrayList<PieEntry> data_p = new ArrayList<>();
        for(int i = 0; i < members_name.size(); i++){
            data_p.add(new PieEntry(members_val.get(i), members_name.get(i)));
        }
        PieDataSet pieDataSet = new PieDataSet(data_p, "");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        binding.piechart.setData(pieData);
        binding.piechart.getLegend().setEnabled(true);
        binding.piechart.getDescription().setEnabled(false);
        binding.piechart.setDrawHoleEnabled(false);
        binding.piechart.animateY(500);

        ArrayList<PieEntry> data_p2 = new ArrayList<>();
        data_p2.add(new PieEntry(progress, "Completed"));
        data_p2.add(new PieEntry(Server.selected_project.tasks.size()-progress, "Pending"));


        if(Server.selected_project.tasks.size() == 0) progress = 100;
        else progress = (int) 100.0*progress/Server.selected_project.tasks.size();

        PieDataSet pieDataSet2 = new PieDataSet(data_p2, "");
        int[] colors = {getResources().getColor(R.color.purple_500), getResources().getColor(R.color.purple_500_light)};
        pieDataSet2.setColors(colors);

        PieData pieData2 = new PieData(pieDataSet2);
        binding.piechart2.setData(pieData2);
        binding.piechart2.getLegend().setEnabled(true);
        binding.piechart2.getDescription().setEnabled(false);
        binding.piechart2.setCenterText(String.valueOf(progress)+"%");
        binding.piechart2.setCenterTextSize(30);
        binding.piechart2.setDrawRoundedSlices(false);
        binding.piechart2.animateY(500);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}