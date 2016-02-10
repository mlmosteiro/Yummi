package com.example.android.yummi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Clase con utilidades de apoyo
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 07/02/2016.
 */
public class Utility {
    public static long fechaHoy() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long fechaAnteayer() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DAY_OF_YEAR, -2);
        return c.getTimeInMillis();
    }

    public static long normalizarFecha(String fecha) {
        try {
            SimpleDateFormat dF = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dF.parse(fecha);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static long normalizarHora(String hora) {
        try {
            SimpleDateFormat dF = new SimpleDateFormat("HH:mm:ss");
            Date date = dF.parse(hora);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String denormalizarFecha(long fecha) {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(fecha));
    }

    public static String denormalizarHora(long hora) {
        return new SimpleDateFormat("HH:mm").format(new Date(hora));
    }
}
