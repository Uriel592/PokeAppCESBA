package com.cesba.pokeapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TorrePokemonActivity extends AppCompatActivity {

    private TextView tvTowerStatus;
    private ProgressBar pbTowerProgress;
    private Button btnAdvanceTower;

    private int currentLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torre_pokemon);

        tvTowerStatus = findViewById(R.id.tvTowerStatus);
        pbTowerProgress = findViewById(R.id.pbTowerProgress);
        btnAdvanceTower = findViewById(R.id.btnAdvanceTower);

        btnAdvanceTower.setOnClickListener(v -> {
            if (currentLevel < 6) {
                currentLevel++;
                pbTowerProgress.setProgress(currentLevel);
                if (currentLevel == 6) {
                    tvTowerStatus.setText("Nivel 6: ¡JEFE FINAL (Legendarios)!");
                    Toast.makeText(this, "¡Has llegado al Jefe Final de la Torre!", Toast.LENGTH_LONG).show();
                } else {
                    tvTowerStatus.setText("Nivel Actual: " + currentLevel);
                    Toast.makeText(this, "¡Nivel " + (currentLevel - 1) * 1 + " superado con éxito!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "¡Felicidades! Has completado la Torre y ganado la Medalla.", Toast.LENGTH_LONG).show();
                tvTowerStatus.setText("¡TORRE COMPLETADA - CAMPEÓN!");
                btnAdvanceTower.setEnabled(false);
            }
        });
    }
}