package com.android.red.packet;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

public class NotificationService extends NotificationListenerService {
    public static final String TAG = "_hongbao";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (null != notification) {
            Bundle extras = NotificationCompat.getExtras(notification);
            if (null != extras) {
                List<String> textList = new ArrayList<String>();
                String title = extras.getString(NotificationCompat.EXTRA_TITLE);
                if (!TextUtils.isEmpty(title)) textList.add(title);

                String detailText = extras.getString(NotificationCompat.EXTRA_TEXT);
                if (!TextUtils.isEmpty(detailText)) textList.add(detailText);

                if (textList.size() > 0) {
                    for (String text : textList) {
                        if (!TextUtils.isEmpty(text) && text.contains("[微信红包]")) {
                        	LuckyApplication.unlockScreen(getApplication());
                            final PendingIntent pendingIntent = notification.contentIntent;
                            try {
                            	LuckyApplication.autoGetMoney = true;
                                pendingIntent.send();
                                Log.w(TAG, "打开通知");
                            } catch (PendingIntent.CanceledException e) {
                            	Log.w(TAG, "事件e:"+e.getMessage());
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }
}
