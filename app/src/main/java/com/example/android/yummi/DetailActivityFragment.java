package com.example.android.yummi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.yummi.data.ComedoresContract;
import com.example.android.yummi.services.ComedoresService;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, AdapterDetailActivityFragment.AbridorLocalizacion {
    private final String LOG_TAG = DetailActivity.class.getSimpleName();

    // TODO: Hacer que esta actividad permita mostrar los platos del día anterior!

    // Columnas para la consulta de datos del comedor al provider
    public static final String[] COLUMNAS_COMEDOR = {
            ComedoresContract.ComedoresEntry._ID,
            ComedoresContract.ComedoresEntry.COLUMN_NOMBRE,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_INI,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_FIN,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_INI,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_FIN,
            ComedoresContract.ComedoresEntry.COLUMN_TLFN,
            ComedoresContract.ComedoresEntry.COLUMN_NOMBRE_CONTACTO,
            ComedoresContract.ComedoresEntry.COLUMN_COORD_LAT,
            ComedoresContract.ComedoresEntry.COLUMN_COORD_LONG,
            ComedoresContract.ComedoresEntry.COLUMN_DIR
    };

    public static final int COL_COMEDOR_ID = 0;
    public static final int COL_COMEDOR_NOMBRE = 1;
    public static final int COL_COMEDOR_HORA_INI = 2;
    public static final int COL_COMEDOR_HORA_FIN = 3;
    public static final int COL_COMEDOR_APERTURA = 4;
    public static final int COL_COMEDOR_CIERRE = 5;
    public static final int COL_COMEDOR_TLFN = 6;
    public static final int COL_COMEDOR_NOMBRE_CONTACTO = 7;
    public static final int COL_COMEDOR_LAT = 8;
    public static final int COL_COMEDOR_LONG = 9;
    public static final int COL_COMEDOR_DIR = 10;

    // Columnas para la consulta de platos al provider
    public static final String[] COLUMNAS_PLATOS = {
            ComedoresContract.PlatosEntry._ID,
            ComedoresContract.PlatosEntry.COLUMN_NOMBRE,
            ComedoresContract.PlatosEntry.COLUMN_DESCRIPCION,
            ComedoresContract.PlatosEntry.COLUMN_TIPO,
            ComedoresContract.PlatosEntry.COLUMN_AGOTADO
    };
    // Constantes con los id's de columnas.
    // Importante: Modificar si se modifica COLUMNAS_PLATOS.
    public static final int COL_PLATO_ID = 0;
    public static final int COL_PLATO_NOMBRE = 1;
    public static final int COL_PLATO_DESCRIPCION = 2;
    public static final int COL_PLATO_TIPO = 3;
    public static final int COL_PLATO_AGOTADO = 4;

    public static final String COMEDOR_ID = "ID";
    public static final String COMEDOR_NOMBRE = "comedor";
    public static final String COMEDOR_TWOPANE = "twopane";
    public static final String COMEDOR_PROMO = "promo";

    private static final int LOADER_PLATOS = 0;
    private static final int LOADER_INFO_COMEDOR = 1;
    private static final int LOADER_TITULO_COMEDOR = 2;

    private AdapterDetailActivityFragment mAdapter;

    private long mComedorId = -1;
    private String mComedorNombre = "null";
    private String mComedorPromo = "promo";
    private boolean mtwoPane;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Snackbar.make(getActivity().findViewById(android.R.id.content),
                    "Platos desactualizados, comprueba tu conexion a internet", Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    public DetailActivityFragment() {
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mComedorId = arguments.getLong(COMEDOR_ID);
            mtwoPane = arguments.getBoolean(COMEDOR_TWOPANE);
            mComedorNombre = arguments.getString(COMEDOR_NOMBRE);
            mComedorPromo = arguments.getString(COMEDOR_PROMO);
        }

        if(mtwoPane) {
            FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), PricesActivity.class);
                            intent.putExtra(PricesActivity.ID_COMEDOR, mComedorId);
                            intent.putExtra(PricesActivity.PROMO_COMEDOR, mComedorPromo);
                            startActivity(intent);
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        //Lanzamos el servicio de actualización de la lista de platos
        Intent lanzarServicio = new Intent(getActivity(), ComedoresService.class);
        lanzarServicio.putExtra(ComedoresService.KEY_TIPO, ComedoresService.TIPO_CONSULTA_PLATOS);
        lanzarServicio.putExtra(ComedoresService.KEY_ID, mComedorId);
        lanzarServicio.putExtra(ComedoresService.KEY_FECHA, Utility.fechaHoy());
        getActivity().startService(lanzarServicio);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mReceiver, new IntentFilter(ComedoresService.EVENTO_SIN_CONEXION));
        
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        RecyclerView recyclerView = ((RecyclerView) rootView.findViewById(R.id.listView_detail));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        mAdapter = new AdapterDetailActivityFragment(getActivity(), mtwoPane, mComedorNombre, this);
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_PLATOS, null, this);
        getLoaderManager().initLoader(LOADER_INFO_COMEDOR, null, this);
        getLoaderManager().initLoader(LOADER_TITULO_COMEDOR, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_INFO_COMEDOR: {
                return new CursorLoader(
                        getActivity(),
                        ComedoresContract.ComedoresEntry.CONTENT_URI,
                        COLUMNAS_COMEDOR,
                        ComedoresContract.ComedoresEntry._ID + " = ? ",
                        new String[]{Long.toString(mComedorId)},
                        null);
            }
            case LOADER_PLATOS: {
                return new CursorLoader(
                        getActivity(),
                        ComedoresContract.PlatosEntry.buildPlatosByComedorUri(mComedorId),
                        COLUMNAS_PLATOS,
                        null, null,
                        ComedoresContract.PlatosEntry.COLUMN_TIPO);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_INFO_COMEDOR:
                mAdapter.setInfoComedor(data);
                break;
            case LOADER_PLATOS:
                mAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void openLocation (double latitud, double longitud, String label){
        Uri ubicacion = Uri.parse("geo:0,0").buildUpon()
                .appendQueryParameter("q", latitud + "," + longitud + "(" + label + ")").build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(ubicacion);

        if ( intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else{
            Log.d(LOG_TAG, "Couldn't call (" + ubicacion.toString() + ") , no receiving apps installed!");
        }
    }
}

