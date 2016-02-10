package com.example.android.yummi;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.yummi.data.ComedoresContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
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
            ComedoresContract.PlatosEntry.COLUMN_DESCRIPCION
    };
    // Constantes con los id's de columnas.
    // Importante: Modificar si se modifica COLUMNAS_PLATOS.
    public static final int COL_PLATO_ID = 0;
    public static final int COL_PLATO_NOMBRE = 1;
    public static final int COL_PLATO_DESCRIPCION = 2;

    public static final String COMEDOR_ID = "ID";

    private static final int LOADER_PRIMEROS = 0;
    private static final int LOADER_SEGUNDOS = 1;
    private static final int LOADER_POSTRES = 2;
    private static final int LOADER_INFO_COMEDOR = 3;

    private AdapterPlatos mAdapterPrimeros;
    private AdapterPlatos mAdapterSegundos;
    private AdapterPlatos mAdapterPostres;

    private long mComedorId=-1;

    private TextView mViewHoraApertura;
    private TextView mViewHoraComida;
    private TextView mViewContacto;
    private TextView mViewUbicacion;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mComedorId = arguments.getLong(COMEDOR_ID);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mViewHoraApertura = (TextView) rootView.findViewById(R.id.hora_apertura_textview);
        mViewHoraComida = (TextView) rootView.findViewById(R.id.hora_comidas_textview);
        mViewContacto = (TextView) rootView.findViewById(R.id.contacto_textview);
        mViewUbicacion = (TextView) rootView.findViewById(R.id.ubicacion_link);

        mAdapterPrimeros = new AdapterPlatos(getActivity(), null, 0);
        mAdapterSegundos = new AdapterPlatos(getActivity(), null, 0);
        mAdapterPostres = new AdapterPlatos(getActivity(), null, 0);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_primeros);
        listView.setAdapter(mAdapterPrimeros);
        listView = (ListView) rootView.findViewById(R.id.listview_segundos);
        listView.setAdapter(mAdapterSegundos);
        listView = (ListView) rootView.findViewById(R.id.listview_postres);
        listView.setAdapter(mAdapterPostres);

        setListViewHeightBasedOnChildren(listView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_PRIMEROS, null, this);
        getLoaderManager().initLoader(LOADER_SEGUNDOS, null, this);
        getLoaderManager().initLoader(LOADER_POSTRES, null, this);
        getLoaderManager().initLoader(LOADER_INFO_COMEDOR, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case LOADER_INFO_COMEDOR: {
                return new CursorLoader(
                        getActivity(),
                        ComedoresContract.ComedoresEntry.CONTENT_URI,
                        COLUMNAS_COMEDOR,
                        ComedoresContract.ComedoresEntry._ID + " = ? ",
                        new String[]{Long.toString(mComedorId)},
                        null);
            }
            case LOADER_PRIMEROS: {
                return new CursorLoader(
                        getActivity(),
                        ComedoresContract.PlatosEntry.buildPlatosByComedorAndPatronTipoUri(
                                id, "0%"),
                        COLUMNAS_PLATOS,
                        null, null,
                        null);
            }
            case LOADER_SEGUNDOS: {
                return new CursorLoader(
                        getActivity(),
                        ComedoresContract.PlatosEntry.buildPlatosByComedorAndPatronTipoUri(
                                id, "1%"),
                        COLUMNAS_PLATOS,
                        null, null,
                        null);
            }
            case LOADER_POSTRES: {
                return new CursorLoader(
                        getActivity(),
                        ComedoresContract.PlatosEntry.buildPlatosByComedorAndPatronTipoUri(
                                id, "2%"),
                        COLUMNAS_PLATOS,
                        null, null,
                        null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_INFO_COMEDOR:
                if(data.moveToFirst()) {
                    long apertura = data.getLong(COL_COMEDOR_APERTURA);
                    long cierre = data.getLong(COL_COMEDOR_CIERRE);
                    mViewHoraApertura.setText(
                            getActivity().getString(R.string.formato_horario_apertura,
                                    Utility.denormalizarHora(apertura),
                                    Utility.denormalizarHora(cierre)));
                    long ini = data.getLong(COL_COMEDOR_HORA_INI);
                    long fin = data.getLong(COL_COMEDOR_HORA_FIN);
                    mViewHoraComida.setText(
                            getActivity().getString(R.string.formato_horario_apertura,
                                    Utility.denormalizarHora(ini),
                                    Utility.denormalizarHora(fin)));
                    String contacto = data.getString(COL_COMEDOR_NOMBRE_CONTACTO);
                    mViewContacto.setText(
                            data.getString(COL_COMEDOR_TLFN) + (
                                    !contacto.equals("null") ? " (" + contacto + ")" : ""));
                    mViewUbicacion.setText(
                            data.getString(COL_COMEDOR_DIR));
                }
                break;
            case LOADER_PRIMEROS:
                mAdapterPrimeros.swapCursor(data);
                break;
            case LOADER_SEGUNDOS:
                mAdapterSegundos.swapCursor(data);
                break;
            case LOADER_POSTRES:
                mAdapterPostres.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

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

