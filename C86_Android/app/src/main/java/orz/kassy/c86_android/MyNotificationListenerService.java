package orz.kassy.c86_android;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import aqkanji2koe.AqKanji2Koe;


public class MyNotificationListenerService extends NotificationListenerService {
    
    private String TAG = "MyNotification";
    private AqKanji2Koe kanji2koe;

    
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        kanji2koe = new AqKanji2Koe();
        super.onCreate();
    }
 
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     *  ステータスバーに通知があった場合に呼ばれる
      */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG,"onNotificationPosted");
        // 通知内容をログに出力する
        showLog(sbn);
    }

    /**
     * ステータスバーから通知が消された場合
     * @param sbn
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationRemoved");
    }

    /**
     * 通知内容をログに出す
     * @param sbn
     */
    private void showLog( StatusBarNotification sbn ){

        // sbnから各種データを取り出し
        int id = sbn.getId();
        String packageName = sbn.getPackageName();
        String tag = sbn.getTag();
        long time = sbn.getPostTime();
        boolean clearable = sbn.isClearable();
        boolean ongoing = sbn.isOngoing();
        CharSequence text = sbn.getNotification().tickerText;

        // ログに表示してみる
        Log.i(TAG,"id:" + id + " time:" +time + " isClearable:" + clearable + " isOngoing:" + ongoing);
        Log.i(TAG,"packageName : " + packageName);
        Log.i(TAG,"tickerText  : " + text);
        Log.i(TAG,"tag         : " + tag);
        Log.i(TAG,"tostring:" + sbn.toString());


        // 受信した結果をBLEに投げてみる
        // いくつかのアプリは特別扱いしよう
        // Gmailの場合
        if("com.google.android.gm".equals(packageName)) {
            Log.i(TAG,"app: Gmail");

            String message1  = "メールが届きました";
            String message1r = AqKanji2Koe.getRomaFromKanji(this, message1);
            // BLEに流し込む
            BluetoothLeService.sendStringToBleDevice(message1r);

            // ローマ字に変換して
            String romaStr = AqKanji2Koe.getRomaFromKanji(this, text.toString());
            // BLEに流し込む
            BluetoothLeService.sendStringToBleDevice(romaStr);
        }
        // Twitterの場合
        else if("com.twitter.android".equals(packageName)) {
            Log.i(TAG,"app: twitter");
            String message1  = "twitterのメンションです";
            String message1r = AqKanji2Koe.getRomaFromKanji(this, message1);
            // BLEに流し込む
            BluetoothLeService.sendStringToBleDevice(message1r);

            // ローマ字に変換して
            String romaStr = AqKanji2Koe.getRomaFromKanji(this, text.toString());
            // BLEに流し込む
            BluetoothLeService.sendStringToBleDevice(romaStr);

        }
        // LINEの場合
        else if("jp.naver.line.android".equals(packageName)) {
            Log.i(TAG,"app: LINE");
            String message1  = "ラインのメッセージです";
            String message1r = AqKanji2Koe.getRomaFromKanji(this, message1);
            // BLEに流し込む
            BluetoothLeService.sendStringToBleDevice(message1r);

            // ローマ字に変換して
            String romaStr = AqKanji2Koe.getRomaFromKanji(this, text.toString());
            // BLEに流し込む
            BluetoothLeService.sendStringToBleDevice(romaStr);
        }
        // その他
        else {
            Log.i(TAG,"app: other");
            // ローマ字に変換して
            String romaStr = AqKanji2Koe.getRomaFromKanji(this, text.toString());
            // BLEに流し込む
            BluetoothLeService.sendStringToBleDevice(romaStr);
        }
    }
}