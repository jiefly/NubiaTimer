package com.gao.jiefly.nubiaclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    int hour;
    int min;
    int second;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Timer timer = (Timer) findViewById(R.id.id_timer);
        final EditText timeH = (EditText) findViewById(R.id.id_time_hour_et);
        final EditText timeM = (EditText) findViewById(R.id.id_time_min__et);
        final EditText timeS = (EditText) findViewById(R.id.id_time_second_et);

        Button btn = (Button) findViewById(R.id.id_set_time_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hour = Integer.valueOf(timeH.getText().toString());
                min = Integer.valueOf(timeM.getText().toString());
                second = Integer.valueOf(timeS.getText().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timer.setTime(hour,min,second);
                    }
                });
            }
        });
        findViewById(R.id.id_start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.startTimer();
            }
        });
    }
}
