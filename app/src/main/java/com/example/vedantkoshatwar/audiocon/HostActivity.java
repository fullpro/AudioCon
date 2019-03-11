package com.example.vedantkoshatwar.audiocon;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

public class HostActivity extends AppCompatActivity {

    //Declaring Variables
    WifiManager wifiManager;
    WifiConfiguration wifiConfiguration;

    Switch hostSwitch;
    Button Audio, btnSend;
    EditText writeMsg;
    TextView read_msg_box;

   private TextView OnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        Audio = findViewById(R.id.Audio);
        hostSwitch = findViewById(R.id.switch_hotspot);
        OnText =  findViewById(R.id.OnOff);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiConfiguration = new WifiConfiguration();

        //ANIMATION
        YoYo.with(Techniques.Shake)
                .duration(700)
                .playOn(OnText);

        //Add listener to Switch Button i.e. Switch ON/OFF
        hostSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hostSwitch.setText("Turn OFF Hotspot");
                    OnText.setText("ON");
                    Toast.makeText(HostActivity.this, "Turning on Hotspot", Toast.LENGTH_LONG).show();
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .playOn(OnText);
                    Method method;
                    try {
                        method = wifiManager.getClass().getDeclaredMethod("setWifiEnabled", WifiConfiguration.class, Boolean.TYPE);
                        method.invoke(wifiManager, wifiConfiguration, isChecked);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    hostSwitch.setText("Turn On Hotspot");
                    OnText.setText("OFF");
                    YoYo.with(Techniques.Shake)
                            .duration(700)
                            .playOn(OnText);
                }
            }

        });
        Audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HostActivity.this, RecordActivity.class);
                startActivity(myIntent);
            }
        });
    }


    




}
