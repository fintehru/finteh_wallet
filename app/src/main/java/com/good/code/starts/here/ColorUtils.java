package com.good.code.starts.here;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.preference.PreferenceManager;

public class ColorUtils {

    public static int getMainColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("color", Color.parseColor("#303f9f"));
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

}
