package com.example.android.yummi;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter para la lista de comedores de {@code MainActivityFragment}.
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 09/02/2016.
 */
public class AdapterComedores extends CursorAdapter {
    /**
     * Cache con los View hijos para un elemento de la lista de comedores
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView tituloView;
        public final TextView subtituloView;

        public ViewHolder(View view){
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            tituloView = (TextView) view.findViewById(R.id.comedores_titulo);
            subtituloView = (TextView) view.findViewById(R.id.comedores_subtitulo);
        }
    }

    public AdapterComedores(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_comedores, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder vH = (ViewHolder) view.getTag();

        String titulo = cursor.getString(MainActivityFragment.COL_NOMBRE);
        vH.tituloView.setText(titulo);

        long apertura = cursor.getLong(MainActivityFragment.COL_HORA_APERTURA);
        long cierre = cursor.getLong(MainActivityFragment.COL_HORA_CIERRE);
        String textoApertura = context.getString(R.string.formato_horario_apertura,
                Utility.denormalizarHora(apertura), Utility.denormalizarHora(cierre));
        vH.subtituloView.setText(textoApertura);

        //TODO: manejarle ahí lo de si está en el horario de comedor
    }
}
