package com.example.bloodstamina;

import static com.example.bloodstamina.CsvReader.loadScoreBjj;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.bloodstamina.charts.CycleScoreChartView;
import com.example.bloodstamina.charts.CycleScoreChartView.Phase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public List<Menstrual_Cycle> cycles;
    public List<CycleScoreChartView.Score> scores;
    private List<CycleScoreChartView.Score> ex_scores;
    BJJ_Rolls bjj_lvl;
    List<Phase> phases;
    public int def_cycle_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // whatever your layout file is called

        TextView tvDate = findViewById(R.id.tvDate);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH);
        tvDate.setText(formatter.format(calendar.getTime()));

        scores = CsvReader.loadScoreCycle(this);
        cycles = CsvReader.loadCycle(this);
        def_cycle_id = cycles.size()-1;

        showCycle(scores);
        showCycleLabel();
        getResultsOfThisCycle(scores);

    }

    @Override
    protected void onResume() {
        super.onResume();

        scores = CsvReader.loadScoreCycle(this);
        cycles = CsvReader.loadCycle(this);
        bjj_lvl = loadScoreBjj(this,def_cycle_id);

        if (def_cycle_id > cycles.size() - 1) {
            def_cycle_id = cycles.size() - 1;
        }

        showCycle(scores);
        showCycleLabel();
        getResultsOfThisCycle(scores);
    }

    private void getResultsOfThisCycle(List<CycleScoreChartView.Score> data) {
        Menstrual_Cycle latest = cycles.get(cycles.size() - 1);
        int totalDays = latest.sum_duration(3);
        List<CycleScoreChartView.Score> this_cycle =
                CsvReader.fillMissingDays(data, latest.id_cycle, totalDays);

        float max_score = 0;
        int max_day = 0;
        String max_phase = "";
        for (CycleScoreChartView.Score s : this_cycle) {
            if (s.score > max_score) {
                max_score = s.score;
                max_day = s.day;
                max_phase = latest.getPhaseFromDay(max_day);
            }
        }

        TextView txtview = findViewById(R.id.tvWorkoutType);
        switch (max_day){
            case 1:
                txtview.setText("BEST DAY: "+max_day+"st · SCORE: "+max_score);
                break;
            case 2:
                txtview.setText("BEST DAY: "+max_day+"nd · SCORE: "+max_score);
                break;
            case 3:
                txtview.setText("BEST DAY: "+max_day+"rd · SCORE: "+max_score);
                break;
            default:
                txtview.setText("BEST DAY: "+max_day+"th · SCORE: "+max_score);
        }

        View dot = findViewById(R.id.vTypeDot);
        switch (max_phase) {
            case "Menstrual":
                dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.menstrual));
                break;
            case "Follicular":
                dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.follicular));
                break;
            case "Ovulation":
                dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.ovulation));
                break;
            case "Luteal":
                dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.lutheal));
                break;
            default:
                dot.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.menstrual));
                break;
        }

        int streak = 0;
        for (int i=latest.getNumDay(LocalDate.now())-1; i>=0; i--){

            if( (this_cycle.get(i).score == 0) && (i!=latest.getNumDay(LocalDate.now())-1) ){
                break;
            }else {
                if((i==latest.getNumDay(LocalDate.now())-1) && (this_cycle.get(i).score == 0)){

                }else {
                    streak++;
                }
            }
        }

        File csvFile = new File(getFilesDir(), "exercises_score.csv");
        StreakCalculator.StreakResult result = StreakCalculator.calculateStreaks(csvFile);

        TextView strk = findViewById(R.id.tvWorkoutMeta);
        TextView bjj_sessions = findViewById(R.id.bjj_session);
        TextView gym_sessions = findViewById(R.id.gym_session);
        strk.setText(streak+" Day Streak");
        gym_sessions.setText(String.valueOf(result.otherStreak));
        bjj_sessions.setText(String.valueOf(result.bjjStreak));

        TextView daysStreak = findViewById(R.id.daysStreak);
        daysStreak.setText(String.valueOf(streak));

        if(streak>5){
            strk.setText(streak+" Day Streak · Tomorrow is recommened for rest o.O");
        }
    }

    private void showCycleLabel(){
        ImageView icon_phase = findViewById(R.id.iconPhase);
        TextView cycle_phase_name = findViewById(R.id.tvCyclePhaseName);
        TextView cycle_phase_insight = findViewById(R.id.tvCycleInsight);
        ProgressBar progressCycle = findViewById(R.id.progressCycle);
        cycle_phase_name.setText(cycles.get(cycles.size()-1).getPhase(LocalDate.now())+" Phase");

        switch (cycles.get(cycles.size() - 1).getPhase(LocalDate.now())) {
            case "Menstrual":
                icon_phase.setImageResource(R.drawable.menstrual);
                cycle_phase_insight.setText("Recovery-focused phase — listen to your body and keep movement comfortable.");
                cycle_phase_name.setTextColor(ContextCompat.getColor(this, R.color.menstrual));
                progressCycle.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.menstrual)));
                break;

            case "Follicular":
                icon_phase.setImageResource(R.drawable.follicular);
                cycle_phase_insight.setText("Energy may be rising — a great phase to build strength and increase intensity.");
                cycle_phase_name.setTextColor(ContextCompat.getColor(this, R.color.follicular));
                progressCycle.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.follicular)));
                break;

            case "Ovulation":
                icon_phase.setImageResource(R.drawable.ovulation);
                cycle_phase_insight.setText("Potentially high-energy phase — a good time for strength and power-focused workouts.");
                cycle_phase_name.setTextColor(ContextCompat.getColor(this, R.color.ovulation));
                progressCycle.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.ovulation)));
                break;

            case "Lutheal":
                icon_phase.setImageResource(R.drawable.lutheal);
                cycle_phase_insight.setText("Energy may gradually shift — prioritize consistency, recovery, and manageable intensity.");
                cycle_phase_name.setTextColor(ContextCompat.getColor(this, R.color.lutheal));
                progressCycle.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lutheal)));
                break;

            default:
                cycle_phase_name.setTextColor(ContextCompat.getColor(this, R.color.menstrual));
                break;
        }
    }
    private void showCycle(List<CycleScoreChartView.Score> data) {
        int totalDays = cycles.get(def_cycle_id).sum_duration(3);
        List<CycleScoreChartView.Score> this_cycle =
                CsvReader.fillMissingDays(data, cycles.get(def_cycle_id).id_cycle, totalDays);

        CycleScoreChartView cycleScoreChart = findViewById(R.id.cycleScoreChart);
        phases = new ArrayList<>();

        phases.add(new Phase("Menstrual",  1,  5,  Color.parseColor("#F49A9A"))); // creamy coral-red
        phases.add(new Phase("Follicular", 6,  13, Color.parseColor("#C7A8F5"))); // soft lavender
        phases.add(new Phase("Ovulation",  14, 16, Color.parseColor("#9AC7F5"))); // soft periwinkle blue
        phases.add(new Phase("Luteal",     17, cycles.get(def_cycle_id).sum_duration(3), Color.parseColor("#B4E5A2"))); // creamy butter yellow

        while (phases.size()>4){
            phases.remove(phases.size()-1);
        }

        cycleScoreChart.setStartingDate(cycles.get(def_cycle_id).getStartingDate());
        cycleScoreChart.setData(this_cycle, phases, cycles.get(def_cycle_id).sum_duration(3));

        ImageView backButton = findViewById(R.id.btnPreviousCycle);
        Drawable drawable_bck = backButton.getDrawable().mutate();

        if (def_cycle_id == 0) {
            drawable_bck.setTint(ContextCompat.getColor(this, R.color.accent));
        } else {
            drawable_bck.setTint(ContextCompat.getColor(this, R.color.secondary));
        }

        backButton.setImageDrawable(drawable_bck);

        ImageView fontButton = findViewById(R.id.btnNextCycle);
        Drawable drawable_fr = fontButton.getDrawable().mutate();

        if (def_cycle_id == cycles.size()-1) {
            drawable_fr.setTint(ContextCompat.getColor(this, R.color.accent));
        } else {
            drawable_fr.setTint(ContextCompat.getColor(this, R.color.secondary));
        }

        fontButton.setImageDrawable(drawable_fr);

        TextView tvCycleLogged = findViewById(R.id.cycleLogged);
        tvCycleLogged.setText("Cycle: "+(def_cycle_id+1)+" / "+cycles.size());
        TextView tvCycleLoggedMonth = findViewById(R.id.cycleLoggedMonth);
        tvCycleLoggedMonth.setText(cycles.get(def_cycle_id).getStartingDate().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())+" "+cycles.get(def_cycle_id).getStartingDate().getYear());

        TextView tvCyclePhaseLabel = findViewById(R.id.tvCycleDayCount);
        tvCyclePhaseLabel.setText("Day "+(cycles.get(def_cycle_id).getNumDay(LocalDate.now())+1));

        ProgressBar progressCycle = findViewById(R.id.progressCycle);
        progressCycle.setMax(cycles.get(cycles.size()-1).sum_duration(3));
        progressCycle.setProgress(cycles.get(cycles.size()-1).getNumDay(LocalDate.now()));
        belt_show();

    }

    public void belt_show() {
        bjj_lvl = loadScoreBjj(this,def_cycle_id);

        View beltLeft = findViewById(R.id.beltLeft);
        View beltRight = findViewById(R.id.beltRight);

        View stripe1 = findViewById(R.id.stripe1);
        View stripe2 = findViewById(R.id.stripe2);
        View stripe3 = findViewById(R.id.stripe3);
        View stripe4 = findViewById(R.id.stripe4);

        View back_belt1 = findViewById(R.id.backgr1);
        View back_belt2 = findViewById(R.id.backgr2);
        View back_belt3 = findViewById(R.id.backgr3);
        View back_belt4 = findViewById(R.id.backgr4);
        View back_belt5 = findViewById(R.id.backgr5);

        int lvl = bjj_lvl.getLvl();

        System.out.println("0 LEVEL "+lvl);

        if((lvl+1)%5==0){ //four stripes
            stripe1.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
            stripe2.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
            stripe3.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
            stripe4.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));

            if (lvl >= 20){
                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
            } else {
                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
            }
        }else if ((lvl+2)%5==0){//three stripes
            if(lvl < 20){
                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));

                stripe1.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe2.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe3.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe4.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
            }else {

                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));

                stripe1.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe2.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe3.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe4.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
            }
        }else if ((lvl+3)%5==0){//two stripes
            if(lvl < 20){
                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));

                stripe1.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe2.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe3.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                stripe4.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
            }else {

                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));

                stripe1.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe2.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe3.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                stripe4.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
            }
        }else if ((lvl+4)%5==0){//one stripe
            if(lvl < 20){
                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));

                stripe1.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe2.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                stripe3.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                stripe4.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
            }else {
                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));

                stripe1.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
                stripe2.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                stripe3.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                stripe4.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
            }
        }else if ((lvl+5)%5==0){//no stripes
            if(lvl < 20){
                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));

                stripe1.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                stripe2.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                stripe3.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
                stripe4.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
            }else {
                System.out.println("1 LEVEL "+lvl);
                back_belt1.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt2.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt3.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt4.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                back_belt5.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));

                stripe1.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                stripe2.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                stripe3.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
                stripe4.setBackgroundColor(ContextCompat.getColor(this, R.color.red_stripe));
            }
        }

        if (lvl <= 4) {
            beltLeft.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
            beltRight.setBackgroundColor(ContextCompat.getColor(this, R.color.white_belt_stripe));
        } else if (lvl <= 9) {
            beltLeft.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_belt));
            beltRight.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_belt));
        } else if (lvl <= 14) {
            beltLeft.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_belt));
            beltRight.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_belt));
        } else if (lvl <= 19) {
            beltLeft.setBackgroundColor(ContextCompat.getColor(this, R.color.brown_belt));
            beltRight.setBackgroundColor(ContextCompat.getColor(this, R.color.brown_belt));
        } else {
            beltLeft.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
            beltRight.setBackgroundColor(ContextCompat.getColor(this, R.color.black_belt));
        }

    }

    public void logExercices(View view) {
        Intent intent = new Intent( this, ExerciceActivity.class);
        startActivity(intent);
    }

    public void nextMonth(View view) {
        if(def_cycle_id!=cycles.size()-1) {
            def_cycle_id++;
            showCycle(scores);
        }
    }

    public void perviousMonth(View view) {
        if(def_cycle_id!=0) {
            def_cycle_id--;
            showCycle(scores);
        }
    }

    public void logCycle(View view){
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confrim, null);

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Yes", (dialog, which) -> {
                    Menstrual_Cycle latest = cycles.get(cycles.size() - 1);
                    CsvReader.appendCycle(this, latest.id_cycle, latest.getStartingDate(), latest.phases_duration[3]);
                    cycles = CsvReader.loadCycle(this);

                    def_cycle_id = cycles.size()-1;

                    showCycle(scores);
                    showCycleLabel();
                    getResultsOfThisCycle(scores);
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    public void logBjjSession(View view){
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confrim, null);

        AlertDialog dial = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Yes", (dialog, which) -> {
                    logWorkout();

                    scores = CsvReader.loadScoreCycle(this);
//                    cycles = CsvReader.loadCycle(this);
//                    bjj_lvl = loadScoreBjj(this,def_cycle_id);
//
//                    if (def_cycle_id > cycles.size() - 1) {
//                        def_cycle_id = cycles.size() - 1;
//                    }

                    showCycle(scores);
                    showCycleLabel();
                    getResultsOfThisCycle(scores);
                })
                .setNegativeButton("Nah", null)
                .show();

//        dial.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#141342")));
        ImageView img = dialogView.findViewById(R.id.ivDialogIcon);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);

        img.setImageResource(R.drawable.duck_fight);
        img.setRotationX(180f);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) img.getLayoutParams();
        params.topMargin = (int) (10 * getResources().getDisplayMetrics().density); // 5dp -> px
        img.setLayoutParams(params);

        img.setAlpha(0f);
        img.animate()
                .alpha(0.1f)
                .setDuration(3000)
                .withEndAction(() -> {
                    img.animate()
                            .alpha(1f)
                            .setDuration(1000)
                            .start();
                })
                .start();

        tvTitle.setText("Ooh finally, a BJJ session");
        tvMessage.setText("Ready to fight?");
    }

    public void logBjjBelt(View view){
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confrim, null);

        AlertDialog dial = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setView(dialogView)
                .setPositiveButton("Yes!", (dialog, which) -> {
                    CsvReader.appendBjjLevel(this, bjj_lvl.getLvl() + 1, cycles.size() - 1);

                    bjj_lvl = loadScoreBjj(this, def_cycle_id);
                    belt_show();
                })
                .setNegativeButton("Misclick sorry", null)
                .show();

        ImageView img = dialogView.findViewById(R.id.ivDialogIcon);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);

        img.setImageResource(R.drawable.look);
        img.setRotationX(180f);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) img.getLayoutParams();
        params.rightMargin = (int) (35 * getResources().getDisplayMetrics().density); // 5dp -> px
        params.topMargin = (int) (15 * getResources().getDisplayMetrics().density); // 5dp -> px
        img.setLayoutParams(params);

        tvTitle.setText("Holy upgrade");
        tvMessage.setText("Are we sure?");
    }

    public void logWorkout() {
        LocalDate today = LocalDate.now();
        Menstrual_Cycle currentCycle = cycles.get(cycles.size() - 1);
        int cycleDay = currentCycle.getNumDay(today);

        CsvReader.appendExerciseScore(
                this,
                today,
                "BJJ Session",
                bjj_lvl.getReps(),
                bjj_lvl.getWeight(),
                bjj_lvl.getScore(),
                currentCycle.getPhaseFromDay(currentCycle.getNumDay(today)),
                "BJJ Session",
                currentCycle.getId_cycle(),
                currentCycle.getNumDay(today)
        );
        int dayScoreSum = CsvReader.sumExerciseScoresForDate(this, today);
        CsvReader.upsertCycleScore(this, today, dayScoreSum, currentCycle.getId_cycle(), cycleDay, currentCycle.getPhase(LocalDate.now()));

        Toast.makeText(this, "Bjj Session logged", Toast.LENGTH_SHORT).show();
    }


}