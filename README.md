# Rhythm & Rolls

*Menstrual cycle tracking meets BJJ & gym training.*

Rhythm & Rolls helps you understand how your training performance connects to where you are in your menstrual cycle — log BJJ rolls and gym sessions, track your cycle phase, and see how your daily "score" trends across Menstrual, Follicular, Ovulation, and Luteal phases.

> **Working title.** "Rhythm & Rolls" is a placeholder name — swap it out below once you settle on something final.

---

## Features

- **Cycle phase tracking** — automatically calculates your current phase (Menstrual, Follicular, Ovulation, Luteal) based on cycle start date and per-phase durations, with phase-specific colors, icons, and training insight text.
- **Self-correcting cycle length** — if a period runs late, the app detects the overrun and extends the current cycle's Luteal phase instead of crashing or silently miscounting days.
- **BJJ session logging** — log rolls, track reps/weight/score, and progress through a belt system (white → blue → purple → brown → black) with stripe indicators, all driven by a running level score.
- **Gym session logging** — logs exercises alongside BJJ sessions, feeding into a combined daily performance score.
- **Daily scoring & streaks** — combines session data into a daily score per cycle day, tracks best-day-of-cycle, and calculates workout streaks (with a rest-day nudge after 5+ days).
- **Cycle score chart** — visualizes score trends across the cycle, phase-shaded, via a custom `CycleScoreChartView`.
- **Manual cycle logging** — closes out the current cycle (with a corrected final Luteal duration) and starts a new one, all from a single confirmation dialog.

---

## Tech stack

- **Platform:** Android (Java)
- **UI:** AppCompatActivity, ConstraintLayout, Material Components (`MaterialAlertDialogBuilder`)
- **Data storage:** Local CSV files, seeded from bundled `res/raw/` resources on first launch and read/written from app-private internal storage (`getFilesDir()`) thereafter
- **Date handling:** `java.time` (`LocalDate`, `ChronoUnit`, `DateTimeFormatter`)

---

## Data model

All data is stored as CSVs, seeded once from `res/raw/` into internal storage:

| File | Purpose | Key columns |
|---|---|---|
| `cycle.csv` | One row per menstrual cycle | `cycleid, starting_date, ending_date, Menstrual, Follicular, Ovulation, Luteal` |
| `bjj_data.csv` | BJJ belt/level progression per cycle | `lvl, cycleId` |
| `exercises_score.csv` | Per-exercise session log | date, exercise type, reps, weight, score, phase, cycle id, cycle day |

Each CSV follows the same read/write pattern: a `getXFile(context)` helper copies the seed resource into `getFilesDir()` on first access, and every subsequent read/write targets that internal copy — **not** the bundled `res/raw/` resource, which stays fixed at install time.

### Cycle correction logic

`Menstrual_Cycle` phases are defined by fixed day-counts for Menstrual (5), Follicular (8), and Ovulation (3), with a variable Luteal duration. Because periods don't always arrive on schedule, `loadCycle()`:

1. Loads all cycles from `cycle.csv`.
2. Checks whether the **most recent** cycle has run past its expected total duration.
3. If so, extends that cycle's Luteal duration in memory so the current day still falls within a valid phase — preventing out-of-bounds crashes in day-indexed lookups (chart data, streak calculation, etc.).

Logging a new cycle (`logCycle`) closes out the previous cycle with its true final Luteal duration (based on elapsed days) and appends a new cycle row starting today — this is the point at which the correction becomes permanent in the CSV rather than just an in-memory adjustment.

---

## Project structure

```
app/src/main/java/com/example/bloodstamina/
├── MainActivity.java          # Home screen: cycle display, phase info, logging actions
├── Menstrual_Cycle.java       # Cycle phase math, duration/day calculations
├── CsvReader.java             # CSV read/write for cycles, BJJ levels, exercise scores
├── BJJ_Rolls.java             # BJJ session data (level, reps, weight, score)
├── StreakCalculator.java      # Workout streak calculation from exercise log
├── ExerciceActivity.java      # Exercise logging screen
└── charts/
    └── CycleScoreChartView.java   # Custom view for phase-shaded score chart

app/src/main/res/raw/
├── cycle.csv                  # Seed data for cycles
├── bjj_data.csv                # Seed data for BJJ levels
└── exercises_score.csv        # Seed data for exercise scores (if applicable)
```

---

## Setup

1. Clone the repo and open in Android Studio.
2. Ensure `res/raw/cycle.csv`, `res/raw/bjj_data.csv`, etc. exist with valid seed data (see table above for `cycle.csv`'s required columns).
3. Build and run on a device/emulator running a supported minimum SDK (see `build.gradle`).
4. On first launch, seed CSVs are copied into app-private internal storage — subsequent runs read/write from there.

> **Note:** If you edit a seed CSV in `res/raw/` after the app has already run once, those changes won't appear until you clear app data or reinstall — the internal copy takes precedence once it exists.

---

## Known limitations / open items

- Cycle correction on overrun assumes a single ongoing cycle at a time (the most recent row); no support yet for retroactively editing older, already-closed cycles.
- Default Luteal duration for a newly logged cycle is currently a fixed placeholder rather than personalized (e.g. averaged from past cycles).
- No cloud sync/backup — all data is local to the device via internal-storage CSVs.

---

## License

*(Add your preferred license here.)*
