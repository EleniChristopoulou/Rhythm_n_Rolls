package com.example.bloodstamina;

public class BJJ_Rolls {
    int lvl = 0, reps = lvl*50 + 200;
    float weight = lvl*50 + 200;
    int cycle_id = 1;

    public BJJ_Rolls(int lvl, int cycle_id){
        this.lvl = lvl;
        this.cycle_id = cycle_id;
    }

    public BJJ_Rolls(){
    }

    public float getWeight() {
        return weight;
    }
    public int getReps() {
        return reps;
    }

    public float getScore() {
        return (float) (0.6 * weight + 0.4 * reps);
    }

    public int getLvl() {
        return lvl;
    }

}
