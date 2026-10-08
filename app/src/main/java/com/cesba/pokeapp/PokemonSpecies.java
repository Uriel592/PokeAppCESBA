package com.cesba.pokeapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonSpecies {

    @SerializedName("evolution_chain")
    private EvolutionChainRef evolutionChain;

    @SerializedName("flavor_text_entries")
    private List<FlavorTextEntry> flavorTextEntries;

    private List<Variety> varieties;

    public EvolutionChainRef getEvolutionChain() {
        return evolutionChain;
    }

    public List<FlavorTextEntry> getFlavorTextEntries() {
        return flavorTextEntries;
    }

    public List<Variety> getVarieties() {
        return varieties;
    }

    public static class EvolutionChainRef {
        private String url;
        public String getUrl() { return url; }
    }

    public static class FlavorTextEntry {
        @SerializedName("flavor_text")
        private String flavorText;

        @SerializedName("language")
        private Language language;

        public String getFlavorText() { return flavorText; }
        public Language getLanguage() { return language; }

        public static class Language {
            @SerializedName("name")
            private String name;
            public String getName() { return name; }
        }
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