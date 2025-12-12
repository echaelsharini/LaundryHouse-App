package com.example.laundry_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

/**
 * SplashScreenActivity adalah layar pembuka yang muncul saat aplikasi pertama kali dijalankan.
 * Pada versi revisi ini, ditambahkan proses Background Task menggunakan Thread
 * untuk mensimulasikan proses loading data sebelum masuk ke LoginActivity.
 */
public class SplashScreenActivity extends AppCompatActivity {

    // Durasi minimal tampilan splash screen
    private static final int SPLASH_DISPLAY_LENGTH = 3000; // 3 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Background Task: simulasi proses loading di thread terpisah
        new Thread(() -> {
            try {
                // Simulasi proses berat (load data, inisialisasi, dsb.)
                Thread.sleep(SPLASH_DISPLAY_LENGTH);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Kembali ke UI Thread untuk berpindah ke halaman login
            runOnUiThread(() -> {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });

        }).start();
    }
}
