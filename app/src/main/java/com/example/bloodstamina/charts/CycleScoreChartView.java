package com.example.bloodstamina.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Renders a per-day cycle score line (0-100) with faded background bands
 * marking the 4 cycle phases (Menstrual, Follicular, Ovulation, Luteal).
 *
 * XML usage:
 *   <com.ironlog.ui.charts.CycleScoreChartView
 *       android:id="@+id/cycleScoreChart"
 *       android:layout_width="match_parent"
 *       android:layout_height="220dp" />
 *
 * Code usage:
 *   cycleScoreChart.setData(scores, phases, 28);
 */
public class CycleScoreChartView extends View {

    // ---------- Public data model ----------

    public static class Score {
        public final int day;
        public final float score; // 0..100
        public final int cycleid;

        public Score(int day, float score, int cycleid) {
            this.day = day;
            this.score = score;
            this.cycleid = cycleid;
        }
    }

    public static class Phase {
        public final String name;
        public final int startDay; // inclusive, 1-indexed
        public final int endDay;   // inclusive
        public final int color;

        public Phase(String name, int startDay, int endDay, int color) {
            this.name = name;
            this.startDay = startDay;
            this.endDay = endDay;
            this.color = color;
        }
    }

    // ---------- Data ----------

    private List<Score> scores = new ArrayList<>();
    private List<Phase> phases = new ArrayList<>();
    private int cycleLength = 28;

    public void setData(List<Score> scores, List<Phase> phases, int cycleLength) {
        this.scores = scores != null ? scores : new ArrayList<Score>();
        this.phases = phases != null ? phases : new ArrayList<Phase>();
        this.cycleLength = cycleLength;
        invalidate();
    }

    public void setData(List<Score> scores, List<Phase> phases) {
        setData(scores, phases, 28);
    }

    // ---------- Theming ----------

    private int lineColor = Color.parseColor("#A4F4A9C3");   // pink
    private int pointColor = Color.parseColor("#A4F4F4F4");  // text/pink
    private int gridColor = Color.parseColor("#40595B82"); // secondary, low alpha
    private int labelColor = Color.parseColor("#ECDFCC");  // white
    private int chartBackground = Color.TRANSPARENT;

    public void setLineColor(int color) { this.lineColor = color; invalidate(); }
    public void setPointColor(int color) { this.pointColor = color; invalidate(); }
    public void setGridColor(int color) { this.gridColor = color; invalidate(); }
    public void setLabelColor(int color) { this.labelColor = color; invalidate(); }
    public void setChartBackground(int color) { this.chartBackground = color; invalidate(); }

    /** Alpha (0-255) applied to each phase band. Default 40 = faded. */
    private int bandAlpha = 40;
    public void setBandAlpha(int alpha) { this.bandAlpha = alpha; invalidate(); }

    // ---------- Paint setup ----------

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path fillPath = new Path();
    private final Path linePath = new Path();

    private final RectF chartPadding;
    private LocalDate startingDate;

    public CycleScoreChartView(Context context) {
        this(context, null);
    }

    public CycleScoreChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CycleScoreChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        chartPadding = new RectF(dp(8f), dp(16f), dp(8f), dp(28f));

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2.5f));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);
        pointPaint.setStyle(Paint.Style.FILL);

        pointRingPaint.setStyle(Paint.Style.STROKE);
//        pointRingPaint.setStrokeWidth(dp(2f));

        bandPaint.setStyle(Paint.Style.FILL);

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1f));

        labelPaint.setTextSize(dp(11f));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        // Safe default for alpha-layered drawing (gradients + translucent rects)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (chartBackground != Color.TRANSPARENT) {
            canvas.drawColor(chartBackground);
        }

        final float plotLeft = chartPadding.left;
        final float plotTop = chartPadding.top;
        final float plotRight = getWidth() - chartPadding.right;
        final float plotBottom = getHeight() - chartPadding.bottom;
        final float plotWidth = plotRight - plotLeft;
        final float plotHeight = plotBottom - plotTop;

        if (plotWidth <= 0 || plotHeight <= 0 || cycleLength <= 1) return;

        // ---------- 1. Faded phase bands (background) ----------
        for (Phase phase : phases) {
            float left = xForDay(phase.startDay, plotLeft, plotWidth);
            // extend right edge half a day-step so bands sit edge-to-edge, no gaps
            float dayStep = plotWidth / (cycleLength - 1);
            float right = xForDay(phase.endDay, plotLeft, plotWidth) + dayStep * 0.5f;

            bandPaint.setColor(phase.color);
            bandPaint.setAlpha(bandAlpha);

            canvas.drawRect(
                    Math.max(left, plotLeft),
                    plotTop,
                    Math.min(right, plotRight),
                    plotBottom,
                    bandPaint
            );
        }

        // ---------- 2. Grid lines at 25/50/75 ----------
        gridPaint.setColor(gridColor);
        float[] gridFractions = {0.4f, 0.5f, 0.6f, 0.7f};
        for (float fraction : gridFractions) {
            float y = yForScore(yAxisMax * fraction, plotTop, plotHeight);
            canvas.drawLine(plotLeft, y, plotRight, y, gridPaint);
        }

        // ---------- 3. Score line + gradient fill ----------
        if (!scores.isEmpty()) {
            linePath.reset();
            fillPath.reset();

            List<Score> sorted = new ArrayList<>(scores);
            Collections.sort(sorted, new Comparator<Score>() {
                @Override
                public int compare(Score a, Score b) {
                    return Integer.compare(a.day, b.day);
                }
            });

            // Precompute pixel coordinates for every point first
            float[] xs = new float[sorted.size()];
            float[] ys = new float[sorted.size()];
            for (int i = 0; i < sorted.size(); i++) {
                Score point = sorted.get(i);
                xs[i] = xForDay(point.day, plotLeft, plotWidth);
                ys[i] = yForScore(point.score, plotTop, plotHeight);
            }

            linePath.moveTo(xs[0], ys[0]);
            fillPath.moveTo(xs[0], plotBottom);
            fillPath.lineTo(xs[0], ys[0]);

            // Smooth curve: cubic bezier between each pair of points, with control
            // points pulled from the neighbouring points (Catmull-Rom style) so the
            // curve bends naturally instead of just rounding each segment in isolation.
            for (int i = 0; i < xs.length - 1; i++) {
                float x0 = i == 0 ? xs[i] : xs[i - 1];
                float y0 = i == 0 ? ys[i] : ys[i - 1];
                float x1 = xs[i];
                float y1 = ys[i];
                float x2 = xs[i + 1];
                float y2 = ys[i + 1];
                float x3 = (i + 2 < xs.length) ? xs[i + 2] : x2;
                float y3 = (i + 2 < xs.length) ? ys[i + 2] : y2;

                // Catmull-Rom to Bezier control point conversion
                float cp1x = x1 + (x2 - x0) / 6f;
                float cp1y = y1 + (y2 - y0) / 6f;
                float cp2x = x2 - (x3 - x1) / 6f;
                float cp2y = y2 - (y3 - y1) / 6f;

                linePath.cubicTo(cp1x, cp1y, cp2x, cp2y, x2, y2);
                fillPath.cubicTo(cp1x, cp1y, cp2x, cp2y, x2, y2);
            }

            float lastX = xs[xs.length - 1];
            fillPath.lineTo(lastX, plotBottom);
            fillPath.close();

            fillPaint.setShader(new LinearGradient(
                    0f, plotTop, 0f, plotBottom,
                    Color.argb(90, Color.red(lineColor), Color.green(lineColor), Color.blue(lineColor)),
                    Color.argb(0, Color.red(lineColor), Color.green(lineColor), Color.blue(lineColor)),
                    Shader.TileMode.CLAMP
            ));
            canvas.drawPath(fillPath, fillPaint);

            linePaint.setColor(lineColor);
            canvas.drawPath(linePath, linePaint);

            // ---------- 4. Points per day ----------
            for (Score point : sorted) {
                float x = xForDay(point.day, plotLeft, plotWidth);
                float y = yForScore(point.score, plotTop, plotHeight);

                pointPaint.setColor(pointColor);
                canvas.drawCircle(x, y, dp(2.5f), pointPaint);

                pointRingPaint.setColor(chartBackgroundOrFallback());
                canvas.drawCircle(x, y, dp(2.5f), pointRingPaint);
            }
        }

//        // ---------- 5. Day labels (every 7 days) ----------
//        labelPaint.setColor(labelColor);
//        int day = 1;
//        while (day <= cycleLength) {
//            float x = xForDay(day, plotLeft, plotWidth);
//            canvas.drawText(String.valueOf(day), x, getHeight() - dp(6f), labelPaint);
//            day += 7;
//        }
        // ---------- 5. Calendar date labels ----------
        labelPaint.setColor(labelColor);

        int day = 1;

        while (day <= cycleLength) {

            float x = xForDay(day, plotLeft, plotWidth);

            String label;

            if (startingDate != null) {
                LocalDate date = startingDate.plusDays(day - 1);
                label = String.valueOf(date.getDayOfMonth());
            } else {
                // Fallback to cycle day if no starting date was supplied
                label = String.valueOf(day);
            }

            canvas.drawText(
                    label,
                    x,
                    getHeight() - dp(6f),
                    labelPaint
            );

            day += 7;
        }
    }

    private float xForDay(int day, float plotLeft, float plotWidth) {
        int clampedDay = Math.max(1, Math.min(day, cycleLength));
        return plotLeft + ((clampedDay - 1) / (float) (cycleLength - 1)) * plotWidth;
    }
    private float yAxisMax = 500f; // default keeps existing cycle-score behavior

    public void setYAxisMax(float max) {
        this.yAxisMax = Math.max(max, 1f); // avoid divide-by-zero / degenerate scale
        invalidate();
    }

    private float yForScore(float score, float plotTop, float plotHeight) {
        float clamped = Math.max(0f, Math.min(score, yAxisMax));
        return plotTop + (1f - (clamped / yAxisMax)) * plotHeight;
    }

    private int chartBackgroundOrFallback() {
        return chartBackground != Color.TRANSPARENT ? chartBackground : Color.parseColor("#333457");
    }

    public void setStartingDate(LocalDate startingDate) {
        this.startingDate = startingDate;
        invalidate();
    }
}