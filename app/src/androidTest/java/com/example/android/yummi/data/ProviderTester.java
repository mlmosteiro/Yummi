package com.example.android.yummi.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Test para el ContentProvider, muy básico
 * Created by David Campos Rodríguez <david.campos@rai.usc.es> on 06/02/2016.
 */
public class ProviderTester extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testUris() throws Throwable {
        ContentResolver cp = getContext().getContentResolver();
        final Uri uri1 = Uri.parse(ComedoresContract.BASE_CONTENT_URI + "/comedores/");
        final Uri uri2 = Uri.parse(ComedoresContract.BASE_CONTENT_URI + "/comedores/1/tiposmenu/");
        final Uri uri3 = Uri.parse(ComedoresContract.BASE_CONTENT_URI + "/comedores/123891/platos/");
        final Uri uri4 = Uri.parse(ComedoresContract.BASE_CONTENT_URI + "/comedores/1231/platos/121013");
        final Uri uri5 = Uri.parse(ComedoresContract.BASE_CONTENT_URI + "/tiposmenu/");
        final Uri uri6 = Uri.parse(ComedoresContract.BASE_CONTENT_URI + "/tiposmenu/15/elementos/");
        final Uri uri7 = Uri.parse(ComedoresContract.BASE_CONTENT_URI + "/platos/");
        final Uri uri8 = Uri.parse(ComedoresContract.BASE_CONTENT_URI + "/elementos/");

        assertEquals(cp.getType(uri1), ComedoresContract.ComedoresEntry.CONTENT_TYPE);
        assertEquals(cp.getType(uri2), ComedoresContract.TiposMenuEntry.CONTENT_TYPE);
        assertEquals(cp.getType(uri3), ComedoresContract.PlatosEntry.CONTENT_TYPE);
        assertEquals(cp.getType(uri4), ComedoresContract.PlatosEntry.CONTENT_TYPE);
        assertEquals(cp.getType(uri5), ComedoresContract.TiposMenuEntry.CONTENT_TYPE);
        assertEquals(cp.getType(uri6), ComedoresContract.ElementosEntry.CONTENT_TYPE);
        assertEquals(cp.getType(uri7), ComedoresContract.PlatosEntry.CONTENT_TYPE);
        assertEquals(cp.getType(uri8), ComedoresContract.ElementosEntry.CONTENT_TYPE);
    }

    public void testInsertsAndQueries() throws Throwable {
        getContext().getContentResolver().delete(ComedoresContract.ComedoresEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(ComedoresContract.PlatosEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(ComedoresContract.TiposMenuEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(ComedoresContract.ElementosEntry.CONTENT_URI, null, null);

        ContentValues cvComedores = new ContentValues();
        long idComedor = 1;
        cvComedores.put(ComedoresContract.ComedoresEntry._ID, idComedor); //ID
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_NOMBRE, "Pepito");
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_DIR, "Calle las Rameras, 91");
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_COORD_LAT, 984654);
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_COORD_LONG, -894991);
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_INI, 166464L);
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_HORA_AP_FIN, 156464L);
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_HORA_INI, 162364L);
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_HORA_FIN, 111564L);
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_NOMBRE_CONTACTO, "Manuel Lamas");
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_TLFN, "123456789");
        cvComedores.put(ComedoresContract.ComedoresEntry.COLUMN_PROMO, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla a augue id lorem pretium ullamcorper at id justo. Aenean purus orci, vulputate efficitur augue eget, lobortis aliquet velit. Integer ipsum leo, fermentum non est a, faucibus fermentum lectus. Fusce porta lobortis dignissim. Ut quis vulputate nisl. Aliquam sed sapien id nisi imperdiet sodales eget vel diam. In bibendum ipsum imperdiet ante molestie, id sodales velit vulputate. Curabitur elementum faucibus cursus. Proin eu sollicitudin orci.");

        ContentValues cvPlatos = new ContentValues();
        cvPlatos.put(ComedoresContract.PlatosEntry._ID, "1");
        cvPlatos.put(ComedoresContract.PlatosEntry.COLUMN_NOMBRE, "Plato1");
        cvPlatos.put(ComedoresContract.PlatosEntry.COLUMN_TIPO, "carne");
        cvPlatos.put(ComedoresContract.PlatosEntry.COLUMN_DESCRIPCION, "Un plato.");
        String fecha = "11648949";

        ContentValues cvTiposMenu = new ContentValues();
        cvTiposMenu.put(ComedoresContract.TiposMenuEntry.COLUMN_NOMBRE, "nombre");
        cvTiposMenu.put(ComedoresContract.TiposMenuEntry.COLUMN_COMEDOR, 1);
        cvTiposMenu.put(ComedoresContract.TiposMenuEntry.COLUMN_PRECIO, 5.70);

        ContentValues cvElementos = new ContentValues();
        cvElementos.put(ComedoresContract.ElementosEntry._ID, 1);
        cvElementos.put(ComedoresContract.ElementosEntry.COLUMN_NOMBRE, "nombreElemento");
        cvElementos.put(ComedoresContract.ElementosEntry.COLUMN_TIPO, "extra");

        Uri uriInsertarComedor = ComedoresContract.ComedoresEntry.CONTENT_URI;
        Uri uriInsertarPlato = ComedoresContract.PlatosEntry.buildInsercionUri(
                idComedor,
                Long.parseLong(fecha));
        Uri uriInsertarMenu = ComedoresContract.TiposMenuEntry.CONTENT_URI;

        getContext().getContentResolver().insert(uriInsertarComedor, cvComedores);
        getContext().getContentResolver().insert(uriInsertarPlato, cvPlatos);
        long idMenu = ContentUris.parseId(
                getContext().getContentResolver().insert(uriInsertarMenu, cvTiposMenu));

        Uri uriInsertarElemento = ComedoresContract.ElementosEntry.buildInsercionUri(idMenu);
        getContext().getContentResolver().insert(uriInsertarElemento, cvElementos);

        Cursor c = getContext().getContentResolver()
                .query(ComedoresContract.ComedoresEntry.CONTENT_URI, null, null, null, null);

        if(!c.moveToFirst())
            fail("Comedores no devolvió nada");
        validateCurrentRecord("No coinciden los datos insertados en comedores", c, cvComedores);

        Cursor c2 = getContext().getContentResolver().query(
                ComedoresContract.PlatosEntry.buildPlatosByComedorAndFechaUri(idComedor, Long.parseLong(fecha)), null, null, null, null);

        if(!c2.moveToFirst())
            fail("Platos no devolvió nada");
        validateCurrentRecord("No coinciden los datos insertados en tener o platos", c2, cvPlatos);

        Cursor c3 = getContext().getContentResolver().query(
                ComedoresContract.TiposMenuEntry.buildTipoMenuByComedorUri(idComedor), null, null, null, null);

        if(!c3.moveToFirst())
            fail("Tipos menu no devolvió nada");
        validateCurrentRecord("No coinciden los datos insertados en tipos menu", c3, cvTiposMenu);

        Cursor c4 = getContext().getContentResolver().query(
                ComedoresContract.ElementosEntry.buildElementosByMenuUri(idMenu), null, null, null, null);

        if(!c4.moveToFirst())
            fail("Tipos menu no devolvió nada");
        validateCurrentRecord("No coinciden los datos insertados en tipos menu", c4, cvElementos);

        c.close();
        c2.close();
        c3.close();
        c4.close();

        getContext().getContentResolver().delete(ComedoresContract.ComedoresEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(ComedoresContract.PlatosEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(ComedoresContract.TiposMenuEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(ComedoresContract.ElementosEntry.CONTENT_URI, null, null);
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);

            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
}
