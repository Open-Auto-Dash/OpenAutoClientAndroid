package com.openautodash.client.bluetooth;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BLEScanner {
    private final static String TAG = "BLEScanner";

    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;

    private final Context context;

    public BLEScanner (Context context){
        this.context = context;
    }

    public void startScanning() {
        Log.d(TAG, "startScanning");
        sendMessageToActivity(String.valueOf(" "));

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

        // Create a ScanFilter for your specific device
//        final List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().setDeviceAddress("04:21:44:A7:41:C7").build());
        List<ScanFilter> filters = new ArrayList<>();
        ParcelUuid serviceUuid = ParcelUuid.fromString("00001101-0000-1000-8000-00805F9B34FB");
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(serviceUuid)
                .build();
        filters.add(filter);

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                int rssi = result.getRssi();
                byte[] scanRecord = result.getScanRecord().getBytes();

                Log.d(TAG, "Device found: " + device.getAddress());
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Log.d(TAG, "  Device Name: " + device.getName());
                Log.d(TAG, "  RSSI: " + rssi);
                Log.d(TAG, "  Scan Record: " + bytesToHex(scanRecord));

                stopScanning(); // we found it so stop it!
                sendMessageToActivity(String.valueOf(rssi));

            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, "Scan failed with error code: " + errorCode);
            }
        };

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        scanner.startScan(filters, settings, scanCallback);
    }

    public void stopScanning() {
        Log.d(TAG, "stopScanning");
        if (scanner != null && scanCallback != null) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            scanner.stopScan(scanCallback);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    private void sendMessageToActivity(String data) {
        Intent intent = new Intent("MyServiceData");
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}