package com.cesba.pokeapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EvolutionChainResponse {

    private EvoNode chain;

    public EvoNode getChain() {
        return chain;
    }

    public static class EvoNode {
        private NamedAPIResource species;

        @SerializedName("evolves_to")
        private List<EvoNode> evolvesTo;

        public NamedAPIResource getSpecies() {
            return species;
        }

        public List<EvoNode> getEvolvesTo() {
            return evolvesTo;
        }
    }

    public static class NamedAPIResource {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }
}