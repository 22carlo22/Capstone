package com.example.myapplication.ui.employees;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentEmployeesBinding;
import com.example.myapplication.ui.server.Employee;
import com.example.myapplication.ui.server.Server;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class EmployeesFragment extends Fragment {
    private FragmentEmployeesBinding binding;
    public final static int GREEN = Color.rgb(0,200,0);
    public final static int YELLOW = Color.rgb(240, 240, 0);
    public final static int ORANGE = Color.rgb(255,165,0);
    public final static int RED = Color.rgb(200,0,0);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEmployeesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        if(Server.admin) {
            updatemanagerFragment();
            binding.refreshLayoutEmployee.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Server.readAll();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updatemanagerFragment();
                            binding.refreshLayoutEmployee.setRefreshing(false);
                        }
                    }, 2000);
                }
            });
       }
        else{
            binding.refreshLayoutEmployee.setEnabled(false);
            binding.linearLayoutEmployeesFragment.removeAllViews();
            TextView text = new TextView(getActivity());
            text.setText("Please login with an admin account to view this content.");
            binding.linearLayoutEmployeesFragment.addView(text);
        }

        return root;
    }

    public void updatemanagerFragment(){
        ArrayList<Employee> temp = new ArrayList<>(Server.employees);
        ArrayList<Employee> ranking = new ArrayList<>();
        Employee e_min = null;
        while (!temp.isEmpty()) {
            int min = 2147483647;
            for (Employee e : temp) {
                if (e.getAvgCompletionTime_ms() < min) {
                    min = e.getAvgCompletionTime_ms();
                    e_min = e;
                }
            }
            temp.remove(e_min);
            ranking.add(e_min);
        }

        binding.linearLayoutEmployees.removeAllViews();
        for (Employee e : Server.employees) {
            e.rank = ranking.indexOf(e) + 1;
            addEmployeeView(e);
        }

        String out = "\n";
        for (int i = 0; i < ranking.size(); i++) {
            out += String.format("%d) %s • %s\n", i + 1, ranking.get(i).username, ranking.get(i).getAvgCompletionTime());
        }


        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_rank);
        Button buttonView_cancel = dialog.findViewById(R.id.buttonView_cancel);
        TextView textView_rankList = dialog.findViewById(R.id.textView_rankList);
        textView_rankList.setText(out);


        binding.buttonViewRank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                buttonView_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        updatePie();
    }


    public void addEmployeeView(Employee e){
        final View e_view = getLayoutInflater().inflate(R.layout.employee_item, null, false);
        TextView textView_name = ((TextView) e_view.findViewById(R.id.textView_employeeName));
        LinearLayout linearLayout_summary = e_view.findViewById(R.id.linearLayout_employeeSummary);
        CardView cardView = e_view.findViewById(R.id.cardView_employee);
        ImageView imageview_down = e_view.findViewById(R.id.imageView2_down);
        ImageView imageView_up = e_view.findViewById(R.id.imageView2_up);



        textView_name.setText(e.username);
        linearLayout_summary.setVisibility(View.GONE);


        int p = e.getPending();
        int w = e.getAssignedWork();
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageview_down.getVisibility() == View.VISIBLE){
                    textView_name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    linearLayout_summary.setVisibility(View.VISIBLE);
                    imageview_down.setVisibility(View.GONE);
                    imageView_up.setVisibility(View.VISIBLE);

                    for(int i = 0; i < 5; i++){
                        LinearLayout linearLayout_vertical = new LinearLayout(getActivity());
                        linearLayout_vertical.setOrientation(LinearLayout.HORIZONTAL);
                        TextView text1 = new TextView(getActivity());
                        TextView text2 = new TextView(getActivity());

                        switch(i){
                            case 0:
                                text1.setText("Status: ");
                                if(w >= 9) text2.setText("Overloaded");
                                else if(w >= 6) text2.setText("Busy");
                                else if(w >= 3) text2.setText("Moderate");
                                else text2.setText("Free");
                                break;
                            case 2:
                                text1.setText("Skills: ");
                                text2.setText(String.join(", ", e.skills));
                                break;
                            case 3:
                                text1.setText("Pending tasks: ");
                                text2.setText(""+p);
                                break;
                            case 4:
                                text1.setText("Avg completion time: ");
                                text2.setText(e.getAvgCompletionTime());
                                break;
                            case 1:
                                text1.setText("Efficiency rank: ");
                                text2.setText("#"+e.rank);
                                break;
                        }

                        text1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        text1.setTextSize(18);
                        text2.setTextSize(18);
                        linearLayout_vertical.addView(text1);
                        linearLayout_vertical.addView(text2);
                        linearLayout_summary.addView(linearLayout_vertical);
                    }
                }
                else{
                    textView_name.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    linearLayout_summary.setVisibility(View.GONE);
                    imageview_down.setVisibility(View.VISIBLE);
                    imageView_up.setVisibility(View.GONE);

                    linearLayout_summary.removeAllViews();
                }
            }
        });

        if(w >= 9){
            imageview_down.setColorFilter(RED);
            imageView_up.setColorFilter(RED);
        }
        else if(w >= 6){
            imageview_down.setColorFilter(ORANGE);
            imageView_up.setColorFilter(ORANGE);
        }
        else if(w >= 3){
            imageview_down.setColorFilter(YELLOW);
            imageView_up.setColorFilter(YELLOW);
        }
        else{
            imageview_down.setColorFilter(GREEN);
            imageView_up.setColorFilter(GREEN);
        }

        binding.linearLayoutEmployees.addView(e_view);
    }

    public void updatePie(){

        int free = 0;
        int moderate = 0;
        int busy = 0;
        int overloaded = 0;
        for(Employee e:Server.employees){
                int temp = e.getAssignedWork();
                if(temp >= 9) overloaded++;
                else if(temp >= 6) busy++;
                else if(temp >= 3) moderate++;
                else free++;
        }

        ArrayList<PieEntry> data_p2 = new ArrayList<>();
        data_p2.add(new PieEntry(free, "Free"));
        data_p2.add(new PieEntry(moderate, "Moderate"));
        data_p2.add(new PieEntry(busy, "Busy"));
        data_p2.add(new PieEntry(overloaded, "Overloaded"));


        PieDataSet pieDataSet2 = new PieDataSet(data_p2, "");
        int[] colors = {GREEN, YELLOW, ORANGE, RED};
        pieDataSet2.setColors(colors);

        PieData pieData2 = new PieData(pieDataSet2);
        binding.piechartResource.setData(pieData2);
        binding.piechartResource.getLegend().setEnabled(true);
        binding.piechartResource.getDescription().setEnabled(false);
        binding.piechartResource.setDrawHoleEnabled(false);
        binding.piechartResource.setDrawRoundedSlices(true);
        binding.piechartResource.animateY(500);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}