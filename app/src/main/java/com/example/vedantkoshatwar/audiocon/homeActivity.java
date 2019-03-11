package com.example.vedantkoshatwar.audiocon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class homeActivity extends AppCompatActivity {

    Button bt1,bt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        bt1 =  findViewById(R.id.HOST);
        YoYo.with(Techniques.Tada)
                .duration(700)
                .playOn(bt1);


        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(homeActivity.this, HostActivity.class);
                startActivity(myIntent);
            }
        });

        bt2 = findViewById(R.id.CONNECT);
        YoYo.with(Techniques.Tada)
                .duration(700)
                .playOn(bt2);


        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(homeActivity.this, ConnectActivity.class);
                startActivity(myIntent);
            }
        });


    }
}
