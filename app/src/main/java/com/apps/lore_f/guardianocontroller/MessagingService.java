/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apps.lore_f.guardianocontroller;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "->MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> messageData;
        String messageTitle;
        String messageCommand="";
        String messageSender=remoteMessage.getFrom();

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            /*
            recupera i dati contenuti nel corpo del messaggio
            * */
            messageData = remoteMessage.getData();
            messageTitle=messageData.get("title");
            messageCommand=messageData.get("message");

        }

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        /*
        // inizializzo il BroadcastManager
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent("CAMERACONTROL___REMOTE_COMMAND_RECEIVED")
                .putExtra("REMOTE_COMMAND_MESSAGE", remoteMessage.getNotification().getBody());

        broadcastManager.sendBroadcast(intent);
        */

        /*
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        */

        switch(messageCommand){

            case "RESPONSE_FROM_SERVER:::YES_I_AM_ALIVE":

                // inizializzo il BroadcastManager
                LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
                    Intent intent = new Intent("GUARDIANOCONTROLLER___HEARTBEAT_RECEIVED")
                .putExtra("_sender-token", remoteMessage.getFrom());

                broadcastManager.sendBroadcast(intent);

            break;

        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {


//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.eye)
//                .setContentTitle("FCM Message")
//                .setContentText(messageBody)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//

    }

}

