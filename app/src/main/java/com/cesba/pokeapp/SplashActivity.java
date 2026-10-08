package com.cesba.pokeapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Vinculamos el VideoView con el ID del XML
        videoView = findViewById(R.id.videoBackground);

        try {
            // Apuntamos al video ubicado en res/raw/splash_background.mp4[cite: 10]
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_background);
            videoView.setVideoURI(uri);

            // Iniciamos la reproducción tan pronto como esté listo
            videoView.setOnPreparedListener(mp -> mp.start());

            // Cuando termine el video, saltamos automáticamente a la MainActivity
            videoView.setOnCompletionListener(mp -> {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });

            videoView.start();

        } catch (Exception e) {
            e.printStackTrace();
            // Si ocurre algún error, avanzamos directo a la aplicación para evitar bloqueos
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}