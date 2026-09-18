package com.example.bloodstamina;

import android.content.Context;
import android.util.Log;

import com.example.bloodstamina.charts.CycleScoreChartView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
public class CsvReader {

    public static List<Exercise> loadExercises(Context context) {

        List<Exercise> exercises = new ArrayList<>();

        try {
            InputStream inputStream =
                    context.getResources().openRawResource(R.raw.exercises);

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream));
            String line;

            // Skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3) {

                    String name = values[0].trim();
                    String group = values[1].trim();
                    String muscle = values[2].trim();

                    exercises.add(
                            new Exercise(name, group, muscle)
                    );
                }
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return exercises;
    }

    public static List<Menstrual_Cycle> loadCycle(Context context) {

        List<Menstrual_Cycle> menstraulCycles = new ArrayList<>();
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("d/M/yyyy", Locale.getDefault());

        try {
//            InputStream inputStream =
//                    context.getResources().openRawResource(R.raw.cycle);
//
//            BufferedReader reader =
//                    new BufferedReader(new InputStreamReader(inputStream));

            File file = getCycleFile(context);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            // Skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3) {

                    int cycleid = Integer.parseInt(values[0].trim());
                    Log.d("BS_DEBUG", "loadCycle row -> id_cycle=" + cycleid + " start=" + values[1].trim());

                    LocalDate starting_date = LocalDate.parse(values[1].trim(), formatter);
                    LocalDate ending_date = LocalDate.parse(values[2].trim(), formatter);
                    int lutheal_duration = Integer.parseInt(values[6].trim());;

                    menstraulCycles.add(
                            new Menstrual_Cycle(starting_date, ending_date, lutheal_duration, cycleid)
                    );
                }
            }

            reader.close();

            if (!menstraulCycles.isEmpty()) {
                int lastIdx = menstraulCycles.size() - 1;
                Menstrual_Cycle last = menstraulCycles.get(lastIdx);

                LocalDate today = LocalDate.now();
                int elapsedDays = (int) ChronoUnit.DAYS.between(last.getStartingDate(), today);
                int expectedTotal = last.sum_duration(3); // 5 + 8 + 3 + lutheal_duration

                if (elapsedDays >= expectedTotal) {
                    // Period is late: stretch luteal duration so "today" still falls inside the cycle.
                    int correctedLutealDuration =
                            (elapsedDays - (5 + 8 + 3)) + 1; // +1 so today is the last valid day, not one past it

                    Log.d("BS_DEBUG", "loadCycle: last cycle overrun, correcting lutheal_duration "
                            + "from " + (expectedTotal - (5 + 8 + 3)) + " to " + correctedLutealDuration);

                    menstraulCycles.set(lastIdx, new Menstrual_Cycle(
                            last.getStartingDate(),
                            last.getStartingDate(), // ending_cycle unused until period actually logged; harmless placeholder
                            correctedLutealDuration,
                            last.getId_cycle()
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return menstraulCycles;
    }


    public static BJJ_Rolls loadScoreBjj(Context context, int cycle_id) {
        BJJ_Rolls bjj_lvl = new BJJ_Rolls();

        try {
            File file = getBjjScoreFile(context);
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            reader.readLine(); // skip header

            int bestCycleId = -1;
            int bestLevel = 0;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                if (values.length >= 2) {
                    int lvl = Integer.parseInt(values[0].trim());
                    int rowCycleId = Integer.parseInt(values[1].trim());

                    if (rowCycleId <= cycle_id && rowCycleId > bestCycleId) {
                        bestCycleId = rowCycleId;
                        bestLevel = lvl;
                    }
                }
            }

            reader.close();
            bjj_lvl = new BJJ_Rolls(bestLevel, cycle_id);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bjj_lvl;
    }

    public static List<CycleScoreChartView.Score> loadScoreCycle(Context context) {
        List<CycleScoreChartView.Score> scores = new ArrayList<>();

        try {
            File file = getCycleScoreFile(context);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3) {
                    float score = Float.parseFloat(values[1].trim());
                    int cycleid  = Integer.parseInt(values[2].trim());
                    int day = Integer.parseInt(values[3].trim());
                    Log.d("BS_DEBUG", "loadScoreCycle row -> cycle=" + cycleid + " day=" + day + " score=" + score);
                    scores.add(new CycleScoreChartView.Score(day, score, cycleid));
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scores;
    }

    public static List<CycleScoreChartView.Score> loadScoreExercise(Context context, String ex_name) {
        List<CycleScoreChartView.Score> scores = new ArrayList<>();

        try {
            File file = getScoreFile(context); // was: context.getResources().openRawResource(R.raw.exercises_score)
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 9 && values[1].trim().equalsIgnoreCase(ex_name.trim())) {
                    float score = Float.parseFloat(values[5].trim());
                    int cycleid  = Integer.parseInt(values[7].trim());
                    int day = Integer.parseInt(values[8].trim());

                    scores.add(new CycleScoreChartView.Score(day, score, cycleid));
                }
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return scores;
    }

    private static File getCycleScoreFile(Context context) throws IOException {
        File file = new File(context.getFilesDir(), CYCLE_SCORE_FILE);
        if (!file.exists()) {
            InputStream in = context.getResources().openRawResource(R.raw.cycle_scores);
            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        }
        return file;
    }

    private static File getScoreFile(Context context) throws IOException {
        File file = new File(context.getFilesDir(), "exercises_score.csv");
        if (!file.exists()) {
            InputStream in = context.getResources().openRawResource(R.raw.exercises_score);
            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        }
        return file;
    }

    public static void appendExerciseScore(Context context, LocalDate date, String name,
                                           int reps, float weight, float score,
                                           String period, String group,
                                           int cycleNumber, int cycleDay) {
        try {
            File file = getScoreFile(context);
            FileWriter writer = new FileWriter(file, true); // append mode

            String dateStr = date.format(DateTimeFormatter.ofPattern("d/M/yyyy", Locale.getDefault()));

            writer.append(String.format(Locale.US, "%s,%s,%d,%.1f,%s,%.1f,%s,%d,%d\n",
                    dateStr, name, reps, weight, period, score, group, cycleNumber, cycleDay));

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, float[]> loadLastExerciseValues(Context context) {
        Map<String, float[]> lastValues = new HashMap<>();

        try {
            File file = getScoreFile(context);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 9) {
                    String name = values[1].trim();
                    int reps = Integer.parseInt(values[2].trim());
                    float weight = Float.parseFloat(values[3].trim());

                    // overwritten on every match, so the last line in the file wins
                    lastValues.put(name, new float[]{reps, weight});
                }
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lastValues;
    }

    private static final String EXERCISE_SCORE_FILE = "exercises_score.csv";
    private static final String CYCLE_SCORE_FILE = "cycle_scores.csv";
    private static final DateTimeFormatter CYCLE_DATE_FMT = DateTimeFormatter.ofPattern("d/M/yyyy");

    public static int sumExerciseScoresForDate(Context context, LocalDate date) {
        File file = new File(context.getFilesDir(), EXERCISE_SCORE_FILE);
        String targetDate = date.format(CYCLE_DATE_FMT);
        int sum = 0;

        if (!file.exists()) return sum;

        System.out.println("2 DAYSCORESUM: geia");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",");
                // ASSUMPTION: column order is date,name,reps,weight,score,period,group,cycle,day_of_cycle
                String rowDate = cols[0];
                System.out.println("2 DAYSCORESUM: "+Float.parseFloat(cols[5].trim()));
                if (rowDate.equals(targetDate)) {
                    sum += Float.parseFloat(cols[5].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sum;
    }

    public static void upsertCycleScore(Context context, LocalDate date, int dayScore,
                                        int cycle, int dayOfCycle, String period) {
        try {
            File file = getCycleScoreFile(context);
            List<String> lines = new ArrayList<>();
            String header = "date,day_score,cycle,day_of_cycle,period";
            String targetDate = date.format(CYCLE_DATE_FMT);
            boolean found = false;

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    boolean first = true;
                    while ((line = reader.readLine()) != null) {
                        if (first) {
                            header = line;
                            first = false;
                            continue;
                        }
                        if (line.trim().isEmpty()) continue;

                        String rowDate = line.split(",", 2)[0];
                        if (rowDate.equals(targetDate)) {
                            lines.add(String.format("%s,%d,%d,%d,%s",
                                    targetDate, dayScore, cycle, dayOfCycle, period));
                            found = true;
                        } else {
                            lines.add(line);
                        }
                    }
                }
            }

            if (!found) {
                lines.add(String.format("%s,%d,%d,%d,%s",
                        targetDate, dayScore, cycle, dayOfCycle, period));
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                writer.write(header);
                writer.newLine();
                for (String l : lines) {
                    writer.write(l);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<CycleScoreChartView.Score> fillMissingDays(
            List<CycleScoreChartView.Score> scores, int cycleId, int totalDays) {

        Map<Integer, Float> byDay = new HashMap<>();
        for (CycleScoreChartView.Score s : scores) {
            if (s.cycleid == cycleId) {
                byDay.put(s.day, s.score);
            }
        }

        List<CycleScoreChartView.Score> filled = new ArrayList<>();
        for (int day = 1; day <= totalDays; day++) {
            float score = byDay.containsKey(day) ? byDay.get(day) : 0f;
            filled.add(new CycleScoreChartView.Score(day, score, cycleId));
        }
        return filled;
    }

    private static File getBjjScoreFile(Context context) {
        File file = new File(context.getFilesDir(), "bjj_data.csv");

        if (!file.exists()) {
            try (InputStream in = context.getResources().openRawResource(R.raw.bjj_data);
                 FileOutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public static void appendBjjLevel(MainActivity context, int lvl, int cycleId) {
        try {
            File file = getBjjScoreFile(context);
            FileWriter writer = new FileWriter(file, true); // append mode

            writer.append(String.format(Locale.US, "%d,%d\n", lvl, cycleId));

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getCycleFile(Context context) {
        File file = new File(context.getFilesDir(), "cycle.csv");

        if (!file.exists()) {
            try (InputStream in = context.getResources().openRawResource(R.raw.cycle);
                 FileOutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public static void appendCycle(Context context, int cycleId, LocalDate startDate, int lutheal_duration) {
        File file = getCycleFile(context);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy", Locale.US);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            String lastLine = null;

            while ((line = reader.readLine()) != null) {
                if (first) {
                    lines.add(line); // header, unchanged
                    first = false;
                    continue;
                }
                lastLine = line;
                lines.add(line);
            }

            if (lastLine != null) {
                String patched = String.format(Locale.US, "%d,%s,%s,5,8,3,%d",
                        cycleId,
                        startDate.format(formatter),
                        yesterday.format(formatter),
                        lutheal_duration);

                lines.set(lines.size() - 1, patched); // overwrite last row
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        LocalDate newEndDate = LocalDate.now().plusDays(5+8+3+12);

        lines.add(String.format(Locale.US, "%d,%s,%s,5,8,3,%d",
                cycleId+1,
                LocalDate.now().format(formatter),
                newEndDate.format(formatter),
                12));

        try (FileWriter writer = new FileWriter(file, false)) { // overwrite whole file
            for (String l : lines) {
                writer.append(l).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}