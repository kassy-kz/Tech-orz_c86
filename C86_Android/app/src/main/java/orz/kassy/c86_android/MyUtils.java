package orz.kassy.c86_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;

/**
 * ユーティリティクラス
 */
public class MyUtils {

    /**
     * 通知を表示するだけの関数
     * @param context
     * @param iconResource
     * @param titleText
     * @param contentText
     */
    public static void showNotification(Context context,
                                         int iconResource,
                                         String titleText,
                                         String contentText) {
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(iconResource)
                        .setContentTitle(titleText)
                        .setContentText(contentText);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }


    /**
     * 通知を表示するだけの関数
     * @param context
     * @param iconResource
     * @param titleText
     * @param contentText
     */
    public static void showSubNotification(Context context,
                                        int iconResource,
                                        String titleText,
                                        String contentText) {
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(iconResource)
                        .setContentTitle(titleText)
                        .setContentText(contentText);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(2, mBuilder.build());
    }

    /**
     * 通知を表示するだけの関数
     * @param context
     * @param iconResource
     * @param titleText
     * @param contentText
     */
    public static void showSubSubNotification(Context context,
                                           int iconResource,
                                           String titleText,
                                           String contentText) {
        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(iconResource)
                        .setContentTitle(titleText)
                        .setContentText(contentText);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(3, mBuilder.build());
    }
}
