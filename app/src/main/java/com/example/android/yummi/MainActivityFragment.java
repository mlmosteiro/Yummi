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

    private static final long MES_EN_MILLIS = 32L * 24L * 60L * 60L * 1000L;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);



        //Comprobamos si ha actualizado los comedores este mes
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String lastAct = getActivity().getString(R.string.pref_ultima_act_comedores);
        long lastSync = prefs.getLong(lastAct, 0);
        if( System.currentTimeMillis() - lastSync >= MES_EN_MILLIS) {
            //Si hace un mes que no se actualiza (32 días más bien), actualizamos
            Intent lanzarServicio = new Intent(getActivity(), ComedoresService.class);
            lanzarServicio.putExtra(ComedoresService.KEY_TIPO, ComedoresService.TIPO_CONSULTA_COMEDORES);
            getActivity().startService(lanzarServicio);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(lastAct, System.currentTimeMillis());
            editor.commit();
        }
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
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor c = (Cursor) adapterView.getItemAtPosition(position);

                if(c!=null){
                    long comedorId = c.getLong(COL_ID);
                    String comedorName = c.getString(COL_NOMBRE);
                    ((Callback) getActivity())
                            .comedorSeleccionado(comedorId, comedorName);
                }

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

    public interface Callback {
        void comedorSeleccionado(long comedorId, String comedorName);
    }

}
