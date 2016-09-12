package com.example.android.yummi;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.yummi.services.ComedoresService;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.Locale;
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

    private static final String LOG_TAG = AdapterComedores.class.getSimpleName();
    public static final String MINIATURAS_PATH = "miniaturas";
    public static final String MINIATURAS_PATTERN = "mini_%d.png";

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

        view.setTag(new ViewHolder(view));

        return view;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder vH = (ViewHolder) view.getTag();

        long ini = cursor.getLong(MainActivityFragment.COL_HORA_INI);
        long fin = cursor.getLong(MainActivityFragment.COL_HORA_FIN);
        int diaIni = cursor.getInt(MainActivityFragment.COL_DIA_INI);
        int diaFin = cursor.getInt(MainActivityFragment.COL_DIA_FIN);
        boolean abierto = Utility.horaActualEn(ini, fin) && Utility.diaActualEn(diaIni, diaFin);

        String titulo = cursor.getString(MainActivityFragment.COL_NOMBRE);
        vH.tituloView.setText(titulo);

        long apertura = cursor.getLong(MainActivityFragment.COL_HORA_APERTURA);
        long cierre = cursor.getLong(MainActivityFragment.COL_HORA_CIERRE);
        String textoApertura = context.getString(R.string.formato_horario_apertura,
                Utility.denormalizarHora(apertura), Utility.denormalizarHora(cierre));
        vH.subtituloView.setText(textoApertura);

        Uri uri = Uri.parse(ComedoresService.API_DIR).buildUpon()
                .appendPath(MINIATURAS_PATH)
                .appendPath(String.format(Locale.ENGLISH, MINIATURAS_PATTERN, cursor.getLong(
                        MainActivityFragment.COL_ID)))
                .build();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean soloEnWifi = sharedPref.getBoolean(context.getString(R.string.pref_downloadOnWifi_key), false);
        RequestCreator rq = Picasso.with(context)
                .load(uri);
        if(soloEnWifi && !Utility.conectadoWifi(context))
                rq.networkPolicy(NetworkPolicy.OFFLINE);
        rq.placeholder(R.drawable.icono).into(vH.iconView);

        if (abierto) {
            vH.iconView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        } else {
            vH.iconView.setBackgroundColor(context.getResources().getColor(R.color.plato_agotado));
        }
    }
}
