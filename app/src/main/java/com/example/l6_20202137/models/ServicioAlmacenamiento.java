package com.example.l6_20202137.models;

import android.util.Log;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Clase ServicioAlmacenamiento para gestionar archivos en Supabase Storage
 * Cumple con los requerimientos del prompt: conexión, guardar archivo y obtener archivo
 * Diseñada específicamente para manejar imágenes de boletas
 */
public class ServicioAlmacenamiento {
    private static final String TAG = "ServicioAlmacenamiento";

    // Configuración de Supabase
    private static final String SUPABASE_URL = "https://zozahkgzonwaemokkdsx.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpvemFoa2d6b253YWVtb2trZHN4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA4ODA5MjcsImV4cCI6MjA2NjQ1NjkyN30.ElzLNcpX9dUmYXJhxRX9Ocxgv7NqUmchAzuqvvsSsG4";
    private static final String BUCKET_NAME = "boletas-dinero";

    private OkHttpClient client;
    private boolean isConnected;

    public ServicioAlmacenamiento() {
        this.client = new OkHttpClient();
        this.isConnected = false;
    }

    /**
     * Método para realizar la conexión con el servicio Supabase Storage
     * Cumple requerimiento: "Conexión al servicio"
     * @return CompletableFuture<Boolean> que indica si la conexión fue exitosa
     */
    public CompletableFuture<Boolean> conectarServicio() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Log.i(TAG, "Iniciando conexión con Supabase Storage...");

        // Verificar conectividad con Supabase Storage
        String url = SUPABASE_URL + "/storage/v1/bucket/" + BUCKET_NAME;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error al conectar con Supabase Storage: " + e.getMessage());
                isConnected = false;
                future.complete(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() || response.code() == 404) {
                    // 404 es normal si el bucket no existe aún, pero la conexión funciona
                    Log.i(TAG, "Conexión exitosa con Supabase Storage");
                    isConnected = true;
                    future.complete(true);
                } else {
                    Log.e(TAG, "Error en la conexión. Código de respuesta: " + response.code());
                    isConnected = false;
                    future.complete(false);
                }
                response.close();
            }
        });

        return future;
    }

    /**
     * Método para guardar un archivo en Supabase Storage
     * Cumple requerimiento: "Guardar archivo"
     * @param nombreArchivo Nombre del archivo a guardar (ej: "boleta_egreso_123.jpg")
     * @param contenidoArchivo Contenido del archivo como byte array
     * @param contentType Tipo de contenido (ej: "image/jpeg", "image/png")
     * @return CompletableFuture<String> con la URL pública del archivo o null si hay error
     */
    public CompletableFuture<String> guardarArchivo(String nombreArchivo, byte[] contenidoArchivo, String contentType) {
        CompletableFuture<String> future = new CompletableFuture<>();

        if (!isConnected) {
            Log.e(TAG, "No hay conexión con Supabase Storage");
            future.complete(null);
            return future;
        }

        if (contenidoArchivo == null || contenidoArchivo.length == 0) {
            Log.e(TAG, "Contenido del archivo vacío");
            future.complete(null);
            return future;
        }

        Log.i(TAG, "Guardando archivo: " + nombreArchivo + " (" + contenidoArchivo.length + " bytes)");

        // URL para subir archivo a Supabase Storage
        String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + nombreArchivo;

        RequestBody body = RequestBody.create(contenidoArchivo, MediaType.parse(contentType));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .addHeader("Content-Type", contentType)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error al guardar archivo: " + e.getMessage());
                future.complete(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        // Archivo guardado exitosamente, generar URL pública
                        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + nombreArchivo;
                        Log.i(TAG, "Archivo guardado exitosamente: " + nombreArchivo);
                        Log.i(TAG, "URL pública: " + publicUrl);
                        future.complete(publicUrl);
                    } else if (response.code() == 409) {
                        // El archivo ya existe, intentar actualizar
                        Log.i(TAG, "Archivo existe, actualizando...");
                        actualizarArchivo(nombreArchivo, contenidoArchivo, contentType, future);
                    } else {
                        Log.e(TAG, "Error al guardar archivo. Código: " + response.code());
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.e(TAG, "Respuesta: " + responseBody);
                        future.complete(null);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al procesar respuesta: " + e.getMessage());
                    future.complete(null);
                } finally {
                    response.close();
                }
            }
        });

        return future;
    }

    /**
     * Método privado para actualizar un archivo existente
     */
    private void actualizarArchivo(String nombreArchivo, byte[] contenidoArchivo, String contentType, CompletableFuture<String> future) {
        String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + nombreArchivo;

        RequestBody body = RequestBody.create(contenidoArchivo, MediaType.parse(contentType));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .addHeader("Content-Type", contentType)
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error al actualizar archivo: " + e.getMessage());
                future.complete(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + nombreArchivo;
                        Log.i(TAG, "Archivo actualizado exitosamente: " + nombreArchivo);
                        future.complete(publicUrl);
                    } else {
                        Log.e(TAG, "Error al actualizar archivo. Código: " + response.code());
                        future.complete(null);
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    /**
     * Método para obtener un archivo específico de Supabase Storage
     * Cumple requerimiento: "Obtener archivo"
     * @param nombreArchivo Nombre del archivo a obtener
     * @return CompletableFuture<byte[]> con el contenido del archivo o null si no se encuentra
     */
    public CompletableFuture<byte[]> obtenerArchivo(String nombreArchivo) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        if (!isConnected) {
            Log.e(TAG, "No hay conexión con Supabase Storage");
            future.complete(null);
            return future;
        }

        Log.i(TAG, "Obteniendo archivo: " + nombreArchivo);

        // URL para obtener archivo de Supabase Storage
        String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + nombreArchivo;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error al obtener archivo: " + e.getMessage());
                future.complete(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        byte[] contenido = response.body().bytes();
                        Log.i(TAG, "Archivo obtenido exitosamente: " + nombreArchivo + " (" + contenido.length + " bytes)");
                        future.complete(contenido);
                    } else if (response.code() == 404) {
                        Log.w(TAG, "Archivo no encontrado: " + nombreArchivo);
                        future.complete(null);
                    } else {
                        Log.e(TAG, "Error al obtener archivo. Código: " + response.code());
                        future.complete(null);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al procesar respuesta: " + e.getMessage());
                    future.complete(null);
                } finally {
                    response.close();
                }
            }
        });

        return future;
    }

    /**
     * Método para obtener la URL pública de un archivo
     * Útil para mostrar imágenes sin descargar el archivo completo
     * @param nombreArchivo Nombre del archivo
     * @return String con la URL pública del archivo
     */
    public String obtenerUrlPublica(String nombreArchivo) {
        return SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + nombreArchivo;
    }

    /**
     * Método para verificar si hay conexión activa
     * @return boolean indicando el estado de la conexión
     */
    public boolean estaConectado() {
        return isConnected;
    }

    /**
     * Método para cerrar la conexión y liberar recursos
     */
    public void cerrarConexion() {
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
        isConnected = false;
        Log.i(TAG, "Conexión cerrada y recursos liberados");
    }
}
