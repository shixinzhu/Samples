package com.concierge.salesforce.hybrid.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.concierge.salesforce.hybrid.MainActivity;
import com.concierge.salesforce.hybrid.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Iterator;
import java.util.Map;

public class PushNotificationService extends FirebaseMessagingService {

    String TAG = "PushNotificationService";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {

        }

        Map<String, String> data = remoteMessage.getData();
        String title = "title", body = "";

        ObjectMapper objectMapper = new ObjectMapper();

        Iterator<Map.Entry<String, String>> iterator = data.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            Log.w(TAG, "#### <<<< receive >>>> Key :: " + entry.getKey() + ", Value :: " + entry.getValue());

            if ("appName".equals(entry.getKey())) {
                title = entry.getValue();
                Log.w("SEKI", ">>>> receive title :: " + title);
            } else {
                try {
                    if ("payload".equals(entry.getKey())) {
                        Payload payload = objectMapper.readValue(entry.getValue(), Payload.class);
                        Log.w("SEKI", ">>>> receive sendNotification payload ");
                        sendNotification(payload);
                    }
                } catch (JsonProcessingException exception) {
                    Log.e(TAG, ">>>> receive sendNotification payload :: " + exception.getMessage());
                }
            }

//        String messageBody = "";
//
//        if (remoteMessage.getNotification() != null) {
//            messageBody = remoteMessage.getNotification().getBody();
//        }

            // sendNotification(messageBody);
        }
    }

    private void sendNotification(Payload payload) {

        Log.d(TAG, "#### <<<< >>>> receive sendNotification ");

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "001";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.sf__icon)
                .setContentTitle(payload.getAlertTitle())
                .setContentText(payload.getAlertBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "$$$$$$", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
