package me.nathan3882.howfast;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.text.Spanned;
import android.view.WindowManager;
import android.widget.ProgressBar;

public class Util {

    public static final String BREAK = "<br>";
    public static final String SPANNABLE_BREAK = "\n";
    private static final String WEB_SERVICE_TOKEN = "aToken";
    public static final String WEB_SERVICE_PARAMS = "?format=json&token=" + WEB_SERVICE_TOKEN;

    public static Spanned html(String string) {
        return Html.fromHtml(string);
    }

    public static void addToArray(String[] array, String val, boolean fill) {
        int length = getArrayLength(array);
        int refIndex = length + 1;
        if (fill) {
            int index = refIndex;
            for (int i = 0; i < length; i++) {
                if (array[i] == null) {
                    index = i;
                    break;
                }
            }
            array[index] = val;
            return;
        }
        array[refIndex] = val;
    }

    public static String upperFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    public static int getArrayLength(String[] array) {
        int length = 0;
        for (String s : array) {
            if (s != null) {
                length++;
            }
        }
        return length;
    }

    public static void updateProgress(Activity reference, ProgressBar bar, Integer progress) {
        bar.setProgress(progress);
        dimBackground(reference.getWindow().getAttributes(), 0.75f);
    }

    public static boolean hasInternet(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void dimBackground(WindowManager.LayoutParams wp, float dimAmount) {
        wp.dimAmount = dimAmount;
    }

    public static String getPrettyMinute(int minute) {
        String prettyMinute = String.valueOf(minute);
        if (minute < 10) prettyMinute = "0" + prettyMinute;
        return prettyMinute;
    }
}
