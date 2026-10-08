package com.cesba.pokeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BattleSelectionActivity extends AppCompatActivity {

    private EditText etEnemyPokemon, etPlayerPokemon;
    private Spinner spinnerEnemyForm, spinnerPlayerForm;
    private ImageView ivEnemyPreview, ivPlayerPreview;
    private SwitchCompat switchEnemyShiny, switchPlayerShiny;
    private Button btnStartBattle, btnVerHistorialPantalla;

    private boolean isEnemyShiny = false;
    private boolean isPlayerShiny = false;

    private String finalEnemyTarget = "3";
    private String finalPlayerTarget = "6";

    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable runnableEnemy, runnablePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_selection);

        // 1. Vincular componentes
        etEnemyPokemon = findViewById(R.id.etEnemyPokemon);
        etPlayerPokemon = findViewById(R.id.etPlayerPokemon);
        spinnerEnemyForm = findViewById(R.id.spinnerEnemyForm);
        spinnerPlayerForm = findViewById(R.id.spinnerPlayerForm);
        ivEnemyPreview = findViewById(R.id.ivEnemyPreview);
        ivPlayerPreview = findViewById(R.id.ivPlayerPreview);
        switchEnemyShiny = findViewById(R.id.switchEnemyShiny);
        switchPlayerShiny = findViewById(R.id.switchPlayerShiny);
        btnStartBattle = findViewById(R.id.btnStartBattle);
        btnVerHistorialPantalla = findViewById(R.id.btnVerHistorialPantalla);

        // 2. Adaptador de Formas
        String[] formas = new String[]{"Base", "Mega", "Gigantamax", "Alola", "Galar"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, formas);
        spinnerEnemyForm.setAdapter(adapter);
        spinnerPlayerForm.setAdapter(adapter);

        // Valores por defecto
        etEnemyPokemon.setText("3");
        etPlayerPokemon.setText("6");

        // 3. Listeners Shiny
        switchEnemyShiny.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isEnemyShiny = isChecked;
            refrescarPreviewRival();
        });

        switchPlayerShiny.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isPlayerShiny = isChecked;
            refrescarPreviewJugador();
        });

        // 4. Listeners Spinners de Formas
        spinnerEnemyForm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refrescarPreviewRival();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerPlayerForm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refrescarPreviewJugador();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 5. TextWatchers con debounce
        etEnemyPokemon.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (runnableEnemy != null) debounceHandler.removeCallbacks(runnableEnemy);
                runnableEnemy = BattleSelectionActivity.this::refrescarPreviewRival;
                debounceHandler.postDelayed(runnableEnemy, 500);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etPlayerPokemon.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (runnablePlayer != null) debounceHandler.removeCallbacks(runnablePlayer);
                runnablePlayer = BattleSelectionActivity.this::refrescarPreviewJugador;
                debounceHandler.postDelayed(runnablePlayer, 500);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 6. Iniciar Combate
        btnStartBattle.setOnClickListener(v -> {
            Intent intent = new Intent(BattleSelectionActivity.this, BattleEmulatorActivity.class);
            intent.putExtra("PLAYER_ID", finalPlayerTarget);
            intent.putExtra("ENEMY_ID", finalEnemyTarget);
            intent.putExtra("IS_PLAYER_SHINY", isPlayerShiny);
            intent.putExtra("IS_ENEMY_SHINY", isEnemyShiny);
            startActivity(intent);
        });

        // 7. Abrir pantalla del Historial de Combates
        btnVerHistorialPantalla.setOnClickListener(v -> {
            Intent intent = new Intent(BattleSelectionActivity.this, BattleHistoryActivity.class);
            startActivity(intent);
        });

        refrescarPreviewRival();
        refrescarPreviewJugador();
    }

    private void refrescarPreviewRival() {
        String base = etEnemyPokemon.getText().toString().trim();
        String forma = spinnerEnemyForm.getSelectedItem() != null ? spinnerEnemyForm.getSelectedItem().toString() : "Base";
        resolverYMostrarPokemon(base, forma, isEnemyShiny, ivEnemyPreview, true);
    }

    private void refrescarPreviewJugador() {
        String base = etPlayerPokemon.getText().toString().trim();
        String forma = spinnerPlayerForm.getSelectedItem() != null ? spinnerPlayerForm.getSelectedItem().toString() : "Base";
        resolverYMostrarPokemon(base, forma, isPlayerShiny, ivPlayerPreview, false);
    }

    private void resolverYMostrarPokemon(String input, String forma, boolean isShiny, ImageView targetView, boolean isEnemy) {
        if (input.isEmpty()) return;

        PokeApiService apiService = ApiClient.getClient().create(PokeApiService.class);
        apiService.getPokemon(input.toLowerCase()).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String nombreReal = response.body().getName().toLowerCase();

                    if (forma.equals("Base")) {
                        mostrarPokemonEnPantalla(response.body(), isShiny, targetView);
                        if (isEnemy) finalEnemyTarget = nombreReal;
                        else finalPlayerTarget = nombreReal;
                    } else {
                        buscarVariante(nombreReal, forma, isShiny, targetView, isEnemy, response.body());
                    }
                } else {
                    targetView.setImageResource(android.R.drawable.ic_dialog_alert);
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                targetView.setImageResource(android.R.drawable.ic_dialog_alert);
            }
        });
    }

    private void buscarVariante(String baseName, String forma, boolean isShiny, ImageView targetView, boolean isEnemy, Pokemon fallbackBase) {
        String variantCandidate = construirNombreVariante(baseName, forma);

        PokeApiService apiService = ApiClient.getClient().create(PokeApiService.class);
        apiService.getPokemon(variantCandidate).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarPokemonEnPantalla(response.body(), isShiny, targetView);
                    if (isEnemy) finalEnemyTarget = variantCandidate;
                    else finalPlayerTarget = variantCandidate;
                } else {
                    if (forma.equals("Mega") && !variantCandidate.endsWith("-x")) {
                        probarMegaAlterna(baseName, isShiny, targetView, isEnemy, fallbackBase);
                    } else {
                        mostrarPokemonEnPantalla(fallbackBase, isShiny, targetView);
                        if (isEnemy) finalEnemyTarget = baseName;
                        else finalPlayerTarget = baseName;
                    }
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                mostrarPokemonEnPantalla(fallbackBase, isShiny, targetView);
                if (isEnemy) finalEnemyTarget = baseName;
                else finalPlayerTarget = baseName;
            }
        });
    }

    private void probarMegaAlterna(String baseName, boolean isShiny, ImageView targetView, boolean isEnemy, Pokemon fallbackBase) {
        String megaX = baseName + "-mega-x";
        PokeApiService apiService = ApiClient.getClient().create(PokeApiService.class);
        apiService.getPokemon(megaX).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarPokemonEnPantalla(response.body(), isShiny, targetView);
                    if (isEnemy) finalEnemyTarget = megaX;
                    else finalPlayerTarget = megaX;
                } else {
                    mostrarPokemonEnPantalla(fallbackBase, isShiny, targetView);
                    if (isEnemy) finalEnemyTarget = baseName;
                    else finalPlayerTarget = baseName;
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                mostrarPokemonEnPantalla(fallbackBase, isShiny, targetView);
            }
        });
    }

    private String construirNombreVariante(String name, String forma) {
        switch (forma) {
            case "Mega":
                if (name.equals("charizard") || name.equals("mewtwo")) return name + "-mega-x";
                return name + "-mega";
            case "Gigantamax":
                return name + "-gmax";
            case "Alola":
                return name + "-alola";
            case "Galar":
                return name + "-galar";
            default:
                return name;
        }
    }

    private void mostrarPokemonEnPantalla(Pokemon pokemon, boolean isShiny, ImageView targetView) {
        int pokeId = pokemon.getId();
        String url;

        if (isShiny) {
            url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/shiny/" + pokeId + ".png";
        } else if (pokemon.getSprites() != null && pokemon.getSprites().getFrontDefault() != null) {
            url = pokemon.getSprites().getFrontDefault();
        } else {
            url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + pokeId + ".png";
        }

        Glide.with(BattleSelectionActivity.this)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(targetView);
    }
}