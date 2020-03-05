package com.sami.oninecabsystem.FragmentClasses;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sami.oninecabsystem.MapsPassengerActivity;
import com.sami.oninecabsystem.R;

public class MapsPassengerFragment extends Fragment {
    private Button openMap;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map,container,false);
        openMap=view.findViewById(R.id.btnOpenMap);
        openMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MapsPassengerActivity.class));
            }
        });
        return  view;
    }
}
