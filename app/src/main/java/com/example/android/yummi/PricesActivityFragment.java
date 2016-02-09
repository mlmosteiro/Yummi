package com.example.android.yummi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

        initTable(rootView,cursor);

        return rootView;
    }

    public void initTable(View rootView, ArrayAdapter<String> cursor){

        TableLayout tabla = (TableLayout) rootView.findViewById(R.id.tablaMenus);
        TableRow.LayoutParams layoutFila = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);

        TableRow.LayoutParams layoutMenu = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);

//        TableRow.LayoutParams layoutPrecio=new TableRow.LayoutParams(
    //           TableRow.LayoutParams.WRAP_CONTENT,
//               TableRow.LayoutParams.WRAP_CONTENT);

        TableRow fila;
        TextView txtMenu;
        // TextView txtPrecio;

        tabla.removeAllViews();

        int control = 0;
        if (!cursor.isEmpty()) {
            do {

                fila = new TableRow(getActivity());
                fila.setLayoutParams(layoutFila);

                txtMenu = new TextView(getActivity());
//                txtPrecio=new TextView(getActivity());

                txtMenu.setText(cursor.getItem(0));
                txtMenu.setGravity(Gravity.RIGHT);
                txtMenu.setPadding(0, 0, 5, 0);
                txtMenu.setLayoutParams(layoutMenu);

//                txtPrecio.setText(cursor.getItem(1));
//                txtPrecio.setPadding(0, 0, 5, 0);
//                txtPrecio.setLayoutParams(layoutPrecio);

                fila.addView(txtMenu);
//                fila.addView(txtPrecio);

                tabla.addView(fila);
                control++;
            } while (control < cursor.getCount());
        }
    }

}

