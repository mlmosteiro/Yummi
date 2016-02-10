package com.example.android.yummi;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Adapter para las listas de platos de {@code DetailActivityFragment}
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 10/02/2016.
 */
public class AdapterPlatos extends CursorAdapter {
    /**
     * Cache con los View hijos para un elemento de una lista de platos
     */
    public static class ViewHolder {
        public final TextView platoView;
        public final TextView descripcionView;

        public ViewHolder(View view){
            platoView = (TextView) view.findViewById(R.id.plato_textView);
            descripcionView = (TextView) view.findViewById(R.id.descripcionPlato_textView);
        }
    }

    public AdapterPlatos(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_platos, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder vH = (ViewHolder) view.getTag();

        vH.platoView.setText(cursor.getString(DetailActivityFragment.COL_PLATO_NOMBRE));
        vH.descripcionView.setText(cursor.getString(DetailActivityFragment.COL_PLATO_DESCRIPCION));
    }


}
