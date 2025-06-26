package com.example.l6_20202137.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.l6_20202137.R;
import com.example.l6_20202137.models.Ingreso;
import com.example.l6_20202137.models.ServicioAlmacenamiento;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IngresoAdapter extends RecyclerView.Adapter<IngresoAdapter.IngresoViewHolder> {

    private List<Ingreso> listaIngresos;
    private OnIngresoListener listener;
    private SimpleDateFormat dateFormat;
    private ServicioAlmacenamiento servicioAlmacenamiento;

    public IngresoAdapter(List<Ingreso> listaIngresos, OnIngresoListener listener) {
        this.listaIngresos = listaIngresos;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.servicioAlmacenamiento = new ServicioAlmacenamiento();
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

        // NUEVA FUNCIONALIDAD: Cargar y mostrar imagen del comprobante
        cargarImagenComprobante(ingreso, holder.ivComprobante);

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

    /**
     * Método optimizado para cargar la imagen del comprobante desde URL pública
     */
    private void cargarImagenComprobante(Ingreso ingreso, ImageView imageView) {
        if (ingreso.getFoto() != null && !ingreso.getFoto().isEmpty()) {
            // Mostrar indicador de carga
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            imageView.setVisibility(View.VISIBLE);
            imageView.setScaleType(ImageView.ScaleType.CENTER);

            // Cargar imagen usando OkHttp directamente desde la URL pública
            cargarImagenDesdeURL(ingreso.getFoto(), imageView);
        } else {
            // No hay comprobante, ocultar ImageView
            imageView.setVisibility(View.GONE);
        }
    }

    /**
     * Método para cargar imagen directamente desde URL usando OkHttp
     */
    private void cargarImagenDesdeURL(String url, ImageView imageView) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Mostrar imagen de error en el hilo principal
                imageView.post(() -> mostrarImagenError(imageView));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] bytesImagen = response.body().bytes();

                        // Convertir a Bitmap en el hilo principal
                        imageView.post(() -> {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytesImagen, 0, bytesImagen.length);
                                if (bitmap != null) {
                                    imageView.setImageBitmap(bitmap);
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                } else {
                                    mostrarImagenError(imageView);
                                }
                            } catch (Exception e) {
                                mostrarImagenError(imageView);
                            }
                        });
                    } catch (Exception e) {
                        imageView.post(() -> mostrarImagenError(imageView));
                    }
                } else {
                    imageView.post(() -> mostrarImagenError(imageView));
                }
                response.close();
            }
        });
    }

    /**
     * Mostrar imagen de error cuando no se puede cargar el comprobante
     */
    private void mostrarImagenError(ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_image_error);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
    }

    @Override
    public int getItemCount() {
        return listaIngresos.size();
    }

    // ViewHolder para mantener las referencias a las vistas
    public static class IngresoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMonto, tvDescripcion, tvFecha;
        ImageButton btnEditar, btnEliminar;
        ImageView ivComprobante; // NUEVO: ImageView para mostrar el comprobante

        public IngresoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            ivComprobante = itemView.findViewById(R.id.ivComprobante); // NUEVO
        }
    }

    // Interfaz para manejar eventos de clic
    public interface OnIngresoListener {
        void onIngresoClick(Ingreso ingreso);
        void onEditarClick(Ingreso ingreso);
        void onEliminarClick(Ingreso ingreso);
    }

    /**
     * Limpiar recursos al destruir el adapter
     */
    public void limpiarRecursos() {
        if (servicioAlmacenamiento != null) {
            servicioAlmacenamiento.cerrarConexion();
        }
    }
}
