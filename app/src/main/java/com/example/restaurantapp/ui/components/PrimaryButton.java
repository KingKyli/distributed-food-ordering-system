package com.example.restaurantapp.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Primary Button Component
 * 
 * A modern, Material Design-inspired button for main actions like:
 * Checkout, Place Order, Add to Cart, Reorder
 * 
 * Features:
 * - Loading state with progress indicator
 * - Disabled state with custom styling
 * - Press animation (scale effect)
 * - Customizable colors and text
 * - Optional right arrow icon
 * - Full width or wrap content
 * 
 * Design Specs:
 * - Height: 52dp
 * - Border Radius: 14dp
 * - Padding: 20dp horizontal, 14dp vertical
 * - Background: #F97316 (orange-600)
 * - Text: #FFFFFF, 16sp, Semibold (600)
 * - Pressed: #EA580C (orange-700)
 * - Disabled: #E2E8F0 (slate-200), Text: #94A3B8
 */
public class PrimaryButton extends FrameLayout {
    
    // Default colors from design specs
    private static final int DEFAULT_BG_COLOR = 0xFFF97316;      // #F97316 orange-600
    private static final int PRESSED_BG_COLOR = 0xFFEA580C;      // #EA580C orange-700
    private static final int DISABLED_BG_COLOR = 0xFFE2E8F0;     // #E2E8F0 slate-200
    private static final int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;    // #FFFFFF white
    private static final int DISABLED_TEXT_COLOR = 0xFF94A3B8;   // #94A3B8 slate-400
    
    private static final int DEFAULT_HEIGHT_DP = 52;
    private static final int BORDER_RADIUS_DP = 14;
    private static final int HORIZONTAL_PADDING_DP = 20;
    private static final int VERTICAL_PADDING_DP = 14;
    private static final float TEXT_SIZE_SP = 16f;
    private static final float PRESS_SCALE = 0.985f;
    private static final int ICON_GAP_DP = 8;
    
    // UI Components
    private TextView textView;
    private ProgressBar progressBar;
    private ArrowIconView arrowIcon;
    
    // State
    private ButtonState currentState = ButtonState.DEFAULT;
    private String buttonText = "";
    private boolean fullWidth = true;
    private boolean showIcon = true;
    private boolean isUpdatingUI = false; // Prevent recursion
    
    // Colors (can be customized)
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int pressedColor = PRESSED_BG_COLOR;
    private int disabledColor = DISABLED_BG_COLOR;
    private int textColor = DEFAULT_TEXT_COLOR;
    private int disabledTextColor = DISABLED_TEXT_COLOR;
    
    // Animation
    private ValueAnimator scaleAnimator;
    private float currentScale = 1.0f;
    
    // Listeners
    private OnClickListener externalClickListener;
    
    public PrimaryButton(@NonNull Context context) {
        super(context);
        init(context, null);
    }
    
    public PrimaryButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public PrimaryButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        // Set default layout params
        if (getLayoutParams() == null) {
            setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(DEFAULT_HEIGHT_DP)
            ));
        }
        
        // Set padding
        int hPadding = dpToPx(HORIZONTAL_PADDING_DP);
        int vPadding = dpToPx(VERTICAL_PADDING_DP);
        setPadding(hPadding, vPadding, hPadding, vPadding);
        
        // Create background
        updateBackground();
        
        // Set clickable
        setClickable(true);
        setFocusable(true);
        
        // Create text view
        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP);
        textView.setTextColor(textColor);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.CENTER;
        addView(textView, textParams);
        
        // Create arrow icon
        arrowIcon = new ArrowIconView(context);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
            dpToPx(20),
            dpToPx(20)
        );
        iconParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        iconParams.setMarginEnd(dpToPx(4));
        addView(arrowIcon, iconParams);
        
        // Create progress bar
        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));
        }
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
            dpToPx(24),
            dpToPx(24)
        );
        progressParams.gravity = Gravity.CENTER;
        addView(progressBar, progressParams);
        progressBar.setVisibility(GONE);
        
        // Set internal click listener
        super.setOnClickListener(v -> {
            if (currentState == ButtonState.DEFAULT && externalClickListener != null) {
                externalClickListener.onClick(v);
            }
        });
        
        updateUI();
    }
    
    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.externalClickListener = l;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentState != ButtonState.DEFAULT) {
            return false;
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                animatePress(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                animatePress(false);
                break;
        }
        
        return super.onTouchEvent(event);
    }
    
    private void animatePress(boolean pressed) {
        if (scaleAnimator != null && scaleAnimator.isRunning()) {
            scaleAnimator.cancel();
        }
        
        float targetScale = pressed ? PRESS_SCALE : 1.0f;
        scaleAnimator = ValueAnimator.ofFloat(currentScale, targetScale);
        scaleAnimator.setDuration(100);
        scaleAnimator.addUpdateListener(animation -> {
            currentScale = (float) animation.getAnimatedValue();
            setScaleX(currentScale);
            setScaleY(currentScale);
        });
        scaleAnimator.start();
        
        // Update background color
        if (pressed) {
            setBackgroundTint(pressedColor);
        } else {
            updateBackground();
        }
    }
    
    private void updateBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(BORDER_RADIUS_DP));
        
        int bgColor;
        switch (currentState) {
            case DISABLED:
            case LOADING:
                bgColor = disabledColor;
                break;
            default:
                bgColor = backgroundColor;
                break;
        }
        
        drawable.setColor(bgColor);
        setBackground(drawable);
    }
    
    private void setBackgroundTint(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dpToPx(BORDER_RADIUS_DP));
        drawable.setColor(color);
        setBackground(drawable);
    }
    
    private void updateUI() {
        if (isUpdatingUI) {
            return; // Prevent recursion
        }
        
        isUpdatingUI = true;
        
        try {
            switch (currentState) {
                case LOADING:
                    textView.setVisibility(GONE);
                    arrowIcon.setVisibility(GONE);
                    progressBar.setVisibility(VISIBLE);
                    super.setEnabled(false); // Use super to avoid recursion
                    break;
                    
                case DISABLED:
                    textView.setVisibility(VISIBLE);
                    textView.setTextColor(disabledTextColor);
                    arrowIcon.setVisibility(showIcon ? VISIBLE : GONE);
                    arrowIcon.setColor(disabledTextColor);
                    progressBar.setVisibility(GONE);
                    super.setEnabled(false); // Use super to avoid recursion
                    break;
                    
                case DEFAULT:
                case PRESSED:
                default:
                    textView.setVisibility(VISIBLE);
                    textView.setTextColor(textColor);
                    arrowIcon.setVisibility(showIcon ? VISIBLE : GONE);
                    arrowIcon.setColor(textColor);
                    progressBar.setVisibility(GONE);
                    super.setEnabled(true); // Use super to avoid recursion
                    break;
            }
            
            updateBackground();
        } finally {
            isUpdatingUI = false;
        }
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
     * Set the button state
     */
    public void setState(ButtonState state) {
        if (this.currentState == state) {
            return; // Avoid unnecessary updates
        }
        this.currentState = state;
        updateUI();
    }
    
    /**
     * Show loading state
     */
    public void setLoading(boolean loading) {
        setState(loading ? ButtonState.LOADING : ButtonState.DEFAULT);
    }
    
    /**
     * Enable/disable the button
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setState(enabled ? ButtonState.DEFAULT : ButtonState.DISABLED);
    }
    
    /**
     * Show or hide the right arrow icon
     */
    public void setShowIcon(boolean show) {
        this.showIcon = show;
        updateUI();
    }
    
    /**
     * Set custom background color
     */
    public void setButtonBackgroundColor(int color) {
        this.backgroundColor = color;
        updateBackground();
    }
    
    /**
     * Set custom text color
     */
    public void setTextColor(int color) {
        this.textColor = color;
        this.textView.setTextColor(color);
    }
    
    /**
     * Get current state
     */
    public ButtonState getState() {
        return currentState;
    }
    
    /**
     * Get button text
     */
    public String getText() {
        return buttonText;
    }
    
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            getResources().getDisplayMetrics()
        );
    }
    
    /**
     * Custom arrow icon view for the right side of the button
     */
    private static class ArrowIconView extends View {
        private Paint paint;
        private Path path;
        private int color = Color.WHITE;
        
        public ArrowIconView(Context context) {
            super(context);
            init();
        }
        
        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dpToPx(2));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(color);
            
            path = new Path();
        }
        
        public void setColor(int color) {
            this.color = color;
            paint.setColor(color);
            invalidate();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            int width = getWidth();
            int height = getHeight();
            
            // Draw arrow: → 
            // Line from left to right
            float startX = width * 0.2f;
            float endX = width * 0.8f;
            float centerY = height * 0.5f;
            
            canvas.drawLine(startX, centerY, endX, centerY, paint);
            
            // Arrow head
            float headSize = width * 0.25f;
            path.reset();
            path.moveTo(endX - headSize, centerY - headSize);
            path.lineTo(endX, centerY);
            path.lineTo(endX - headSize, centerY + headSize);
            canvas.drawPath(path, paint);
        }
        
        private int dpToPx(int dp) {
            return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
            );
        }
    }
}

