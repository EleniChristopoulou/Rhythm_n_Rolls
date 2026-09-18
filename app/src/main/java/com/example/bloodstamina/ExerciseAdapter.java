package com.example.bloodstamina;

import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises;

    public ExerciseAdapter(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercises, parent, false);

        return new ExerciseViewHolder(view);
    }

    public static float computeScore(int reps, float weight) {
        return (0.6f * weight) + (0.4f * reps);
    }

    private void updateScoreDisplay(ExerciseViewHolder holder, Exercise exercise) {
        float score = computeScore(exercise.getReps(), exercise.getWeight());
        exercise.setScore(score);
        holder.etScore.setText(score > 0 ? String.valueOf(Math.round(score * 10) / 10.0) : "0");
    }

    private int parseIntOrZero(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private float parseFloatOrZero(String s) {
        try { return Float.parseFloat(s.trim()); } catch (Exception e) { return 0f; }
    }

    public void updateExercises(List<Exercise> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);

        if (holder.repsWatcher != null) holder.etReps.removeTextChangedListener(holder.repsWatcher);
        if (holder.kgWatcher != null) holder.etKg.removeTextChangedListener(holder.kgWatcher);

        holder.etReps.setText(exercise.getReps() > 0 ? String.valueOf(exercise.getReps()) : "");
        holder.etKg.setText(exercise.getWeight() > 0 ? String.valueOf(exercise.getWeight()) : "");
        updateScoreDisplay(holder, exercise);

        holder.repsWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                exercise.setReps(parseIntOrZero(s.toString()));
                updateScoreDisplay(holder, exercise);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        holder.etReps.addTextChangedListener(holder.repsWatcher);

        holder.kgWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                exercise.setWeight(parseFloatOrZero(s.toString()));
                updateScoreDisplay(holder, exercise);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        holder.etKg.addTextChangedListener(holder.kgWatcher);

        holder.tvExerciseName.setText(exercise.getName());

        holder.tvExerciseSets.setText(
                exercise.getMuscle()
        );

        String muscle = exercise.getMuscle();

        if ("Bicep".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.bicep);
        } else if ("Tricep".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.tricep);

        } else if ("Chest".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.chest);

        } else if ("Calves".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.calves);

        } else if ("Hamstrings".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.hamstrings);

        } else if ("Shoulder".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.shoulder);

        } else if ("Tricep".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.tricep);

        } else if ("Quadriceps".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.quadriceps);

        } else if ("Abs".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.abs);

        } else if ("Upper Back".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.upper_back);

        } else if ("Lower Back".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.lower_back);

        } else if ("Forearm".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.wrist);

        } else if ("Adductor".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.adductor);

        } else if ("Glutes".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.glutes);

        } else if ("Abductor".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.abductor);

        } else if ("Overall".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.run);
        } else if ("Stretching".equalsIgnoreCase(muscle)) {
            holder.cbCompleted.setBackgroundResource(R.drawable.yoga);
        } else {
            holder.cbCompleted.setBackgroundResource(R.drawable.logo);
        }

        // Reset checkbox when RecyclerView reuses the item
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(exercise.isCompleted());

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            exercise.setCompleted(isChecked);
            MaterialCardView card = (MaterialCardView) holder.itemView;
            TextView txtView = (TextView) holder.etScore;
            if (isChecked) {
                card.setCardBackgroundColor(
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                R.color.accent
                        )
                );
                card.setAlpha(0.7f);
                txtView.setTextColor(
                        ContextCompat.getColor(
                        holder.etScore.getContext(),
                        R.color.secondary
                ));
            } else {
                card.setCardBackgroundColor(
                        ContextCompat.getColor(
                                holder.itemView.getContext(),
                                R.color.primary
                        )
                );
                card.setAlpha(1.0f);
                txtView.setTextColor(
                        ContextCompat.getColor(
                                holder.etScore.getContext(),
                                R.color.accent
                        ));
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {

        CheckBox cbCompleted;
        TextView tvExerciseName;
        TextView tvExerciseSets;
        TextView tvExerciseWeight;

        EditText etReps;
        EditText etKg;
        TextView etScore;
        TextWatcher repsWatcher;
        TextWatcher kgWatcher;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);

            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseSets = itemView.findViewById(R.id.tvExerciseSets);
            tvExerciseWeight = itemView.findViewById(R.id.tvExerciseSets);

            etReps = itemView.findViewById(R.id.etReps);
            etKg = itemView.findViewById(R.id.etKg);
            etScore = itemView.findViewById(R.id.etScore);
        }
    }

    public Exercise getExerciseAt(int position) {
        return exercises.get(position); // whatever your internal list field is called
    }

}

