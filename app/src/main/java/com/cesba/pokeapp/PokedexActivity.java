package com.cesba.pokeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class PokedexActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayerBGM, mediaPlayerCry;
    private EditText etSearch;
    private Button btnSearch, btnFormBase, btnFormRegional, btnFormMega, btnFormGmax;
    private Button btnPrevPokemon, btnNextPokemon, btnCry;
    private ImageView btnBack, ivPokemonImage, ivFavorite, btnPrevSprite, btnNextSprite;
    private ImageView ivEvo1, ivEvo2, ivEvo3;
    private TextView tvPokemonName, tvPokemonId, tvPokemonType, tvDescriptionContent, tvMovesContent;
    private TextView tvEvo1Name, tvEvo2Name, tvEvo3Name;
    private TextView tvHp, tvAtk, tvDef, tvSpeed, tabDescription, tabMoves, tabEvolution;
    private ProgressBar pbHp, pbAtk, pbDef, pbSpeed;
    private LinearLayout layoutEvolutionContent;

    private int currentPokemonId = 1;
    private Pokemon currentPokemonData;
    private List<PokemonSpecies.Variety> currentVarieties;
    private boolean isShiny = false;
    private boolean isFavorite = false;
    private String formActual = "base";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokedex);

        sharedPreferences = getSharedPreferences("PokeAppFavs", Context.MODE_PRIVATE);

        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack);
        btnCry = findViewById(R.id.btnCry);

        btnPrevPokemon = findViewById(R.id.btnPrevPokemon);
        btnNextPokemon = findViewById(R.id.btnNextPokemon);
        btnPrevSprite = findViewById(R.id.btnPrev);
        btnNextSprite = findViewById(R.id.btnNext);

        btnFormBase = findViewById(R.id.btnFormBase);
        btnFormRegional = findViewById(R.id.btnFormRegional);
        btnFormMega = findViewById(R.id.btnFormMega);
        btnFormGmax = findViewById(R.id.btnFormGmax);

        ivPokemonImage = findViewById(R.id.ivPokemonImage);
        ivFavorite = findViewById(R.id.ivFavorite);
        tvPokemonName = findViewById(R.id.tvPokemonName);
        tvPokemonId = findViewById(R.id.tvPokemonId);
        tvPokemonType = findViewById(R.id.tvPokemonType);

        tvHp = findViewById(R.id.tvHp);
        tvAtk = findViewById(R.id.tvAtk);
        tvDef = findViewById(R.id.tvDef);
        tvSpeed = findViewById(R.id.tvSpeed);

        pbHp = findViewById(R.id.pbHp);
        pbAtk = findViewById(R.id.pbAtk);
        pbDef = findViewById(R.id.pbDef);
        pbSpeed = findViewById(R.id.pbSpeed);

        tabDescription = findViewById(R.id.tabDescription);
        tabMoves = findViewById(R.id.tabMoves);
        tabEvolution = findViewById(R.id.tabEvolution);

        tvDescriptionContent = findViewById(R.id.tvDescriptionContent);
        tvMovesContent = findViewById(R.id.tvMovesContent);
        layoutEvolutionContent = findViewById(R.id.layoutEvolutionContent);

        ivEvo1 = findViewById(R.id.ivEvo1);
        ivEvo2 = findViewById(R.id.ivEvo2);
        ivEvo3 = findViewById(R.id.ivEvo3);
        tvEvo1Name = findViewById(R.id.tvEvo1Name);
        tvEvo2Name = findViewById(R.id.tvEvo2Name);
        tvEvo3Name = findViewById(R.id.tvEvo3Name);

        reproducirSonidoPokedex();

        btnBack.setOnClickListener(v -> finish());

        ivFavorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("fav_" + currentPokemonId, isFavorite);
            editor.apply();

            if (isFavorite) {
                ivFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                Toast.makeText(this, "¡Agregado a favoritos!", Toast.LENGTH_SHORT).show();
            } else {
                ivFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            }
        });

        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim().toLowerCase();
            if (!query.isEmpty()) {
                formActual = "base";
                isShiny = false;
                buscarPokemon(query);
            } else {
                Toast.makeText(PokedexActivity.this, "Ingresa un nombre o número", Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevPokemon.setOnClickListener(v -> {
            if (currentPokemonId > 1) {
                currentPokemonId--;
                formActual = "base";
                isShiny = false;
                buscarPokemon(String.valueOf(currentPokemonId));
            }
        });

        btnNextPokemon.setOnClickListener(v -> {
            currentPokemonId++;
            formActual = "base";
            isShiny = false;
            buscarPokemon(String.valueOf(currentPokemonId));
        });

        btnCry.setOnClickListener(v -> reproducirGritoPokemon());

        View.OnClickListener toggleShinyListener = v -> {
            isShiny = !isShiny;
            actualizarImagenPokemon();
            reproducirSonidoShiny();
        };
        btnPrevSprite.setOnClickListener(toggleShinyListener);
        btnNextSprite.setOnClickListener(toggleShinyListener);

        btnFormBase.setOnClickListener(v -> {
            formActual = "base";
            buscarPokemon(String.valueOf(currentPokemonId));
            Toast.makeText(this, "Forma Base", Toast.LENGTH_SHORT).show();
        });
        btnFormRegional.setOnClickListener(v -> { formActual = "regional"; cargarFormaAlternativaPorTipo("regional"); });
        btnFormMega.setOnClickListener(v -> { formActual = "mega"; cargarFormaAlternativaPorTipo("mega"); });
        btnFormGmax.setOnClickListener(v -> { formActual = "gmax"; cargarFormaAlternativaPorTipo("gmax"); });

        tabDescription.setOnClickListener(v -> cambiarPestaña(1));
        tabMoves.setOnClickListener(v -> cambiarPestaña(2));
        tabEvolution.setOnClickListener(v -> cambiarPestaña(3));

        buscarPokemon("1");
    }

    private void reproducirSonidoPokedex() {
        try {
            if (mediaPlayerBGM != null) { mediaPlayerBGM.release(); }
            mediaPlayerBGM = MediaPlayer.create(this, R.raw.bgm_pokedex);
            if (mediaPlayerBGM != null) {
                mediaPlayerBGM.setLooping(true);
                // Música de fondo al 50% de volumen
                mediaPlayerBGM.setVolume(0.5f, 0.5f);
                mediaPlayerBGM.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void reproducirSonidoShiny() {
        try {
            MediaPlayer shinyPlayer = new MediaPlayer();
            android.content.res.AssetFileDescriptor descriptor = getAssets().openFd("shiny_sound.mpeg");
            shinyPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            shinyPlayer.prepare();
            // Sonido shiny al 100% de volumen original
            shinyPlayer.setVolume(1.0f, 1.0f);
            shinyPlayer.start();
            shinyPlayer.setOnCompletionListener(MediaPlayer::release);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void reproducirGritoPokemon() {
        if (currentPokemonData != null && currentPokemonData.getCries() != null && currentPokemonData.getCries().getLatest() != null) {
            try {
                if (mediaPlayerCry != null) { mediaPlayerCry.release(); }
                mediaPlayerCry = new MediaPlayer();
                mediaPlayerCry.setDataSource(currentPokemonData.getCries().getLatest());
                // Gritos al 100% de volumen original
                mediaPlayerCry.setVolume(1.0f, 1.0f);
                mediaPlayerCry.prepareAsync();
                mediaPlayerCry.setOnPreparedListener(MediaPlayer::start);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "No se pudo reproducir el grito", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Grito no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarImagenPokemon() {
        if (currentPokemonData != null && currentPokemonData.getSprites() != null) {
            if (formActual.equals("base")) {
                String imageUrl = isShiny ? currentPokemonData.getSprites().getFrontShiny() : currentPokemonData.getSprites().getFrontDefault();
                if (imageUrl == null) imageUrl = currentPokemonData.getSprites().getFrontDefault();
                Glide.with(this).load(imageUrl).placeholder(android.R.drawable.ic_menu_gallery).into(ivPokemonImage);
            } else {
                if (formActual.equals("mega")) cargarFormaAlternativaPorTipo("mega");
                else if (formActual.equals("gmax")) cargarFormaAlternativaPorTipo("gmax");
                else if (formActual.equals("regional")) cargarFormaAlternativaPorTipo("regional");
            }
        }
    }

    private void cargarFormaAlternativaPorTipo(String tipoFiltro) {
        if (currentVarieties == null || currentVarieties.isEmpty()) {
            Toast.makeText(this, "Forma no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        String urlVariedad = null;
        String nombreRegionEncontrada = "";

        for (PokemonSpecies.Variety variety : currentVarieties) {
            String nombreVariedad = variety.getPokemon().getName().toLowerCase();
            if (!variety.isDefault()) {
                if (tipoFiltro.equals("regional")) {
                    if (nombreVariedad.contains("alola") || nombreVariedad.contains("galar") ||
                            nombreVariedad.contains("hisui") || nombreVariedad.contains("paldea")) {
                        urlVariedad = variety.getPokemon().getUrl();
                        if (nombreVariedad.contains("alola")) nombreRegionEncontrada = "Alola";
                        else if (nombreVariedad.contains("galar")) nombreRegionEncontrada = "Galar";
                        else if (nombreVariedad.contains("hisui")) nombreRegionEncontrada = "Hisui";
                        else if (nombreVariedad.contains("paldea")) nombreRegionEncontrada = "Paldea";
                        break;
                    }
                } else if (nombreVariedad.contains(tipoFiltro)) {
                    urlVariedad = variety.getPokemon().getUrl();
                    break;
                }
            }
        }

        if (urlVariedad == null) {
            int id = currentPokemonData.getId();
            String targetIdStr = "";
            if (tipoFiltro.equals("mega")) {
                if (id == 3) targetIdStr = "10033";
                else if (id == 6) targetIdStr = "10034";
                else if (id == 9) targetIdStr = "10036";
                else if (id == 150) targetIdStr = "10043";
            } else if (tipoFiltro.equals("gmax")) {
                if (id == 3) targetIdStr = "10197";
                else if (id == 6) targetIdStr = "10196";
                else if (id == 9) targetIdStr = "10198";
            } else if (tipoFiltro.equals("regional")) {
                if (id == 19) { targetIdStr = "10109"; nombreRegionEncontrada = "Alola"; }
                else if (id == 37) { targetIdStr = "10113"; nombreRegionEncontrada = "Alola"; }
                else if (id == 59) { targetIdStr = "10231"; nombreRegionEncontrada = "Hisui"; }
            }

            if (!targetIdStr.isEmpty()) {
                obtenerDatosFormaAlternativa(targetIdStr, nombreRegionEncontrada);
                return;
            } else {
                Toast.makeText(this, "Forma no disponible para este Pokémon", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (urlVariedad != null) {
            String[] partes = urlVariedad.split("/");
            String pokeId = partes[partes.length - 1];
            obtenerDatosFormaAlternativa(pokeId, nombreRegionEncontrada);
        }
    }

    private void obtenerDatosFormaAlternativa(String pokeId, String regionName) {
        PokeApiService apiService = ApiClient.getClient().create(PokeApiService.class);
        Call<Pokemon> call = apiService.getPokemon(pokeId);
        call.enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPokemonData = response.body();

                    String imageUrl = isShiny ? currentPokemonData.getSprites().getFrontShiny() : currentPokemonData.getSprites().getFrontDefault();
                    if (imageUrl == null) imageUrl = currentPokemonData.getSprites().getFrontDefault();
                    Glide.with(PokedexActivity.this).load(imageUrl).placeholder(android.R.drawable.ic_menu_gallery).into(ivPokemonImage);

                    if (!regionName.isEmpty()) {
                        Toast.makeText(PokedexActivity.this, "Forma regional: " + regionName, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PokedexActivity.this, "Forma cargada con éxito", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(PokedexActivity.this, "Error al cargar la forma", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarFormasEspeciales(int id) {
        btnFormMega.setVisibility(View.GONE);
        btnFormGmax.setVisibility(View.GONE);
        btnFormRegional.setVisibility(View.GONE);

        if (currentVarieties != null) {
            for (PokemonSpecies.Variety variety : currentVarieties) {
                String nombre = variety.getPokemon().getName().toLowerCase();
                if (nombre.contains("mega")) { btnFormMega.setVisibility(View.VISIBLE); }
                if (nombre.contains("gmax") || nombre.contains("gigantamax")) { btnFormGmax.setVisibility(View.VISIBLE); }
                if (nombre.contains("alola") || nombre.contains("galar") || nombre.contains("hisui") || nombre.contains("paldea")) { btnFormRegional.setVisibility(View.VISIBLE); }
            }
        }

        if (id == 3 || id == 6 || id == 9 || id == 150) btnFormMega.setVisibility(View.VISIBLE);
        if (id == 3 || id == 6 || id == 9) btnFormGmax.setVisibility(View.VISIBLE);
        if (id == 19 || id == 37 || id == 59) btnFormRegional.setVisibility(View.VISIBLE);
    }

    private void cambiarPestaña(int tabIndex) {
        tvDescriptionContent.setVisibility(View.GONE);
        tvMovesContent.setVisibility(View.GONE);
        layoutEvolutionContent.setVisibility(View.GONE);

        tabDescription.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tabMoves.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tabEvolution.setTextColor(getResources().getColor(android.R.color.darker_gray));

        if (tabIndex == 1) {
            tvDescriptionContent.setVisibility(View.VISIBLE);
            tabDescription.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (tabIndex == 2) {
            tvMovesContent.setVisibility(View.VISIBLE);
            tabMoves.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (tabIndex == 3) {
            layoutEvolutionContent.setVisibility(View.VISIBLE);
            tabEvolution.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void buscarPokemon(String query) {
        tvDescriptionContent.setText("Cargando descripción...");

        PokeApiService apiService = ApiClient.getClient().create(PokeApiService.class);
        Call<Pokemon> call = apiService.getPokemon(query);

        call.enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPokemonData = response.body();
                    currentPokemonId = currentPokemonData.getId();

                    isFavorite = sharedPreferences.getBoolean("fav_" + currentPokemonId, false);
                    if (isFavorite) {
                        ivFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                    } else {
                        ivFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                    }

                    String pokeName = currentPokemonData.getName().toLowerCase();
                    tvPokemonName.setText(pokeName.toUpperCase());
                    tvPokemonId.setText("#" + currentPokemonData.getId());
                    tvPokemonType.setText("ALT: " + currentPokemonData.getHeight() + " | PES: " + currentPokemonData.getWeight());

                    actualizarImagenPokemon();
                    cargarInformacionEspecie(String.valueOf(currentPokemonId));

                    if (currentPokemonData.getMoves() != null && !currentPokemonData.getMoves().isEmpty()) {
                        StringBuilder movesList = new StringBuilder("Movimientos principales:\n");
                        int limit = Math.min(currentPokemonData.getMoves().size(), 10);
                        for (int i = 0; i < limit; i++) {
                            movesList.append("• ").append(currentPokemonData.getMoves().get(i).getMove().getName().toUpperCase()).append("\n");
                        }
                        tvMovesContent.setText(movesList.toString());
                    } else {
                        tvMovesContent.setText("No hay movimientos registrados.");
                    }

                    if (currentPokemonData.getStats() != null) {
                        for (PokemonStat stat : currentPokemonData.getStats()) {
                            int val = stat.getBaseStat();
                            String statName = stat.getStat().getName();

                            if (statName.equals("hp")) {
                                tvHp.setText(String.valueOf(val));
                                pbHp.setProgress(val);
                            } else if (statName.equals("attack")) {
                                tvAtk.setText(String.valueOf(val));
                                pbAtk.setProgress(val);
                            } else if (statName.equals("defense")) {
                                tvDef.setText(String.valueOf(val));
                                pbDef.setProgress(val);
                            } else if (statName.equals("speed")) {
                                tvSpeed.setText(String.valueOf(val));
                                pbSpeed.setProgress(val);
                            }
                        }
                    }

                } else {
                    Toast.makeText(PokedexActivity.this, "Pokémon no encontrado", Toast.LENGTH_SHORT).show();
                    tvDescriptionContent.setText("No se encontró información.");
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(PokedexActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvDescriptionContent.setText("Error al conectar con el servidor.");
            }
        });
    }

    private void cargarInformacionEspecie(String idOrName) {
        PokeApiService apiService = ApiClient.getClient().create(PokeApiService.class);
        Call<PokemonSpecies> call = apiService.getPokemonSpecies(idOrName);
        call.enqueue(new Callback<PokemonSpecies>() {
            @Override
            public void onResponse(Call<PokemonSpecies> call, Response<PokemonSpecies> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PokemonSpecies species = response.body();
                    currentVarieties = species.getVarieties();
                    verificarFormasEspeciales(currentPokemonId);

                    String flavorText = "Descripción no disponible.";
                    if (species.getFlavorTextEntries() != null) {
                        for (PokemonSpecies.FlavorTextEntry entry : species.getFlavorTextEntries()) {
                            if (entry.getLanguage() != null && entry.getLanguage().getName().equals("es")) {
                                flavorText = entry.getFlavorText().replace("\n", " ").replace("\f", " ");
                                break;
                            } else if (entry.getLanguage() != null && entry.getLanguage().getName().equals("en") && flavorText.equals("Descripción no disponible.")) {
                                flavorText = entry.getFlavorText().replace("\n", " ").replace("\f", " ");
                            }
                        }
                    }
                    tvDescriptionContent.setText(flavorText);

                    if (species.getEvolutionChain() != null) {
                        String evolutionUrl = species.getEvolutionChain().getUrl();
                        String[] parts = evolutionUrl.split("/");
                        String chainId = parts[parts.length - 1];
                        cargarCadenaEvolutivaReal(chainId);
                    }
                }
            }

            @Override
            public void onFailure(Call<PokemonSpecies> call, Throwable t) {
                tvDescriptionContent.setText("No se pudo cargar la descripción.");
            }
        });
    }

    private void cargarCadenaEvolutivaReal(String chainId) {
        PokeApiService apiService = ApiClient.getClient().create(PokeApiService.class);
        Call<EvolutionChainResponse> call = apiService.getEvolutionChain(chainId);
        call.enqueue(new Callback<EvolutionChainResponse>() {
            @Override
            public void onResponse(Call<EvolutionChainResponse> call, Response<EvolutionChainResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EvolutionChainResponse.EvoNode chain = response.body().getChain();
                    List<String> evolutionNames = new ArrayList<>();
                    List<Integer> evolutionIds = new ArrayList<>();

                    extraerNodosEvolutivos(chain, evolutionNames, evolutionIds);
                    mostrarEvolucionesEnUI(evolutionNames, evolutionIds);
                }
            }

            @Override
            public void onFailure(Call<EvolutionChainResponse> call, Throwable t) {
            }
        });
    }

    private void extraerNodosEvolutivos(EvolutionChainResponse.EvoNode node, List<String> names, List<Integer> ids) {
        if (node == null || node.getSpecies() == null) return;

        String name = node.getSpecies().getName();
        String url = node.getSpecies().getUrl();

        try {
            String[] parts = url.split("/");
            int id = Integer.parseInt(parts[parts.length - 1]);
            names.add(name);
            ids.add(id);
        } catch (Exception e) { e.printStackTrace(); }

        if (node.getEvolvesTo() != null && !node.getEvolvesTo().isEmpty()) {
            extraerNodosEvolutivos(node.getEvolvesTo().get(0), names, ids);
        }
    }

    private void mostrarEvolucionesEnUI(List<String> names, List<Integer> ids) {
        ivEvo1.setVisibility(View.GONE); ivEvo2.setVisibility(View.GONE); ivEvo3.setVisibility(View.GONE);
        tvEvo1Name.setVisibility(View.GONE); tvEvo2Name.setVisibility(View.GONE); tvEvo3Name.setVisibility(View.GONE);

        if (ids.size() > 0) {
            ivEvo1.setVisibility(View.VISIBLE); tvEvo1Name.setVisibility(View.VISIBLE);
            tvEvo1Name.setText(names.get(0).toUpperCase() + " (#" + ids.get(0) + ")");
            Glide.with(this).load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + ids.get(0) + ".png").into(ivEvo1);
        }
        if (ids.size() > 1) {
            ivEvo2.setVisibility(View.VISIBLE); tvEvo2Name.setVisibility(View.VISIBLE);
            tvEvo2Name.setText(names.get(1).toUpperCase() + " (#" + ids.get(1) + ")");
            Glide.with(this).load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + ids.get(1) + ".png").into(ivEvo2);
        }
        if (ids.size() > 2) {
            ivEvo3.setVisibility(View.VISIBLE); tvEvo3Name.setVisibility(View.VISIBLE);
            tvEvo3Name.setText(names.get(2).toUpperCase() + " (#" + ids.get(2) + ")");
            Glide.with(this).load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + ids.get(2) + ".png").into(ivEvo3);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayerBGM != null && !mediaPlayerBGM.isPlaying()) { mediaPlayerBGM.start(); }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayerBGM != null && mediaPlayerBGM.isPlaying()) { mediaPlayerBGM.pause(); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayerBGM != null) { mediaPlayerBGM.release(); mediaPlayerBGM = null; }
        if (mediaPlayerCry != null) { mediaPlayerCry.release(); mediaPlayerCry = null; }
    }
}