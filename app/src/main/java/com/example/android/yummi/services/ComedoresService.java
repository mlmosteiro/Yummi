package com.example.android.yummi.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.yummi.R;
import com.example.android.yummi.Utility;
import com.example.android.yummi.data.ComedoresContract;
import com.example.android.yummi.data.ManejadorImagenes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * David Campos Rodríguez, 07/02/2016
 * <p>An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * </p>
 */
public class ComedoresService extends IntentService {

    private static final String LOG_TAG = ComedoresService.class.getSimpleName();

    public ComedoresService() {
        super("ComedoresService");
    }

    public static final String API_DIR = "http://comedoresusc.site88.net";
    /**
     * Consulta básica por los comedores disponibles en la API.
     */
    public static final int TIPO_CONSULTA_COMEDORES = 0;
    /**
     * Valor para el campo 'tipo' para una consulta por los elementos que conforman un tipo de menú dado
     */
    public static final int TIPO_CONSULTA_ELEMENTOS = 4; //4 = nueva forma de obtenerlos
    /**
     * Valor para el campo 'tipo' para una consulta por los menús disponibles en un comedor dado
     */
    public static final int TIPO_CONSULTA_MENUS = 2;
    /**
     * Valor para el campo 'tipo' para una consulta por los platos de un comedor dado
     */
    public static final int TIPO_CONSULTA_PLATOS = 3;

    //Cadenas para identificar el extra en el intent, que es el mismo que el identificador para la api
    public static final String KEY_TIPO = "tipo";
    public static final String KEY_ID = "id";
    public static final String KEY_FECHA = "fecha";

    private static final String OWM_ID = "_id";
    //Campos de comedores
    private static final String OWM_COMEDORES_NOMBRE = "nombre";
    private static final String OWM_COMEDORES_HORA_INI = "horaInicio";
    private static final String OWM_COMEDORES_HORA_FIN = "horaFin";
    private static final String OWM_COMEDORES_COORD_LAT = "coordLat";
    private static final String OWM_COMEDORES_COORD_LON = "coordLon";
    private static final String OWM_COMEDORES_TLF = "telefono";
    private static final String OWM_COMEDORES_CONTACTO = "nombreContacto";
    private static final String OWM_COMEDORES_DIRECCION = "direccion";
    private static final String OWM_COMEDORES_APERTURA = "hAperturaIni";
    private static final String OWM_COMEDORES_CIERRE = "hAperturaFin";
    private static final String OWM_COMEDORES_PROMOCION = "promocion";
    //Campos de elementos
    private static final String OWM_ELEMENTOS_NOMBRE = "nombre";
    private static final String OWM_ELEMENTOS_TIPO = "tipo";
    private static final String OWM_ELEMENTOS_ARRAY = "elementos";
    private static final String OWM_ELEMENTOS_RELACIONES = "relaciones";
    //Campos de menus
    private static final String OWM_MENU_NOMBRE = "nombre";
    private static final String OWM_MENU_PRECIO = "precio";
    //Campos de platos
    private static final String OWM_PLATOS_NOMBRE = "nombre";
    private static final String OWM_PLATOS_DESCRIPCION = "descripcion";
    private static final String OWM_PLATOS_TIPO = "tipo";

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        //Declarados fuera para poder ser cerrados en finally
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String jsonStr;
        try {
            int tipo = intent.getIntExtra(KEY_TIPO, TIPO_CONSULTA_PLATOS);
            long id = intent.getLongExtra(KEY_ID, -1);
            long fecha = intent.getLongExtra(KEY_FECHA, -1);
            Uri uriConexion = Uri.parse(API_DIR).buildUpon()
                    .appendQueryParameter(KEY_TIPO, Integer.toString(tipo))
                    .appendQueryParameter(KEY_ID, Long.toString(id))
                    .appendQueryParameter(KEY_FECHA, Utility.denormalizarFecha(fecha)).build();

            URL url = new URL(uriConexion.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer stringBuffer = new StringBuffer();
            if (inputStream == null) {
                //No podemos hacer nada
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }

            if (stringBuffer.length() == 0) {
                // Se recibió vacío
                return;
            }

            jsonStr = stringBuffer.toString();

            //Hacemos el substring porque usamos un hosting gratuito, que añade código de
            //análisis al final ^^' se podrá eliminar cuando tengamos server propio
            obtenerInformacion(tipo, jsonStr.substring(0, jsonStr.lastIndexOf("<!--FIN-->") + 1), id, fecha);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void obtenerInformacion(int tipo, String jsonStr, long id, long fecha) throws JSONException {
        switch(tipo) {
            case TIPO_CONSULTA_COMEDORES: {
                JSONArray listaJson = new JSONArray(jsonStr);
                guardarComedores(listaJson);
                break;
            }
            case TIPO_CONSULTA_ELEMENTOS: {
                JSONObject objetoJson = new JSONObject(jsonStr);
                guardarElementos(objetoJson, id);
                break;
            }
            case TIPO_CONSULTA_MENUS: {
                JSONArray listaJson = new JSONArray(jsonStr);
                guardarMenus(listaJson, id);
                break;
            }
            case TIPO_CONSULTA_PLATOS: {
                JSONArray listaJson = new JSONArray(jsonStr);
                guardarPlatos(listaJson, id, fecha);
                break;
            }
            default:
                throw new IllegalArgumentException("Tipo de consulta no válido");
        }
    }

    private void guardarComedores(JSONArray jsonArray) throws JSONException {
        ArrayList<ContentValues> cVListInsertar = new ArrayList<>(jsonArray.length());
        ArrayList<ContentValues> cVListActualizar = new ArrayList<>(jsonArray.length());
        ArrayList<String> ids = new ArrayList<>(jsonArray.length());

        //Obtenemos los ids que ya están en la base
        Cursor c = getContentResolver().query(
                ComedoresContract.ComedoresEntry.CONTENT_URI,
                new String[]{ComedoresContract.ComedoresEntry._ID},
                null, null,
                null);
        ArrayList<Long> idsExistentes = null;
        if(c.moveToFirst()) {
            idsExistentes = new ArrayList<>(c.getCount());
            while(!c.isAfterLast()) {
                idsExistentes.add(c.getLong(0));
                c.moveToNext();
            }
        }
        c.close();

        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);

            long nuevoId = jsonObject.getLong(OWM_ID);
            ids.add(Long.toString(nuevoId));

            ContentValues nuevaFila = new ContentValues();
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry._ID,
                    nuevoId);
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_NOMBRE,
                    jsonObject.getString(OWM_COMEDORES_NOMBRE));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_HORA_INI,
                    Utility.normalizarHora(
                            jsonObject.getString(OWM_COMEDORES_HORA_INI)));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_HORA_FIN,
                    Utility.normalizarHora(
                            jsonObject.getString(OWM_COMEDORES_HORA_FIN)));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_COORD_LAT,
                    jsonObject.getDouble(OWM_COMEDORES_COORD_LAT));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_COORD_LONG,
                    jsonObject.getDouble(OWM_COMEDORES_COORD_LON));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_TLFN,
                    jsonObject.getString(OWM_COMEDORES_TLF));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_NOMBRE_CONTACTO,
                    jsonObject.getString(OWM_COMEDORES_CONTACTO));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_DIR,
                    jsonObject.getString(OWM_COMEDORES_DIRECCION));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_INI,
                    Utility.normalizarHora(
                            jsonObject.getString(OWM_COMEDORES_APERTURA)));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_FIN,
                    Utility.normalizarHora(
                            jsonObject.getString(OWM_COMEDORES_CIERRE)));
            nuevaFila.put(
                    ComedoresContract.ComedoresEntry.COLUMN_PROMO,
                    jsonObject.getString(OWM_COMEDORES_PROMOCION));

            if(idsExistentes != null && idsExistentes.contains(nuevoId)) {
                cVListActualizar.add(nuevaFila);
            } else {
                cVListInsertar.add(nuevaFila);
            }
        }
        int eliminados = 0;
        int insertados = 0;
        int actualizados = 0;
        //Eliminamos de la tabla los comedores sobrantes
        if(ids.size() > 0) {
            for(String idAborrar : ids) {
                ManejadorImagenes.borrarImagenesSiExisten(this, idAborrar);
            }
            eliminados = getContentResolver().delete(
                    ComedoresContract.ComedoresEntry.CONTENT_URI,
                    ComedoresContract.ComedoresEntry._ID + " NOT IN (" +
                            TextUtils.join(", ", ids) + ")",
                    null);
        }
        //Insertamos en la tabla de comedores los nuevos
        if(cVListInsertar.size() > 0) {
            ContentValues[] cVArray = new ContentValues[cVListInsertar.size()];
            cVListInsertar.toArray(cVArray);

            insertados = this.getContentResolver().bulkInsert(ComedoresContract.ComedoresEntry.CONTENT_URI, cVArray);
        }
        //Actualizamos los comedores que ya estaban
        if(cVListActualizar.size() > 0) {
            for(ContentValues cv : cVListActualizar) {
                String idComedor = cv.getAsString(ComedoresContract.ComedoresEntry._ID);
                cv.remove(ComedoresContract.ComedoresEntry._ID);
                actualizados += this.getContentResolver().update(
                        ComedoresContract.ComedoresEntry.CONTENT_URI,
                        cv,
                        ComedoresContract.ComedoresEntry._ID + " = ?", new String[]{idComedor});
            }
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(getString(R.string.pref_ultima_act_comedores), System.currentTimeMillis());
        editor.commit();
        Log.d(LOG_TAG, "Service completado: " + insertados + " comedores insertados, "
                + eliminados + " eliminados, " + actualizados + " actualizados.");
    }

    private void guardarElementos(JSONObject jsonObjeto, long id) throws JSONException {
        // Lista para guardar los contenValues de los elementos a añadir
        JSONArray jsonElementos = jsonObjeto.getJSONArray(OWM_ELEMENTOS_ARRAY);

        ArrayList<ContentValues> cVElementos = new ArrayList<>(jsonElementos.length());
        // Recorremos los elementos y los guardamos
        for (int i = 0; i < jsonElementos.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonElementos.get(i);
            ContentValues nuevaFila = new ContentValues();
            long idElemento = jsonObject.getLong(OWM_ID);

            nuevaFila.put(ComedoresContract.ElementosEntry._ID,
                    idElemento);
            nuevaFila.put(ComedoresContract.ElementosEntry.COLUMN_NOMBRE,
                    jsonObject.getString(OWM_ELEMENTOS_NOMBRE));
            nuevaFila.put(ComedoresContract.ElementosEntry.COLUMN_TIPO,
                    jsonObject.getString(OWM_ELEMENTOS_TIPO));
            cVElementos.add(nuevaFila);
        }
        int insertados = 0;
        if (cVElementos.size() > 0) {
            ContentValues[] cVArray = new ContentValues[cVElementos.size()];
            cVElementos.toArray(cVArray);

            //Insertamos en la tabla de elementos
            insertados = this.getContentResolver().bulkInsert(
                    ComedoresContract.ElementosEntry.buildInsercionUri(), cVArray);
        }

        //Asociamos elementos y menús
        JSONObject jsonRelaciones = jsonObjeto.getJSONObject(OWM_ELEMENTOS_RELACIONES);
        JSONArray jsonMenus = jsonRelaciones.names();

        //Suponemos que todos los menus se asocien con todos los elementos, por poner un límite y no
        //redimensionar luego el array.
        ArrayList<ContentValues> cVTienen = new ArrayList<>(jsonMenus.length() * jsonElementos.length());

        // Relaciones es un objeto json que contiene objetos del tipo {idMenu: arrayDeIdsDeElementos}
        for(int menuIndx=0; menuIndx < jsonMenus.length(); menuIndx++) {
            //Obtenemos el array de ids de elementos
            JSONArray jsonMenu = jsonRelaciones.getJSONArray(jsonMenus.getString(menuIndx));

            for(int elementoIdx=0; elementoIdx < jsonMenu.length(); elementoIdx++ ) {
                ContentValues nuevoCV = new ContentValues();
                nuevoCV.put(ComedoresContract.TienenEntry.COLUMN_MENU, jsonMenus.getLong(menuIndx));
                nuevoCV.put(ComedoresContract.TienenEntry.COLUMN_ELEMENTO, jsonMenu.getLong(elementoIdx));

                cVTienen.add(nuevoCV);
            }
        }
        int asociados = 0;
        if (cVTienen.size() > 0) {
            ContentValues[] cVArray = new ContentValues[cVTienen.size()];
            cVTienen.toArray(cVArray);

            //Insertamos en la tabla de asociaciones, con el menu asociado correspondiente
            asociados = this.getContentResolver().bulkInsert(
                    ComedoresContract.TienenEntry.CONTENT_URI, cVArray);
        }
        Log.d(LOG_TAG, "Service completado, " + insertados + " elementos insertados y " + asociados + " asociados.");
    }

    private void guardarMenus(JSONArray jsonArray, long id) throws JSONException {
        ArrayList<ContentValues> cVList = new ArrayList<>(jsonArray.length());
        ArrayList<ContentValues> cVListActualizar = new ArrayList<>(jsonArray.length());
        ArrayList<String> ids = new ArrayList<>(jsonArray.length());

        //Obtenemos los ids que ya están asociados a este comedor
        Cursor c = getContentResolver().query(
                ComedoresContract.TiposMenuEntry.CONTENT_URI,
                new String[]{ComedoresContract.TiposMenuEntry._ID},
                ComedoresContract.TiposMenuEntry.COLUMN_COMEDOR + " = ?", new String[]{Long.toString(id)},
                null);
        ArrayList<Long> idsExistentes = null;
        if(c.moveToFirst()) {
            idsExistentes = new ArrayList<>(c.getCount());
            while(!c.isAfterLast()) {
                idsExistentes.add(c.getLong(0));
                c.moveToNext();
            }
        }
        c.close();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            ContentValues nuevaFila = new ContentValues();
            long idTipoMenu = jsonObject.getLong(OWM_ID);

            ids.add(Long.toString(idTipoMenu));

            nuevaFila.put(ComedoresContract.TiposMenuEntry._ID,
                    idTipoMenu);
            nuevaFila.put(ComedoresContract.TiposMenuEntry.COLUMN_NOMBRE,
                    jsonObject.getString(OWM_MENU_NOMBRE));
            nuevaFila.put(ComedoresContract.TiposMenuEntry.COLUMN_PRECIO,
                    jsonObject.getDouble(OWM_MENU_PRECIO));
            nuevaFila.put(ComedoresContract.TiposMenuEntry.COLUMN_COMEDOR,
                    id);

            if(idsExistentes != null && idsExistentes.contains(idTipoMenu)) {
                cVListActualizar.add(nuevaFila);
            } else {
                cVList.add(nuevaFila);
            }
        }
        int insertados = 0;
        int eliminados = 0;
        int actualizados = 0;
        //Eliminamos de la tabla los menús de este comedor sobrantes
        if(ids.size() > 0) {
            for(String idAborrar : ids) {
                ManejadorImagenes.borrarImagenesSiExisten(this, idAborrar);
            }
            eliminados = getContentResolver().delete(
                    ComedoresContract.TiposMenuEntry.CONTENT_URI,
                    ComedoresContract.TiposMenuEntry.COLUMN_COMEDOR + " = ?" +
                            " AND " +
                    ComedoresContract.TiposMenuEntry._ID + " NOT IN (" +
                            TextUtils.join(", ", ids) + ")",
                    new String[]{Long.toString(id)});
        }
        if(cVList.size() > 0) {
            ContentValues[] cVArray = new ContentValues[cVList.size()];
            cVList.toArray(cVArray);

            //Insertamos en la tabla de menus
            insertados = this.getContentResolver().bulkInsert(
                    ComedoresContract.TiposMenuEntry.CONTENT_URI, cVArray);
        }
        //Actualizamos los menús que ya estaban
        if(cVListActualizar.size() > 0) {
            for(ContentValues cv : cVListActualizar) {
                String idMenu = cv.getAsString(ComedoresContract.TiposMenuEntry._ID);
                cv.remove(ComedoresContract.TiposMenuEntry._ID);
                actualizados += this.getContentResolver().update(
                        ComedoresContract.TiposMenuEntry.CONTENT_URI,
                        cv,
                        ComedoresContract.TiposMenuEntry._ID + " = ?", new String[]{idMenu});
            }
        }
        // Guardamos la fecha de última actualización de los menús de este comedor
        ContentValues values = new ContentValues();
        values.put(ComedoresContract.ComedoresEntry.COLUMN_LAST_ACT, System.currentTimeMillis());
        this.getContentResolver().update(
                ComedoresContract.ComedoresEntry.CONTENT_URI,
                values,
                ComedoresContract.ComedoresEntry._ID + " = ?", new String[]{Long.toString(id)}
        );
        Log.d(LOG_TAG, "Service completado, " + insertados + " menus insertados, " +
                eliminados + " eliminados, " + actualizados + " actualizados.");
    }

    private void guardarPlatos(JSONArray jsonArray, long id, long fecha) throws JSONException {
        ArrayList<ContentValues> cVList = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            ContentValues nuevaFila = new ContentValues();
            nuevaFila.put(ComedoresContract.PlatosEntry._ID,
                    jsonObject.getLong(OWM_ID));
            nuevaFila.put(ComedoresContract.PlatosEntry.COLUMN_NOMBRE,
                    jsonObject.getString(OWM_PLATOS_NOMBRE));
            nuevaFila.put(ComedoresContract.PlatosEntry.COLUMN_DESCRIPCION,
                    jsonObject.getString(OWM_PLATOS_DESCRIPCION));
            nuevaFila.put(ComedoresContract.PlatosEntry.COLUMN_TIPO,
                    jsonObject.getString(OWM_PLATOS_TIPO));
            cVList.add(nuevaFila);
        }
        int insertados = 0;
        int eliminados = 0;
        if(cVList.size() > 0) {
            ContentValues[] cVArray = new ContentValues[cVList.size()];
            cVList.toArray(cVArray);

            //Insertamos en la tabla de platos, con el comedor asociado correspondiente y fecha
            insertados = this.getContentResolver().bulkInsert(
                    ComedoresContract.PlatosEntry.buildInsercionUri(id, fecha), cVArray);

            //Al insertar platos, queremos eliminar todos los anteriores o iguales a 'anteayer'
            eliminados = this.getContentResolver().delete(
                    ComedoresContract.PlatosEntry.CONTENT_URI,
                    ComedoresContract.TenerEntry.COLUMN_FECHA + " <= ?",
                    new String[]{Long.toString(Utility.fechaAnteayer())});
        }
        Log.d(LOG_TAG, "Service completado: " + insertados + " platos insertados/actualizados, " + eliminados + " eliminados.");
    }
}
