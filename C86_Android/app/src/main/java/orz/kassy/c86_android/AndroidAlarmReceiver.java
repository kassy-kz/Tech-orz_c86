package orz.kassy.c86_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import aqkanji2koe.AqKanji2Koe;

public class AndroidAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AndroidAlarmReceiver";

    /**
     * onReceive
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 標準アラームアプリのアラームが鳴った
        if ("com.android.deskclock.ALARM_ALERT".equals(intent.getAction())) {
            Log.i(TAG,"alarm fire");

            // メイドガジェットに起こしてもらう。
            String kanjiStr = "朝です、起床の時間です。";
            String romaStr = AqKanji2Koe.getRomaFromKanji(context, kanjiStr);
            BluetoothLeService.sendStringToBleDevice(romaStr);
        }
    }
}
