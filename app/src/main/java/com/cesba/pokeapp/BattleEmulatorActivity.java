package com.cesba.pokeapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BattleEmulatorActivity extends AppCompatActivity {

    private ProgressBar pbPlayerHp, pbEnemyHp;
    private TextView tvBattleLog, tvPlayerName, tvEnemyName, tvPlayerLevel, tvEnemyLevel, tvPlayerHpValues;
    private ImageView ivBattleArena, ivEnemySprite, ivPlayerSprite;

    private int playerMaxHp = 100;
    private int playerHp = 100;
    private int enemyMaxHp = 100;
    private int enemyHp = 100;

    private int playerAttackStage = 0;
    private int enemyAttackStage = 0;

    private boolean isPlayerParalyzed = false;
    private boolean isEnemyParalyzed = false;
    private boolean isPlayerFlinched = false;
    private boolean isEnemyFlinched = false;

    private String playerPrimaryType = "normal";
    private String enemyPrimaryType = "normal";

    private String playerDisplayName = "";
    private String enemyDisplayName = "";

    private boolean isPlayerShiny = false;
    private boolean isEnemyShiny = false;

    private boolean playerLoaded = false;
    private boolean enemyLoaded = false;

    private Random random;
    private List<String> playerMoves = new ArrayList<>();
    private List<String> enemyMoves = new ArrayList<>();

    private MediaPlayer mediaPlayer;
    private MediaPlayer backgroundMusic;
    private MediaPlayer criticalMusic;
    private MediaPlayer victoryMusic;
    private MediaPlayer defeatMusic;
    private boolean isCriticalMusicPlaying = false;
    private boolean isBattleEnded = false;

    private Handler typewriterHandler = new Handler(Looper.getMainLooper());
    private Runnable typewriterRunnable;

    private static final List<String> INTIMIDATE_SPECIES = Arrays.asList(
            "gyarados", "arcanine", "salamence", "krookodile", "incineroar", "staraptor", "mightyena", "luxray"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_emulator);

        reproducirMusicaNormal();
        random = new Random();

        pbPlayerHp = findViewById(R.id.pbPlayerHp);
        pbEnemyHp = findViewById(R.id.pbEnemyHp);
        tvBattleLog = findViewById(R.id.tvBattleLog);
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvEnemyName = findViewById(R.id.tvEnemyName);
        tvPlayerLevel = findViewById(R.id.tvPlayerLevel);
        tvEnemyLevel = findViewById(R.id.tvEnemyLevel);
        tvPlayerHpValues = findViewById(R.id.tvPlayerHpValues);

        ivBattleArena = findViewById(R.id.ivBattleArena);
        ivEnemySprite = findViewById(R.id.ivEnemySprite);
        ivPlayerSprite = findViewById(R.id.ivPlayerSprite);

        ivEnemySprite.setAlpha(0f);
        ivPlayerSprite.setAlpha(0f);

        String playerId = getIntent().getStringExtra("PLAYER_ID");
        if (playerId == null) playerId = "25";

        String enemyId = getIntent().getStringExtra("ENEMY_ID");
        if (enemyId == null) enemyId = "4";

        isPlayerShiny = getIntent().getBooleanExtra("IS_PLAYER_SHINY", false);
        isEnemyShiny = getIntent().getBooleanExtra("IS_ENEMY_SHINY", false);

        playTransitionEffect();

        String finalPlayerId = playerId;
        String finalEnemyId = enemyId;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            cargarPokemonCombate(finalPlayerId, true);
            cargarPokemonCombate(finalEnemyId, false);
        }, 500);
    }

    private void comprobarInicioAutomatico() {
        if (playerLoaded && enemyLoaded && !isBattleEnded) {
            new Handler(Looper.getMainLooper()).postDelayed(this::ejecutarTurnoJugadorAutomatico, 2000);
        }
    }

    private void ejecutarTurnoJugadorAutomatico() {
        if (isBattleEnded || playerHp <= 0 || enemyHp <= 0) return;

        if (isPlayerFlinched) {
            isPlayerFlinched = false;
            mostrarTextoGradual("¡Tu Pokémon dudó por el miedo y no pudo atacar!");
            new Handler(Looper.getMainLooper()).postDelayed(this::ejecutarTurnoEnemigoAutomatico, 2200);
            return;
        }

        if (isPlayerParalyzed && random.nextDouble() < 0.25) {
            mostrarTextoGradual("¡Tu Pokémon está paralizado y no puede moverse!");
            new Handler(Looper.getMainLooper()).postDelayed(this::ejecutarTurnoEnemigoAutomatico, 2200);
            return;
        }

        String moveName = "PLACAJE";
        if (!playerMoves.isEmpty()) {
            List<String> validos = new ArrayList<>();
            for (String m : playerMoves) {
                if (!m.equals("-")) validos.add(m);
            }
            if (!validos.isEmpty()) {
                moveName = validos.get(random.nextInt(validos.size()));
            }
        }

        reproducirSonidoMovimiento(moveName);

        double effectiveness = calcularEfectividad(moveName, enemyPrimaryType);
        int baseDamage = random.nextInt(10) + 12;
        double attackMultiplier = 1.0 + (playerAttackStage * 0.2);
        int finalDamage = (int)(baseDamage * attackMultiplier * effectiveness);

        boolean esCritico = random.nextDouble() < 0.15;
        if (esCritico) {
            finalDamage = (int)(finalDamage * 1.5);
        }

        StringBuilder msg = new StringBuilder("¡Tu Pokémon usó " + moveName + "!");
        if (effectiveness > 1.0) msg.append("\n¡Es muy eficaz!");
        else if (effectiveness == 0.0) { finalDamage = 0; msg.append("\n¡No afecta al rival!"); }
        else if (effectiveness < 1.0) msg.append("\nNo es muy eficaz...");
        if (esCritico) msg.append(" ¡Golpe crítico!");

        mostrarTextoGradual(msg.toString());

        final int danioCalculado = finalDamage;

        animarAtaque(ivPlayerSprite, true, () -> {
            animarDanio(ivEnemySprite, () -> {
                int hpAnterior = enemyHp;
                enemyHp -= danioCalculado;
                if (enemyHp < 0) enemyHp = 0;

                animarCambioVida(pbEnemyHp, hpAnterior, enemyHp);
                actualizarColorBarra(pbEnemyHp, enemyHp, enemyMaxHp);

                if (enemyHp == 0) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        mostrarTextoGradual("¡Has derrotado al Pokémon rival!");
                        mostrarDialogoVictoria();
                    }, 1800);
                } else {
                    new Handler(Looper.getMainLooper()).postDelayed(this::ejecutarTurnoEnemigoAutomatico, 1800);
                }
            });
        });
    }

    private void ejecutarTurnoEnemigoAutomatico() {
        if (isBattleEnded || playerHp <= 0 || enemyHp <= 0) return;

        if (isEnemyFlinched) {
            isEnemyFlinched = false;
            mostrarTextoGradual("¡El rival dudó por el miedo y no pudo atacar!");
            siguienteRonda();
            return;
        }

        if (isEnemyParalyzed && random.nextDouble() < 0.25) {
            mostrarTextoGradual("¡El rival está paralizado y no puede moverse!");
            siguienteRonda();
            return;
        }

        String enemyMove = "PLACAJE";
        if (!enemyMoves.isEmpty()) {
            int randIndex = random.nextInt(enemyMoves.size());
            enemyMove = enemyMoves.get(randIndex);
        }

        reproducirSonidoMovimiento(enemyMove);

        double effectiveness = calcularEfectividad(enemyMove, playerPrimaryType);
        int baseDamage = random.nextInt(8) + 10;
        int enemyDamage = (int)(baseDamage * effectiveness);

        StringBuilder msg = new StringBuilder("¡El rival usó " + enemyMove + "!");
        if (effectiveness > 1.0) msg.append("\n¡Es muy eficaz contra ti!");
        else if (effectiveness < 1.0 && effectiveness > 0.0) msg.append("\nNo es muy eficaz...");
        else if (effectiveness == 0.0) { enemyDamage = 0; msg.append("\n¡No te afecta!"); }

        mostrarTextoGradual(msg.toString());

        final int danioCalculado = enemyDamage;

        animarAtaque(ivEnemySprite, false, () -> {
            animarDanio(ivPlayerSprite, () -> {
                int hpAnterior = playerHp;
                playerHp -= danioCalculado;
                if (playerHp < 0) playerHp = 0;

                animarCambioVida(pbPlayerHp, hpAnterior, playerHp);
                actualizarColorBarra(pbPlayerHp, playerHp, playerMaxHp);
                if (tvPlayerHpValues != null) {
                    tvPlayerHpValues.setText(playerHp + " / " + playerMaxHp);
                }
                verificarSaludCriticaJugador();

                if (playerHp == 0) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        mostrarTextoGradual("¡Tu Pokémon se ha debilitado!");
                        mostrarDialogoDerrota();
                    }, 1800);
                } else {
                    siguienteRonda();
                }
            });
        });
    }

    private void siguienteRonda() {
        new Handler(Looper.getMainLooper()).postDelayed(this::ejecutarTurnoJugadorAutomatico, 1800);
    }

    /**
     * Guarda el registro permanente de este combate en SharedPreferences.
     */
    private void guardarCombateEnHistorial(String resultado) {
        try {
            SharedPreferences sp = getSharedPreferences("PokemonBattleHistory", MODE_PRIVATE);
            String historialJson = sp.getString("registro_combates", "[]");
            JSONArray array = new JSONArray(historialJson);

            String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());

            JSONObject nuevoCombate = new JSONObject();
            nuevoCombate.put("fecha", fechaActual);
            nuevoCombate.put("jugador", playerDisplayName + (isPlayerShiny ? " ✨" : ""));
            nuevoCombate.put("rival", enemyDisplayName + (isEnemyShiny ? " ✨" : ""));
            nuevoCombate.put("resultado", resultado);

            array.put(nuevoCombate);

            sp.edit().putString("registro_combates", array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String inferirTipoMovimiento(String moveName) {
        String name = moveName.toLowerCase();
        if (name.contains("flame") || name.contains("fire") || name.contains("ember") || name.contains("heat") || name.contains("blaze")) return "fuego";
        if (name.contains("water") || name.contains("surf") || name.contains("hydro") || name.contains("bubble") || name.contains("fall")) return "agua";
        if (name.contains("vine") || name.contains("leaf") || name.contains("razor") || name.contains("seed") || name.contains("absorb")) return "hierba";
        if (name.contains("thunder") || name.contains("spark") || name.contains("bolt") || name.contains("shock") || name.contains("wave")) return "electrico";
        return playerPrimaryType;
    }

    private double calcularEfectividad(String moveName, String defensorTipo) {
        String tipoAtaque = inferirTipoMovimiento(moveName);
        defensorTipo = defensorTipo.toLowerCase();

        if (tipoAtaque.equals("agua") && (defensorTipo.equals("fuego") || defensorTipo.equals("ground") || defensorTipo.equals("rock"))) return 2.0;
        if (tipoAtaque.equals("fuego") && (defensorTipo.equals("grass") || defensorTipo.equals("hierba") || defensorTipo.equals("ice") || defensorTipo.equals("bug") || defensorTipo.equals("steel"))) return 2.0;
        if (tipoAtaque.equals("hierba") && (defensorTipo.equals("agua") || defensorTipo.equals("ground") || defensorTipo.equals("rock"))) return 2.0;
        if (tipoAtaque.equals("electrico") && (defensorTipo.equals("agua") || defensorTipo.equals("flying"))) return 2.0;

        if (tipoAtaque.equals("agua") && (defensorTipo.equals("agua") || defensorTipo.equals("grass") || defensorTipo.equals("hierba") || defensorTipo.equals("dragon"))) return 0.5;
        if (tipoAtaque.equals("fuego") && (defensorTipo.equals("fuego") || defensorTipo.equals("agua") || defensorTipo.equals("rock") || defensorTipo.equals("dragon"))) return 0.5;
        if (tipoAtaque.equals("hierba") && (defensorTipo.equals("fuego") || defensorTipo.equals("grass") || defensorTipo.equals("hierba") || defensorTipo.equals("poison") || defensorTipo.equals("flying") || defensorTipo.equals("bug") || defensorTipo.equals("dragon"))) return 0.5;
        if (tipoAtaque.equals("electrico") && (defensorTipo.equals("electrico") || defensorTipo.equals("grass") || defensorTipo.equals("hierba") || defensorTipo.equals("dragon"))) return 0.5;
        if (tipoAtaque.equals("electrico") && defensorTipo.equals("ground")) return 0.0;

        return 1.0;
    }

    private void reproducirMusicaNormal() {
        try {
            if (criticalMusic != null && criticalMusic.isPlaying()) { criticalMusic.stop(); criticalMusic.release(); criticalMusic = null; }
            if (victoryMusic != null && victoryMusic.isPlaying()) { victoryMusic.stop(); victoryMusic.release(); victoryMusic = null; }
            if (defeatMusic != null && defeatMusic.isPlaying()) { defeatMusic.stop(); defeatMusic.release(); defeatMusic = null; }
            if (backgroundMusic == null) {
                backgroundMusic = MediaPlayer.create(this, R.raw.pokemon_battle);
                if (backgroundMusic != null) {
                    backgroundMusic.setLooping(true);
                    backgroundMusic.setVolume(0.5f, 0.5f);
                    backgroundMusic.start();
                }
            } else if (!backgroundMusic.isPlaying()) {
                backgroundMusic.start();
            }
            isCriticalMusicPlaying = false;
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void reproducirMusicaCritica() {
        try {
            if (backgroundMusic != null && backgroundMusic.isPlaying()) { backgroundMusic.pause(); }
            if (criticalMusic == null) {
                criticalMusic = MediaPlayer.create(this, R.raw.pokemon_critical);
                if (criticalMusic != null) {
                    criticalMusic.setLooping(true);
                    criticalMusic.setVolume(0.6f, 0.6f);
                    criticalMusic.start();
                }
            } else if (!criticalMusic.isPlaying()) { criticalMusic.start(); }
            isCriticalMusicPlaying = true;
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void reproducirMusicaVictoria() {
        try {
            if (backgroundMusic != null) { backgroundMusic.stop(); backgroundMusic.release(); backgroundMusic = null; }
            if (criticalMusic != null) { criticalMusic.stop(); criticalMusic.release(); criticalMusic = null; }
            victoryMusic = MediaPlayer.create(this, R.raw.pokemon_victory);
            if (victoryMusic != null) {
                victoryMusic.setLooping(false);
                victoryMusic.setVolume(0.8f, 0.8f);
                victoryMusic.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void reproducirMusicaDerrota() {
        try {
            if (backgroundMusic != null) { backgroundMusic.stop(); backgroundMusic.release(); backgroundMusic = null; }
            if (criticalMusic != null) { criticalMusic.stop(); criticalMusic.release(); criticalMusic = null; }
            defeatMusic = MediaPlayer.create(this, R.raw.pokemon_defeat);
            if (defeatMusic != null) {
                defeatMusic.setLooping(true);
                defeatMusic.setVolume(0.8f, 0.8f);
                defeatMusic.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void verificarSaludCriticaJugador() {
        if (isBattleEnded) return;
        int porcentajeHp = (playerHp * 100) / playerMaxHp;
        if (porcentajeHp <= 20 && playerHp > 0) {
            if (!isCriticalMusicPlaying) reproducirMusicaCritica();
        } else {
            if (isCriticalMusicPlaying) reproducirMusicaNormal();
        }
    }

    private void mostrarDialogoVictoria() {
        isBattleEnded = true;
        guardarCombateEnHistorial("VICTORIA");
        reproducirMusicaVictoria();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡VICTORIA!");
        builder.setMessage("¡El combate automático ha concluido con victoria!");
        builder.setCancelable(false);
        builder.setPositiveButton("CONTINUAR", (dialog, which) -> { dialog.dismiss(); finish(); });
        builder.show();
    }

    private void mostrarDialogoDerrota() {
        isBattleEnded = true;
        guardarCombateEnHistorial("DERROTA");
        reproducirMusicaDerrota();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DERROTA...");
        builder.setMessage("¡Tu Pokémon se ha debilitado en combate!");
        builder.setCancelable(false);
        builder.setPositiveButton("REINTENTAR", (dialog, which) -> { dialog.dismiss(); finish(); });
        builder.show();
    }

    private void cargarPokemonCombate(String idOrName, boolean isPlayer) {
        PokeApiService apiService = ApiClient.getClient().create(PokeApiService.class);
        apiService.getPokemon(idOrName).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pokemon pokemon = response.body();
                    String rawName = pokemon.getName().toLowerCase();
                    String displayName = rawName.toUpperCase();
                    int pokeId = pokemon.getId();

                    int hpStat = 100;
                    if (pokemon.getStats() != null && !pokemon.getStats().isEmpty()) {
                        hpStat = pokemon.getStats().get(0).getBaseStat();
                    }

                    String primaryType = "normal";
                    if (pokemon.getTypes() != null && !pokemon.getTypes().isEmpty()) {
                        primaryType = pokemon.getTypes().get(0).getType().getName().toLowerCase();
                    }

                    if (isPlayer) {
                        playerDisplayName = displayName;
                        playerPrimaryType = primaryType;
                        playerMaxHp = hpStat;
                        playerHp = hpStat;
                        tvPlayerName.setText(displayName);
                        tvPlayerLevel.setText("Lv50");
                        if (tvPlayerHpValues != null) {
                            tvPlayerHpValues.setText(playerHp + " / " + playerMaxHp);
                        }
                        pbPlayerHp.setMax(playerMaxHp);
                        pbPlayerHp.setProgress(playerHp);
                        actualizarColorBarra(pbPlayerHp, playerHp, playerMaxHp);
                        verificarSaludCriticaJugador();

                        playerMoves.clear();
                        if (pokemon.getMoves() != null && !pokemon.getMoves().isEmpty()) {
                            for (int i = 0; i < Math.min(pokemon.getMoves().size(), 4); i++) {
                                playerMoves.add(pokemon.getMoves().get(i).getMove().getName().toUpperCase());
                            }
                        }
                        if (playerMoves.isEmpty()) playerMoves.add("PLACAJE");

                        String spriteBackUrl;
                        if (isPlayerShiny) {
                            spriteBackUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/shiny/" + pokeId + ".png";
                        } else {
                            spriteBackUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/" + pokeId + ".png";
                        }

                        Glide.with(BattleEmulatorActivity.this)
                                .load(spriteBackUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(Glide.with(BattleEmulatorActivity.this).load(isPlayerShiny
                                        ? "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/shiny/" + pokeId + ".png"
                                        : pokemon.getSprites().getFrontDefault()))
                                .into(ivPlayerSprite);

                        animarEntradaPokemon(ivPlayerSprite);

                        if (INTIMIDATE_SPECIES.contains(rawName)) {
                            enemyAttackStage = Math.max(-6, enemyAttackStage - 1);
                        }

                        playerLoaded = true;
                        comprobarInicioAutomatico();

                    } else {
                        enemyDisplayName = displayName;
                        enemyPrimaryType = primaryType;
                        enemyMaxHp = hpStat;
                        enemyHp = hpStat;
                        tvEnemyName.setText(displayName);
                        tvEnemyLevel.setText("Lv50");
                        pbEnemyHp.setMax(enemyMaxHp);
                        pbEnemyHp.setProgress(enemyHp);
                        actualizarColorBarra(pbEnemyHp, enemyHp, enemyMaxHp);

                        enemyMoves.clear();
                        if (pokemon.getMoves() != null && !pokemon.getMoves().isEmpty()) {
                            for (int i = 0; i < Math.min(pokemon.getMoves().size(), 4); i++) {
                                enemyMoves.add(pokemon.getMoves().get(i).getMove().getName().toUpperCase());
                            }
                        }
                        if (enemyMoves.isEmpty()) enemyMoves.add("PLACAJE");

                        String spriteFrontUrl;
                        if (isEnemyShiny) {
                            spriteFrontUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/shiny/" + pokeId + ".png";
                        } else {
                            spriteFrontUrl = pokemon.getSprites() != null ? pokemon.getSprites().getFrontDefault() : null;
                        }

                        if (spriteFrontUrl != null) {
                            Glide.with(BattleEmulatorActivity.this)
                                    .load(spriteFrontUrl)
                                    .into(ivEnemySprite);
                            animarEntradaPokemon(ivEnemySprite);
                        }

                        mostrarTextoGradual("¡Un " + displayName + (isEnemyShiny ? " ✨ variocolor" : "") + " rival apareció!");

                        if (INTIMIDATE_SPECIES.contains(rawName)) {
                            playerAttackStage = Math.max(-6, playerAttackStage - 1);
                        }

                        if (pokemon.getCries() != null && pokemon.getCries().getLatest() != null) {
                            reproducirGritoEntrada(pokemon.getCries().getLatest());
                        }

                        enemyLoaded = true;
                        comprobarInicioAutomatico();
                    }
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
            }
        });
    }

    private void mostrarTextoGradual(String textoCompleto) {
        if (typewriterRunnable != null) {
            typewriterHandler.removeCallbacks(typewriterRunnable);
        }
        tvBattleLog.setText("");
        final int[] index = {0};

        typewriterRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < textoCompleto.length()) {
                    tvBattleLog.append(String.valueOf(textoCompleto.charAt(index[0])));
                    index[0]++;
                    typewriterHandler.postDelayed(this, 20);
                }
            }
        };
        typewriterHandler.post(typewriterRunnable);
    }

    private void animarCambioVida(ProgressBar progressBar, int viejoHp, int nuevoHp) {
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", viejoHp, nuevoHp);
        animator.setDuration(800);
        animator.start();
    }

    private void actualizarColorBarra(ProgressBar progressBar, int currentHp, int maxHp) {
        int porcentaje = (currentHp * 100) / maxHp;
        int color;
        if (porcentaje > 50) {
            color = Color.parseColor("#50C878");
        } else if (porcentaje > 20) {
            color = Color.parseColor("#F8D030");
        } else {
            color = Color.parseColor("#E53E3E");
        }
        progressBar.setProgressTintList(ColorStateList.valueOf(color));
    }

    private void animarEntradaPokemon(ImageView imageView) {
        imageView.setAlpha(1f);
        ScaleAnimation scale = new ScaleAnimation(0.2f, 1.0f, 0.2f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(400);
        imageView.startAnimation(scale);
    }

    private void animarAtaque(View atacante, boolean esJugador, Runnable alTerminar) {
        float deltaX = esJugador ? 60f : -60f;
        float deltaY = esJugador ? -45f : 45f;

        ObjectAnimator advanceX = ObjectAnimator.ofFloat(atacante, "translationX", 0f, deltaX);
        ObjectAnimator advanceY = ObjectAnimator.ofFloat(atacante, "translationY", 0f, deltaY);
        advanceX.setDuration(120);
        advanceY.setDuration(120);

        ObjectAnimator returnX = ObjectAnimator.ofFloat(atacante, "translationX", deltaX, 0f);
        ObjectAnimator returnY = ObjectAnimator.ofFloat(atacante, "translationY", deltaY, 0f);
        returnX.setDuration(140);
        returnY.setDuration(140);

        AnimatorSet set = new AnimatorSet();
        set.play(advanceX).with(advanceY);
        set.play(returnX).with(returnY).after(advanceX);

        if (alTerminar != null) {
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    alTerminar.run();
                }
            });
        }
        set.start();
    }

    private void animarDanio(View objetivo, Runnable alTerminar) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(
                objetivo, "translationX", 0f, 25f, -25f, 20f, -20f, 10f, -10f, 0f
        );
        shake.setDuration(400);

        ObjectAnimator blink = ObjectAnimator.ofFloat(
                objetivo, "alpha", 1f, 0.2f, 1f, 0.2f, 1f
        );
        blink.setDuration(400);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(shake, blink);
        set.setInterpolator(new AccelerateDecelerateInterpolator());

        if (alTerminar != null) {
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    objetivo.setAlpha(1f);
                    objetivo.setTranslationX(0f);
                    alTerminar.run();
                }
            });
        }
        set.start();
    }

    private void reproducirSonidoMovimiento(String moveName) {
        try {
            String fileName = moveName.toLowerCase().replace("-", "_").replace(" ", "_") + ".mp3";
            if (mediaPlayer != null) { mediaPlayer.release(); }
            mediaPlayer = new MediaPlayer();
            AssetFileDescriptor descriptor = getAssets().openFd(fileName);
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void reproducirGritoEntrada(String audioUrl) {
        try {
            if (mediaPlayer != null) { mediaPlayer.release(); }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void playTransitionEffect() {
        AlphaAnimation blink = new AlphaAnimation(0.0f, 1.0f);
        blink.setDuration(100);
        blink.setRepeatCount(4);
        ivBattleArena.startAnimation(blink);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundMusic != null) { backgroundMusic.stop(); backgroundMusic.release(); backgroundMusic = null; }
        if (criticalMusic != null) { criticalMusic.stop(); criticalMusic.release(); criticalMusic = null; }
        if (victoryMusic != null) { victoryMusic.stop(); victoryMusic.release(); victoryMusic = null; }
        if (defeatMusic != null) { defeatMusic.stop(); defeatMusic.release(); defeatMusic = null; }
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
        if (typewriterHandler != null && typewriterRunnable != null) { typewriterHandler.removeCallbacks(typewriterRunnable); }
    }
}