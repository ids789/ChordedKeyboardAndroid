package com.ids789.chordedkeyboard;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

public class ChordedKeyboardService extends InputMethodService {
    GridView switchGridView;
    GridView keyGridView;
    TextView disconnectedText;
    InputConnection ic;

    View v;

    private BluetoothLeScanner bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private boolean mScanning;
    private Handler handler = new Handler();

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    boolean deviceFound = false;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    ChordHandler ch = new ChordHandler();

    @Override
    public View onCreateInputView() {
        v = getLayoutInflater().inflate(R.layout.keyboard_view, null);

        switchGridView = (GridView)v.findViewById(R.id.switch_gridview);
        keyGridView = (GridView)v.findViewById(R.id.key_gridview);
        disconnectedText = (TextView)v.findViewById(R.id.disconnected_text);

        ch.loadChordMap(getResources().getXml(R.xml.chord_map));
        updateReference();

        Log.v("CHORD", "Keyboard Starting");

        // Check if the bluetooth is switched on
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            disconnectedText.setVisibility(View.VISIBLE);
            disconnectedText.setText(getString(R.string.bluetooth_disabled));
        }
        else {
            scanBluetooth();
        }

        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        return v;
    }

    // Once a chorded keyboard device has been found, connect to it
    private void connectDevice(BluetoothDevice device) {
        BluetoothGatt gatt;
        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.v("BLE", "BLE DEVICE STATE: " + newState);
                if (newState == STATE_CONNECTED){
                    gatt.discoverServices();
                    Log.v("BLE", "BLE DEVICE SERVICES: " + gatt.getServices());
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            switchGridView.setVisibility(View.VISIBLE);
                            keyGridView.setVisibility(View.VISIBLE);
                            disconnectedText.setVisibility(View.GONE);
                        }
                    });
                }
                else if (newState == STATE_DISCONNECTED){
                    gatt.close();
                    gatt.disconnect();
                    Log.v("BLE", "BLE DEVICE DISCONNECTED");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            switchGridView.setVisibility(View.GONE);
                            keyGridView.setVisibility(View.GONE);
                            disconnectedText.setText(getString(R.string.keyboard_disconnected));
                            disconnectedText.setVisibility(View.VISIBLE);
                            deviceFound = false;
                        }
                    });
                }
            }

            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.w("BLE", "DISCOVERED SERVICE: " + gatt.getService(UUID.fromString(getString(R.string.ble_service_uuid))).getCharacteristic(UUID.fromString(getString(R.string.ble_charactoristic_uuid))));
                    BluetoothGattCharacteristic bleChar = gatt.getService(UUID.fromString(getString(R.string.ble_service_uuid))).getCharacteristic(UUID.fromString(getString(R.string.ble_charactoristic_uuid)));
                    BluetoothGattDescriptor descriptor = bleChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                    gatt.setCharacteristicNotification(bleChar, true);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) { };

            @Override
            public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                final byte[] data = characteristic.getValue();
                Log.w("BLE", "BLE CHAR CHANGED: " + data[0]);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        handleChord(data[0]);
                    }
                });
            };
        };
        gatt = device.connectGatt(this, true, gattCallback);

        bluetoothLeScanner.stopScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
            }
        });
    }


    // Scan bluetooth for the chorded keyboard device
    void scanBluetooth() {
        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(getString(R.string.ble_service_uuid))).build());

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();

        Log.v("CHORD", "Starting bluetooth scan");
        bluetoothLeScanner.startScan(filters, settings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                if (!deviceFound) {
                    Log.v("CHORD", "BLE SCAN RESULT: " + result.getDevice() + " (" + result.getScanRecord().getServiceUuids() + ")");
                    connectDevice(result.getDevice());
                    deviceFound = true;

                    Log.v("CHORD", "Connected to Bluetooth Device");
                }
                else
                    Log.v("CHORD", "Found Another Bluetooth Device");
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.v("CHORD", "BLE Scan Failed: " + errorCode);
            }
        });
    }


    // Handle if the user switches bluetooth ON or OFF
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // It means the user has changed his bluetooth state.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                if (btAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                    // The user bluetooth is turning off yet, but it is not disabled yet.
                    Log.v("CHORD", "Bluetooth is switching OFF");
                }

                else if (btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    // The user bluetooth is already disabled.
                    Log.v("CHORD", "Bluetooth is switched OFF");
                    deviceFound = false;
                }

                else if (btAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    // The user bluetooth is already disabled.
                    Log.v("CHORD", "Bluetooth is switched ON");

                    bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
                    btAdapter = BluetoothAdapter.getDefaultAdapter();

                    scanBluetooth();
                }
            }
        }
    };

    void handleChord(byte chord) {
        Log.v("CHORD", "GOT: " + chord);

        ChordAction action = ch.processChord(chord);
        updateReference();

        if (action != null && action.action.equals("key")) {
            ic = getCurrentInputConnection();
            if (ic == null) {
                Log.v("CHORD", "InputConnection was null");
                return;
            }
            switch (action.value) {
                case "backspace":
                    CharSequence selectedText = ic.getSelectedText(0);
                    if (TextUtils.isEmpty(selectedText)) {
                        // no selection, so delete previous character
                        ic.deleteSurroundingText(1, 0);
                    } else {
                        // delete the selection
                        ic.commitText("", 1);
                    }
                    break;
                case "delete":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    break;
                case "newline":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER));
                    break;
                case "tab":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_TAB));
                    break;
                case "left":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_LEFT));
                    break;
                case "right":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT));
                    break;
                case "up":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_UP));
                    break;
                case "down":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_DOWN));
                    break;
                case "home":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
                    break;
                case "end":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END));
                    break;
                case "space":
                    ic.commitText(" ", 1);
                    break;
                case "escape":
                    ic.sendKeyEvent(new KeyEvent(android.view.KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
                    break;
                default:
                    ic.commitText(action.value, 1);
            }
        }
    }

    // Update the chords shown in the reference GridView
    void updateReference() {
        ChordAction[] chords = ch.availableChords();
        List<ChordAction> switchChords = new ArrayList<ChordAction>();
        List<ChordAction> keyChords = new ArrayList<ChordAction>();
        for (int i = 0; i < chords.length; i++) {

            if (chords[i].action.equals("switch")) {
                if (ch.currentSet.contains(chords[i].value))
                    switchChords.add(new ChordAction(chords[i].trigger, chords[i].name, "current-group", chords[i].value));
                else
                    switchChords.add(chords[i]);
            }
            else if (chords[i].action.equals("key")) {
                keyChords.add(chords[i]);
            }
        }

        ReferenceAdapter switchAdapter = new ReferenceAdapter(v.getContext(), switchChords.toArray(new ChordAction[0]));
        ReferenceAdapter keysAdapter = new ReferenceAdapter(v.getContext(), keyChords.toArray(new ChordAction[0]));

        switchGridView.setAdapter(switchAdapter);
        keyGridView.setAdapter(keysAdapter);
    }
}