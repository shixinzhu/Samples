package com.concierge.salesforce.hybrid.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.concierge.salesforce.hybrid.MainActivity;
import com.concierge.salesforce.hybrid.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.androidsdk.push.PushNotificationInterface;

import java.util.Iterator;
import java.util.Map;

public class MyPushNotificationInterface implements PushNotificationInterface {

    private Context context;

    public MyPushNotificationInterface(Context context) {
        this.context = context;
    }

    private static final String TAG = "MyPushNotification";

    @Override
    public void onPushMessageReceived(Map<String, String> data) {
        Iterator<Map.Entry<String, String>> iterator = data.entrySet().iterator();

        String title = "title", body = "";

        ObjectMapper objectMapper = new ObjectMapper();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            Log.w(TAG, "#### <<<< receive >>>> Key :: " + entry.getKey() + ", Value :: " + entry.getValue());

            if ("appName".equals(entry.getKey())) {
                title = entry.getValue();
            }

            try {
                if ("payload".equals(entry.getKey())) {
                    Payload payload = objectMapper.readValue(entry.getValue(), Payload.class);
                    sendNotification(payload);
                }
            } catch (JsonProcessingException exception) {

            }
        }

        Log.d(TAG, data.toString());
    }

    @Override
    public void onPushMessageReceived(Bundle message) {
        Log.d(TAG, "#### <<<< >>>> receive " + message.toString());
    }

    private void sendNotification(Payload payload) {

        Log.d(TAG, "#### <<<< >>>> receive sendNotification ");

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "001";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.sf__icon)
                .setContentTitle(payload.getAlertTitle())
                .setContentText(payload.getAlertBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "$$$$$$", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
