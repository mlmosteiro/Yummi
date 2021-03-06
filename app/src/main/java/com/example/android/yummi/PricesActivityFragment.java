package com.example.android.yummi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.yummi.data.ComedoresContract;
import com.example.android.yummi.services.ComedoresService;


public class PricesActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = PricesActivityFragment.class.getSimpleName();

    public static final String PROMO_COMEDOR = "promo";
    public static final String ID_COMEDOR = "ID";

    private static final String ARGS_MENU_ID_KEY = "menuId";

    public static final String[] COLUMNAS_MENU_ELEMENTO = {
            ComedoresContract.TiposMenuEntry.TABLE_NAME + "." + ComedoresContract.TiposMenuEntry._ID,
            ComedoresContract.TiposMenuEntry.TABLE_NAME + "." + ComedoresContract.TiposMenuEntry.COLUMN_NOMBRE,
            ComedoresContract.TiposMenuEntry.COLUMN_PRECIO,
            ComedoresContract.ElementosEntry.COLUMN_TIPO,
            ComedoresContract.ElementosEntry.TABLE_NAME + "." + ComedoresContract.ElementosEntry.COLUMN_NOMBRE
    };
    public static final int COL_MENU_ID = 0;
    public static final int COL_MENU_NOMBRE = 1;
    public static final int COL_MENU_PRECIO = 2;
    public static final int COL_ELEM_TIPO = 3;
    public static final int COL_ELEM_NOMBRE = 4;

    private AdapterPricesActivityFragment mAdapter;

    private long mComedorId = -1;
    private String mComedorPromo = "null";

    private static final int LOADER_MENUS = 0;

    // Error de actualizacion de menus
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Snackbar.make(getActivity().findViewById(android.R.id.content),
                    "Información desactualizada, comprueba tu conexion a internet", Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    public PricesActivityFragment() {/*Vacio*/}

    @Override
    public void onResume() {
        super.onResume();
        // Registramos el receptor de la difusión local de sin conexión
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mReceiver, new IntentFilter(ComedoresService.EVENTO_SIN_CONEXION));
    }

    @Override
    public void onPause() {
        super.onPause();
        // Adios al receptor de la difusión local del evento_sin_conexion
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) { // Esta actividad requiere que se le pase un id de comedor y su promo
            mComedorId = arguments.getLong(ID_COMEDOR);
            mComedorPromo = arguments.getString(PROMO_COMEDOR);
            // Comprobamos si se ha actualizado el comedor este mes
            // con una consulta a la base de comedores sobre la ultima actualizacion
            Cursor c = getActivity().getContentResolver().query(
                    ComedoresContract.ComedoresEntry.CONTENT_URI,
                    new String[]{ComedoresContract.ComedoresEntry.COLUMN_LAST_ACT},
                    ComedoresContract.ComedoresEntry._ID + " = ?", new String[]{Long.toString(mComedorId)},
                    null);
            if(c != null){
                if(c.moveToFirst()) {
                    Long lastSync = c.getLong(0);
                    if (System.currentTimeMillis() - lastSync >= Utility.MES_EN_MILLIS) {
                        //Si hace un mes que no se actualiza (32 días más bien), actualizamos
                        Intent lanzarServicio = new Intent(getActivity(), ComedoresService.class);
                        lanzarServicio.putExtra(ComedoresService.KEY_TIPO, ComedoresService.TIPO_CONSULTA_MENUS);
                        lanzarServicio.putExtra(ComedoresService.KEY_ID, mComedorId);
                        getActivity().startService(lanzarServicio);

                        // Los elementos se actualizan tambien
                        Intent serv = new Intent(getActivity(), ComedoresService.class);
                        serv.putExtra(ComedoresService.KEY_TIPO, ComedoresService.TIPO_CONSULTA_ELEMENTOS);
                        serv.putExtra(ComedoresService.KEY_ID, mComedorId);
                        getActivity().startService(serv);
                    }
                }
                c.close();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Iniciamos el Loader que carga los menus
        getLoaderManager().initLoader(LOADER_MENUS, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Se infla el xml y se busca el RecyclerView
        View rootView = inflater.inflate(R.layout.fragment_prices, container, false);
        RecyclerView recyclerView = ((RecyclerView) rootView.findViewById(R.id.menus_view));

        // Layout manager para el RecyclerView
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        // if(getActivity().getRequestedOrientation() == )

        // Adapter para el RecyclerView
        mAdapter = new AdapterPricesActivityFragment(getActivity(), mComedorPromo);
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                ComedoresContract.TiposMenuEntry.buildTipoMenuByComedorUri(mComedorId),
                COLUMNAS_MENU_ELEMENTO,
                null, null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Cambiamos el cursor del adapter
        mAdapter.cargarCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}





