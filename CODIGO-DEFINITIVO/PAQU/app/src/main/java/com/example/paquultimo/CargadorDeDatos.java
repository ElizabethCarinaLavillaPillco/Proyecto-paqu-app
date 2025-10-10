package com.example.paquultimo;

import java.util.Arrays;

public class CargadorDeDatos {

    public static EjercicioData obtenerDatos(int nivel, int ejercicio) {
        if (nivel == 1) {
            switch (ejercicio) {
                case 1:
                    return new EjercicioData(
                            "t'anta wawa",
                            R.raw.voz_tantawawa,
                            "Muñeca de pan",
                            R.drawable.tanta_wawa,
                            Arrays.asList("ch’usaqchakuy", "wiñaypaq", "t'anta", "samay", "wawa")
                    );

                case 2:
                    return new EjercicioData(
                            "chaymi sumaq kawsay",
                            R.raw.voz1,
                            "Muñeca de pan",
                            R.drawable.tanta_wawa,
                            Arrays.asList("chaymi", "sumaq", "kawsay", "ch’usaqchakuy")
                    );

                case 3:
                    return new EjercicioData(
                            "sumaq punchay kachun",
                            R.raw.voz2,
                            "Muñeca de pan",
                            R.drawable.tanta_wawa,
                            Arrays.asList("sumaq", "punchay", "kachun", "t’anta")
                    );

                case 4:
                    return new EjercicioData(
                            "mana allinmi",
                            R.raw.voz3,
                            "Muñeca de pan",
                            R.drawable.tanta_wawa,
                            Arrays.asList("mana", "allinmi", "kawsay", "winay")
                    );

                case 5:
                    return new EjercicioData(
                            "ñuqaqa allin kani",
                            R.raw.voz4,
                            "Muñeca de pan",
                            R.drawable.tanta_wawa,
                            Arrays.asList("ñuqaqa", "allin", "kani", "samay", "wiñaypaq")
                    );
            }
        }

        // Puedes agregar más niveles aquí
        return null;
    }
}
