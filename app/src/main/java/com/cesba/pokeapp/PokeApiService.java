package com.cesba.pokeapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PokeApiService {

    @GET("pokemon/{id}")
    Call<Pokemon> getPokemon(@Path("id") String id);

    @GET("pokemon-species/{id}")
    Call<PokemonSpecies> getPokemonSpecies(@Path("id") String id);

    @GET("evolution-chain/{id}")
    Call<EvolutionChainResponse> getEvolutionChain(@Path("id") String id);

}