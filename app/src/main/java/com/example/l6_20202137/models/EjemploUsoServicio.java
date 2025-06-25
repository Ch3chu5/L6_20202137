package com.example.l6_20202137.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Clase de ejemplo para demostrar el uso de ServicioAlmacenamiento
 * Esta clase servirá como base para la futura implementación de boletas
 */
public class EjemploUsoServicio {
    private static final String TAG = "EjemploUsoServicio";
    private ServicioAlmacenamiento servicioAlmacenamiento;

    public EjemploUsoServicio() {
        this.servicioAlmacenamiento = new ServicioAlmacenamiento();
    }

    /**
     * Método de ejemplo que demuestra el uso completo del ServicioAlmacenamiento
     * Esto prepara el terreno para la futura funcionalidad de boletas
     */
    public void ejemploCompletoDeUso() {
        Log.i(TAG, "=== DEMO DE SERVICIO ALMACENAMIENTO ===");

        // 1. Conectar al servicio (requerimiento del prompt)
        servicioAlmacenamiento.conectarServicio().thenAccept(conectado -> {
            if (conectado) {
                Log.i(TAG, "✅ Conexión exitosa con Supabase Storage");

                // Ejemplo de datos que simula una boleta
                String contenidoEjemplo = "Datos de ejemplo para demostrar funcionamiento";
                byte[] datosEjemplo = contenidoEjemplo.getBytes();

                // 2. Guardar archivo (requerimiento del prompt)
                String nombreArchivo = "ejemplo_boleta_" + System.currentTimeMillis() + ".txt";
                servicioAlmacenamiento.guardarArchivo(nombreArchivo, datosEjemplo, "text/plain")
                    .thenAccept(urlPublica -> {
                        if (urlPublica != null) {
                            Log.i(TAG, "✅ Archivo guardado exitosamente");
                            Log.i(TAG, "📄 Nombre: " + nombreArchivo);
                            Log.i(TAG, "🔗 URL: " + urlPublica);

                            // 3. Obtener archivo (requerimiento del prompt)
                            servicioAlmacenamiento.obtenerArchivo(nombreArchivo)
                                .thenAccept(contenidoDescargado -> {
                                    if (contenidoDescargado != null) {
                                        String contenidoTexto = new String(contenidoDescargado);
                                        Log.i(TAG, "✅ Archivo obtenido exitosamente");
                                        Log.i(TAG, "📝 Contenido: " + contenidoTexto);
                                        Log.i(TAG, "=== DEMO COMPLETADA EXITOSAMENTE ===");
                                    } else {
                                        Log.e(TAG, "❌ Error al obtener archivo");
                                    }
                                });
                        } else {
                            Log.e(TAG, "❌ Error al guardar archivo");
                        }
                    });
            } else {
                Log.e(TAG, "❌ Error de conexión con Supabase Storage");
            }
        });
    }

    /**
     * Método de utilidad para convertir Bitmap a byte array
     * Esto será útil para la futura funcionalidad de boletas
     */
    public byte[] bitmapToByteArray(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, stream);
        return stream.toByteArray();
    }

    /**
     * Método para generar nombres únicos de archivos
     * Formato preparado para boletas: boleta_{tipo}_{userId}_{timestamp}.{ext}
     */
    public String generarNombreArchivo(String tipo, String userId, String extension) {
        long timestamp = System.currentTimeMillis();
        return String.format("boleta_%s_%s_%d.%s", tipo, userId, timestamp, extension);
    }

    /**
     * Método para cerrar el servicio
     */
    public void cerrar() {
        if (servicioAlmacenamiento != null) {
            servicioAlmacenamiento.cerrarConexion();
        }
    }
}
