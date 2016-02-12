package com.example.android.yummi.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.example.android.yummi.services.ImagenesService;

import java.io.File;

/**
 * Clase que maneja las imágenes que descarga la aplicación de los comedores
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 11/02/2016.
 */
public class ManejadorImagenes {
    public static final Uri URI_BASE_DECARGA = Uri.parse("http://comedoresusc.site88.net/imgComedores");
    public static final String DESCARGA_PARAMETRO_ID = "id";
    public static final String EXTENSION_IMAGEN = "png";
    public static final String BASE_NOMBRE_DETAIL = "detail_";

    private Context mContext;
    private View view;
    private long idComedor;
    private File imagen;
    private String nombreImagen;

    public static Uri getUrlImagenDetailById(Long id) {
        return URI_BASE_DECARGA.buildUpon().appendQueryParameter(DESCARGA_PARAMETRO_ID, Long.toString(id)).build();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getLongExtra(ImagenesService.EXTRA_ID_KEY, -1) == idComedor) {
                asignarImagen();
            }
        }
    };

    public ManejadorImagenes(Context context, View view, long idComedor) {
        this.mContext = context;
        this.view = view;
        this.idComedor = idComedor;
        nombreImagen = BASE_NOMBRE_DETAIL + Long.toString(idComedor) + "." + EXTENSION_IMAGEN;
        imagen = new File(mContext.getFilesDir(), nombreImagen);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                mReceiver, new IntentFilter(ImagenesService.EVENTO_IMAGEN_DISPONIBLE));
    }

    public void shutdown() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }

    public void conseguirImagen() {
        if( imagen.exists() ) {
            asignarImagen();
        } else {
            Intent intent = new Intent(mContext, ImagenesService.class);
            intent.putExtra(ImagenesService.DIRECCION_KEY, getUrlImagenDetailById(idComedor));
            intent.putExtra(ImagenesService.NOMBRE_ARCHIVO_KEY, nombreImagen);
            intent.putExtra(ImagenesService.EXTRA_ID_KEY, idComedor);
            mContext.startService(intent);
        }
    }

    private void asignarImagen(){
        if(imagen.exists()) {
            Drawable nuevoDraw = Drawable.createFromPath(imagen.toString());
            view.setBackground(nuevoDraw);
        }
    }
}
