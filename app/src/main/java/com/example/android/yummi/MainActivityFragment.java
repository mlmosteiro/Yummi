package com.example.android.yummi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.yummi.data.ComedoresContract;
import com.example.android.yummi.service.ComedoresService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private AdapterComedores comedoresAdapter;

    /**
     * Columnas que serán consultadas para rellenar el ListView de este fragment
     */
    public static final String[] COLUMNAS_COMEDORES = {
            ComedoresContract.ComedoresEntry._ID,
            ComedoresContract.ComedoresEntry.COLUMN_NOMBRE,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_INI,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_FIN,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_INI,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_FIN
    };
    // Se nos asegura que serán devueltas en el orden indicado, por tanto estas constantes nos
    // ayudan a ganar algo de eficiencia en tiempo de ejecución
    // ACTUALIZAR SI SE MODIFICAN LAS COLUMNAS
    public static final int COL_ID = 0;
    public static final int COL_NOMBRE = 1;
    public static final int COL_HORA_APERTURA = 2;
    public static final int COL_HORA_CIERRE = 3;
    public static final int COL_HORA_INI = 4;
    public static final int COL_HORA_FIN = 5;

    private static final int LOADER_ID = 0;

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
        if( id == R.id.action_updateComedores) {
            Intent lanzarServicio = new Intent(getActivity(), ComedoresService.class);
            lanzarServicio.putExtra(ComedoresService.KEY_TIPO, ComedoresService.TIPO_CONSULTA_COMEDORES);
            getActivity().startService(lanzarServicio);
        } else if( id == R.id.action_settings){
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

        comedoresAdapter = new AdapterComedores(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_comedores);
        listView.setAdapter(comedoresAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                startActivity(intent);
            }
        });


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = ComedoresContract.ComedoresEntry.COLUMN_NOMBRE + " ASC";
        return new CursorLoader(getActivity(),
                ComedoresContract.ComedoresEntry.CONTENT_URI,
                COLUMNAS_COMEDORES,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        comedoresAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {comedoresAdapter.swapCursor(null);}
}
