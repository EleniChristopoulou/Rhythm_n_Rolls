package com.example.bloodstamina;

import java.time.LocalDate;

public class Exercise {

    private String name;
    private String muscle;
    private String group;
    private int sets = 3;
    private int reps = 0;

    private LocalDate date;

    private float score = 0;
    private int cycle_id = 0, cycle_day = 0;

    private float weight = 0;

    private boolean completed;

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Exercise(String name, String group, String muscle) {
        this.name = name;
        this.group = group;
        this.muscle = muscle;
    }

    public Exercise(String name, LocalDate date, float score, int cycle_id, int cycle_day) {
        this.name = name;
        this.score = score;
        this.date = date;
        this.cycle_id = cycle_id;
        this.cycle_day = cycle_day;
    }

    public String getName() {
        return name;
    }

    public float getScore() {
        return score;
    }

    public int getReps() {
        return reps;
    }

    public float getWeight() {
        return weight;
    }

    public String getMuscle() {
        return muscle;
    }

    public String getGroup() {
        return group;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
    public String SetsToString(){
        return String.valueOf(this.sets);
    }
    public String RepsToString(){
        return String.valueOf(this.reps);
    }
    public String WeightToString(){
        return String.valueOf(this.weight);
    }
}
