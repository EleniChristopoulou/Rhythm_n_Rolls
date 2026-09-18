package com.example.bloodstamina;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bloodstamina.MainActivity;
import com.example.bloodstamina.charts.CycleScoreChartView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExerciceActivity extends AppCompatActivity {
    private RecyclerView rvExercises;
    String ex_name;
    private List<CycleScoreChartView.Score> ex_scores;
    private TextView chipUpper, chipLower, chipWarmup;
    private TextView selectedChip;

    private List<Menstrual_Cycle> cycles;
    private List<CycleScoreChartView.Score> scores;
    List<CycleScoreChartView.Phase> phases;
    public int def_cycle_id = 0;

    private TextView total_exercise, title;

    private String filter_group = "Upper Body";

    private List<Exercise> exercises;
    private ExerciseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exercice);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.exerc),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );


        rvExercises = findViewById(R.id.rvExercises);
        exercises = CsvReader.loadExercises(this);

        Map<String, float[]> lastValues = CsvReader.loadLastExerciseValues(this);
        for (Exercise ex : exercises) {
            float[] last = lastValues.get(ex.getName());
            if (last != null) {
                ex.setReps((int) last[0]);
                ex.setWeight(last[1]);
            }
        }

        adapter = new ExerciseAdapter(filterExercisesByGroup(exercises, filter_group));

        adapter = new ExerciseAdapter(filterExercisesByGroup(exercises, filter_group));
        rvExercises.setAdapter(adapter);

        setupWorkoutChips();
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // not used, we're not reordering
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                Exercise exercise = adapter.getExerciseAt(position);
                ex_name = exercise.getName();

                ex_scores = CsvReader.loadScoreExercise(ExerciceActivity.this, ex_name);
                List<CycleScoreChartView.Score> filledScores = buildZeroFilledScores(ex_scores, def_cycle_id);

                showProgressDialog(exercise, filledScores);

                adapter.notifyItemChanged(position);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvExercises);
        scores = CsvReader.loadScoreCycle(this);
        cycles = CsvReader.loadCycle(this);

        def_cycle_id = cycles.size()-1;
    }
    private float computeMaxScore(List<CycleScoreChartView.Score> data) {
        float max = 0f;
        for (CycleScoreChartView.Score s : data) {
            if (s.score > max) max = s.score;
        }
        return max;
    }

    private List<CycleScoreChartView.Score> buildZeroFilledScores(
            List<CycleScoreChartView.Score> exerciseScores, int cycleId) {

        Map<Integer, Float> dayScoreMap = new HashMap<>();
        for (CycleScoreChartView.Score s : exerciseScores) {
            if (s.cycleid == cycleId) {
                dayScoreMap.put(s.day, s.score);
            }
        }

        int totalDays = cycles.get(cycleId).sum_duration(3);
        List<CycleScoreChartView.Score> filled = new ArrayList<>();

        for (int day = 1; day <= totalDays; day++) {
            float score = dayScoreMap.containsKey(day) ? dayScoreMap.get(day) : 0f;
            filled.add(new CycleScoreChartView.Score(day, score, cycleId));
        }

        return filled;
    }

    private List<Exercise> filterExercisesByGroup(List<Exercise> exercises, String group) {
        List<Exercise> filteredExercises = new ArrayList<>();

        for (Exercise exercise : exercises) {
            if (group.equalsIgnoreCase(exercise.getGroup())) {
                filteredExercises.add(exercise);
            }
        }

        return filteredExercises;
    }
    private void setupWorkoutChips() {
        chipUpper = findViewById(R.id.upper);
        chipLower = findViewById(R.id.lower);
        chipWarmup = findViewById(R.id.warmup);

        selectedChip = chipLower; // default selected
        selectChip(chipUpper);

        View.OnClickListener chipClickListener = v -> selectChip((TextView) v);

        chipUpper.setOnClickListener(chipClickListener);
        chipLower.setOnClickListener(chipClickListener);
        chipWarmup.setOnClickListener(chipClickListener);

    }

    private void selectChip(TextView chip) {
        if (chip == selectedChip) return; // already selected, no-op

        selectedChip.setBackgroundResource(R.drawable.bg_chip_unselected);
        selectedChip.setTextColor(Color.parseColor("#AAAAAA"));

        chip.setBackgroundResource(R.drawable.bg_chip_selected);
        chip.setTextColor(Color.parseColor("#FFFFFF"));

        selectedChip = chip;
        onWorkoutTypeSelected(chip.getId());
    }

    private void onWorkoutTypeSelected(int chipId) {
        total_exercise = findViewById(R.id.tvWorkoutSubtitle);
        title = findViewById(R.id.tvWorkoutTitle);

        if (chipId == R.id.upper) {
            filter_group = "Upper Body";
        } else if (chipId == R.id.lower) {
            filter_group = "Lower Body";
        } else if (chipId == R.id.warmup) {
            filter_group = "Warm Up";
        }

        List<Exercise> filteredExercises = filterExercisesByGroup(exercises, filter_group);

        adapter.updateExercises(filteredExercises);
        total_exercise.setText(String.valueOf(filteredExercises.size())+" exercises");
        title.setText(filter_group);
    }

    private void showProgressDialog(Exercise exercise, List<CycleScoreChartView.Score> data) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_progress, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvProgressTitle);
        tvTitle.setText(exercise.getName());

        CycleScoreChartView cycleScoreChart = dialogView.findViewById(R.id.graphView);

        float maxScore = computeMaxScore(data);
        cycleScoreChart.setYAxisMax(maxScore <= 0 ? 10f : maxScore * 1.15f); // 15% headroom, fallback if all zero

        List<CycleScoreChartView.Phase> phases = new ArrayList<>();
        phases.add(new CycleScoreChartView.Phase("Menstrual",  1,  5,  Color.parseColor("#F49A9A")));
        phases.add(new CycleScoreChartView.Phase("Follicular", 6,  13, Color.parseColor("#C7A8F5")));
        phases.add(new CycleScoreChartView.Phase("Ovulation",  14, 16, Color.parseColor("#9AC7F5")));
        phases.add(new CycleScoreChartView.Phase("Luteal",     17, cycles.get(def_cycle_id).sum_duration(3), Color.parseColor("#B4E5A2")));

        cycleScoreChart.setStartingDate(cycles.get(def_cycle_id).getStartingDate());
        cycleScoreChart.setData(data, phases, cycles.get(def_cycle_id).sum_duration(3));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setView(dialogView)
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#141342")));
        dialog.show();
    }

    public void logWorkout(View view) {
        LocalDate today = LocalDate.now();
        Menstrual_Cycle currentCycle = cycles.get(cycles.size() - 1);
        int cycleDay = currentCycle.getNumDay(today);

        int loggedCount = 0;
        for (Exercise ex : exercises) {
            if (ex.isCompleted()) {
                CsvReader.appendExerciseScore(
                        this,
                        today,
                        ex.getName(),
                        ex.getReps(),
                        ex.getWeight(),
                        ex.getScore(),
                        currentCycle.getPhaseFromDay(cycleDay),
                        ex.getGroup(),
                        currentCycle.getId_cycle(),
                        cycleDay+1
                );
                loggedCount++;
            }
        }

        if (loggedCount > 0) {
            int dayScoreSum = CsvReader.sumExerciseScoresForDate(this, today);
            System.out.println("DAYSCORESUM: "+dayScoreSum);
            CsvReader.upsertCycleScore(this, today, dayScoreSum, currentCycle.getId_cycle(), cycleDay, currentCycle.getPhase(LocalDate.now()));
        }

        Toast.makeText(this, loggedCount + " exercises logged", Toast.LENGTH_SHORT).show();
    }
    public void backIntent(View view) {
        finish();
    }

}