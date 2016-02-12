package com.example.android.yummi.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Servicio que se conecta a una dirección y guarda una imagen con el nombre indicado
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 11/02/2016.
 */
public class ImagenesService extends IntentService {

    public static final String DIRECCION_KEY = "dir";
    public static final String NOMBRE_ARCHIVO_KEY = "nombre_archivo";
    public static final String EVENTO_IMAGEN_DISPONIBLE = "imagen-lista";
    public static final String EXTRA_ID_KEY = "id";
    public static final String ID_KEY = "id";

    public ImagenesService() {super("ImagenesService");}

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String direccion = intent.getParcelableExtra(DIRECCION_KEY).toString();
        String nombreArchivo = intent.getStringExtra(NOMBRE_ARCHIVO_KEY);
        long id = intent.getLongExtra(ID_KEY, -1);
        if(direccion == null || nombreArchivo == null || id == -1) {
            return;
        }

        URL url = null;
        BufferedOutputStream out = null;
        InputStream in = null;

        try {
            url = new URL(direccion);
            in = url.openStream();

            out = new BufferedOutputStream(new FileOutputStream(this.getFilesDir().getPath() + "/" + nombreArchivo));
            int i;
            while ((i = in.read()) != -1) {
                out.write(i);
            }

            out.close();
            in.close();

            Intent finalizado = new Intent(EVENTO_IMAGEN_DISPONIBLE);
            finalizado.putExtra(EXTRA_ID_KEY, id);
            LocalBroadcastManager.getInstance(this).sendBroadcast(finalizado);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
