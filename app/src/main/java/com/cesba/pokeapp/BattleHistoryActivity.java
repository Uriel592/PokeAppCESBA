package com.cesba.pokeapp;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattleHistoryActivity extends AppCompatActivity {

    private RecyclerView rvBattleHistory;
    private TextView tvEmptyState;
    private Button btnClearHistory;
    private BattleHistoryAdapter adapter;
    private List<BattleRecord> battleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_history);

        rvBattleHistory = findViewById(R.id.rvBattleHistory);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnClearHistory = findViewById(R.id.btnClearHistory);

        rvBattleHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BattleHistoryAdapter(battleList);
        rvBattleHistory.setAdapter(adapter);

        cargarHistorial();

        btnClearHistory.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("PokemonBattleHistory", MODE_PRIVATE);
            sp.edit().remove("registro_combates").apply();
            battleList.clear();
            adapter.notifyDataSetChanged();
            tvEmptyState.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Historial vaciado", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarHistorial() {
        battleList.clear();
        SharedPreferences sp = getSharedPreferences("PokemonBattleHistory", MODE_PRIVATE);
        String json = sp.getString("registro_combates", "[]");

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                battleList.add(new BattleRecord(
                        obj.optString("fecha", ""),
                        obj.optString("jugador", ""),
                        obj.optString("rival", ""),
                        obj.optString("resultado", "VICTORIA")
                ));
            }
            // Mostrar los combates más recientes primero
            Collections.reverse(battleList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (battleList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    // Modelo de datos interno
    public static class BattleRecord {
        public String fecha;
        public String jugador;
        public String rival;
        public String resultado;

        public BattleRecord(String fecha, String jugador, String rival, String resultado) {
            this.fecha = fecha;
            this.jugador = jugador;
            this.rival = rival;
            this.resultado = resultado;
        }
    }

    // Adaptador del RecyclerView
    public static class BattleHistoryAdapter extends RecyclerView.Adapter<BattleHistoryAdapter.ViewHolder> {
        private final List<BattleRecord> records;

        public BattleHistoryAdapter(List<BattleRecord> records) {
            this.records = records;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_battle_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BattleRecord item = records.get(position);
            holder.tvMatchDate.setText(item.fecha);
            holder.tvMatchVersus.setText(item.jugador + " vs " + item.rival);
            holder.tvResultBadge.setText(item.resultado);

            if ("VICTORIA".equalsIgnoreCase(item.resultado)) {
                holder.tvResultBadge.setTextColor(Color.parseColor("#50C878"));
            } else {
                holder.tvResultBadge.setTextColor(Color.parseColor("#E53E3E"));
            }
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvResultBadge, tvMatchDate, tvMatchVersus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvResultBadge = itemView.findViewById(R.id.tvResultBadge);
                tvMatchDate = itemView.findViewById(R.id.tvMatchDate);
                tvMatchVersus = itemView.findViewById(R.id.tvMatchVersus);
            }
        }
    }
}