package com.cesba.pokeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private View cardPokedex, cardBattleEmulator, cardBattleVersus, cardTorre;
    private View cardQuienEs, cardSafari, cardMaestro, cardMemory;
    private View cardFavoritos, cardHistorial, cardMedallas, cardPerfil;
    private View cardConfiguracion, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWelcome = findViewById(R.id.tvWelcome);
        cardPokedex = findViewById(R.id.cardPokedex);
        cardBattleEmulator = findViewById(R.id.cardBattleEmulator);
        cardBattleVersus = findViewById(R.id.cardBattleVersus);
        cardTorre = findViewById(R.id.cardTorre);
        cardQuienEs = findViewById(R.id.cardQuienEs);
        cardSafari = findViewById(R.id.cardSafari);
        cardMaestro = findViewById(R.id.cardMaestro);
        cardMemory = findViewById(R.id.cardMemory);
        cardFavoritos = findViewById(R.id.cardFavoritos);
        cardHistorial = findViewById(R.id.cardHistorial);
        cardMedallas = findViewById(R.id.cardMedallas);
        cardPerfil = findViewById(R.id.cardPerfil);
        cardConfiguracion = findViewById(R.id.cardConfiguracion);
        cardLogout = findViewById(R.id.cardLogout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();
            String username = email.contains("@") ? email.split("@")[0] : email;
            tvWelcome.setText("¡Hola, " + username + "!");
        }

        if (cardPokedex != null) {
            cardPokedex.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, PokedexActivity.class))
            );
        }

        if (cardBattleEmulator != null) {
            cardBattleEmulator.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, BattleSelectionActivity.class))
            );
        }

        if (cardBattleVersus != null) {
            cardBattleVersus.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, BattleVersusActivity.class))
            );
        }

        if (cardTorre != null) {
            cardTorre.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, TorrePokemonActivity.class))
            );
        }

        if (cardPerfil != null) {
            cardPerfil.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, PerfilActivity.class))
            );
        }

        View.OnClickListener comingSoonListener = v ->
                Toast.makeText(MainActivity.this, "Módulo próximamente disponible", Toast.LENGTH_SHORT).show();

        if (cardQuienEs != null) cardQuienEs.setOnClickListener(comingSoonListener);
        if (cardSafari != null) cardSafari.setOnClickListener(comingSoonListener);
        if (cardMaestro != null) cardMaestro.setOnClickListener(comingSoonListener);
        if (cardMemory != null) cardMemory.setOnClickListener(comingSoonListener);
        if (cardFavoritos != null) cardFavoritos.setOnClickListener(comingSoonListener);
        if (cardHistorial != null) cardHistorial.setOnClickListener(comingSoonListener);
        if (cardMedallas != null) cardMedallas.setOnClickListener(comingSoonListener);
        if (cardConfiguracion != null) cardConfiguracion.setOnClickListener(comingSoonListener);

        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}