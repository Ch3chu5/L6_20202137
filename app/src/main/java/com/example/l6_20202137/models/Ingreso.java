package com.example.l6_20202137.models;

import java.io.Serializable;
import java.util.Date;

public class Ingreso implements Serializable {
    private String id;
    private String titulo;
    private double monto;
    private String descripcion;
    private Date fecha;
    private String foto; // Campo que coincide con Firebase

    // Constructor vacío requerido para Firestore
    public Ingreso() {
    }

    public Ingreso(String titulo, double monto, String descripcion, Date fecha) {
        this.titulo = titulo;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    // Constructor con foto
    public Ingreso(String titulo, double monto, String descripcion, Date fecha, String foto) {
        this.titulo = titulo;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.foto = foto;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    // Método de compatibilidad para el adapter existente
    public String getUrlComprobante() {
        return foto;
    }

    public void setUrlComprobante(String urlComprobante) {
        this.foto = urlComprobante;
    }
}
