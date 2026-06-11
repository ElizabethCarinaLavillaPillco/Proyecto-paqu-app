package com.example.paqu;

public class Usuario {
    public String nombre;
    public String correo;
    public String edad;
    public String role;
    public int racha;
    public int diamantes;
    public int vidas;
    public int puntajeEXP;
    public int seguidores;
    public int seguidos;

    public Usuario() {
    }

    public Usuario(String nombre, String correo, String edad) {
        this.nombre = nombre;
        this.correo = correo;
        this.edad = edad;

        // Rol por defecto
        this.role = "usuario_comun";

        this.racha = 0;
        this.diamantes = 100;
        this.vidas = 5;
        this.puntajeEXP = 0;
        this.seguidores = 0;
        this.seguidos = 0;
    }
}