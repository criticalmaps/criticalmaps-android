package de.stephanlindauer.criticalmaps.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

import de.stephanlindauer.criticalmaps.R;

public class StorageSpaceGraph extends View {

    private float usedSpacePercentage = 0.4f;
    private float tilecachePercentage = 0.1f;

    private Paint usedSpacePaint;
    private Paint tilecachePaint;
    private Paint freeSpacePaint;

    private Rect usedSpaceArea;
    private Rect tilecacheBarArea;
    private Rect freeSpaceArea;

    private Rect drawableArea;

    public StorageSpaceGraph(Context context) {
        this(context, null);
    }

    public StorageSpaceGraph(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StorageSpaceGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.StorageSpaceGraph, 0, 0);

        int usedSpaceColor;
        int tilecacheColor;
        int freeSpaceColor;

        try {
            usedSpaceColor = typedArray.getColor(
                    R.styleable.StorageSpaceGraph_usedSpaceColor, 0xFFFF0000);
            tilecacheColor = typedArray.getColor(
                    R.styleable.StorageSpaceGraph_tilecacheColor, 0xFF00FF00);
            freeSpaceColor = typedArray.getColor(
                    R.styleable.StorageSpaceGraph_freeSpaceColor, 0xFF0000FF);
        } finally {
            typedArray.recycle();
        }

        usedSpacePaint = new Paint();
        usedSpacePaint.setColor(usedSpaceColor);
        usedSpacePaint.setStyle(Paint.Style.FILL);
        usedSpacePaint.setAntiAlias(true);

        tilecachePaint = new Paint(usedSpacePaint);
        tilecachePaint.setColor(tilecacheColor);

        freeSpacePaint = new Paint(usedSpacePaint);
        freeSpacePaint.setColor(freeSpaceColor);

        usedSpaceArea = new Rect();
        tilecacheBarArea = new Rect();
        freeSpaceArea = new Rect();

        drawableArea = new Rect();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        drawableArea.set(
                getPaddingLeft(), getPaddingTop(),w - getPaddingRight(), h - getPaddingBottom());
        setBars();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(usedSpaceArea, usedSpacePaint);
        canvas.drawRect(tilecacheBarArea, tilecachePaint);
        canvas.drawRect(freeSpaceArea, freeSpacePaint);
    }

    private void setBars() {
        int w = drawableArea.width();
        // bar for tilecache should be at least 4px wide to indicate it exists
        int visibilityExtra = (tilecachePercentage > 0.0f) && (w * tilecachePercentage < 1) ? 4 : 0;

        usedSpaceArea.set(
                drawableArea.left,
                drawableArea.top,
                (int) (w * usedSpacePercentage),
                drawableArea.bottom);
        tilecacheBarArea.set(
                (int) (w * usedSpacePercentage),
                drawableArea.top,
                (int) (w * (usedSpacePercentage + tilecachePercentage)) + visibilityExtra,
                drawableArea.bottom);
        freeSpaceArea.set(
                (int) (w * (usedSpacePercentage + tilecachePercentage)) + visibilityExtra,
                drawableArea.top,
                drawableArea.right,
                drawableArea.bottom);
    }

    public void setBarPercentages(float usedSpacePercentage, float tilecachePercentage) {
        this.usedSpacePercentage = usedSpacePercentage;
        this.tilecachePercentage = tilecachePercentage;
        setBars();
        invalidate();
    }

    public void setBarPercentagesAnimated(float usedSpacePercentage, float tilecachePercentage) {
        float totalPercentage = usedSpacePercentage + tilecachePercentage;
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, totalPercentage);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearOutSlowInInterpolator());
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            if (animatedValue < usedSpacePercentage) {
                setBarPercentages(animatedValue, 0);
            } else {
                setBarPercentages(usedSpacePercentage, animatedValue - usedSpacePercentage);
            }
        });
        animator.start();
    }
}
