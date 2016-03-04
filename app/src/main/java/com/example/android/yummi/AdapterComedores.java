package com.example.android.yummi;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.yummi.data.ManejadorImagenes;

import java.util.Random;

/**
 * Adapter para la lista de comedores de {@code MainActivityFragment}.
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 09/02/2016.
 */
public class AdapterComedores extends CursorAdapter {
    /**
     * Cache con los View hijos para un elemento de la lista de comedores
     */
    final Random r = new Random();

    public void sinConexion(boolean value) {
        mConexion = !value;
        notifyDataSetChanged();
    }

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

    public static class Tag {
        public ViewHolder viewHolder;
        public ManejadorImagenes manejador;
    }

    private Context mContext;
    private boolean mConexion = true;

    public AdapterComedores(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_comedores, parent, false);

        Tag tag = new Tag();
        tag.viewHolder = new ViewHolder(view);
        view.setTag(tag);

        return view;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView != null && ((Tag)convertView.getTag()).manejador != null) {
            ((Tag)convertView.getTag()).manejador.shutdown();
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder vH = ((Tag) view.getTag()).viewHolder;

        long ini = cursor.getLong(MainActivityFragment.COL_HORA_INI);
        long fin = cursor.getLong(MainActivityFragment.COL_HORA_FIN);
        boolean abierto = Utility.horaActualEn(ini, fin);

        String titulo = cursor.getString(MainActivityFragment.COL_NOMBRE);
        vH.tituloView.setText(titulo);

        long apertura = cursor.getLong(MainActivityFragment.COL_HORA_APERTURA);
        long cierre = cursor.getLong(MainActivityFragment.COL_HORA_CIERRE);
        String textoApertura = context.getString(R.string.formato_horario_apertura,
                Utility.denormalizarHora(apertura), Utility.denormalizarHora(cierre));
        vH.subtituloView.setText(textoApertura);

        ShapeDrawable sD = new ShapeDrawable(new OvalShape());

        int red = (int)Math.round(r.nextFloat()*155)+100;
        int gre = (int)Math.round(r.nextFloat()*155)+100;
        int blu = (int)Math.round(r.nextFloat()*155)+100;

        LinearGradient lg = new LinearGradient(
                0, 0,
                100, 100,
                Color.rgb(red,gre, blu),
                Color.rgb(red-100, gre-100, blu-100),
                Shader.TileMode.CLAMP);
        sD.getPaint().setDither(true);
        sD.getPaint().setShader(lg);
        sD.setIntrinsicHeight(100);
        sD.setIntrinsicWidth(100);
        vH.iconView.setImageDrawable(sD);
        ManejadorImagenes miManejador = new ManejadorImagenes(
                context, vH.iconView, cursor.getLong(MainActivityFragment.COL_ID), true);
        miManejador.conseguirImagen();
        ((Tag) view.getTag()).manejador = miManejador;

        if (abierto) {
            vH.iconView.setBackgroundColor(Color.rgb(200, 240, 160));
        } else {
            vH.iconView.setBackgroundColor(Color.rgb(240, 160, 160));
        }
    }
}
