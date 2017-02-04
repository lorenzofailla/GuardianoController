package com.apps.lore_f.guardianocontroller;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by lore_f on 04/02/2017.
 */

public class Messaging {

    private static final String TAG = "->Messaging";

    private static OkHttpClient okHttpClient = new OkHttpClient();

    public static void sendMessage(final String recipient, final String message, final String sender) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {

                    return getResponseFromMessagingServer(
                            "http://lorenzofailla.esy.es/Guardiano/Messaging/sendmessage.php?Action=M&t=title&m="+message+"&r="+recipient+"&sender="+sender);

                    // TODO: 29/01/2017 implementare, sia lato server che lato client, il time to live del messaggio

                } catch (Exception e) {

                    e.printStackTrace();
                    return null;

                }

            }

            @Override
            protected void onPostExecute(String result) {
                try {

                    if(result!=null) {

                        JSONObject resultJson = new JSONObject(result);
                        int success, failure;
                        success = resultJson.getInt("success");
                        failure = resultJson.getInt("failure");

                        Log.d(TAG, "got response from messaging server: " + success + " success, " + failure + " failure");

                    } else {

                        Log.d(TAG, "got NULL response from messaging server");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private static String getResponseFromMessagingServer(String requestUrl) throws IOException {

        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        return response.body().string();

    }

}
