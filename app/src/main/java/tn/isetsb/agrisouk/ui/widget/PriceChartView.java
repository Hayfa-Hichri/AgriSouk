package tn.isetsb.agrisouk.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.model.PricePoint;

/**
 * Vue personnalisée : courbe d'évolution du prix dessinée avec Canvas.
 */
public class PriceChartView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private final List<PricePoint> points = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.FRANCE);

    public PriceChartView(Context context) {
        super(context);
        init();
    }

    public PriceChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PriceChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2.5f));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.green_700));

        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.green_800));

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setColor(ContextCompat.getColor(getContext(), R.color.outline));

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.ink_500));
        textPaint.setTextSize(sp(10));
    }

    public void setPoints(List<PricePoint> history) {
        points.clear();
        if (history != null) {
            points.addAll(history);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = dp(6);
        float right = getWidth() - dp(6);
        float top = dp(14);
        float bottom = getHeight() - dp(22);

        if (points.size() < 2 || right <= left || bottom <= top) {
            String empty = "Historique indisponible";
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(empty, getWidth() / 2f,
                    getHeight() / 2f - (textPaint.ascent() + textPaint.descent()) / 2f, textPaint);
            return;
        }

        double min = points.get(0).getPrice();
        double max = min;
        for (PricePoint point : points) {
            min = Math.min(min, point.getPrice());
            max = Math.max(max, point.getPrice());
        }
        double range = max - min;
        if (range < 0.0001) {
            range = Math.max(max * 0.1, 0.1);
        }
        min -= range * 0.2;
        max += range * 0.2;
        range = max - min;

        for (int index = 0; index <= 2; index++) {
            float y = top + (bottom - top) * index / 2f;
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        int count = points.size();
        float step = (right - left) / (count - 1);
        linePath.reset();
        fillPath.reset();
        for (int index = 0; index < count; index++) {
            float x = left + step * index;
            float ratio = (float) ((points.get(index).getPrice() - min) / range);
            float y = bottom - ratio * (bottom - top);
            if (index == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        fillPath.lineTo(right, bottom);
        fillPath.lineTo(left, bottom);
        fillPath.close();

        fillPaint.setShader(new LinearGradient(0, top, 0, bottom,
                ContextCompat.getColor(getContext(), R.color.green_100),
                ContextCompat.getColor(getContext(), R.color.transparent),
                Shader.TileMode.CLAMP));
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        for (int index = 0; index < count; index++) {
            float x = left + step * index;
            float ratio = (float) ((points.get(index).getPrice() - min) / range);
            float y = bottom - ratio * (bottom - top);
            canvas.drawCircle(x, y, dp(3), dotPaint);
        }

        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(dateFormat.format(new Date(points.get(0).getRecordedAt())),
                left, getHeight() - dp(6), textPaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(dateFormat.format(new Date(points.get(count - 1).getRecordedAt())),
                right, getHeight() - dp(6), textPaint);
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value,
                getResources().getDisplayMetrics());
    }
}
