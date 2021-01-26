package com.pete.nearsample

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.adroitandroid.near.connect.NearConnect
import com.adroitandroid.near.discovery.NearDiscovery
import com.adroitandroid.near.model.Host
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.pete.nearsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DISCOVERABLE_TIMEOUT_MILLIS: Long = 60000
        private const val DISCOVERY_TIMEOUT_MILLIS: Long = 10000
        private const val DISCOVERABLE_PING_INTERVAL_MILLIS: Long = 5000

        //wrritten by Near
        const val MESSAGE_REQUEST_ = "start_chat"
        const val MESSAGE_RESPONSE_DECLINE_REQUEST = "decline_request"
        const val MESSAGE_RESPONSE_ACCEPT_REQUEST = "accept_request"

        const val MESSAGE_LISTENING_NEWBLOCK = "listening_newblock"
    }

    private lateinit var nearDiscovery: NearDiscovery
    private lateinit var nearConnect: NearConnect
    private lateinit var binding : ActivityMainBinding
    private var blockData : User? = null

    var adapter : BlockAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //NearDiscovery take $hostname
        nearDiscovery = NearDiscovery.Builder()
                .setContext(this)
                .setDiscoverableTimeoutMillis(DISCOVERABLE_TIMEOUT_MILLIS)
                .setDiscoveryTimeoutMillis(DISCOVERY_TIMEOUT_MILLIS)
                .setDiscoverablePingIntervalMillis(DISCOVERABLE_PING_INTERVAL_MILLIS)
                .setDiscoveryListener(nearDiscoveryListener, Looper.getMainLooper())
                //this one create thread runs on following thread by looper
                .build()

        nearConnect = NearConnect.Builder()
                .fromDiscovery(nearDiscovery)
                .setContext(this)
                .setListener(nearConnectListener,Looper.getMainLooper())
                .build()

        adapter = BlockAdapter()
        initViews()

    }

    fun initViews() {
        with(binding) {
            //Node start sending with #hostname
            var myHostName = hostEt.text
            connectBt.setOnClickListener {
                nearDiscovery.makeDiscoverable(hostName = myHostName.toString())
                startDiscovery()
                if (!nearConnect.isReceiving) {
                    nearConnect.startReceiving()
                }
            }
            usrRecycler.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.VERTICAL,false)
            usrRecycler.adapter = this@MainActivity.adapter

            blockBt.setOnClickListener {
                val newUser = User("TEST VOTER",12,"myPREVIOUSHAHS","19/09/2020")
                this@MainActivity.adapter!!.addNewUser(newUser)
            }
        }
    }

    //this anonymous object extend the #NearDiscovery#Listener which had 4 call back
    val nearDiscoveryListener: NearDiscovery.Listener
        get() = object : NearDiscovery.Listener {
            override fun onDiscoverableTimeout() {
                Toast.makeText(this@MainActivity,"discovery:Timeout",Toast.LENGTH_SHORT).show()
            }

            override fun onDiscoveryFailure(e: Throwable) {
                Toast.makeText(this@MainActivity,"discovery:Failed",Toast.LENGTH_SHORT).show()
            }

            override fun onDiscoveryTimeout() {
                Toast.makeText(this@MainActivity,"discovery:Timeout",Toast.LENGTH_SHORT).show()
            }

            override fun onPeersUpdate(host: Set<Host>) {
                val hostList = ArrayList(host)
                val currentHost = hostList[0]
                Snackbar.make(binding.root, currentHost.name, Snackbar.LENGTH_SHORT).show()
                nearConnect.send(MESSAGE_LISTENING_NEWBLOCK.toByteArray(),currentHost)
            }

        }

    fun startDiscovery() {
        nearDiscovery.startDiscovery()
    }

    fun setNewBlockData(user: User) {
        this.blockData = user
    }

    private val nearConnectListener: NearConnect.Listener
        get() = object : NearConnect.Listener {
            override fun onReceive(bytes: ByteArray, sender: Host) {
                when (val data = String(bytes)) {
                    MESSAGE_LISTENING_NEWBLOCK-> {
                        AlertDialog.Builder(this@MainActivity)
                                .setMessage(sender.name + "SEND BLOCK TO YOU")

                    }
                    //0..9
                    else -> if (data.startsWith("NewBlock:")) {
                        val json = data.substring(9,data.length-1)
                        val newUser = Gson().fromJson(json,User::class.java)
                        runOnUiThread {
                            setNewBlockData(newUser)
                            adapter!!.addNewUser(this@MainActivity.blockData!!)
                        }
                    }
                }
            }

            override fun onSendComplete(jobId: Long) { }
            override fun onSendFailure(e: Throwable?, jobId: Long) { }
            override fun onStartListenFailure(e: Throwable?) { }
        }
}