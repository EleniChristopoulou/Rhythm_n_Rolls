package com.example.bloodstamina;

import com.example.bloodstamina.Exercise;

import java.time.LocalDate;

public class Workoutlog {

    private LocalDate date;
    private String menstrual_phs;
    private float effectiveness;

    private Exercise exercise;

    public Workoutlog(
            LocalDate date,
            Exercise exercise,
            String menstrual_phs) {

        this.date = date;

        exercise = new Exercise(exercise.getName(), exercise.getGroup(), exercise.getMuscle());

        this.exercise.setReps(exercise.getReps());
        this.exercise.setWeight(exercise.getWeight());

        this.menstrual_phs = menstrual_phs;

        this.effectiveness = (float) (exercise.getReps()*0.4 + exercise.getWeight()*0.6);
    }

    public float getEffectiveness() {
        return effectiveness;
    }
}
