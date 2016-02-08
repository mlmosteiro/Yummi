package com.example.android.yummi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private ArrayAdapter<String> comedoresAdapter;


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainactivityfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if( id == R.id.action_settings){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            boolean valor = prefs.getBoolean(getString(R.string.pref_showAll_key),false);

            //TODO=> implementar para mostrar los todos los comedores o solo los abiertos

            return true;
        }

        return super.onOptionsItemSelected(item);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        //DUMMY DATA

        String[] comedoresArray = {
                "Comedor de Mates",
                "El de Psico",
                "Solpor ☼",
                "Condesa",
                "Piso Yus",
                "Los demás"
        };
        final ArrayList<String> comedores = new ArrayList<>(Arrays.asList(comedoresArray));


        comedoresAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_comedores,
                R.id.comedores_textView,
                comedores);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_comedores);
        listView.setAdapter(comedoresAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String comedor = comedoresAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, comedor);
                startActivity(intent);
            }
        });

        //TODO: El segundo textView está actualmente como un string en el XML
        return rootView;
    }
}
