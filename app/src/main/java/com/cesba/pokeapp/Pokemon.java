package com.cesba.pokeapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Pokemon {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("height")
    private int height;

    @SerializedName("weight")
    private int weight;

    @SerializedName("sprites")
    private Sprites sprites;

    @SerializedName("stats")
    private List<PokemonStat> stats;

    @SerializedName("cries")
    private Cries cries;

    @SerializedName("moves")
    private List<PokemonMove> moves;

    @SerializedName("types")
    private List<PokemonType> types;

    public int getId() { return id; }
    public String getName() { return name; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public Sprites getSprites() { return sprites; }
    public List<PokemonStat> getStats() { return stats; }
    public Cries getCries() { return cries; }
    public List<PokemonMove> getMoves() { return moves; }
    public List<PokemonType> getTypes() { return types; }

    public static class Cries {
        @SerializedName("latest")
        private String latest;

        @SerializedName("legacy")
        private String legacy;

        public String getLatest() { return latest; }
        public String getLegacy() { return legacy; }
    }

    public static class PokemonMove {
        @SerializedName("move")
        private Move move;

        public Move getMove() { return move; }

        public static class Move {
            @SerializedName("name")
            private String name;

            @SerializedName("url")
            private String url;

            public String getName() { return name; }
            public String getUrl() { return url; }
        }
    }

    public static class PokemonType {
        @SerializedName("type")
        private Type type;

        public Type getType() { return type; }

        public static class Type {
            @SerializedName("name")
            private String name;

            @SerializedName("url")
            private String url;

            public String getName() { return name; }
            public String getUrl() { return url; }
        }
    }
}