package orz.kassy.c86_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

public class WifiEventReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiEventReceiver";
    private static NotificationManager sNotificationMgr;
    private static final String MY_ACCESSPOINT_SSID = "nebulosity2";
    
    /**
     * onReceive
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // WiFi STATE CHANGED
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            logToast(context, "NETWORK_STATE_CHANGED_ACTION");

            Bundle extras = intent.getExtras();
            if (extras == null) {
                return;
            }

            NetworkInfo nwInfo = extras.getParcelable(WifiManager.EXTRA_NETWORK_INFO);
            if (nwInfo == null) {
                return;
            }

            /**
             * WiFiアクセスポイントに接続した場合はここを通る
             *  connectedの場合のみEXTRA_WIFI_INFOとEXTRA_BSSIDが取得可能
             */
            if(nwInfo.isConnected()) {
                WifiInfo wInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                String ssid = wInfo.getSSID();
                logToast(context, "SSID = " + wInfo.getSSID());

                // SSID名の前後にダブルコーテーションが必要なので注意
                if(ssid.equals("\"" + MY_ACCESSPOINT_SSID + "\"")) {
                    // BLEスキャン開始
                    startBleService(context);
                }
            }
            // WiFiアクセスポイント切断した場合
            else {
                stopBleService(context);
            }
        }

        // RSSIが変化した時のイベント。家の構造やAPの配置によってはこれも使う。
        else if (WifiManager.RSSI_CHANGED_ACTION.equals(intent.getAction())) {
            logToast(context, "RSSI_CHANGED_ACTION");
        }
    }

    /**
     * 帰宅を検知 -> BLEScanを開始するメソッド
     * @param context
     */
    private void startBleService(Context context) {
        // 帰宅を検知した旨をNotificationで通知してみる
        MyUtils.showNotification(context,
                         R.drawable.ic_launcher,
                         context.getResources().getString(R.string.msg_ap_connect_title),
                         context.getResources().getString(R.string.msg_ap_connect_body));

        // BLE接続サービス立ち上げ（Activityを使用しない）
        Intent bleScan = new Intent(context, BleScanService.class);
        context.startService(bleScan);
    }


    /**
     * wifi切断 -> BLEScanServiceを止める
     * @param context
     */
    private void stopBleService(Context context) {
        // 帰宅を検知した旨をNotificationで通知してみる
        MyUtils.showNotification(context,
                R.drawable.ic_launcher,
                context.getResources().getString(R.string.msg_ap_disconnect_title),
                context.getResources().getString(R.string.msg_ap_disconnect_body));

        // BLE接続サービス立ち上げ（Activityを使用しない）
        Intent bleScan = new Intent(context, BleScanService.class);
        context.stopService(bleScan);
    }

    /**
     * ログ吐き用のメソッド
     * @param context
     * @param text
     */
    private void logToast(Context context, String text) {
        Log.i(TAG,text);
//        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
