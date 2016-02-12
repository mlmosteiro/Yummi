package com.example.android.yummi;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class NotSelectedFragment extends Fragment {
    public static final String NOTSELECTED_TAG = "NSTAG";

    public NotSelectedFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_not_selected, container, false);
    }

}
