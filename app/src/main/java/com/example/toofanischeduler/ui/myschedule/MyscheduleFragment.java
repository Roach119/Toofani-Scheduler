package com.example.toofanischeduler.ui.myschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.toofanischeduler.R;
import com.example.toofanischeduler.ViewSchedule;
import com.example.toofanischeduler.databinding.FragmentMyscheduleBinding;

public class MyscheduleFragment extends Fragment {

    ListView listView;
    String WeekDay[] = {"Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"};

    private FragmentMyscheduleBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myschedule, container, false);

        listView= view.findViewById(R.id.listview);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(view.getContext(),
                R.layout.days, R.id.itemTextView, WeekDay);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedDay = WeekDay[i];

                Intent intent=new Intent(view.getContext(), ViewSchedule.class);

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