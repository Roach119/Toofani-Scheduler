package com.example.toofanischeduler.ui.editschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.toofanischeduler.EditMySchedule;
import com.example.toofanischeduler.R;
import com.example.toofanischeduler.ViewSchedule;
import com.example.toofanischeduler.databinding.FragmentEditscheduleBinding;


public class EditscheduleFragment extends Fragment {

    ListView listView;
    String WeekDay[] = {"Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"};

    private FragmentEditscheduleBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_editschedule, container, false);

        listView= view.findViewById(R.id.listview);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(view.getContext(),
                R.layout.days, R.id.itemTextView, WeekDay);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedDay = WeekDay[i];

                //Toast.makeText(getActivity(), "Day: "+selectedDay, Toast.LENGTH_SHORT).show();

                Intent intent=new Intent(view.getContext(), EditMySchedule.class);

                intent.putExtra("selectedDay", selectedDay);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}