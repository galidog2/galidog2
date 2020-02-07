package com.example.galidog2;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class GenericActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic);
    }

    public void audioModeStart() {
        Toast.makeText(this, "On entre dans le mode audio", Toast.LENGTH_SHORT).show();

        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
                wl.acquire();
            }

            public void onFinish() {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
                wl.release();

            }
        }.start();
    }
}
