package com.example.restaurantapp.ui.components;

/**
 * Represents the visual state of a PrimaryButton
 */
public enum ButtonState {
    /** Default/idle state - button is ready for interaction */
    DEFAULT,
    
    /** Pressed state - button is currently being touched */
    PRESSED,
    
    /** Disabled state - button cannot be interacted with */
    DISABLED,
    
    /** Loading state - button is processing an action */
    LOADING
}

