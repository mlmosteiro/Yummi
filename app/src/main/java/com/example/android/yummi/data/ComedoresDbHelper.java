package com.example.android.yummi.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.yummi.data.ComedoresContract.ComedoresEntry;
import com.example.android.yummi.data.ComedoresContract.ElementosEntry;
import com.example.android.yummi.data.ComedoresContract.PlatosEntry;
import com.example.android.yummi.data.ComedoresContract.TenerEntry;
import com.example.android.yummi.data.ComedoresContract.TienenEntry;
import com.example.android.yummi.data.ComedoresContract.TiposMenuEntry;
/**
 * Maneja la base de datos local
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 06/02/2016.
 */
public class ComedoresDbHelper extends SQLiteOpenHelper {

    //Si se actualiza el esquema, debe incrementarse la versión
    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "comedores.db";

    public ComedoresDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Creamos tabla de comedores
        final String SQL_CREATE_COMEDORES_TABLE = "CREATE TABLE " + ComedoresEntry.TABLE_NAME + " (" +
                ComedoresEntry._ID + " INTEGER PRIMARY KEY, " +

                ComedoresEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                ComedoresEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                ComedoresEntry.COLUMN_DIR + " TEXT NOT NULL, " +
                ComedoresEntry.COLUMN_HORA_AP_INI + " INTEGER NOT NULL, " +
                ComedoresEntry.COLUMN_HORA_AP_FIN + " INTEGER NOT NULL, " +
                ComedoresEntry.COLUMN_HORA_INI + " INTEGER NOT NULL, " +
                ComedoresEntry.COLUMN_HORA_FIN + " INTEGER NOT NULL, " +
                ComedoresEntry.COLUMN_NOMBRE + " TEXT UNIQUE NOT NULL, " +
                ComedoresEntry.COLUMN_NOMBRE_CONTACTO + " TEXT, " +
                ComedoresEntry.COLUMN_TLFN + " TEXT, " +
                ComedoresEntry.COLUMN_PROMO + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_COMEDORES_TABLE);

        // Creamos tabla de tipos de menu
        final String SQL_CREATE_TIPOS_MENU = "CREATE TABLE " + TiposMenuEntry.TABLE_NAME + " (" +
                TiposMenuEntry._ID + " INTEGER PRIMARY KEY, " +

                TiposMenuEntry.COLUMN_COMEDOR + " INTEGER NOT NULL, " +
                TiposMenuEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                TiposMenuEntry.COLUMN_PRECIO + " REAL NOT NULL, " +

                " FOREIGN KEY ( " + TiposMenuEntry.COLUMN_COMEDOR + " ) REFERENCES " +
                ComedoresEntry.TABLE_NAME + " (" + ComedoresEntry._ID + ") );";
        db.execSQL(SQL_CREATE_TIPOS_MENU);

        // Creamos tabla de platos
        final String SQL_CREATE_PLATOS_TABLE = "CREATE TABLE " + PlatosEntry.TABLE_NAME + " (" +
                PlatosEntry._ID + " INTEGER PRIMARY KEY, " +

                PlatosEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                PlatosEntry.COLUMN_DESCRIPCION + " TEXT NOT NULL, " +
                PlatosEntry.COLUMN_TIPO + " TEXT NOT NULL); ";
        db.execSQL(SQL_CREATE_PLATOS_TABLE);

        // Creamos tabla de elementos
        final String SQL_CREATE_ELEMENTOS_TABLE = "CREATE TABLE " + ElementosEntry.TABLE_NAME + " (" +
                ElementosEntry._ID + " INTEGER PRIMARY KEY, " +

                ElementosEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                ElementosEntry.COLUMN_TIPO + " TEXT NOT NULL); ";
        db.execSQL(SQL_CREATE_ELEMENTOS_TABLE);

        // Creamos tabla 'tener'
        final String SQL_CREATE_TENER_TABLE = "CREATE TABLE " + TenerEntry.TABLE_NAME + " (" +
                TenerEntry._ID + " INTEGER PRIMARY KEY, " +

                TenerEntry.COLUMN_COMEDOR +  " INTEGER NOT NULL, " +
                TenerEntry.COLUMN_FECHA + " INTEGER NOT NULL, " +
                TenerEntry.COLUMN_PLATO + " INTEGER NOT NULL, " +


                // Integridad referencial con comedores y platos
                " FOREIGN KEY (" + TenerEntry.COLUMN_COMEDOR + ") REFERENCES " +
                ComedoresEntry.TABLE_NAME + " (" + ComedoresEntry._ID + "), " +
                " FOREIGN KEY (" + TenerEntry.COLUMN_PLATO + ") REFERENCES " +
                PlatosEntry.TABLE_NAME + " (" + PlatosEntry._ID + ") );";
        db.execSQL(SQL_CREATE_TENER_TABLE);

        // Creamos tabla 'tienen'
        final String SQL_CREATE_TIENEN_TABLE = "CREATE TABLE " + TienenEntry.TABLE_NAME + " (" +
                TienenEntry._ID + " INTEGER PRIMARY KEY, " +

                TienenEntry.COLUMN_ELEMENTO +  " INTEGER NOT NULL, " +
                TienenEntry.COLUMN_MENU + " INTEGER NOT NULL, " +

                // Integridad referencial con comedores y platos
                " FOREIGN KEY (" + TienenEntry.COLUMN_ELEMENTO + ") REFERENCES " +
                ElementosEntry.TABLE_NAME + " (" + ElementosEntry._ID + "), " +
                " FOREIGN KEY (" + TienenEntry.COLUMN_MENU + ") REFERENCES " +
                TiposMenuEntry.TABLE_NAME + " (" + TiposMenuEntry._ID + ") );";
        db.execSQL(SQL_CREATE_TIENEN_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Método de actualización muy sencillo que consiste en borrar todas las tablas
        //y luego recrearlas. Sí, no es la mejor forma...
        db.execSQL("DROP TABLE IF EXISTS " + ComedoresEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TiposMenuEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ElementosEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PlatosEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TenerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TienenEntry.TABLE_NAME);
        onCreate(db);
    }
}
