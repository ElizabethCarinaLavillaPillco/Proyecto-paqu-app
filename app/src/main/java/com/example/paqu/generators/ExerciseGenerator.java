package com.example.paqu.generators;

import com.example.paqu.R;
import com.example.paqu.models.TimeAttackExercise;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Generador de ejercicios para el reto contrarreloj
 * Tema: Saludos en Quechua
 */
public class ExerciseGenerator {

    public static List<TimeAttackExercise> generateGreetingsExercises() {
        List<TimeAttackExercise> exercises = new ArrayList<>();

        // Ejercicio 1: Traducir "Hola" - Selección múltiple
        TimeAttackExercise ex1 = new TimeAttackExercise();
        ex1.setId("ex1");
        ex1.setType(TimeAttackExercise.ExerciseType.SELECT_CORRECT);
        ex1.setQuestion("¿Cómo se dice 'Hola' en Quechua?");
        ex1.setCorrectAnswer("Napaykullayki");
        ex1.setOptions(Arrays.asList("Napaykullayki", "Allinllachu", "Tupananchiskama", "Imaynallan"));
        ex1.setAudioResId(R.raw.voz4); // Audio: "Napaykullayki"
        ex1.setImageResId(R.drawable.avatar2);
        exercises.add(ex1);

        // Ejercicio 2: Traducir "¿Cómo estás?" - Selección múltiple
        TimeAttackExercise ex2 = new TimeAttackExercise();
        ex2.setId("ex2");
        ex2.setType(TimeAttackExercise.ExerciseType.SELECT_CORRECT);
        ex2.setQuestion("Escucha el audio y selecciona la traducción correcta");
        ex2.setQuechuaText("Imaynallan kashanki?");
        ex2.setCorrectAnswer("¿Cómo estás?");
        ex2.setOptions(Arrays.asList("¿Cómo estás?", "Hola", "Adiós", "Buenos días"));
        ex2.setAudioResId(R.raw.voz_buenas_noches);
        ex2.setImageResId(R.drawable.avatar4);
        exercises.add(ex2);

        // Ejercicio 3: Formar oración "Estoy bien"
        TimeAttackExercise ex3 = new TimeAttackExercise();
        ex3.setId("ex3");
        ex3.setType(TimeAttackExercise.ExerciseType.FORM_SENTENCE);
        ex3.setQuestion("Ordena las palabras: 'Estoy bien'");
        ex3.setCorrectAnswer("Allinmi kashani");
        List<String> words3 = Arrays.asList("kashani", "Allinmi", "mana", "kachkani");
        Collections.shuffle(words3);
        ex3.setOptions(words3);
        ex3.setAudioResId(R.raw.voz2);
        exercises.add(ex3);

        // Ejercicio 4: Traducir "Buenos días"
        TimeAttackExercise ex4 = new TimeAttackExercise();
        ex4.setId("ex4");
        ex4.setType(TimeAttackExercise.ExerciseType.SELECT_CORRECT);
        ex4.setQuestion("¿Cómo se dice 'Buenos días' en Quechua?");
        ex4.setCorrectAnswer("Allin p'unchay");
        ex4.setOptions(Arrays.asList("Allin p'unchay", "Allin tuta", "Tupananchiskama", "Napaykullayki"));
        ex4.setAudioResId(R.raw.voz_buenos_dias);
        ex4.setImageResId(R.drawable.buenos_dias);
        exercises.add(ex4);

        // Ejercicio 5: Traducir "Gracias"
        TimeAttackExercise ex5 = new TimeAttackExercise();
        ex5.setId("ex5");
        ex5.setType(TimeAttackExercise.ExerciseType.SELECT_CORRECT);
        ex5.setQuestion("Escucha y selecciona la palabra correcta");
        ex5.setCorrectAnswer("Sulpayki");
        ex5.setOptions(Arrays.asList("Sulpayki", "Allinmi", "Manan", "Arí"));
        ex5.setAudioResId(R.raw.voz6);
        ex5.setImageResId(R.drawable.avatar3);
        exercises.add(ex5);

        // Ejercicio 6: Traducir "Adiós"
        TimeAttackExercise ex6 = new TimeAttackExercise();
        ex6.setId("ex6");
        ex6.setType(TimeAttackExercise.ExerciseType.SELECT_CORRECT);
        ex6.setQuestion("¿Cómo se dice 'Hasta luego' en Quechua?");
        ex6.setCorrectAnswer("Tupananchiskama");
        ex6.setOptions(Arrays.asList("Tupananchiskama", "Napaykullayki", "Imaynallan", "Allinmi"));
        ex6.setAudioResId(R.raw.voz_buenas);
        ex6.setImageResId(R.drawable.avatar1);
        exercises.add(ex6);

        // Ejercicio 7: Formar oración "Mucho gusto"
        TimeAttackExercise ex7 = new TimeAttackExercise();
        ex7.setId("ex7");
        ex7.setType(TimeAttackExercise.ExerciseType.FORM_SENTENCE);
        ex7.setQuestion("Ordena: 'Mucho gusto en conocerte'");
        ex7.setCorrectAnswer("Anchata kusisqa riqsisqaykiwan");
        List<String> words7 = Arrays.asList("Anchata", "kusisqa", "riqsisqaykiwan", "kani");
        Collections.shuffle(words7);
        ex7.setOptions(words7);
        ex7.setAudioResId(R.raw.voz4);
        exercises.add(ex7);

        // Ejercicio 8: Traducir "Sí"
        TimeAttackExercise ex8 = new TimeAttackExercise();
        ex8.setId("ex8");
        ex8.setType(TimeAttackExercise.ExerciseType.SELECT_CORRECT);
        ex8.setQuestion("¿Cómo se dice 'Sí' en Quechua?");
        ex8.setCorrectAnswer("Arí");
        ex8.setOptions(Arrays.asList("Arí", "Manan", "Icha", "Allin"));
        ex8.setAudioResId(R.raw.voz3);
        exercises.add(ex8);

        // Ejercicio 9: Traducir "No"
        TimeAttackExercise ex9 = new TimeAttackExercise();
        ex9.setId("ex9");
        ex9.setType(TimeAttackExercise.ExerciseType.SELECT_CORRECT);
        ex9.setQuestion("¿Cómo se dice 'No' en Quechua?");
        ex9.setCorrectAnswer("Manan");
        ex9.setOptions(Arrays.asList("Manan", "Arí", "Icha", "Manam"));
        ex9.setAudioResId(R.raw.voz2);
        exercises.add(ex9);

        // Ejercicio 10: Formar oración "¿Cuál es tu nombre?"
        TimeAttackExercise ex10 = new TimeAttackExercise();
        ex10.setId("ex10");
        ex10.setType(TimeAttackExercise.ExerciseType.FORM_SENTENCE);
        ex10.setQuestion("Ordena: '¿Cuál es tu nombre?'");
        ex10.setCorrectAnswer("Imataq sutiyki");
        List<String> words10 = Arrays.asList("Imataq", "sutiyki", "kanki", "munani");
        Collections.shuffle(words10);
        ex10.setOptions(words10);
        ex10.setAudioResId(R.raw.voz1);
        exercises.add(ex10);

        return exercises;
    }

    /**
     * Genera ejercicios aleatorios para mayor variedad
     */
    public static List<TimeAttackExercise> generateRandomExercises(int count) {
        List<TimeAttackExercise> allExercises = generateGreetingsExercises();
        Collections.shuffle(allExercises);
        return allExercises.subList(0, Math.min(count, allExercises.size()));
    }
}