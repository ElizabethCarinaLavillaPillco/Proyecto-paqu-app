package com.example.paqu.models;

/**
 * Modelo para palabras favoritas
 */
public class FavoritoPalabra {
    private String id;
    private String quechua;
    private String espanol;
    private String categoria;
    private String pronunciacion;
    private long fechaAgregado;

    public FavoritoPalabra() {
        // Constructor vacío para Firebase
    }

    public FavoritoPalabra(String id, String quechua, String espanol, String categoria, String pronunciacion) {
        this.id = id;
        this.quechua = quechua;
        this.espanol = espanol;
        this.categoria = categoria;
        this.pronunciacion = pronunciacion;
        this.fechaAgregado = System.currentTimeMillis();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuechua() { return quechua; }
    public void setQuechua(String quechua) { this.quechua = quechua; }

    public String getEspanol() { return espanol; }
    public void setEspanol(String espanol) { this.espanol = espanol; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getPronunciacion() { return pronunciacion; }
    public void setPronunciacion(String pronunciacion) { this.pronunciacion = pronunciacion; }

    public long getFechaAgregado() { return fechaAgregado; }
    public void setFechaAgregado(long fechaAgregado) { this.fechaAgregado = fechaAgregado; }
}