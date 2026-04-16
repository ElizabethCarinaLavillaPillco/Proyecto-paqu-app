package com.example.paqu.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Diccionario local de Quechua Cusqueño
 * Palabras básicas para traducción sin conexión
 */
public class DiccionarioQuechuaLocal {

    private static DiccionarioQuechuaLocal instance;
    private Map<String, String> quechuaAEspanol;
    private Map<String, String> espanolAQuechua;

    private DiccionarioQuechuaLocal() {
        inicializarDiccionario();
    }

    public static DiccionarioQuechuaLocal getInstance() {
        if (instance == null) {
            instance = new DiccionarioQuechuaLocal();
        }
        return instance;
    }

    private void inicializarDiccionario() {
        quechuaAEspanol = new HashMap<>();
        espanolAQuechua = new HashMap<>();

        // ========== SALUDOS ==========
        agregar("napaykullayki", "hola");
        agregar("allillanchu", "¿cómo estás?");
        agregar("allinllaymi", "estoy bien");
        agregar("imaynalla", "¿cómo estás?");
        agregar("waliqmi", "bien");
        agregar("tupananchiskama", "hasta luego");
        agregar("pacha kama", "adiós");
        agregar("tinkunanchiskama", "hasta pronto");

        // ========== FAMILIA ==========
        agregar("mama", "madre");
        agregar("tata", "padre");
        agregar("wawa", "bebé");
        agregar("wawqi", "hermano");
        agregar("ñaña", "hermana");
        agregar("paya", "abuela");
        agregar("awichu", "abuelo");
        agregar("churin", "hijo");
        agregar("ususi", "hija");
        agregar("ayllu", "familia");

        // ========== NÚMEROS ==========
        agregar("huk", "uno");
        agregar("iskay", "dos");
        agregar("kinsa", "tres");
        agregar("tawa", "cuatro");
        agregar("pichqa", "cinco");
        agregar("suqta", "seis");
        agregar("qanchis", "siete");
        agregar("pusaq", "ocho");
        agregar("isqun", "nueve");
        agregar("chunka", "diez");
        agregar("pachak", "cien");
        agregar("waranqa", "mil");

        // ========== COLORES ==========
        agregar("yuraq", "blanco");
        agregar("yana", "negro");
        agregar("puka", "rojo");
        agregar("q'illu", "amarillo");
        agregar("anqas", "azul");
        agregar("q'umir", "verde");
        agregar("uqi", "gris");
        agregar("ch'umpi", "café");

        // ========== COMIDA Y BEBIDA ==========
        agregar("mikhuy", "comida");
        agregar("upyay", "bebida");
        agregar("yaku", "agua");
        agregar("t'anta", "pan");
        agregar("sara", "maíz");
        agregar("papa", "papa");
        agregar("chuño", "papa deshidratada");
        agregar("ch'arki", "carne seca");
        agregar("api", "bebida de maíz");
        agregar("chicha", "bebida de maíz fermentada");
        agregar("ají", "ají");
        agregar("muña", "hierba aromática");

        // ========== ANIMALES ==========
        agregar("allqo", "perro");
        agregar("michi", "gato");
        agregar("wallpa", "gallina");
        agregar("urpi", "paloma");
        agregar("llama", "llama");
        agregar("alpaka", "alpaca");
        agregar("wik'uña", "vicuña");
        agregar("taruka", "venado");
        agregar("kuntur", "cóndor");
        agregar("challwa", "pez");

        // ========== NATURALEZA ==========
        agregar("inti", "sol");
        agregar("killa", "luna");
        agregar("quyllur", "estrella");
        agregar("wayra", "viento");
        agregar("para", "lluvia");
        agregar("chiri", "frío");
        agregar("q'uñi", "calor");
        agregar("urqu", "cerro");
        agregar("mayu", "río");
        agregar("qucha", "laguna");
        agregar("sach'a", "árbol");
        agregar("wayta", "flor");

        // ========== CUERPO ==========
        agregar("uma", "cabeza");
        agregar("ñawi", "ojo");
        agregar("rinri", "oreja");
        agregar("simi", "boca");
        agregar("maki", "mano");
        agregar("chaki", "pie");
        agregar("sunqu", "corazón");
        agregar("wiqsa", "estómago");

        // ========== ACCIONES ==========
        agregar("riy", "ir");
        agregar("hamuy", "venir");
        agregar("mikuy", "comer");
        agregar("upyay", "beber");
        agregar("puñuy", "dormir");
        agregar("llank'ay", "trabajar");
        agregar("yachay", "aprender");
        agregar("rimay", "hablar");
        agregar("qaway", "mirar");
        agregar("uyariy", "escuchar");
        agregar("takiy", "cantar");
        agregar("tusuy", "bailar");

        // ========== TIEMPO ==========
        agregar("p'unchaw", "día");
        agregar("tuta", "noche");
        agregar("paqarin", "mañana");
        agregar("kunan", "ahora");
        agregar("qayna", "ayer");
        agregar("wata", "año");
        agregar("killa", "mes");

        // ========== LUGARES ==========
        agregar("wasi", "casa");
        agregar("llaqta", "pueblo");
        agregar("hatun llaqta", "ciudad");
        agregar("qhatu", "mercado");
        agregar("chaqra", "chacra");
        agregar("ñan", "camino");
        agregar("pampa", "llanura");

        // ========== EXPRESIONES COMUNES ==========
        agregar("arí", "sí");
        agregar("mana", "no");
        agregar("añay", "¡qué bonito!");
        agregar("imaynam", "¿cómo?");
        agregar("mayqin", "¿cuál?");
        agregar("hayk'a", "¿cuánto?");
        agregar("imaynata", "¿cómo?");
        agregar("ima", "¿qué?");
        agregar("pi", "¿quién?");
        agregar("may", "¿dónde?");
        agregar("sulpayki", "gracias");
        agregar("yusulpayki", "muchas gracias");
        agregar("pampachaykuway", "discúlpame");
        agregar("haku", "vamos");

        // ========== ADJETIVOS ==========
        agregar("sumaq", "bonito");
        agregar("ch'usaq", "vacío");
        agregar("hunt'a", "lleno");
        agregar("hatun", "grande");
        agregar("huch'uy", "pequeño");
        agregar("allin", "bueno");
        agregar("mana allin", "malo");
        agregar("musuq", "nuevo");
        agregar("machu", "viejo");
        agregar("sinchi", "fuerte");
        agregar("llaki", "triste");
        agregar("kusikuy", "alegre");
    }

    /**
     * Agrega una palabra al diccionario en ambas direcciones
     */
    private void agregar(String quechua, String espanol) {
        quechuaAEspanol.put(quechua.toLowerCase(), espanol.toLowerCase());
        espanolAQuechua.put(espanol.toLowerCase(), quechua.toLowerCase());
    }

    /**
     * Traduce de Quechua a Español
     */
    public String traducirQuechuaAEspanol(String palabra) {
        String palabraLower = palabra.toLowerCase().trim();
        return quechuaAEspanol.getOrDefault(palabraLower, null);
    }

    /**
     * Traduce de Español a Quechua
     */
    public String traducirEspanolAQuechua(String palabra) {
        String palabraLower = palabra.toLowerCase().trim();
        return espanolAQuechua.getOrDefault(palabraLower, null);
    }

    /**
     * Traduce una frase completa palabra por palabra
     */
    public String traducirFrase(String frase, boolean quechuaAEspanol) {
        String[] palabras = frase.toLowerCase().trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        int palabrasTraducidas = 0;

        for (String palabra : palabras) {
            // Limpiar signos de puntuación
            String palabraLimpia = palabra.replaceAll("[^a-záéíóúñ']", "");

            String traduccion;
            if (quechuaAEspanol) {
                traduccion = traducirQuechuaAEspanol(palabraLimpia);
            } else {
                traduccion = traducirEspanolAQuechua(palabraLimpia);
            }

            if (traduccion != null) {
                resultado.append(traduccion).append(" ");
                palabrasTraducidas++;
            } else {
                resultado.append(palabra).append(" ");
            }
        }

        // Si no se tradujo ninguna palabra, retornar null
        if (palabrasTraducidas == 0) {
            return null;
        }

        return resultado.toString().trim();
    }

    /**
     * Verifica si una palabra existe en el diccionario
     */
    public boolean existePalabra(String palabra, boolean esQuechua) {
        String palabraLower = palabra.toLowerCase().trim();
        if (esQuechua) {
            return quechuaAEspanol.containsKey(palabraLower);
        } else {
            return espanolAQuechua.containsKey(palabraLower);
        }
    }

    /**
     * Obtiene el tamaño del diccionario
     */
    public int getTamanoDiccionario() {
        return quechuaAEspanol.size();
    }

    /**
     * Busca coincidencias parciales (para sugerencias)
     */
    public String buscarCoincidenciaParcial(String palabra, boolean esQuechua) {
        String palabraLower = palabra.toLowerCase().trim();
        Map<String, String> diccionario = esQuechua ? quechuaAEspanol : espanolAQuechua;

        // Buscar coincidencia exacta primero
        if (diccionario.containsKey(palabraLower)) {
            return diccionario.get(palabraLower);
        }

        // Buscar palabras que empiecen con el texto ingresado
        for (Map.Entry<String, String> entry : diccionario.entrySet()) {
            if (entry.getKey().startsWith(palabraLower)) {
                return entry.getValue() + " (sugerencia)";
            }
        }

        return null;
    }
}