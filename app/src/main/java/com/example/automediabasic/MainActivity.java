package com.example.automediabasic;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.this.getClass().getSimpleName();
    private final String URL = "http://storage.googleapis.com/automotive-media/music.json";
    private Context context = this.getApplicationContext();
    private VolleySingleton volleySingleton = VolleySingleton.getInstance(context);
    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestQueue = volleySingleton.getRequestQueue();
        getMusicalRepertoire();

    }

    private void getMusicalRepertoire() {
        StringRequest request = new StringRequest(Request.Method.GET, URL, onPostsLoaded, onPostsError);
        requestQueue.add(request);
        Log.e(TAG, "request = " + requestQueue);
    }

    private final Response.Listener<String> onPostsLoaded = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Log.i(TAG, response);
        }
    };
    private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
        }
    };

}
