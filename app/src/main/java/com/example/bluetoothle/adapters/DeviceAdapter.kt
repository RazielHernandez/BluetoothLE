package com.example.bluetoothle.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.bluetoothle.R
import com.example.bluetoothle.model.Device

class DeviceAdapter(context: Context, data: ArrayList<Device>) :
    BaseAdapter() {

    private val dataSet: ArrayList<Device> = data
    var mContext: Context = context

    override fun getCount(): Int {
        return dataSet.size
    }

    override fun getItem(position: Int): Any {
        return dataSet.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun addDevice(device: Device) {
        dataSet.add(device)
    }

    fun cleanDeviceList() {
        dataSet.clear()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView: View? = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                .inflate(R.layout.device_view, parent, false)
        }
        val currentItem = getItem(position) as Device
        val textName: TextView = convertView?.findViewById(R.id.device_name)!!
        val textDescription: TextView = convertView.findViewById(R.id.device_description)!!
        val textDetails: TextView = convertView.findViewById(R.id.device_details)!!

        textName.text = currentItem.deviceName
        textDescription.text = currentItem.deviceDescription
        textDetails.text = currentItem.deviceDetails

        return convertView
    }

    }