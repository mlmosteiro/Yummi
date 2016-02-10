package com.example.android.yummi;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment{

    private ArrayAdapter<String> adapter;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();


        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String comedorString = intent.getStringExtra(Intent.EXTRA_TEXT);

//            CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapser);
//
//            collapsingToolbar.setTitle(comedorString);
        }


        String[] primerosArray = {
                "Taco 1",
                "Taco 2",
                "Taco 3",
                "Taco 4",
                "Taco 5",
                "Taco 6",
                "Taco 7",

        };

        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(primerosArray));

        adapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_platos,
                R.id.plato_textView,
                arrayList);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_primeros);
        listView.setAdapter(adapter);

        setListViewHeightBasedOnChildren(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    int variableTontiParaElCommit ;
                    //TODO => Callback y savedInstance
                }

            }
        });

        return rootView;
    }

//    MOVER A UTILITY.JAVA

    public static void setListViewHeightBasedOnChildren(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }




    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        //TODO => Implementar interfaz y callback
    }


}

