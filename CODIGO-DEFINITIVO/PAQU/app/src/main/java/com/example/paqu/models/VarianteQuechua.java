package com.example.paqu.models;

/**
 * Modelo para representar una variante del Quechua
 */
public class VarianteQuechua {
    private String id;
    private String nombre;              // "Quechua Cusqueño"
    private String region;              // "Cusco"
    private String departamento;        // "Cusco"
    private String descripcion;         // Descripción de la variante
    private String caracteristicas;     // Características lingüísticas
    private int hablantes;             // Número de hablantes
    private String ejemploPalabra;     // Palabra ejemplo
    private String ejemploTraduccion;  // Traducción del ejemplo
    private double latitud;            // Coordenadas para el mapa
    private double longitud;
    private String color;              // Color identificador
    private String audioUrl;           // URL del audio de ejemplo
    private boolean principal;         // Si es una variante principal

    // Constructores
    public VarianteQuechua() {}

    public VarianteQuechua(String id, String nombre, String region, String departamento,
                           String descripcion, String caracteristicas, int hablantes,
                           String ejemploPalabra, String ejemploTraduccion,
                           double latitud, double longitud, String color, boolean principal) {
        this.id = id;
        this.nombre = nombre;
        this.region = region;
        this.departamento = departamento;
        this.descripcion = descripcion;
        this.caracteristicas = caracteristicas;
        this.hablantes = hablantes;
        this.ejemploPalabra = ejemploPalabra;
        this.ejemploTraduccion = ejemploTraduccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.color = color;
        this.principal = principal;
        this.audioUrl = "";
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCaracteristicas() { return caracteristicas; }
    public void setCaracteristicas(String caracteristicas) { this.caracteristicas = caracteristicas; }

    public int getHablantes() { return hablantes; }
    public void setHablantes(int hablantes) { this.hablantes = hablantes; }

    public String getEjemploPalabra() { return ejemploPalabra; }
    public void setEjemploPalabra(String ejemploPalabra) { this.ejemploPalabra = ejemploPalabra; }

    public String getEjemploTraduccion() { return ejemploTraduccion; }
    public void setEjemploTraduccion(String ejemploTraduccion) { this.ejemploTraduccion = ejemploTraduccion; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }

    public String getHablantesFormateado() {
        if (hablantes >= 1000000) {
            return String.format("%.1fM", hablantes / 1000000.0);
        } else if (hablantes >= 1000) {
            return String.format("%.1fK", hablantes / 1000.0);
        }
        return String.valueOf(hablantes);
    }
}