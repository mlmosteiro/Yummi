package com.example.android.yummi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.yummi.data.ComedoresContract;
import com.example.android.yummi.services.ComedoresService;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private AdapterComedores comedoresAdapter;

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            View view = getView();
            // Si falló la precarga de platos no importa, aquí mostramos si falló la de comedores
            if (intent.getIntExtra(
                    ComedoresService.KEY_TIPO,
                    ComedoresService.TIPO_CONSULTA_COMEDORES) ==
                    ComedoresService.TIPO_CONSULTA_COMEDORES) {
                if ( view != null ) {
                    mFalloCarga = true;
                    TextView emptyTitle = (TextView) view.findViewById(R.id.empty_label);
                    TextView emptySubtitle = (TextView) view.findViewById(R.id.empty_subtitle);
                    ProgressBar emptyPgb = (ProgressBar) view.findViewById(R.id.empty_pgb);
                    emptyTitle.setText(R.string.load_not_possible);
                    emptySubtitle.setText(R.string.check_connection);
                    emptyPgb.setVisibility(View.GONE);
                    if(mSnackbar == null) {
                        mSnackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                            R.string.comedores_desactualizados, Snackbar.LENGTH_INDEFINITE);
                        mSnackbar.setAction(R.string.reintentar, snackReintentarClick)
                                .setActionTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                        mSnackbar.show();
                    }
                }
            }
        }
    };

    private View.OnClickListener snackReintentarClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSnackbar.dismiss();
            mSnackbar = null;
            actualizaciones();
        }
    };

    /**
     * Columnas que serán consultadas para rellenar el ListView de este fragment
     */
    public static final String[] COLUMNAS_COMEDORES = {
            ComedoresContract.ComedoresEntry._ID,
            ComedoresContract.ComedoresEntry.COLUMN_NOMBRE,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_INI,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_FIN,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_INI,
            ComedoresContract.ComedoresEntry.COLUMN_HORA_FIN,
            ComedoresContract.ComedoresEntry.COLUMN_PROMO,
            ComedoresContract.ComedoresEntry.COLUMN_VECES_CONSULTADO,
            ComedoresContract.ComedoresEntry.COLUMN_DIA_INI_AP,
            ComedoresContract.ComedoresEntry.COLUMN_DIA_FIN_AP
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
    public static final int COL_PROMO = 6;
    public static final int COL_VECES = 7;
    public static final int COL_DIA_INI = 8;
    public static final int COL_DIA_FIN = 9;

    private static final int LOADER_ID = 0;

    private boolean mPrimerLoad = true;
    private boolean mFalloCarga = false;
    private Snackbar mSnackbar = null;

    public MainActivityFragment() {
    }

    private void actualizaciones() {
        //Comprobamos si ha actualizado los comedores este mes
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String lastAct = getActivity().getString(R.string.pref_ultima_act_comedores);
        long lastSync = prefs.getLong(lastAct, 0);
        if( System.currentTimeMillis() - lastSync >= Utility.MES_EN_MILLIS) {
            //Si hace un mes que no se actualiza (32 días más bien), actualizamos

            mFalloCarga = false;
            View view = getView();
            if ( view != null ) {
                TextView emptyTitle = (TextView) view.findViewById(R.id.empty_label);
                TextView emptySubtitle = (TextView) view.findViewById(R.id.empty_subtitle);
                ProgressBar emptyPgb = (ProgressBar) view.findViewById(R.id.empty_pgb);
                emptyTitle.setText(R.string.cargando_comedores_label);
                emptySubtitle.setText(R.string.cargando_comedores_subtitle);
                emptyPgb.setVisibility(View.VISIBLE);
            }

            Intent lanzarServicio = new Intent(getActivity(), ComedoresService.class);
            lanzarServicio.putExtra(ComedoresService.KEY_TIPO, ComedoresService.TIPO_CONSULTA_COMEDORES);
            getActivity().startService(lanzarServicio);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        actualizaciones();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainactivityfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
//        if( id == R.id.action_updateComedores) {
//            Intent lanzarServicio = new Intent(getActivity(), ComedoresService.class);
//            lanzarServicio.putExtra(ComedoresService.KEY_TIPO, ComedoresService.TIPO_CONSULTA_COMEDORES);
//            getActivity().startService(lanzarServicio);
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mReceiver, new IntentFilter(ComedoresService.EVENTO_SIN_CONEXION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        comedoresAdapter = new AdapterComedores(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_comedores);
        listView.setAdapter(comedoresAdapter);

        View empty = rootView.findViewById(android.R.id.empty);
        listView.setEmptyView(empty);

        if(mFalloCarga) {
            TextView emptyTitle = (TextView) rootView.findViewById(R.id.empty_label);
            TextView emptySubtitle = (TextView) rootView.findViewById(R.id.empty_subtitle);
            ProgressBar emptyPgb = (ProgressBar) rootView.findViewById(R.id.empty_pgb);
            emptyTitle.setText(R.string.load_not_possible);
            emptySubtitle.setText(R.string.check_connection);
            emptyPgb.setVisibility(View.GONE);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor c = (Cursor) adapterView.getItemAtPosition(position);
                if (c != null) {
                    long comedorId = c.getLong(COL_ID);
                    String comedorName = c.getString(COL_NOMBRE);
                    String comedorPromo = c.getString(COL_PROMO);
                    ((Callback) getActivity())
                            .comedorSeleccionado(comedorId, comedorName, comedorPromo);
                } else {
                    ((Callback) getActivity()).ningunComedorSeleccionado();
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
        if( mPrimerLoad ) {
            prepararMasVisitado(data);
        }
        mPrimerLoad = false;
    }

    private void prepararMasVisitado(Cursor data) {
        if(data.moveToFirst()) {
            int max=-1;
            long idMaximo=1;
            int filasConMax=0;
            while( !data.isAfterLast() ){
                int veces = data.getInt(COL_VECES);
                if( veces > max ) {
                    max = veces;
                    idMaximo = data.getLong(COL_ID);
                    filasConMax = 1;
                } else if( veces == max )
                    filasConMax++;
                data.moveToNext();
            }
            if(filasConMax == 1) {
                Log.v(LOG_TAG, "Predescargando comedor " + idMaximo + " ( " + max + " veces clickado recientemente) ");
                Intent lanzarServicio = new Intent(getActivity(), ComedoresService.class);
                lanzarServicio.putExtra(ComedoresService.KEY_TIPO, ComedoresService.TIPO_CONSULTA_PLATOS);
                lanzarServicio.putExtra(ComedoresService.KEY_ID, idMaximo);
                lanzarServicio.putExtra(ComedoresService.KEY_FECHA, Utility.fechaHoy());
                getActivity().startService(lanzarServicio);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {comedoresAdapter.swapCursor(null);}


    public interface Callback {
        void comedorSeleccionado(long comedorId, String comedorName, String comedorPromo);
        void ningunComedorSeleccionado();
    }

}
