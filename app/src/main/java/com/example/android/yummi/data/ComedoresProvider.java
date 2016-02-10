package com.example.android.yummi.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.android.yummi.Utility;

/**
 * Proveedor de contenido para la base de datos de la aplicación
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 06/02/2016.
 */
public class ComedoresProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ComedoresDbHelper mOpenHelper;

    static final int COMEDORES = 100;
    static final int MENUS_COMEDOR = 110;
    static final int PLATOS_COMEDOR = 120;
    static final int PLATOS_COMEDOR_FECHA = 121;
    static final int TIPOSMENU = 200;
    static final int ELEMENTOS_MENU = 210;
    static final int PLATOS = 300;
    static final int ELEMENTOS = 400;

    static UriMatcher buildUriMatcher() {
        //La root uri no hace nada
        UriMatcher nUM = new UriMatcher(UriMatcher.NO_MATCH);

        nUM.addURI(ComedoresContract.CONTENT_AUTHORITY, ComedoresContract.PATH_COMEDORES, COMEDORES);
        nUM.addURI(ComedoresContract.CONTENT_AUTHORITY, ComedoresContract.PATH_COMEDORES + "/#/tiposmenu/", MENUS_COMEDOR);
        nUM.addURI(ComedoresContract.CONTENT_AUTHORITY, ComedoresContract.PATH_COMEDORES + "/#/platos/", PLATOS_COMEDOR);
        nUM.addURI(ComedoresContract.CONTENT_AUTHORITY, ComedoresContract.PATH_COMEDORES + "/#/platos/#", PLATOS_COMEDOR_FECHA);

        nUM.addURI(ComedoresContract.CONTENT_AUTHORITY, ComedoresContract.PATH_TIPOSMENU, TIPOSMENU);
        nUM.addURI(ComedoresContract.CONTENT_AUTHORITY, ComedoresContract.PATH_TIPOSMENU + "/#/elementos/", ELEMENTOS_MENU);

        nUM.addURI(ComedoresContract.CONTENT_AUTHORITY, ComedoresContract.PATH_PLATOS, PLATOS);
        nUM.addURI(ComedoresContract.CONTENT_AUTHORITY, ComedoresContract.PATH_ELEMENTOS, ELEMENTOS);

        return nUM;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ComedoresDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int match = sUriMatcher.match(uri);
        Cursor retCursor;
        switch (match) {
            // 'comedores/'
            case COMEDORES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ComedoresContract.ComedoresEntry.TABLE_NAME,
                        projection,
                        selection, selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            }
            // 'comedores/[id]/tiposmenu'
            case MENUS_COMEDOR: {
                retCursor = getMenusByComedor(uri, projection, sortOrder);
                break;
            }
            // 'comedores/[id]/platos'
            case PLATOS_COMEDOR: {
                retCursor = getPlatosByComedor(uri, projection, sortOrder);
                break;
            }
            // 'comedores/[id]/platos/[fecha]'
            case PLATOS_COMEDOR_FECHA: {
                retCursor = getPlatosByComedorAndFecha(
                        uri, projection, sortOrder, Long.parseLong(uri.getPathSegments().get(3)));
                break;
            }
            // 'tiposmenu/'
            case TIPOSMENU: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ComedoresContract.TiposMenuEntry.TABLE_NAME,
                        projection,
                        selection, selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            }
            // 'tiposmenu/[id]/elementos/'
            case ELEMENTOS_MENU: {
                retCursor = getElementosByMenu(uri, projection, sortOrder);
                break;
            }
            case PLATOS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ComedoresContract.PlatosEntry.TABLE_NAME,
                        projection,
                        selection, selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            }
            case ELEMENTOS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ComedoresContract.ElementosEntry.TABLE_NAME,
                        projection,
                        selection, selectionArgs,
                        null, null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    // elementos._id IN (SELECT elemento FROM tienen WHERE menu = [id])
    private static final String sElementosByMenuSelection =
            ComedoresContract.ElementosEntry.TABLE_NAME + "." + ComedoresContract.ElementosEntry._ID +
                    " IN (SELECT " + ComedoresContract.TienenEntry.COLUMN_ELEMENTO +
                    " FROM " + ComedoresContract.TienenEntry.TABLE_NAME +
                    " WHERE " + ComedoresContract.TienenEntry.COLUMN_MENU + " = ? )";

    private Cursor getElementosByMenu(Uri uri, String[] projection, String sortOrder) {
        long idMenu = ComedoresContract.getIdElemento(uri);
        return mOpenHelper.getReadableDatabase().query(
                ComedoresContract.ElementosEntry.TABLE_NAME,
                projection,
                sElementosByMenuSelection, new String[]{Long.toString(idMenu)},
                null, null,
                sortOrder);
    }

    // comedores._id = idComedor
    private static final String sByComedorSelection =
            ComedoresContract.ComedoresEntry.TABLE_NAME + "." + ComedoresContract.ComedoresEntry._ID +
                    " = ?";

    // platos._id IN (SELECT plato FROM tener WHERE comedor = [id] AND fecha = [fecha])
    private static final String sPlatosByComedorAndFechaSelection =
            ComedoresContract.PlatosEntry.TABLE_NAME + "." + ComedoresContract.ComedoresEntry._ID +
                    " IN (SELECT " + ComedoresContract.TenerEntry.COLUMN_PLATO +
                    " FROM " + ComedoresContract.TenerEntry.TABLE_NAME +
                    " WHERE " + ComedoresContract.TenerEntry.COLUMN_COMEDOR + " = ? " +
                    " AND " +
                    ComedoresContract.TenerEntry.COLUMN_FECHA + " = ? )";

    private Cursor getPlatosByComedorAndFecha(Uri uri, String[] projection, String sortOrder, long fecha) {
        long idComedor = ComedoresContract.getIdElemento(uri);
        String tipo = uri.getQueryParameter(ComedoresContract.PlatosEntry.URI_PATRON_TIPO_KEY);
        String seleccion = sPlatosByComedorAndFechaSelection;
        if( tipo != null) {
            seleccion += " AND " + ComedoresContract.PlatosEntry.COLUMN_TIPO + " LIKE " + tipo;
        }
        return mOpenHelper.getReadableDatabase().query(
                ComedoresContract.PlatosEntry.TABLE_NAME,
                projection,
                seleccion, new String[]{Long.toString(idComedor), Long.toString(fecha)},
                null, null,
                sortOrder);
    }

    private Cursor getPlatosByComedor(Uri uri, String[] projection, String sortOrder) {
        return getPlatosByComedorAndFecha(uri, projection, sortOrder, Utility.fechaHoy());
    }

    private Cursor getMenusByComedor(Uri uri, String[] projection, String sortOrder) {
        long idComedor = ComedoresContract.getIdElemento(uri);
        final String tabla = ComedoresContract.ComedoresEntry.TABLE_NAME + " INNER JOIN " +
                ComedoresContract.TiposMenuEntry.TABLE_NAME +
                " ON " + ComedoresContract.TiposMenuEntry.TABLE_NAME +
                "." + ComedoresContract.TiposMenuEntry.COLUMN_COMEDOR +
                " = " + ComedoresContract.ComedoresEntry.TABLE_NAME +
                "." + ComedoresContract.ComedoresEntry._ID;
        return mOpenHelper.getReadableDatabase().query(
                tabla,
                projection,
                sByComedorSelection, new String[]{Long.toString(idComedor)},
                null, null,
                sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMEDORES:
                return ComedoresContract.ComedoresEntry.CONTENT_TYPE;
            case MENUS_COMEDOR:
                return ComedoresContract.TiposMenuEntry.CONTENT_TYPE;
            case PLATOS_COMEDOR:
                return ComedoresContract.PlatosEntry.CONTENT_TYPE;
            case PLATOS_COMEDOR_FECHA:
                return ComedoresContract.PlatosEntry.CONTENT_TYPE;
            case TIPOSMENU:
                return ComedoresContract.TiposMenuEntry.CONTENT_TYPE;
            case ELEMENTOS_MENU:
                return ComedoresContract.ElementosEntry.CONTENT_TYPE;
            case PLATOS:
                return ComedoresContract.PlatosEntry.CONTENT_TYPE;
            case ELEMENTOS:
                return ComedoresContract.ElementosEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case COMEDORES: {
                long _id = db.insert(ComedoresContract.ComedoresEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ComedoresContract.ComedoresEntry.buildComedorUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TIPOSMENU: {
                long _id = db.insert(ComedoresContract.TiposMenuEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ComedoresContract.TiposMenuEntry.buildTipoMenuUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PLATOS: {
                returnUri = insertarPlatos(db, uri, values);
                break;
            }
            case ELEMENTOS: {
                returnUri = insertarElementos(db, uri, values);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    private Uri insertarPlatos(SQLiteDatabase db, Uri uri, ContentValues values){
        String strComedorId = uri.getQueryParameter(ComedoresContract.PlatosEntry.URI_COMEDOR_ID_KEY);
        String strFecha = uri.getQueryParameter(ComedoresContract.PlatosEntry.URI_FECHA_KEY);
        Long _idPlato= values.getAsLong(ComedoresContract.PlatosEntry._ID);

        if(strComedorId != null && strFecha != null && _idPlato != null) {
            long fecha = Long.parseLong(strFecha);
            //Comprobamos si ya existe
            Cursor c = db.query(
                    ComedoresContract.PlatosEntry.TABLE_NAME,
                    null,
                    ComedoresContract.PlatosEntry._ID + " = ?", new String[]{Long.toString(_idPlato)},
                    null, null,
                    null);
            if(!c.moveToFirst()) {
                //No existe, lo insertamos, el _id debería ser el mismo, claro
                _idPlato = db.insert(ComedoresContract.PlatosEntry.TABLE_NAME, null, values);
            }
            //Insertamos la relación
            ContentValues valsRelacion = new ContentValues();
            valsRelacion.put(ComedoresContract.TenerEntry.COLUMN_COMEDOR, strComedorId);
            valsRelacion.put(ComedoresContract.TenerEntry.COLUMN_FECHA, fecha);
            valsRelacion.put(ComedoresContract.TenerEntry.COLUMN_PLATO, _idPlato);
            db.insert(ComedoresContract.TenerEntry.TABLE_NAME, null, valsRelacion);

            if (_idPlato > 0)
                return ComedoresContract.PlatosEntry.buildPlatoUri(_idPlato);
            else
                throw new android.database.SQLException("Failed to insert row into " + uri);
        } else {
            throw new IllegalArgumentException("No se ha pasado algún parámetro a la inserción (id del plato, id del comedor y fecha).");
        }
    }

    private Uri insertarElementos(SQLiteDatabase db, Uri uri, ContentValues values) {
        String strMenuId = uri.getQueryParameter(ComedoresContract.ElementosEntry.URI_MENU_ID_KEY);
        Long _idElemento= values.getAsLong(ComedoresContract.PlatosEntry._ID);

        if(strMenuId != null && _idElemento != null) {
            //Comprobamos si ya existe
            Cursor c = db.query(
                    ComedoresContract.ElementosEntry.TABLE_NAME,
                    null,
                    ComedoresContract.ElementosEntry._ID + " = ?", new String[]{Long.toString(_idElemento)},
                    null, null,
                    null);
            if(!c.moveToFirst()) {
                //No existe, lo insertamos, el _id debería ser el mismo, claro
                _idElemento = db.insert(ComedoresContract.ElementosEntry.TABLE_NAME, null, values);
            }
            //Insertamos la relación
            ContentValues valsRelacion = new ContentValues();
            valsRelacion.put(ComedoresContract.TienenEntry.COLUMN_MENU, strMenuId);
            valsRelacion.put(ComedoresContract.TienenEntry.COLUMN_ELEMENTO, _idElemento);
            db.insert(ComedoresContract.TienenEntry.TABLE_NAME, null, valsRelacion);

            if (_idElemento > 0)
                return ComedoresContract.ElementosEntry.buildElementoUri(_idElemento);
            else
                throw new android.database.SQLException("Failed to insert row into " + uri);
        } else {
            throw new IllegalArgumentException("No se ha pasado algún parámetro a la inserción (id del elemento y del menu).");
        }
    }

    //TODO: sobreescribir bulkInsert

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int num;
        if(null == selection) selection = "1";
        switch (match) {
            case COMEDORES: {
                num = db.delete(
                        ComedoresContract.ComedoresEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            case TIPOSMENU: {
                num = db.delete(
                        ComedoresContract.TiposMenuEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            // Los platos se eliminan con un where que hace referencia a su relación, esto es,
            // eliminamos todos los platos cuya relación con comedores cumpla el where (y la
            // relación también)
            case PLATOS: {
                // platos._id IN (SELECT plato FROM tener WHERE [seleccion] )
                final String platosDeleteSelection =
                        ComedoresContract.PlatosEntry.TABLE_NAME + "." + ComedoresContract.PlatosEntry._ID +
                        " IN (SELECT " + ComedoresContract.TenerEntry.COLUMN_PLATO +
                                " FROM " + ComedoresContract.TenerEntry.TABLE_NAME +
                                " WHERE " + selection + " )";
                num = db.delete(
                        ComedoresContract.PlatosEntry.TABLE_NAME,
                        platosDeleteSelection, selectionArgs);
                //Eliminamos de la relación
                db.delete(ComedoresContract.TenerEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            }
            // Eliminar un elemento elimina todas sus relaciones
            case ELEMENTOS: {
                // elemento IN (SELECT _id FROM elementos WHERE [seleccion] )
                final String tienenDeleteSelection =
                        ComedoresContract.TienenEntry.COLUMN_ELEMENTO +
                        " IN (SELECT " + ComedoresContract.ElementosEntry._ID +
                        " FROM " + ComedoresContract.ElementosEntry.TABLE_NAME +
                        " WHERE " + selection + " )";
                num = db.delete(
                        ComedoresContract.ElementosEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                db.delete(
                        ComedoresContract.TienenEntry.TABLE_NAME,
                        tienenDeleteSelection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(num != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return num;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int num;
        if(null == selection) selection = "1";
        switch (match) {
            case COMEDORES: {
                num = db.update(
                        ComedoresContract.ComedoresEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case TIPOSMENU: {
                num = db.update(
                        ComedoresContract.TiposMenuEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case PLATOS: {
                num = db.update(
                        ComedoresContract.PlatosEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case ELEMENTOS: {
                num = db.update(
                        ComedoresContract.ElementosEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(num != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return num;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
