package com.cesba.pokeapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BattleVersusActivity extends AppCompatActivity {

    private ProgressBar pbP1Hp, pbP2Hp;
    private TextView tvTurnIndicator, tvVersusLog;
    private Button btnVersusAttack1, btnVersusAttack2;

    private int p1Hp = 100;
    private int p2Hp = 100;
    private boolean isPlayer1Turn = true; // true = J1, false = J2

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_versus);

        pbP1Hp = findViewById(R.id.pbP1Hp);
        pbP2Hp = findViewById(R.id.pbP2Hp);
        tvTurnIndicator = findViewById(R.id.tvTurnIndicator);
        tvVersusLog = findViewById(R.id.tvVersusLog);
        btnVersusAttack1 = findViewById(R.id.btnVersusAttack1);
        btnVersusAttack2 = findViewById(R.id.btnVersusAttack2);

        btnVersusAttack1.setOnClickListener(v -> executeVersusTurn(25, "Ataque Fuerte"));
        btnVersusAttack2.setOnClickListener(v -> executeVersusTurn(15, "Ataque Rápido"));
    }

    private void executeVersusTurn(int damage, String moveName) {
        if (p1Hp <= 0 || p2Hp <= 0) return;

        if (isPlayer1Turn) {
            p2Hp -= damage;
            if (p2Hp < 0) p2Hp = 0;
            pbP2Hp.setProgress(p2Hp);
            tvVersusLog.setText("¡Jugador 1 usó " + moveName + " y causó " + damage + " de daño!");

            if (p2Hp == 0) {
                Toast.makeText(this, "¡Jugador 1 ha ganado la batalla!", Toast.LENGTH_LONG).show();
                tvVersusLog.setText("¡Fin del combate! ¡Victoria para el Jugador 1!");
                return;
            }
            isPlayer1Turn = false;
            tvTurnIndicator.setText("Turno: Jugador 2");
        } else {
            p1Hp -= damage;
            if (p1Hp < 0) p1Hp = 0;
            pbP1Hp.setProgress(p1Hp);
            tvVersusLog.setText("¡Jugador 2 usó " + moveName + " y causó " + damage + " de daño!");

            if (p1Hp == 0) {
                Toast.makeText(this, "¡Jugador 2 ha ganado la batalla!", Toast.LENGTH_LONG).show();
                tvVersusLog.setText("¡Fin del combate! ¡Victoria para el Jugador 2!");
                return;
            }
            isPlayer1Turn = true;
            tvTurnIndicator.setText("Turno: Jugador 1");
        }
    }
}