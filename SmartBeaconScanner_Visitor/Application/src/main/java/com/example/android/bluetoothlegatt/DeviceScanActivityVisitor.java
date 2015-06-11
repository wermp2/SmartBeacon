/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The Smart Beacon App is based on the BluetoothLeGatt sample App
 * https://developer.android.com/samples/BluetoothLeGatt/index.html
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.ListActivity;
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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivityVisitor extends ListActivity {

    private SmartBeaconListAdapter mSmartBeaconListAdapter;
    private ArrayList<SmartBeacon> smartBeacons = new ArrayList<>();
    private ArrayList<SmartBeacon> completeSmartBeacons = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScanner;
    private boolean mScanning;
    private boolean doScan;
    private Handler mHandler;

    private final String TAG = "BLE Scanner: ";
    private final int MAXRSSI = -90;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private ScanSettings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();


        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

        ScanSettings.Builder builder = new ScanSettings.Builder().setScanMode(2);
        settings = builder.build();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mSmartBeaconListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        mSmartBeaconListAdapter = new SmartBeaconListAdapter();
        setListAdapter(mSmartBeaconListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mSmartBeaconListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //we dont care
    }

    private void scanLeDevice(final boolean enable) {
        if (enable)
        {
            mScanning = true;
            completeSmartBeacons.clear();
            mBLEScanner.startScan(null, settings, mLeScanCallback);
        } else {
            mScanning = false;
            mBLEScanner.stopScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    // Adapter for holding devices found through scanning.
    private class SmartBeaconListAdapter extends BaseAdapter {
        private ArrayList<SmartBeacon> mSmartBeacons;
        private LayoutInflater mInflator;

        public SmartBeaconListAdapter() {
            super();
            mSmartBeacons = new ArrayList<SmartBeacon>();
            mInflator = DeviceScanActivityVisitor.this.getLayoutInflater();
        }


        //Adds a new smart beacon to the list of smart beacons for which we scan for advertisements
        public void addSmartBeacon(SmartBeacon smartBeacon)
        {
            if (mSmartBeacons.contains(smartBeacon)) {
                mSmartBeacons.remove(smartBeacon);
            }
            mSmartBeacons.add(smartBeacon);
        }

        public void clear()
        {
            mSmartBeacons.clear();
        }

        @Override
        public int getCount()
        {
            return mSmartBeacons.size();
        }

        @Override
        public Object getItem(int position) {
            return  mSmartBeacons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            SmartBeacon smartBeacon = mSmartBeacons.get(i);

            String currentAdvertisement = null;

            if(smartBeacon != null) {
                try {
                    currentAdvertisement = smartBeacon.getUserFriendlyAdvertisement();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (currentAdvertisement != null && currentAdvertisement.length() > 0)
                    viewHolder.deviceName.setText(currentAdvertisement);
                else
                    viewHolder.deviceName.setText(R.string.unknown_device);

                viewHolder.deviceAddress.setText(smartBeacon.getDevice().getAddress());
            }

            return view;
        }
    }

    // Device scan callback.
    private  android.bluetooth.le.ScanCallback mLeScanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult (int callbackType, ScanResult result)
                {
                    byte[] manufacData= result.getScanRecord().getManufacturerSpecificData().get(76);

                    if(manufacData == null)
                    {
                        return;
                    }

                    boolean isIBeacon = manufacData[0] == 2;
                    SmartBeacon currentResult = null;

                    if(result.getRssi() < MAXRSSI || !isIBeacon || (getSmartBeaconFromList(completeSmartBeacons, result.getDevice().getAddress()) != null))
                        return;

                   //smartBeacon device?
                    if (isSmartBeacon(result.getScanRecord().getDeviceName())) {

                        boolean isRegisteredSmartBeacon = false;
                        SmartBeacon resultBeacon = getSmartBeaconFromList(smartBeacons, result.getDevice().getAddress());

                        SmartBeacon removeBeacon = null;

                        //Smart Beacon is already in list
                        if (resultBeacon != null) {
                            isRegisteredSmartBeacon = true;
                            Log.d("Found reg. Beacon: ", result.getDevice().getAddress());
                            currentResult = resultBeacon;

                            try {
                                resultBeacon.addPart(new JSONObject(result.getScanRecord().getDeviceName()), result.getTimestampNanos());
                                System.out.println("Parts: " + resultBeacon.getRecordsSize());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if ((resultBeacon.isScanComplete())) {
                                completeSmartBeacons.add(resultBeacon);
                                removeBeacon = resultBeacon;
                            }

                            if (resultBeacon.isScanInterrupted()) {
                                //Delete old advertisements
                                resultBeacon.clearAdvertisements();
                                //Add new advertisement
                                try {
                                    resultBeacon.addPart(new JSONObject(result.getScanRecord().getDeviceName()), result.getTimestampNanos());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (isRegisteredSmartBeacon) {
                            //If all the parts of every beacon advertisement we currently scan for are collected, we can stop scanning for more.

                            if(removeBeacon != null)
                            {
                                smartBeacons.remove(removeBeacon);
                            }

                            if (smartBeacons.size() == 0)
                            {
                                doScan = false;
                                scanLeDevice(false);
                            }

                        } else
                        {
                            SmartBeacon newBeacon = new SmartBeacon(result.getDevice());
                            currentResult = newBeacon;
                            Log.d("Found new SmartBeacon: ", result.getDevice().getAddress());
                            try {
                                newBeacon.addPart(new JSONObject(result.getScanRecord().getDeviceName()), result.getTimestampNanos());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            smartBeacons.add(newBeacon);

                            doScan = true;
                        }

                        final SmartBeacon finalCurrentResult = currentResult;
                        System.out.println("SmartBeacons in list: " + smartBeacons.size());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSmartBeaconListAdapter.addSmartBeacon(finalCurrentResult);
                                mSmartBeaconListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
        };



    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    //Used to check if a advertisement part contains a valid json formatted string
    private boolean isValidJSONFormat(String json)
    {
        try
        {
            new JSONObject(json);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    //Used to check if a found advertisement belongs to a smart beacon
    private boolean isSmartBeacon(String advertisement)
    {
        Pattern p = Pattern.compile("\\{\"\\d*\\d\":\\{(.*)");
        Matcher m = p.matcher(advertisement);

        Pattern p2 = Pattern.compile("\\{\"\\d*\\d\":(.*)\\}");
        Matcher m2 = p2.matcher(advertisement);

        if((m.find() || m2.find()) && isValidJSONFormat(advertisement))
        {
            return true;
        }
        return false;
    }


    //Gets a smart beacon with given mac address from the list of smart beacons we collect advertisements from
    private SmartBeacon getSmartBeaconFromList(ArrayList<SmartBeacon> list, String address)
    {
        SmartBeacon beacon = null;

        for(SmartBeacon sb : list)
        {
            if(sb.getDevice().getAddress().equals(address))
            {
                return beacon = sb;
            }
        }
        return beacon;
    }
}