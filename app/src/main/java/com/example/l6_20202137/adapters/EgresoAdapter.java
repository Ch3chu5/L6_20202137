package com.example.l6_20202137.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.l6_20202137.R;
import com.example.l6_20202137.models.Egreso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EgresoAdapter extends RecyclerView.Adapter<EgresoAdapter.EgresoViewHolder> {

    private List<Egreso> listaEgresos;
    private OnEgresoListener listener;
    private SimpleDateFormat dateFormat;

    public EgresoAdapter(List<Egreso> listaEgresos, OnEgresoListener listener) {
        this.listaEgresos = listaEgresos;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public EgresoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_egreso, parent, false);
        return new EgresoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EgresoViewHolder holder, int position) {
        Egreso egreso = listaEgresos.get(position);

        holder.tvTitulo.setText(egreso.getTitulo());
        holder.tvMonto.setText(String.format(Locale.getDefault(), "S/ %.2f", egreso.getMonto()));

        // Mostrar descripción si existe, de lo contrario mostrar "Sin descripción"
        if (egreso.getDescripcion() != null && !egreso.getDescripcion().isEmpty()) {
            holder.tvDescripcion.setText(egreso.getDescripcion());
        } else {
            holder.tvDescripcion.setText("Sin descripción");
        }

        // Formatear y mostrar la fecha
        if (egreso.getFecha() != null) {
            holder.tvFecha.setText(dateFormat.format(egreso.getFecha()));
        } else {
            holder.tvFecha.setText("Fecha no disponible");
        }

        // Configurar listeners para los botones
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarClick(egreso);
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarClick(egreso);
            }
        });

        // Configurar listener para el ítem completo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEgresoClick(egreso);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaEgresos.size();
    }

    // ViewHolder para mantener las referencias a las vistas
    public static class EgresoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMonto, tvDescripcion, tvFecha;
        ImageButton btnEditar, btnEliminar;

        public EgresoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }

    // Interfaz para manejar eventos de clic
    public interface OnEgresoListener {
        void onEgresoClick(Egreso egreso);
        void onEditarClick(Egreso egreso);
        void onEliminarClick(Egreso egreso);
    }
}