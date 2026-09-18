package com.example.bloodstamina;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Menstrual_Cycle {
    private LocalDate starting_cycle, ending_cycle, next_starting_cycle;
    int[] phases_duration = {5, 8, 3, 12};
    String[] phases_names = {"Menstrual", "Follicular", "Ovulation", "Lutheal"};

    int id_cycle = 0;

    public Menstrual_Cycle(LocalDate start_date, LocalDate end_date, int lutheal_duration, int cycleNumber){
        this.starting_cycle = start_date;
        this.ending_cycle = end_date;
        this.phases_duration = new int[] {5, 8, 3, lutheal_duration};
        this.id_cycle = cycleNumber;
    }

    public int getId_cycle() {
        return id_cycle;
    }

    public LocalDate getStartingDate() {
        return this.starting_cycle;
    }

    public int sum_duration(int phase){     //5, 13, 16, 28
        int sum = 0;

        if (phase < 0 ){
            return -1;
        }else {
            for (int i=0; i<=phase; i++){
                sum += phases_duration[i];
            }
        }

        return sum;
    }

    public String getPhase(LocalDate date) {        //e.g. 19/8/2026 -> Menstrual
        int num_of_cycle = (int) (ChronoUnit.DAYS.between(starting_cycle, date));

        if(num_of_cycle < 0)    return "Unknown";

        for (int i = 0; i < phases_names.length; i++) {
            if (num_of_cycle <= sum_duration(i)) {
                return phases_names[i];
            }
        }

        return "Unknown";
    }

    public String getPhaseFromDay(int num_of_cycle) {        //e.g. 6th -> Menstrual

        if(num_of_cycle < 0)    return "Unknown";

        for (int i = 0; i < phases_names.length; i++) {
            if (num_of_cycle <= sum_duration(i)) {
                return phases_names[i];
            }
        }

        return "Unknown";
    }

    public int getNumDay(LocalDate date) {        //e.g. 19/8/2026 -> 2nd
        return (int) (ChronoUnit.DAYS.between(starting_cycle, date));
    }

}
