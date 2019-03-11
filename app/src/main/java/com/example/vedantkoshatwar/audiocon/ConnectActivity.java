package com.example.vedantkoshatwar.audiocon;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;

import android.widget.TextView;
import android.widget.Toast;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ConnectActivity extends AppCompatActivity{

    //Declaring Variables
    WifiManager wifiManager;
    Switch aSwitch;
    Button btnDiscover;
    ImageButton btnSend;
    ListView listView;
    TextView connectionStatus, read_msg_box;
    EditText writeMsg;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReciever;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    HostClass hostClass;
    ClientClass clientClass;
    SendReceive sendReceive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        initialWork();
        exqListener();


        //Defining Variables
        aSwitch =  findViewById(R.id.switch_wifi);
        btnDiscover = findViewById(R.id.Discover);
        writeMsg = (EditText)  findViewById(R.id.writeMsg);

        listView = findViewById(R.id.peerListView);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //FOR LATEST BUILDS
        if(Build.VERSION.SDK_INT>17)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        //Checking wifi state
        if (wifiManager.isWifiEnabled()) {
            aSwitch.setChecked(true);
        } else {
            aSwitch.setChecked(false);
        }

        //Add listener to Switch Button i.e. Switch ON/OFF
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifiManager.setWifiEnabled(true);
                    aSwitch.setText("Turn OFF Wi-Fi");
                    Toast.makeText(ConnectActivity.this, "Turning on Wi-Fi", Toast.LENGTH_LONG).show();

                } else {
                    wifiManager.setWifiEnabled(false);
                    aSwitch.setText("Turn On Wi-Fi");
                }
            }
        });


    }


    //MESSAGE HANDLER takes input message
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    read_msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });


    //FUNCTION
    private void exqListener(){
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ConnectActivity.this, "Discovery Started...", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(ConnectActivity.this, "Discovery Starting Failure...", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.wps.setup = WpsInfo.PBC;
                config.deviceAddress = device.deviceAddress;


                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Connected to"+ device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(),"Not Connected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //SEND BUTTON
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = writeMsg.getText().toString();
                sendReceive.write(msg.getBytes());
                writeMsg.setText("");
            }
        });



    }


    //FUNCTION
    private void initialWork(){

        btnDiscover = findViewById(R.id.Discover);
        listView = findViewById(R.id.peerListView);
        connectionStatus = findViewById(R.id.connectionStatus);
        btnSend = findViewById(R.id.sendButton);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);

        mReciever = new WifiDirectBroadcastReciever(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers))
            {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for(WifiP2pDevice device : peerList.getDeviceList())
                {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_expandable_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
            }
            if(peers.size() == 0)
            {
                Toast.makeText(getApplicationContext(),"No Device Found",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };


    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner )
            {
                connectionStatus.setText("Host");
                hostClass = new HostClass();
                hostClass.start();
            }
            else if(wifiP2pInfo.groupFormed)
            {
                connectionStatus.setText("Client");
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();

            }
        }
    };



    //BROADCAST COMMANDS
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReciever,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReciever);
    }

    @Override
    protected void onStart(){
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver,intentFilter);
    }

    @Override
    protected void onStop(){
        super.onStop();
        unregisterReceiver(wifiStateReceiver);
    }
    //BROADCAST COMMANDS



    //HOST SIDE
    public class HostClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive= new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //DATA SENDING AND RECEIVING CLASS
    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)
        {
            socket = skt;
            try{
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket != null)
            {
                try {
                    bytes = inputStream.read(buffer);
                    if(bytes > 0)       //We have something in msg
                    {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }




    //CLIENT SIDE
    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;

        public  ClientClass(InetAddress hostAddress)
        {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();

        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    //BROADCAST FOR WIFI STATUS
    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(wifiManager.EXTRA_WIFI_STATE, wifiManager.WIFI_STATE_UNKNOWN);

            switch(wifiStateExtra){
                case WifiManager.WIFI_STATE_ENABLED:
                    aSwitch.setChecked(true);
                    aSwitch.setText("Turn OFF Wi-Fi");
                    break;

                case WifiManager.WIFI_STATE_DISABLED:
                    aSwitch.setChecked(false);
                    aSwitch.setText("Turn On Wi-Fi");
                    break;

            }
        }
    };
}