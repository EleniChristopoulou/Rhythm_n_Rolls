package com.example.bloodstamina;
// Drop these fields/methods into MainActivity.java (or a small helper class you call from it).
// Assumes exercises_score.csv has already been copied to internal storage via getScoreFile()
// and lives at: new File(getFilesDir(), "exercises_score.csv")

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class StreakCalculator {

    private static final String DATE_PATTERN = "d/M/yyyy";
    private static final String BJJ_EXERCISE_NAME = "BJJ Session";

    // Result holder so MainActivity can grab both numbers in one call
    public static class StreakResult {
        public final int bjjStreak;
        public final int otherStreak;

        public StreakResult(int bjjStreak, int otherStreak) {
            this.bjjStreak = bjjStreak;
            this.otherStreak = otherStreak;
        }
    }

    /**
     * Reads exercises_score.csv from internal storage and returns the current
     * streak (consecutive days, up to and including "today or yesterday")
     * for BJJ Session entries and for every other exercise.
     */
    public static StreakResult calculateStreaks(File csvFile) {
        Set<String> bjjDateStrings = new HashSet<>();
        Set<String> otherDateStrings = new HashSet<>();

        if (csvFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line = reader.readLine(); // skip header row
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String[] cols = line.split(",", -1);
                    if (cols.length < 2) continue;

                    String date = cols[0].trim();
                    String name = cols[1].trim();

                    if (name.equalsIgnoreCase(BJJ_EXERCISE_NAME)) {
                        bjjDateStrings.add(date);
                    } else {
                        otherDateStrings.add(date);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int bjjStreak = calculateStreakFromDates(bjjDateStrings);
        int otherStreak = calculateStreakFromDates(otherDateStrings);

        return new StreakResult(bjjStreak, otherStreak);
    }

    /**
     * Given a set of raw "d/M/yyyy" date strings on which a workout happened,
     * walks backward day by day from today (or yesterday, if today has no
     * entry yet) and counts how many consecutive days have an entry.
     */
    private static int calculateStreakFromDates(Set<String> rawDates) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        sdf.setLenient(false);

        Set<Long> normalizedDays = new HashSet<>();
        for (String raw : rawDates) {
            try {
                Date parsed = sdf.parse(raw);
                normalizedDays.add(startOfDay(parsed).getTimeInMillis());
            } catch (ParseException e) {
                // Malformed row (e.g. a typo'd year) — skip it rather than
                // let it corrupt the streak count.
                System.out.println("Skipping unparseable date: " + raw);
            }
        }

        Calendar cursor = startOfDay(new Date());

        // If nothing logged yet today, don't break an ongoing streak —
        // start checking from yesterday instead.
        if (!normalizedDays.contains(cursor.getTimeInMillis())) {
            cursor.add(Calendar.DAY_OF_YEAR, -1);
        }

        int streak = 0;
        while (normalizedDays.contains(cursor.getTimeInMillis())) {
            streak++;
            cursor.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    private static Calendar startOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
}

/*
Usage in MainActivity.java:

File csvFile = new File(getFilesDir(), "exercises_score.csv");
StreakCalculator.StreakResult result = StreakCalculator.calculateStreaks(csvFile);

bjjStreakTextView.setText(String.valueOf(result.bjjStreak));
otherStreakTextView.setText(String.valueOf(result.otherStreak));
*/