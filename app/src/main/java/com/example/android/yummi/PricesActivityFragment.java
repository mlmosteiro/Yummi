package com.example.android.yummi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;


public class PricesActivityFragment extends Fragment {


    public PricesActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_prices, container, false);


        //DUMMY DATA
        String[] menusArray = {
                "Menu 1",
                "Menú 2",
                "Menú 3",
                "Menú 4"
        };
        ArrayList<String> menus = new ArrayList<>(Arrays.asList(menusArray));


        //En su momento, cambiar por el CursorAdapter
        ArrayAdapter<String> cursor = new ArrayAdapter<String>(
                getActivity(),
                R.layout.fragment_prices, menus);

//        initTable(rootView, cursor);

        return rootView;
    }

}

