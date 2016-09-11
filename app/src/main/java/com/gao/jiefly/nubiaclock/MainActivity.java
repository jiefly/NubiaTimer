package com.gao.jiefly.nubiaclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
        timer.setOnTimeUpListener(new Timer.OnTimeUpListener() {
            @Override
            public void onTimeUp() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"time up",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        Button btn = (Button) findViewById(R.id.id_set_time_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.stopTimer();
            }
        });
        findViewById(R.id.id_start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.startTimer();
            }
        });

        findViewById(R.id.id_reset_timer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.resetTimer();
            }
        });
    }
}
