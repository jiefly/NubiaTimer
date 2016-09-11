package com.gao.jiefly.nubiaclock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gao.jiefly.nubiatimer.Timer;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Timer timer = (Timer) findViewById(R.id.id_timer);
        final EditText timeH = (EditText) findViewById(R.id.id_time_hour_et);
        final EditText timeM = (EditText) findViewById(R.id.id_time_min__et);
        final EditText timeS = (EditText) findViewById(R.id.id_time_second_et);
        assert timer != null;
        timer.setOnTimeUpListener(new Timer.OnTimeUpListener() {
            @Override
            public void onTimeUp() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "time up", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        Button btn = (Button) findViewById(R.id.id_stop_time_btn);
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

        findViewById(R.id.id_set_time_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = timeH.getText() == null || timeH.length()==0 ? 0 : Integer.valueOf(timeH.getText().toString());
                int min = timeM.getText() == null || timeM.length()==0 ? 0 : Integer.valueOf(timeM.getText().toString());
                int second = timeS.getText() == null || timeS.length()==0 ? 0 : Integer.valueOf(timeS.getText().toString());

                timer.setTime(hour, min, second);
            }
        });
    }
}
