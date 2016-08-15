package com.example.android.yummi.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.yummi.R;
import com.example.android.yummi.Utility;
import com.example.android.yummi.data.ComedoresContract;

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
import java.util.List;

/**
 * David Campos Rodríguez, 07/02/2016
 * <p>An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * </p>
 */
public class ComedoresService extends IntentService {
    private static final String LOG_TAG = ComedoresService.class.getSimpleName();

    /**
     * Evento lazado cuando no se dispone de conexión, para que se muestre la snackbar
     */
    public static final String EVENTO_SIN_CONEXION = "sin-conexion";

    /**
     * Dirección web de la API
     */
    public static final String API_DIR = "http://comedoresusc.site88.net/api/";

    /**
     * Consulta básica por los comedores disponibles en la API.
     */
    public static final int TIPO_CONSULTA_COMEDORES = 0;
    /**
     * Valor para el campo 'tipo' para una consulta por los elementos que conforman los menús del comedor
     */
    public static final int TIPO_CONSULTA_ELEMENTOS = 4;
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
    private static final String OWM_C_NOMBRE = "nombre";
    private static final String OWM_C_HORA_I = "horaInicio";
    private static final String OWM_C_HORA_F = "horaFin";
    private static final String OWM_C_COOR_LAT = "coordLat";
    private static final String OWM_C_COOR_LON = "coordLon";
    private static final String OWM_C_TLF = "telefono";
    private static final String OWM_C_CONTACTO = "nombreContacto";
    private static final String OWM_C_DIRECCION = "direccion";
    private static final String OWM_C_APERTURA = "hAperturaIni";
    private static final String OWM_C_CIERRE = "hAperturaFin";
    private static final String OWM_C_PROMO = "promocion";
    private static final String OWM_C_DIA_A = "diaInicioApertura";
    private static final String OWM_C_DIA_C = "diaFinApertura";
    //Campos de elementos
    private static final String OWM_E_NOMBRE = "nombre";
    private static final String OWM_E_TIPO = "tipo";
    private static final String OWM_E_ARRAY = "elementos";
    private static final String OWM_E_RELACIONES = "relaciones";
    //Campos de menus
    private static final String OWM_M_NOMBRE = "nombre";
    private static final String OWM_M_PRECIO = "precio";
    //Campos de platos
    private static final String OWM_P_NOMBRE = "nombre";
    private static final String OWM_P_DESCRIPCION = "descripcion";
    private static final String OWM_P_TIPO = "tipo";
    private static final String OWM_P_AGOTADO = "agotado";

    public ComedoresService() {
        super("ComedoresService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        //Declarados fuera para poder ser cerrados en finally
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String jsonStr;

        // Recogemos tipo, id y fecha del intent
        int tipo = intent.getIntExtra(KEY_TIPO, TIPO_CONSULTA_PLATOS);
        long id = intent.getLongExtra(KEY_ID, -1);
        long fecha = intent.getLongExtra(KEY_FECHA, -1);

        try {
            // Construímos la dirección de conexión y conectamos
            Uri uriConexion = Uri.parse(API_DIR).buildUpon()
                    .appendQueryParameter(KEY_TIPO, Integer.toString(tipo))
                    .appendQueryParameter(KEY_ID, Long.toString(id))
                    .appendQueryParameter(KEY_FECHA, Utility.denormalizarFecha(fecha)).build();
            URL url = new URL(uriConexion.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Preparamos el lector para el stream de entrada
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            if (inputStream == null) {
                //No podemos hacer nada
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Leemos hasta el final
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            if (stringBuilder.length() == 0) {
                // Se recibió vacío
                return;
            }

            jsonStr = stringBuilder.toString();

            //Hacemos el substring porque usamos un hosting gratuito, que añade código de
            //análisis al final ^^' se podrá eliminar cuando tengamos server propio
            obtenerInformacion(jsonStr.substring(0, jsonStr.lastIndexOf("<!--FIN-->") + 1), tipo, id, fecha);
        } catch (IOException e) {
            // Estamos conectados?
            if(!Utility.conectado(this)) {
                Intent sinConexion = new Intent(EVENTO_SIN_CONEXION);
                sinConexion.putExtra(KEY_TIPO, tipo);
                sinConexion.putExtra(KEY_ID, id);
                sinConexion.putExtra(KEY_FECHA, fecha);
                LocalBroadcastManager.getInstance(this).sendBroadcast(sinConexion);
            }
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            // Ha fallado el JSON
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            // Cerramos la conexión y el reader si están abiertos
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

    private void obtenerInformacion(String jsonStr, int tipo, long id, long fecha) throws JSONException {
        JSONObject objetoJson = new JSONObject(jsonStr);
        // Comprobamos el status de la respuesta de la API, si es OK ha ido bien
        if( objetoJson.getString("status").equals("OK") ) {
            switch (tipo) {
                case TIPO_CONSULTA_COMEDORES:
                    guardarComedores(objetoJson.getJSONArray("respuesta"));
                    break;
                case TIPO_CONSULTA_ELEMENTOS:
                    guardarElementos(objetoJson.getJSONObject("respuesta"));
                    break;
                case TIPO_CONSULTA_MENUS:
                    guardarMenus(objetoJson.getJSONArray("respuesta"), id);
                    break;
                case TIPO_CONSULTA_PLATOS:
                    guardarPlatos(objetoJson.getJSONArray("respuesta"), id, fecha);
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de consulta no válido");
            }
        } //TODO: si no, avisar con una snackbar
    }

    private int eliminarSobrantes(List<Long> idsParaConservar, Uri contentUri, String idColName) {
        return getContentResolver().delete(
                contentUri,
                String.format("%s NOT IN (%s)", idColName, TextUtils.join(", ", idsParaConservar)),
                null);
    }

    private int insertar(List<ContentValues> paraInsertar, Uri contentUri) {
        if(paraInsertar.size() > 0) {
            ContentValues[] cVArray = new ContentValues[paraInsertar.size()];
            paraInsertar.toArray(cVArray);
            return this.getContentResolver().bulkInsert(contentUri, cVArray);
        }
        return 0;
    }

    private int actualizar(List<ContentValues> paraActualizar, Uri contentUri, String idColName) {
        int actualizados = 0;
        if(paraActualizar.size() > 0) {
            for(ContentValues fila : paraActualizar) {
                String id = fila.getAsString(idColName);
                fila.remove(idColName); // El ID no se actualiza
                actualizados += this.getContentResolver()
                        .update(contentUri, fila, idColName + " = ?", new String[]{id});
            }
        }
        return actualizados;
    }

    private List<Long> obtenerIdsComedoresExistentes() {
        //Obtenemos los ids que ya están en la base
        Cursor c = getContentResolver().query(
                ComedoresContract.ComedoresEntry.CONTENT_URI,
                new String[]{ComedoresContract.ComedoresEntry._ID},
                null, null,
                null);
        ArrayList<Long> idsExistentes = null;
        if(c != null) {
            if (c.moveToFirst()) {
                idsExistentes = new ArrayList<>(c.getCount());
                while (!c.isAfterLast()) {
                    idsExistentes.add(c.getLong(0));
                    c.moveToNext();
                }
            }
            c.close();
        }
        return idsExistentes;
    }

    private ContentValues crearFilaComedorFromJson(JSONObject jsonObject) throws JSONException {
        ContentValues nuevaFila = new ContentValues();

        // Simplemente ponemos en la fila los elementos
        nuevaFila.put(ComedoresContract.ComedoresEntry._ID, jsonObject.getLong(OWM_ID));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_NOMBRE,
                jsonObject.getString(OWM_C_NOMBRE));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_HORA_INI,
                Utility.normalizarHora(jsonObject.getString(OWM_C_HORA_I)));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_HORA_FIN,
                Utility.normalizarHora(jsonObject.getString(OWM_C_HORA_F)));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_COORD_LAT, jsonObject.getDouble(OWM_C_COOR_LAT));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_COORD_LONG, jsonObject.getDouble(OWM_C_COOR_LON));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_TLFN, jsonObject.getString(OWM_C_TLF));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_NOMBRE_CONTACTO, jsonObject.getString(OWM_C_CONTACTO));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_DIR, jsonObject.getString(OWM_C_DIRECCION));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_INI,
                Utility.normalizarHora(jsonObject.getString(OWM_C_APERTURA)));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_FIN,
                Utility.normalizarHora(jsonObject.getString(OWM_C_CIERRE)));
        //Dias de apertura y cierre
        String  diaA = jsonObject.getString(OWM_C_DIA_A);
        String diaC = jsonObject.getString(OWM_C_DIA_C);
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_DIA_INI_AP, Utility.normalizarDiaSemana(diaA));
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_DIA_FIN_AP, Utility.normalizarDiaSemana(diaC));
        //Promo
        nuevaFila.put(ComedoresContract.ComedoresEntry.COLUMN_PROMO, jsonObject.getString(OWM_C_PROMO));

        return nuevaFila;
    }

    private void guardarComedores(JSONArray jsonArray) throws JSONException {
        List<ContentValues> comedoresParaInsertar = new ArrayList<>();
        List<ContentValues> comedoresParaActualizar = new ArrayList<>();
        List<Long> idsComedores = new ArrayList<>(jsonArray.length());
        List<Long> idsExistentes = obtenerIdsComedoresExistentes();

        // Recorremos el array JSON y generamos el List de filas para insertar
        for(int i = 0; i < jsonArray.length(); i++) {
            ContentValues nuevaFila = crearFilaComedorFromJson( (JSONObject) jsonArray.get(i));
            Long nuevoId = nuevaFila.getAsLong(ComedoresContract.ComedoresEntry._ID);
            idsComedores.add(nuevoId); // Guardamos el id en la lista
            if(idsExistentes != null && idsExistentes.contains(nuevoId)) {
                comedoresParaActualizar.add(nuevaFila);
            } else {
                comedoresParaInsertar.add(nuevaFila);
            }
        }

        Uri uriComedores = ComedoresContract.ComedoresEntry.CONTENT_URI;
        // Eliminamos sobrantes
        int eliminados = eliminarSobrantes(idsComedores, uriComedores,  ComedoresContract.ComedoresEntry._ID);
        // Insertamos nuevos
        int insertados = insertar(comedoresParaInsertar, uriComedores);
        // Actualizamos el resto
        int actualizados = actualizar(comedoresParaActualizar, uriComedores, ComedoresContract.ComedoresEntry._ID);

        // Anotamos la actualización de comedores
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(getString(R.string.pref_ultima_act_comedores), System.currentTimeMillis());
        editor.commit();

        Log.d(LOG_TAG, "Service completado: " + insertados + " comedores insertados, "
                + eliminados + " eliminados, " + actualizados + " actualizados.");
    }

    private ContentValues crearFilaElementoFromJson(JSONObject jsonObject) throws  JSONException {
        ContentValues nuevaFila = new ContentValues();
        nuevaFila.put(ComedoresContract.ElementosEntry._ID, jsonObject.getLong(OWM_ID));
        nuevaFila.put(ComedoresContract.ElementosEntry.COLUMN_NOMBRE, jsonObject.getString(OWM_E_NOMBRE));
        nuevaFila.put(ComedoresContract.ElementosEntry.COLUMN_TIPO, jsonObject.getString(OWM_E_TIPO));
        return nuevaFila;
    }

    private void guardarElementos(JSONObject jsonObjeto) throws JSONException {
        JSONArray arrayElementos = jsonObjeto.getJSONArray(OWM_E_ARRAY);
        List<ContentValues> elementosParaInsertar = new ArrayList<>(arrayElementos.length());

        // Recorremos el array JSON y creamos la lista de elementos
        for (int i = 0; i < arrayElementos.length(); i++) {
            ContentValues elemento = crearFilaElementoFromJson((JSONObject) arrayElementos.get(i));
            elementosParaInsertar.add(elemento);
        }
        // Los insertamos
        int insertados = insertar(elementosParaInsertar, ComedoresContract.ElementosEntry.buildInsercionUri());

        //Asociamos elementos y menús
        JSONObject jsonRelaciones = jsonObjeto.getJSONObject(OWM_E_RELACIONES);
        JSONArray jsonMenus = jsonRelaciones.names();
        ArrayList<ContentValues> relaciones = new ArrayList<>();

        // Relaciones contiene objetos del tipo {idMenu: arrayDeIdsDeElementos}
        for(int i=0; i < jsonMenus.length(); i++) {
            JSONArray jsonMenu = jsonRelaciones.getJSONArray(jsonMenus.getString(i));
            for(int j=0; j < jsonMenu.length(); j++ ) {
                ContentValues nuevaRelacion = new ContentValues();
                nuevaRelacion.put(ComedoresContract.TienenEntry.COLUMN_MENU, jsonMenus.getLong(i));
                nuevaRelacion.put(ComedoresContract.TienenEntry.COLUMN_ELEMENTO, jsonMenu.getLong(j));
                relaciones.add(nuevaRelacion);
            }
        }

        // Asociamos lo que haga falta
        int asociados = insertar(relaciones, ComedoresContract.TienenEntry.CONTENT_URI);
        Log.d(LOG_TAG, "Service completado, " + insertados + " elementos insertados y " + asociados + " asociados.");
    }

    private List<Long> obtenerIdsMenusExistentes(Long idComedor) {
        Cursor c = getContentResolver().query(
                ComedoresContract.TiposMenuEntry.CONTENT_URI,
                new String[]{ComedoresContract.TiposMenuEntry._ID},
                ComedoresContract.TiposMenuEntry.COLUMN_COMEDOR + " = ?", new String[]{Long.toString(idComedor)},
                null);

        ArrayList<Long> idsExistentes = null;
        if(c!=null) {
            if (c.moveToFirst()) {
                idsExistentes = new ArrayList<>(c.getCount());
                while (!c.isAfterLast()) {
                    idsExistentes.add(c.getLong(0));
                    c.moveToNext();
                }
            }
            c.close();
        }

        return idsExistentes;
    }

    private ContentValues crearFilaMenusFromJson(JSONObject jsonObject, long idComedor) throws JSONException {
        ContentValues nuevaFila = new ContentValues();
        nuevaFila.put(ComedoresContract.TiposMenuEntry._ID, jsonObject.getLong(OWM_ID));
        nuevaFila.put(ComedoresContract.TiposMenuEntry.COLUMN_NOMBRE, jsonObject.getString(OWM_M_NOMBRE));
        nuevaFila.put(ComedoresContract.TiposMenuEntry.COLUMN_PRECIO, jsonObject.getDouble(OWM_M_PRECIO));
        nuevaFila.put(ComedoresContract.TiposMenuEntry.COLUMN_COMEDOR, idComedor);
        return nuevaFila;
    }

    private void guardarMenus(JSONArray jsonArray, long idComedor) throws JSONException {
        List<ContentValues> menusParaInsertar = new ArrayList<>();
        List<ContentValues> menusParaActualizar = new ArrayList<>();
        List<Long> ids = new ArrayList<>(jsonArray.length());
        List<Long> idsExistentes = obtenerIdsMenusExistentes(idComedor);

        for (int i = 0; i < jsonArray.length(); i++) {
            ContentValues nuevaFila = crearFilaMenusFromJson((JSONObject) jsonArray.get(i), idComedor);
            long idTipoMenu = nuevaFila.getAsLong(ComedoresContract.TiposMenuEntry._ID);
            ids.add(idTipoMenu);
            if(idsExistentes != null && idsExistentes.contains(idTipoMenu)) {
                menusParaActualizar.add(nuevaFila);
            } else {
                menusParaInsertar.add(nuevaFila);
            }
        }

        Uri contentUri = ComedoresContract.TiposMenuEntry.CONTENT_URI;
        int eliminados = eliminarSobrantes(ids, contentUri, ComedoresContract.TiposMenuEntry._ID);
        int insertados = insertar(menusParaInsertar, contentUri);
        int actualizados = actualizar(menusParaActualizar, contentUri, ComedoresContract.TiposMenuEntry._ID);

        // Guardamos la fecha de última actualización de los menús de este comedor
        ContentValues values = new ContentValues();
        values.put(ComedoresContract.ComedoresEntry.COLUMN_LAST_ACT, System.currentTimeMillis());
        this.getContentResolver().update(
                ComedoresContract.ComedoresEntry.CONTENT_URI,
                values,
                ComedoresContract.ComedoresEntry._ID + " = ?",
                new String[]{Long.toString(idComedor)});

        Log.d(LOG_TAG, "Service completado, " + insertados + " menus insertados, " +
                eliminados + " eliminados, " + actualizados + " actualizados.");
    }

    private ContentValues crearFilaPlatosFromJson(JSONObject jsonObject) throws  JSONException {
        ContentValues nuevaFila = new ContentValues();
        nuevaFila.put(ComedoresContract.PlatosEntry._ID,
                jsonObject.getLong(OWM_ID));
        nuevaFila.put(ComedoresContract.PlatosEntry.COLUMN_NOMBRE,
                jsonObject.getString(OWM_P_NOMBRE));
        nuevaFila.put(ComedoresContract.PlatosEntry.COLUMN_DESCRIPCION,
                jsonObject.getString(OWM_P_DESCRIPCION));
        nuevaFila.put(ComedoresContract.PlatosEntry.COLUMN_TIPO,
                jsonObject.getString(OWM_P_TIPO));
        nuevaFila.put(ComedoresContract.PlatosEntry.COLUMN_AGOTADO,
                jsonObject.getInt(OWM_P_AGOTADO));
        return nuevaFila;
    }

    private void guardarPlatos(JSONArray jsonArray, long id, long fecha) throws JSONException {
        ArrayList<ContentValues> platos = new ArrayList<>(jsonArray.length());

        // Creamos los ContentValues
        for (int i = 0; i < jsonArray.length(); i++)
            platos.add(crearFilaPlatosFromJson((JSONObject) jsonArray.get(i)));

        int insertados = 0;
        int eliminados = 0;
        if(platos.size() > 0) {
            ContentValues[] cVArray = new ContentValues[platos.size()];
            platos.toArray(cVArray);

            //Insertamos en la tabla de platos, con el comedor asociado correspondiente y fecha
            insertados = this.getContentResolver().bulkInsert(
                    ComedoresContract.PlatosEntry.buildInsercionUri(id, fecha), cVArray);

            //Al insertar platos, queremos eliminar todos los anteriores a 'hoy'
            eliminados = this.getContentResolver().delete(
                    ComedoresContract.PlatosEntry.CONTENT_URI,
                    ComedoresContract.TenerEntry.COLUMN_FECHA + " < ?",
                    new String[]{Long.toString(Utility.fechaHoy())});
        }

        Log.d(LOG_TAG, "Service completado: " + insertados + " platos insertados/actualizados, " + eliminados + " eliminados.");
    }
}
