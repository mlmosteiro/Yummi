package com.example.android.yummi.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contrato para la base de datos, compuesta de 6 tablas. ¡Espero que SQLite pueda con ello (y yo)!
 * Created by David Campos Rodríguez on 06/02/2016.
 */
public class ComedoresContract {
    // Usado por el ContentProvider, debe ser único en el dispositivo, por lo que se aconseja usar
    // el nombre de paquete de la app
    public static final String CONTENT_AUTHORITY = "com.example.android.yummi";
    // Uri base para obtener contenido en esta app
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Direcciones para la consulta de las tablas:
    public static final String PATH_COMEDORES = "comedores";
    public static final String PATH_TIPOSMENU = "tiposmenu";
    public static final String PATH_PLATOS = "platos";
    public static final String PATH_ELEMENTOS = "elementos";

    /**
     * Tabla de Comedores
     */
    public static final class ComedoresEntry implements BaseColumns {
        public static final String TABLE_NAME = "comedores";

        //Horarios de comedor
        public static final String COLUMN_HORA_INI = "horaIni";
        public static final String COLUMN_HORA_FIN = "horaFin";
        //Horarios de apertura y cierre
        public static final String COLUMN_HORA_AP_INI = "horaApIni";
        public static final String COLUMN_HORA_AP_FIN = "horaApFin";
        //Coordenadas (ubicación)
        public static final String COLUMN_COORD_LAT = "latitude";
        public static final String COLUMN_COORD_LONG = "longitude";
        //Nombre y contacto
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_NOMBRE_CONTACTO = "nombreContacto";
        public static final String COLUMN_TLFN = "tlfn";
        public static final String COLUMN_DIR = "direccion";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMEDORES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMEDORES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMEDORES;

        public static Uri buildComedorUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Tabla de TiposMenu
     */
    public static final class TiposMenuEntry implements BaseColumns {
        public static final String TABLE_NAME = "tiposmenu";

        public static final String COLUMN_DESCRIPCION = "descripcion";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_PRECIO = "precio";
        public static final String COLUMN_COMEDOR = "comedor";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TIPOSMENU).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TIPOSMENU;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TIPOSMENU;

        public static Uri buildTipoMenuUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /**
     * Tabla de Platos
     */
    public static final class PlatosEntry implements BaseColumns {
        public static final String TABLE_NAME = "platos";

        public static final String COLUMN_DESCRIPCION = "descripcion";
        public static final String COLUMN_NOMBRE = "nombre";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLATOS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLATOS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLATOS;

        public static Uri buildPlatoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Tabla de Elementos que pueden conformar un tipo de menú
     */
    public static final class ElementosEntry implements BaseColumns {
        public static final String TABLE_NAME = "elementos";

        public static final String COLUMN_TIPO = "tipo";
        public static final String COLUMN_NOMBRE = "nombre";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ELEMENTOS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ELEMENTOS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ELEMENTOS;

        public static Uri buildElemntoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Tabla "Tener", guarda las relaciones entre Comedores y Platos
     */
    public static final class TenerEntry implements BaseColumns {
        public static final String TABLE_NAME = "tener";

        public static final String COLUMN_COMEDOR = "comedor"; //Id del comedor
        public static final String COLUMN_FECHA = "fecha"; //Fecha en que tuvo el plato
        public static final String COLUMN_PLATO = "plato"; //Id del plato
    }

    /**
     * Tabla "Tienen", guarda las relaciones entre TiposMenu y Elementos
     */
    public static final class TienenEntry implements BaseColumns {
        public static final String TABLE_NAME = "tienen";

        public static final String COLUMN_MENU = "menu"; //Id del menu
        public static final String COLUMN_ELEMENTO = "elemento"; //Id del elemento
    }
}