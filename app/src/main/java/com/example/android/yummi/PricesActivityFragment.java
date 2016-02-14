package com.example.android.yummi;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.yummi.data.ComedoresContract;


public class PricesActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private LinearLayout linearLayout;

    public static final String PROMO_COMEDOR = "promo";
    public static final String ID_COMEDOR = "ID";

    public static final String[] COLUMNAS_MENU = {
            ComedoresContract.TiposMenuEntry.TABLE_NAME + "." + ComedoresContract.TiposMenuEntry._ID,
            ComedoresContract.TiposMenuEntry.COLUMN_NOMBRE,
            ComedoresContract.TiposMenuEntry.COLUMN_PRECIO
    };

    public static final int COL_MENU_ID = 0;
    public static final int COL_MENU_NOMBRE = 1;
    public static final int COL_MENU_PRECIO = 2;
    public static final int COL_MENU_COMEDOR = 3;

    public static final String[] COLUMNAS_ELEMENTOS = {
            ComedoresContract.ElementosEntry.COLUMN_TIPO,
            ComedoresContract.ElementosEntry.COLUMN_NOMBRE
    };

    public static final int COL_ELEM_TIPO = 0;
    public static final int COL_ELEM_NOMBRE = 1;

    private  AdapterMenu mAdapter;

    private long mComedorId = -1;
    private long mMenuId= -1;
    private String mComedorPromo = "null";


    private static final int LOADER_COLUMNAS_MENU = 0;
    private static final int LOADER_COLUMNAS_ELEM = 1;

    public PricesActivityFragment() {
    }

    public static class ViewHolderMenuItem {
        public  TextView mViewMenuNombre;
        public  TextView mViewMenuPrecio;
        public  TextView mViewMenuElementos;

        public ViewHolderMenuItem(View view){
            mViewMenuNombre = (TextView) view.findViewById(R.id.menu_name);
            mViewMenuPrecio = (TextView) view.findViewById(R.id.menu_price);
            mViewMenuElementos = (TextView) view.findViewById(R.id.menu_elements);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mComedorId = arguments.getLong(ID_COMEDOR);
            mComedorPromo = arguments.getString(PROMO_COMEDOR);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_COLUMNAS_MENU, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_prices, container, false);

        RecyclerView recyclerView = ((RecyclerView) rootView.findViewById(R.id.menus_view));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        mAdapter = new AdapterMenu(getActivity(), mComedorPromo);
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_COLUMNAS_MENU: {
                return new CursorLoader(
                        getActivity(),
                        ComedoresContract.TiposMenuEntry.buildTipoMenuByComedorUri(mComedorId),
                        COLUMNAS_MENU,
                        null, null,
                        null);
            }
            case LOADER_COLUMNAS_ELEM: {
                return new CursorLoader(
                        getActivity(),
                        ComedoresContract.ElementosEntry.buildElementosByMenuUri(mMenuId),
                        COLUMNAS_ELEMENTOS,
                        null, null,
                        null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_COLUMNAS_MENU:
                mAdapter.swapCursor(data);
                break;

            case LOADER_COLUMNAS_ELEM:

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}





