package com.example.l6_20202137.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.l6_20202137.R;
import com.example.l6_20202137.models.Ingreso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IngresoAdapter extends RecyclerView.Adapter<IngresoAdapter.IngresoViewHolder> {

    private List<Ingreso> listaIngresos;
    private OnIngresoListener listener;
    private SimpleDateFormat dateFormat;

    public IngresoAdapter(List<Ingreso> listaIngresos, OnIngresoListener listener) {
        this.listaIngresos = listaIngresos;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public IngresoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingreso, parent, false);
        return new IngresoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull IngresoViewHolder holder, int position) {
        Ingreso ingreso = listaIngresos.get(position);

        holder.tvTitulo.setText(ingreso.getTitulo());
        holder.tvMonto.setText(String.format(Locale.getDefault(), "S/ %.2f", ingreso.getMonto()));

        // Mostrar descripción si existe, de lo contrario mostrar "Sin descripción"
        if (ingreso.getDescripcion() != null && !ingreso.getDescripcion().isEmpty()) {
            holder.tvDescripcion.setText(ingreso.getDescripcion());
        } else {
            holder.tvDescripcion.setText("Sin descripción");
        }

        // Formatear y mostrar la fecha
        if (ingreso.getFecha() != null) {
            holder.tvFecha.setText(dateFormat.format(ingreso.getFecha()));
        } else {
            holder.tvFecha.setText("Fecha no disponible");
        }

        // Configurar listeners para los botones
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarClick(ingreso);
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarClick(ingreso);
            }
        });

        // Configurar listener para el ítem completo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIngresoClick(ingreso);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaIngresos.size();
    }

    // ViewHolder para mantener las referencias a las vistas
    public static class IngresoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMonto, tvDescripcion, tvFecha;
        ImageButton btnEditar, btnEliminar;

        public IngresoViewHolder(@NonNull View itemView) {
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
    public interface OnIngresoListener {
        void onIngresoClick(Ingreso ingreso);
        void onEditarClick(Ingreso ingreso);
        void onEliminarClick(Ingreso ingreso);
    }
}
