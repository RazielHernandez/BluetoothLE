package com.example.bluetoothle

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bluetoothle.adapters.DeviceAdapter
import com.example.bluetoothle.model.Device
import kotlinx.coroutines.coroutineScope


class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private var scanning = false
    private val handler = Handler()
    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    private val deviceAdapater = DeviceAdapter(context = this, data = arrayListOf())


    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val TAG = "BluetoothLE.MainActivity"
        private const val BLUETOOTH_PERMISSION_CODE = 100
        private const val BLUETOOTH_ADMIN_PERMISSION_CODE = 101
        private const val ACCESS_FINE_LOCATION_PERMISSION_CODE = 102
        private const val ACCESS_COARSE_LOCATION_PERMISSION_CODE = 103
        private const val BLUETOOTH_SCAN_CODE = 104
        private const val BLUETOOTH_CONNECT_PERMISSION_CODE = 105
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        findViewById<Button>(R.id.btn_start).setOnClickListener{
            startScanningButton()
        }

        findViewById<ListView>(R.id.bluetooth_list).adapter = deviceAdapater
    }

    private fun startScanningButton() {

        val permisionList : MutableList<String> = ArrayList()
        if (!checkPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            permisionList.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (!checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            permisionList.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permisionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permisionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (permisionList.isNotEmpty()) {
            ActivityCompat.requestPermissions(this@MainActivity,
                permisionList.toTypedArray(), BLUETOOTH_PERMISSION_CODE)
        } else {
            startScanning()
        }

    }

    private fun startScanning() {
        Log.d(TAG, "Start scanning...")
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        //val scanFilters = listOf<ScanFilter>()
        //val scanSettings = ScanSettings.Builder().build()

        Log.d(TAG, "Scanning...")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "NO PERMISSION")
            return
        }
        //scanner?.startScan(scanFilters, scanSettings, leScanCallback)
        //scanner?.startScan(leScanCallback)

        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                Log.d(TAG, "Stop scanning 1")
                scanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            scanner?.startScan(leScanCallback)
        } else {
            Log.d(TAG, "Stop scanning 2")
            scanning = false
            scanner?.stopScan(leScanCallback)
        }
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d(TAG, "leScanCallback")
            super.onScanResult(callbackType, result)
            result?.device?.let { device ->
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.d(TAG, "No permissions")
                    return
                }

                Log.d(TAG, "Device found: ${device.name} - ${device.address}")
                var newDevice: Device =  Device()

                newDevice.deviceName = device.name ?: "Unknown"
                newDevice.deviceDescription = device.address
                newDevice.deviceDetails = device.type.toString()
                deviceAdapater.addDevice(newDevice)
                deviceAdapater.notifyDataSetChanged()
                //device.connectGatt(this@MainActivity, false, gattCallback)
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server.")
                    if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Log.d(TAG, "No permissions")
                        return
                    }
                    gatt?.discoverServices()
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server.")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt?.services?.forEach { service ->
                    Log.d(TAG, "Service discovered: ${service.uuid}")
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristic?.value?.let { value ->
                    Log.d(TAG, "Characteristic read: ${value.joinToString()}")
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic written successfully")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let { value ->
                Log.d(TAG, "Characteristic changed: ${value.joinToString()}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            startScanning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "No permissions")
            return
        }
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    // Function to check and request permission.
    private fun requestPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT).show()
            startScanning()
        } else {
            Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}