package com.example.restaurantapp.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Secondary Button Component
 * 
 * An outlined, minimal button for secondary actions like:
 * "View Menu", "Open", "See More", "Details"
 * 
 * Features:
 * - Outlined style (border, no fill)
 * - Navy border (#1E293B)
 * - Optional icon
 * - Clean & minimal
 * - Press feedback
 * 
 * Design Specs:
 * - Height: 44dp
 * - Border Radius: pill shape
 * - Border Width: 2dp
 * - Text: Navy #1E293B, 16sp, Medium (500)
 * - Background: Transparent (pressed: 5% navy)
 * 
 * Usage:
 * Important but NOT primary actions
 * - Inside cards (e.g., "Open" button)
 * - Lists (e.g., "View more")
 * - Navigation (e.g., "See all")
 */
public class SecondaryButton extends FrameLayout {
    
    // Default colors from design specs
    private static final int BORDER_COLOR = 0xFF1E293B;        // #1E293B navy
    private static final int TEXT_COLOR = 0xFF1E293B;          // #1E293B navy
    private static final int PRESSED_BG_COLOR = 0x140F172A;    // rgba(15, 23, 42, 0.08)
    
    private static final int DEFAULT_HEIGHT_DP = 44;
    private static final int BORDER_RADIUS_DP = 22;
    private static final int HORIZONTAL_PADDING_DP = 24;
    private static final int BORDER_WIDTH_DP = 2;
    private static final float TEXT_SIZE_SP = 16f;
    private static final int ICON_SIZE_DP = 24;
    private static final int ICON_GAP_DP = 12;
    
    // UI Components
    private TextView textView;
    private IconView iconView;
    
    // State
    private String buttonText = "";
    private boolean showIcon = false;
    private int iconColor = 0xFFF97316; // Orange by default
    
    // Colors (can be customized)
    private int borderColor = BORDER_COLOR;
    private int textColor = TEXT_COLOR;
    private int pressedBgColor = PRESSED_BG_COLOR;
    
    // Pressed state
    private boolean isPressed = false;
    
    // Listeners
    private OnClickListener externalClickListener;
    
    public SecondaryButton(@NonNull Context context) {
        super(context);
        init(context, null);
    }
    
    public SecondaryButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public SecondaryButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        // Set default layout params
        if (getLayoutParams() == null) {
            setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dpToPx(DEFAULT_HEIGHT_DP)
            ));
        }
        
        // Keep a fixed minimum touch target and pill silhouette.
        setMinimumHeight(dpToPx(DEFAULT_HEIGHT_DP));

        // Set padding
        int hPadding = dpToPx(HORIZONTAL_PADDING_DP);
        int vPadding = 0;
        setPadding(hPadding, vPadding, hPadding, vPadding);
        
        // Create background
        updateBackground();
        
        // Set clickable
        setClickable(true);
        setFocusable(true);
        
        // Create row container so icon/text alignment is stable across widths.
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.CENTER;
        addView(container, containerParams);
        
        // Create icon view (optional, hidden by default)
        iconView = new IconView(context);
        iconView.setColor(iconColor);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
            dpToPx(ICON_SIZE_DP),
            dpToPx(ICON_SIZE_DP)
        );
        iconParams.gravity = Gravity.CENTER_VERTICAL;
        iconParams.setMarginEnd(dpToPx(ICON_GAP_DP));
        container.addView(iconView, iconParams);
        iconView.setVisibility(GONE);
        
        // Create text view
        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP);
        textView.setTextColor(textColor);
        textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        textView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.CENTER_VERTICAL;
        container.addView(textView, textParams);
        
        // Set internal click listener
        super.setOnClickListener(v -> {
            if (externalClickListener != null) {
                externalClickListener.onClick(v);
            }
        });
    }
    
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.externalClickListener = l;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                updateBackground();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                updateBackground();
                break;
        }
        
        return super.onTouchEvent(event);
    }
    
    private void updateBackground() {
        GradientDrawable drawable = new GradientDrawable();
        float fallbackRadius = dpToPx(BORDER_RADIUS_DP);
        float dynamicRadius = getHeight() > 0 ? getHeight() / 2f : fallbackRadius;
        drawable.setCornerRadius(dynamicRadius);
        drawable.setStroke(dpToPx(BORDER_WIDTH_DP), borderColor);
        
        if (isPressed) {
            drawable.setColor(pressedBgColor);
        } else {
            drawable.setColor(Color.TRANSPARENT);
        }
        
        setBackground(drawable);
    }
    
    // Public API
    
    /**
     * Set the button text
     */
    public void setText(String text) {
        this.buttonText = text;
        this.textView.setText(text);
    }
    
    /**
     * Get button text
     */
    public String getText() {
        return buttonText;
    }
    
    /**
     * Show or hide the icon
     */
    public void setShowIcon(boolean show) {
        this.showIcon = show;
        iconView.setVisibility(show ? VISIBLE : GONE);
    }
    
    /**
     * Set icon color
     */
    public void setIconColor(int color) {
        this.iconColor = color;
        iconView.setColor(color);
    }
    
    /**
     * Set custom border color
     */
    public void setBorderColor(int color) {
        this.borderColor = color;
        updateBackground();
    }
    
    /**
     * Set custom text color
     */
    public void setTextColor(int color) {
        this.textColor = color;
        this.textView.setTextColor(color);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1.0f : 0.55f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateBackground();
    }
    
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            getResources().getDisplayMetrics()
        );
    }
    
    /**
     * Custom icon view (menu icon)
     */
    private static class IconView extends View {
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF docRect = new RectF();
        private int color = 0xFFF97316;
        
        public IconView(Context context) {
            super(context);
            init();
        }
        
        private void init() {
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(dpToPx(2));
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setColor(color);

            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(color);
        }
        
        public void setColor(int color) {
            this.color = color;
            strokePaint.setColor(color);
            fillPaint.setColor(color);
            invalidate();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            int width = getWidth();
            int height = getHeight();

            float left = width * 0.20f;
            float top = height * 0.10f;
            float right = width * 0.80f;
            float bottom = height * 0.90f;
            float radius = dpToPx(4);

            docRect.set(left, top, right, bottom);
            canvas.drawRoundRect(docRect, radius, radius, strokePaint);

            float innerStart = left + width * 0.16f;
            float innerEnd = right - width * 0.16f;
            canvas.drawLine(innerStart, height * 0.38f, innerEnd, height * 0.38f, strokePaint);
            canvas.drawLine(innerStart, height * 0.56f, innerEnd, height * 0.56f, strokePaint);

            canvas.drawCircle(left + width * 0.22f, height * 0.74f, dpToPx(1.8f), fillPaint);
        }
        
        private float dpToPx(float dp) {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
            );
        }
    }
}

