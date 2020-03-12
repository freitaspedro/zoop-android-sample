package com.example.zoopclientsample

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import com.zoop.zoopandroidsdk.TerminalListManager
import com.zoop.zoopandroidsdk.commons.ZLog
import com.zoop.zoopandroidsdk.terminal.DeviceSelectionListener
import kotlinx.android.synthetic.main.activity_config_pinpad.*
import org.json.JSONObject
import java.util.*

class ConfigPinPadActivity : BaseActivity() , DeviceSelectionListener {

    var terminalListManager: TerminalListManager? = null
    var adapter: SimpleAdapter? = null
    var lv: ListView? = null
    var arrayListZoopTerminalsListForUI: ArrayList<HashMap<String, Any>>? =
        null
    var iSelectedDeviceIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_pinpad)
        lv = findViewById<ListView>(R.id.listViewAvailableTerminals)
        setupListView()
        observeListView()
        terminalListManager = TerminalListManager(this, applicationContext)
        terminalListManager!!.startTerminalsDiscovery()
    }

    private fun setupListView() {
        lv!!.choiceMode = ListView.CHOICE_MODE_SINGLE
        arrayListZoopTerminalsListForUI = ArrayList<HashMap<String, Any>>()
        adapter = SimpleAdapter(this,
            arrayListZoopTerminalsListForUI,
            R.layout.item_list_terminals,
            arrayOf("name", "dateTimeDetected"),
            intArrayOf(R.id.textViewTerminalName, R.id.textViewDateTimeDetected))
        lv!!.adapter = adapter
    }

    private fun updateListView() {
        adapter = SimpleAdapter(this,
            arrayListZoopTerminalsListForUI,
            R.layout.item_list_terminals,
            arrayOf("name", "dateTimeDetected"),
            intArrayOf(R.id.textViewTerminalName, R.id.textViewDateTimeDetected))

        //TODO: adapter.setViewBinder precisa??

        lv!!.adapter = adapter
    }

    private fun observeListView() {
        lv?.setOnItemClickListener { _, _, position, _ ->
            iSelectedDeviceIndex = position

            val hmSelectedDevice: HashMap<String, Any>? =
                arrayListZoopTerminalsListForUI?.get(iSelectedDeviceIndex)
            val joZoopDeviceSelectedByClick = hmSelectedDevice?.get("joZoopDevice") as JSONObject

            ZLog.t("Terminal selected by click: $joZoopDeviceSelectedByClick")

            terminalListManager?.requestZoopDeviceSelection(joZoopDeviceSelectedByClick)

            Toast.makeText(this, "Terminal selected by click: $position", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        terminalListManager?.finishTerminalDiscovery()
        super.onDestroy()
    }

    override fun showDeviceListForUserSelection(
        vectorZoopTerminals: Vector<JSONObject?>
    ) {
        try {
            if (vectorZoopTerminals.size > 0) {
                arrayListZoopTerminalsListForUI = ArrayList()
                for (joZoopTerminal in vectorZoopTerminals) {
                    val hashMapZoopTerminalStringsForUI = HashMap<String, Any>()
                    if (joZoopTerminal != null) {
                        hashMapZoopTerminalStringsForUI["name"] = joZoopTerminal.getString("name")
                        hashMapZoopTerminalStringsForUI["dateTimeDetected"] = joZoopTerminal.getString("dateTimeDetected")
                    }
                    arrayListZoopTerminalsListForUI!!.add(hashMapZoopTerminalStringsForUI)
                }
                updateListView()
//                terminalListManager?.requestZoopDeviceSelection(vectorZoopTerminals[0])
            }
            else {
                lv?.visibility = View.GONE
                textViewTerminalList.text = "Não há maquininhas disponíveis"
            }
        } catch (e: Exception) {
            ZLog.exception(300064, e)
        }
    }

    override fun updateDeviceListForUserSelection(
        joNewlyFoundZoopDevice: JSONObject?,
        vectorZoopTerminals: Vector<JSONObject?>?, iNewlyFoundDeviceIndex: Int
    ) {
        try {
            val hashMapZoopTerminalStringsForUI = HashMap<String, Any>()
            if (joNewlyFoundZoopDevice != null) {
                hashMapZoopTerminalStringsForUI["name"] = joNewlyFoundZoopDevice.getString("name")
                hashMapZoopTerminalStringsForUI["dateTimeDetected"] = joNewlyFoundZoopDevice.getString("dateTimeDetected")
            }
            arrayListZoopTerminalsListForUI!!.add(hashMapZoopTerminalStringsForUI)
            adapter!!.notifyDataSetChanged()
//            terminalListManager?.requestZoopDeviceSelection(joNewlyFoundZoopDevice)
        } catch (e: Exception) {
            ZLog.exception(677541, e)
        }
    }

    override fun bluetoothIsNotEnabledNotification() {
        terminalListManager?.enableDeviceBluetoothAdapter()
    }

    override fun deviceSelectedResult(
        joZoopSelectedDevice: JSONObject,
        vectorAllAvailableZoopTerminals: Vector<JSONObject?>?,
        iSelectedDeviceIndex: Int
    ) {
        try {
//        val namePinpad: String = joZoopSelectedDevice.getString("name")
            adapter!!.notifyDataSetChanged()
        } catch (e: Exception) {
            ZLog.exception(300056, e)
        }
    }

}