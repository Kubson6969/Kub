package com.kubson.mmoclasses.util;

import com.kubson.mmoclasses.api.Stat;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class Format {

    private static final DecimalFormat NUMBER = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private Format() {
    }

    public static String number(double value) {
        return NUMBER.format(value);
    }

    public static String stat(Stat stat, double value) {
        return number(value) + (stat.isPercent() ? "%" : "");
    }

    /** Barra de progreso de texto, p. ej. "■■■■□□□□□□". */
    public static String bar(double fraction, int length) {
        int filled = (int) Math.round(Math.max(0, Math.min(1, fraction)) * length);
        return "■".repeat(filled) + "□".repeat(length - filled);
    }
}
