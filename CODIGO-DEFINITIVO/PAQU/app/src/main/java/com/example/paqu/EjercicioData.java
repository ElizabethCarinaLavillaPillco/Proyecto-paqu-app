package com.example.paqu;

import java.util.List;

public class EjercicioData {
    public String palabraCorrecta;
    public int audioPrincipal;
    public String imgText;
    public int img;
    public List<String> palabras;

    public EjercicioData(String palabraCorrecta, int audioPrincipal, String imgText, int img, List<String> palabras) {
        this.palabraCorrecta = palabraCorrecta;
        this.audioPrincipal = audioPrincipal;
        this.imgText = imgText;
        this.img = img;
        this.palabras = palabras;
    }
}
