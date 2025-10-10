package com.example.paqu;

public class User {
    private String nombre;
    private String email;
    private int seguidores;
    private int siguiendo;
    private int avatar;

    // Constructor vacío necesario para Firebase
    public User() {}

    public User(String nombre, String email, int seguidores, int siguiendo, int avatar) {
        this.nombre = nombre;
        this.email = email;
        this.seguidores = seguidores;
        this.siguiendo = siguiendo;
        this.avatar = avatar;
    }

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getSeguidores() { return seguidores; }
    public void setSeguidores(int seguidores) { this.seguidores = seguidores; }

    public int getSiguiendo() { return siguiendo; }
    public void setSiguiendo(int siguiendo) { this.siguiendo = siguiendo; }

    public int getAvatar() { return avatar; }
    public void setAvatar(int avatar) { this.avatar = avatar; }
}