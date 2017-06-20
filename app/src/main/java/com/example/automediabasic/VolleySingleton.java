package com.example.automediabasic;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by usuwi on 20/06/2017.
 */

public class VolleySingleton {

    private Context context;
    private static RequestQueue requestQueue;
    private static VolleySingleton instance;

    public static VolleySingleton getInstance(Context context) {
        if (instance == null) {
            synchronized (VolleySingleton.class) {
                if (instance == null) {
                    instance = new VolleySingleton(context);
                    instance.initialize();
                }

            }
        }

        return instance;
    }

    private void initialize() {
        requestQueue = Volley.newRequestQueue(context);

    }

    private VolleySingleton(Context context) {
        this.context = context;

    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }

}
