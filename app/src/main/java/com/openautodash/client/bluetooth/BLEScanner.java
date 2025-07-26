package com.openautodash.client.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BLEScanner {
    private static final String TAG = "BLEScanner";

    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("3de187e2-5864-435e-b11b-e1e04ab27579");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Modified RSSI thresholds and timing constants
    private static final int INITIAL_RSSI_THRESHOLD = -95; // Initial connection threshold
    private static final long FAST_SCAN_INTERVAL = 1000; // 1 second
    private static final long SLOW_SCAN_INTERVAL = 10000; // 10 seconds
    private static final long FAST_SCAN_DURATION = 3600000; // 1 hour
    private static final long RECONNECT_DELAY = 5000; // 5 seconds

    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Handler rssiUpdateHandler = new Handler(Looper.getMainLooper());
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private ScanCallback scanCallback;

    private String lastConnectedDeviceAddress;
    private boolean isConnected = false;
    private boolean isScanning = false;
    private long connectionStartTime;

    // RSSI update runnable
    private final Runnable rssiUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (isConnected) {
                requestRssiUpdate();
                long scanInterval = shouldUseFastScan() ? FAST_SCAN_INTERVAL : SLOW_SCAN_INTERVAL;
                rssiUpdateHandler.postDelayed(this, scanInterval);
            }
        }
    };

    public BLEScanner(Context context) {
        this.context = context;
        initializeBluetooth();
    }

    private void initializeBluetooth() {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    @SuppressLint("MissingPermission")
    public void startScanning() {
        if (isScanning || isConnected) {
            Log.d(TAG, "Already scanning or connected");
            return;
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not enabled");
            return;
        }

        scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (scanner == null) {
            Log.e(TAG, "Scanner not available");
            return;
        }

        Log.d(TAG, "Starting scan...");
        isScanning = true;

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID.toString()))
                .build());

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                handleScanResult(result);
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, "Scan failed: " + errorCode);
                isScanning = false;
                retryScanning();
            }
        };

        try {
            scanner.startScan(filters, settings, scanCallback);
            sendMessageToActivity("scanning");
        } catch (Exception e) {
            Log.e(TAG, "Error starting scan", e);
            isScanning = false;
            retryScanning();
        }
    }

    @SuppressLint("MissingPermission")
    private void handleScanResult(ScanResult result) {
        BluetoothDevice device = result.getDevice();
        int rssi = result.getRssi();

        Log.d(TAG, String.format("Found device: %s, RSSI: %d", device.getAddress(), rssi));
        sendMessageToActivity("RSSI: " + rssi);

        if (rssi > INITIAL_RSSI_THRESHOLD || device.getAddress().equals(lastConnectedDeviceAddress)) {
            if (!isConnected) {
                stopScanning();
                connectToDevice(device);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        Log.d(TAG, "Connecting to: " + device.getAddress());
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
        lastConnectedDeviceAddress = device.getAddress();
    }

    private boolean shouldUseFastScan() {
        return System.currentTimeMillis() - connectionStartTime < FAST_SCAN_DURATION;
    }

    @SuppressLint("MissingPermission")
    private void requestRssiUpdate() {
        if (bluetoothGatt != null) {
            bluetoothGatt.readRemoteRssi();
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Connection state change error: " + status);
                handleConnectionFailure();
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server");
                isConnected = true;
                connectionStartTime = System.currentTimeMillis();
                rssiUpdateHandler.post(rssiUpdateRunnable);
                sendMessageToActivity("connected");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server");
                handleDisconnection();
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        enableNotifications(gatt);
                    }
                }
            } else {
                Log.e(TAG, "Service discovery failed: " + status);
                handleConnectionFailure();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                String data = new String(characteristic.getValue());
                Log.d(TAG, "Received: " + data);
                sendMessageToActivity(data);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "RSSI Update: " + rssi);
                sendMessage("RSSI:" + rssi);
                sendMessageToActivity("connected " + rssi);
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void enableNotifications(BluetoothGatt gatt) {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

    private void handleConnectionFailure() {
        isConnected = false;
        disconnectGatt();
        retryScanning();
    }

    private void handleDisconnection() {
        isConnected = false;
        sendMessageToActivity("disconnected");
        disconnectGatt();
        retryScanning();
    }

    @SuppressLint("MissingPermission")
    private void disconnectGatt() {
        rssiUpdateHandler.removeCallbacks(rssiUpdateRunnable);
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
            characteristic = null;
        }
    }

    private void retryScanning() {
        handler.postDelayed(() -> {
            if (!isConnected && !isScanning) {
                startScanning();
            }
        }, RECONNECT_DELAY);
    }

    @SuppressLint("MissingPermission")
    public void stopScanning() {
        if (scanner != null && scanCallback != null && isScanning) {
            try {
                scanner.stopScan(scanCallback);
                Log.d(TAG, "Scan stopped");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping scan", e);
            }
        }
        isScanning = false;
    }

    public void cleanup() {
        stopScanning();
        handler.removeCallbacksAndMessages(null);
        rssiUpdateHandler.removeCallbacks(rssiUpdateRunnable);
        disconnectGatt();
        isConnected = false;
        isScanning = false;
    }

    @SuppressLint("MissingPermission")
    public void sendMessage(String message) {
        if (characteristic != null && bluetoothGatt != null && isConnected) {
            try {
                characteristic.setValue(message.getBytes());
                bluetoothGatt.writeCharacteristic(characteristic);
            } catch (Exception e) {
                Log.e(TAG, "Error sending message", e);
                handleConnectionFailure();
            }
        }
    }

    // Methods for sending different types of messages
    public void sendLocationPin(double latitude, double longitude, String label) {
        String message = String.format("PIN:%f,%f,%s", latitude, longitude, label);
        sendMessage(message);
    }

    public void sendVehicleCommand(String command, String... params) {
        StringBuilder message = new StringBuilder("CMD:");
        message.append(command);
        for (String param : params) {
            message.append(",").append(param);
        }
        sendMessage(message.toString());
    }

    private void sendMessageToActivity(String data) {
        Intent intent = new Intent("MyServiceData");
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}