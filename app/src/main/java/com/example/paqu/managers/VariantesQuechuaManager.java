package com.example.paqu.managers;

import com.example.paqu.models.VarianteQuechua;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager que contiene información de todas las variantes del Quechua en Perú
 */
public class VariantesQuechuaManager {

    private static VariantesQuechuaManager instance;
    private List<VarianteQuechua> variantes;

    private VariantesQuechuaManager() {
        inicializarVariantes();
    }

    public static synchronized VariantesQuechuaManager getInstance() {
        if (instance == null) {
            instance = new VariantesQuechuaManager();
        }
        return instance;
    }

    private void inicializarVariantes() {
        variantes = new ArrayList<>();

        // 1. QUECHUA CUSQUEÑO (Qusqu Runasimi)
        variantes.add(new VarianteQuechua(
                "cusco",
                "Quechua Cusqueño",
                "Cusco y Puno",
                "Cusco",
                "La variante más hablada y considerada estándar. Se habla en Cusco, Apurímac y parte de Puno.",
                "Uso de -sqa para el pasado perfectivo, pronunciación de 'q' uvular",
                1500000,
                "Allillanchu",
                "¿Cómo estás?",
                -13.5319,  // Cusco
                -71.9675,
                "#FF6B35",  // Naranja/terracota
                true
        ));

        // 2. QUECHUA AYACUCHANO (Chanka Runasimi)
        variantes.add(new VarianteQuechua(
                "ayacucho",
                "Quechua Ayacuchano",
                "Ayacucho y Huancavelica",
                "Ayacucho",
                "Segunda variante más hablada. Usada en Ayacucho, Huancavelica y norte de Arequipa.",
                "Tres vocales (i, a, u), uso de -sqa y -rqa para pasados",
                900000,
                "Allinllachu",
                "¿Cómo estás?",
                -13.1637,  // Ayacucho
                -74.2235,
                "#9B59B6",  // Morado
                true
        ));

        // 3. QUECHUA ANCASHINO (Ankash Runasimi)
        variantes.add(new VarianteQuechua(
                "ancash",
                "Quechua Ancashino",
                "Áncash",
                "Ancash",
                "Variante norteña con características únicas. Hablada en el departamento de Áncash.",
                "Sistema de cinco vocales, influencia del español",
                250000,
                "Allinllachum",
                "¿Cómo estás?",
                -9.5282,   // Huaraz
                -77.5278,
                "#3498DB",  // Azul
                false
        ));

        // 4. QUECHUA CAJAMARCA-CAÑARIS
        variantes.add(new VarianteQuechua(
                "cajamarca",
                "Quechua Cajamarquino",
                "Cajamarca",
                "Cajamarca",
                "Variante norteña en riesgo. Se habla en zonas rurales de Cajamarca.",
                "Cinco vocales, pérdida de algunas consonantes originales",
                150000,
                "Allinllachu",
                "¿Cómo estás?",
                -7.1619,   // Cajamarca
                -78.5129,
                "#E74C3C",  // Rojo
                false
        ));

        // 5. QUECHUA CHACHAPOYAS-LAMAS (San Martín)
        variantes.add(new VarianteQuechua(
                "sanmartin",
                "Quechua Lamista",
                "San Martín",
                "San Martín",
                "Variante amazónica con influencias de lenguas de la selva.",
                "Vocabulario único, influencias del shipibo y otras lenguas amazónicas",
                80000,
                "Allin kaushaykichik",
                "¿Cómo están?",
                -6.4869,   // Lamas
                -76.5208,
                "#27AE60",  // Verde
                false
        ));

        // 6. QUECHUA HUÁNUCO-HUALLAGA
        variantes.add(new VarianteQuechua(
                "huanuco",
                "Quechua Huanuqueño",
                "Huánuco y Pasco",
                "Huánuco",
                "Variante central hablada en Huánuco y Pasco.",
                "Intermedia entre quechua I y II, características mixtas",
                180000,
                "Allinllachu kaa",
                "¿Cómo estás?",
                -9.9306,   // Huánuco
                -76.2422,
                "#F39C12",  // Amarillo/Dorado
                false
        ));

        // 7. QUECHUA JUNÍN-HUANCA
        variantes.add(new VarianteQuechua(
                "junin",
                "Quechua Huanca",
                "Junín",
                "Junín",
                "Hablada en el valle del Mantaro, Junín. Muy vitalizada en comunidades rurales.",
                "Sistema de cinco vocales, léxico propio",
                120000,
                "Allinllachu kanki",
                "¿Cómo estás?",
                -12.0689,  // Huancayo
                -75.2049,
                "#1ABC9C",  // Turquesa
                false
        ));

        // 8. QUECHUA YAUYOS (Lima)
        variantes.add(new VarianteQuechua(
                "yauyos",
                "Quechua Yauyino",
                "Lima (Yauyos)",
                "Lima",
                "Variante en riesgo crítico. Hablada en la provincia de Yauyos, Lima.",
                "Conserva características arcaicas, vocabulario único",
                5000,
                "Allinllachu kay",
                "¿Cómo estás?",
                -12.4500,  // Yauyos
                -75.9167,
                "#E67E22",  // Naranja oscuro
                false
        ));

        // 9. QUECHUA PUNO (Collao)
        variantes.add(new VarianteQuechua(
                "puno",
                "Quechua Puneño",
                "Puno",
                "Puno",
                "Variante del altiplano con influencia aymara. Hablada en Puno.",
                "Influencias del aymara, léxico compartido",
                400000,
                "Kamisaraki",
                "¿Cómo estás?",
                -15.8402,  // Puno
                -70.0219,
                "#8E44AD",  // Púrpura
                false
        ));

        // 10. QUECHUA APURÍMAC
        variantes.add(new VarianteQuechua(
                "apurimac",
                "Quechua Apurimeño",
                "Apurímac",
                "Apurímac",
                "Cercana al cusqueño. Hablada en Apurímac y zonas aledañas.",
                "Similar al cusqueño, variaciones en pronunciación",
                300000,
                "Allillanchu kachkanki",
                "¿Cómo estás?",
                -13.6447,  // Abancay
                -72.8814,
                "#D35400",  // Naranja quemado
                false
        ));
    }

    public List<VarianteQuechua> getVariantes() {
        return new ArrayList<>(variantes);
    }

    public List<VarianteQuechua> getVariantesPrincipales() {
        List<VarianteQuechua> principales = new ArrayList<>();
        for (VarianteQuechua variante : variantes) {
            if (variante.isPrincipal()) {
                principales.add(variante);
            }
        }
        return principales;
    }

    public VarianteQuechua getVariantePorId(String id) {
        for (VarianteQuechua variante : variantes) {
            if (variante.getId().equals(id)) {
                return variante;
            }
        }
        return null;
    }

    public int getTotalHablantes() {
        int total = 0;
        for (VarianteQuechua variante : variantes) {
            total += variante.getHablantes();
        }
        return total;
    }

    public String getEstadisticasResumen() {
        int total = getTotalHablantes();
        int numVariantes = variantes.size();
        return String.format("%.1fM hablantes en %d variantes", total / 1000000.0, numVariantes);
    }
}