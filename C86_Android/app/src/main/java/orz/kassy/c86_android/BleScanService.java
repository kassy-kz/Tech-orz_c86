package orz.kassy.c86_android;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


/**
 * バックグラウンドでBLEスキャン-> 接続を行うためのサービス
 * DeviceScanActivityとどちらかを用いる
 */
public class BleScanService extends Service {

    private static final String TAG = "BleScanService";
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private BluetoothLeService mBluetoothLeService;
    private static BleScanService sSelf = null;
    private BluetoothDevice mDevice;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
        sSelf = this;
        mHandler = new Handler();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        // BLEサポートチェック
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand");
        // Bluetooth機能が有効になっているかのチェック。無効の場合はダイアログを表示して有効をうながす。(intentにて)
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
            }
        }
        scanLeDevice(true);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sSelf = null;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            Log.i(TAG,"startLeScan");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG,"onLeScan");
            // BLESerialを見つけた時
            if(device.getName().equals("BLESerial")){
                Log.i(TAG,"found BLESerial");
                mDevice = device;
                // BluetoothLeServiceに接続する
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }

                // サービス接続（BluetoothLeService）
                Intent gattServiceIntent = new Intent(sSelf, BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            }
        }
    };


    /**
     * BluetoothLeServiceとのサービスコネクション
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG,"onServiceConnected");

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                stopSelf();
            }

            // BTアダプタへの参照の初期化が成功したら、接続動作を開始
            mBluetoothLeService.connect(mDevice.getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG,"onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };


    // BLEサービスからのイベントを処理
    // ACTION_GATT_CONNECTED: GATT server　接続.
    // ACTION_GATT_DISCONNECTED: GATT server　切断.
    // ACTION_GATT_SERVICES_DISCOVERED: GATT services　取得.
    // ACTION_DATA_AVAILABLE: 受信データあり.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            // GATT接続時
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                MyUtils.showNotification(context, R.drawable.ic_launcher,
                        getResources().getString(R.string.gatt_connected_title),
                        getResources().getString(R.string.gatt_connected_body));
            }
            // GATT切断時
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                MyUtils.showNotification(context, R.drawable.ic_launcher,
                        getResources().getString(R.string.gatt_disconnected_title),
                        getResources().getString(R.string.gatt_disconnected_body));
            }
            // GATTサービス一覧取得時
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            }
            // データ受信時
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                MyUtils.showNotification(context, R.drawable.ic_launcher,
                        getResources().getString(R.string.gatt_disconnected_title),
                        data);
            }
        }
    };

    /**
     * Gattアクションフィルタ
     * @return
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}