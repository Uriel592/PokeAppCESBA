package com.cesba.pokeapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonSpeciesResponse {

    @SerializedName("evolution_chain")
    private EvolutionChainRef evolutionChain;

    private List<Variety> varieties;

    public EvolutionChainRef getEvolutionChain() {
        return evolutionChain;
    }

    public List<Variety> getVarieties() {
        return varieties;
    }

    public static class EvolutionChainRef {
        private String url;
        public String getUrl() { return url; }
    }

    public static class Variety {
        @SerializedName("is_default")
        private boolean isDefault;
        private PokemonRef pokemon;

        public boolean isDefault() { return isDefault; }
        public PokemonRef getPokemon() { return pokemon; }
    }

    public static class PokemonRef {
        private String name;
        private String url;
        public String getName() { return name; }
        public String getUrl() { return url; }
    }
}