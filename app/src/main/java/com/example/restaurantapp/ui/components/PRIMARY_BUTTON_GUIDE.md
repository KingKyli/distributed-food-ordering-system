# Primary Button Component

A modern, Material Design-inspired button for main actions in the Foodie Express app.

## 📐 Design Specs

Based on the design system:

| Property | Value |
|----------|-------|
| Height | 52dp |
| Border Radius | 14dp |
| Padding | 20dp (horizontal) / 14px (vertical) |
| Background | #F97316 (orange-600) |
| Text Color | #FFFFFF (white) |
| Font Size | 16sp |
| Font Weight | Semibold (600) |
| Pressed State | #EA580C (orange-700) |
| Disabled BG | #E2E8F0 (slate-200) |
| Disabled Text | #94A3B8 (slate-400) |
| Icon Gap | 8dp |

## 🎨 States

- **DEFAULT** - Ready for interaction
- **PRESSED** - Visual feedback when touched (scale animation)
- **DISABLED** - Cannot be interacted with
- **LOADING** - Shows spinner, blocks interaction

## 📦 Usage

### In XML Layout

```xml
<com.example.restaurantapp.ui.components.PrimaryButton
    android:id="@+id/checkoutButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp" />
```

### In Java Code

#### Basic Usage

```java
import com.example.restaurantapp.ui.components.PrimaryButton;
import com.example.restaurantapp.ui.components.ButtonState;

// In your Activity or Fragment
PrimaryButton checkoutButton = findViewById(R.id.checkoutButton);
checkoutButton.setText("CHECKOUT");
checkoutButton.setOnClickListener(v -> {
    // Handle checkout
    Toast.makeText(this, "Checkout clicked!", Toast.LENGTH_SHORT).show();
});
```

#### With Loading State

```java
PrimaryButton placeOrderButton = findViewById(R.id.placeOrderButton);
placeOrderButton.setText("PLACE ORDER");
placeOrderButton.setOnClickListener(v -> {
    // Show loading
    placeOrderButton.setLoading(true);
    
    // Simulate API call
    new Handler().postDelayed(() -> {
        // Hide loading
        placeOrderButton.setLoading(false);
        Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
    }, 2000);
});
```

#### Programmatic Creation

```java
PrimaryButton button = new PrimaryButton(context);
button.setText("ADD TO CART");
button.setShowIcon(true); // Show right arrow
button.setOnClickListener(v -> {
    // Add to cart logic
});

// Add to layout
yourLayout.addView(button);
```

#### Disabled State

```java
PrimaryButton submitButton = findViewById(R.id.submitButton);
submitButton.setText("SUBMIT");

// Disable the button
submitButton.setEnabled(false);

// Enable when form is valid
if (isFormValid()) {
    submitButton.setEnabled(true);
}
```

#### Without Icon

```java
PrimaryButton button = findViewById(R.id.button);
button.setText("REORDER");
button.setShowIcon(false); // Hide right arrow
```

#### Custom Colors

```java
PrimaryButton customButton = findViewById(R.id.customButton);
customButton.setText("CUSTOM");
customButton.setButtonBackgroundColor(0xFF10B981); // Green
customButton.setTextColor(0xFFFFFFFF); // White
```

## 🎯 Best Practices

### ✅ DO

- Use **only one primary button per screen** to maintain focus and drive action
- Use for main CTAs: Checkout, Place Order, Add to Cart, Reorder
- Show loading state during async operations
- Disable button when action cannot be performed
- Use clear, action-oriented text (verb + noun)

### ❌ DON'T

- Don't use multiple primary buttons on the same screen
- Don't use for secondary actions (use secondary button instead)
- Don't use very long text (keep it concise)
- Don't forget to handle loading/error states

## 📱 Example Screens

### Basket Activity (Checkout)

```java
public class BasketActivity extends AppCompatActivity {
    private PrimaryButton checkoutButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
        
        checkoutButton = findViewById(R.id.checkoutButton);
        checkoutButton.setText("CHECKOUT");
        checkoutButton.setOnClickListener(v -> handleCheckout());
        
        updateCheckoutButton();
    }
    
    private void updateCheckoutButton() {
        // Disable if basket is empty
        boolean hasItems = basket != null && !basket.getItems().isEmpty();
        checkoutButton.setEnabled(hasItems);
    }
    
    private void handleCheckout() {
        checkoutButton.setLoading(true);
        
        // Process order
        orderService.placeOrder(basket, new OrderCallback() {
            @Override
            public void onSuccess(OrderRecord order) {
                runOnUiThread(() -> {
                    checkoutButton.setLoading(false);
                    Toast.makeText(BasketActivity.this, 
                        "Order placed successfully!", 
                        Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    checkoutButton.setLoading(false);
                    Toast.makeText(BasketActivity.this, 
                        "Failed to place order: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
```

### Restaurant Details (Add to Basket)

```java
public class RestaurantDetailsActivity extends AppCompatActivity {
    private PrimaryButton addToBasketButton;
    private Product selectedProduct;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        
        addToBasketButton = findViewById(R.id.addToBasketButton);
        addToBasketButton.setText("ADD TO BASKET");
        addToBasketButton.setOnClickListener(v -> addToBasket());
    }
    
    private void addToBasket() {
        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        basket.addItem(selectedProduct);
        
        // Visual feedback
        Toast.makeText(this, 
            selectedProduct.getName() + " added to basket", 
            Toast.LENGTH_SHORT).show();
    }
}
```

### Order History (Reorder)

```java
public class OrderHistoryActivity extends AppCompatActivity {
    
    private void setupReorderButton(OrderRecord order, PrimaryButton reorderButton) {
        reorderButton.setText("REORDER");
        reorderButton.setOnClickListener(v -> {
            reorderButton.setLoading(true);
            
            // Reorder logic
            orderService.reorder(order.getOrderId(), new OrderCallback() {
                @Override
                public void onSuccess(OrderRecord newOrder) {
                    runOnUiThread(() -> {
                        reorderButton.setLoading(false);
                        Toast.makeText(OrderHistoryActivity.this, 
                            "Order placed!", 
                            Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        reorderButton.setLoading(false);
                        Toast.makeText(OrderHistoryActivity.this, 
                            "Failed: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }
}
```

## 🎨 Sample XML Layout

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    
    <!-- Order Summary -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@android:color/white"
        android:padding="16dp"
        android:layout_marginBottom="16dp">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Subtotal"
            android:textSize="14sp" />
        
        <TextView
            android:id="@+id/subtotalText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="$24.90"
            android:textSize="16sp"
            android:textStyle="bold" />
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Delivery Fee"
            android:textSize="14sp"
            android:layout_marginTop="8dp" />
        
        <TextView
            android:id="@+id/deliveryFeeText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="$2.99"
            android:textSize="16sp"
            android:textStyle="bold" />
        
        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="#E0E0E0"
            android:layout_marginVertical="12dp" />
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Total"
            android:textSize="16sp"
            android:textStyle="bold" />
        
        <TextView
            android:id="@+id/totalText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="$27.89"
            android:textSize="20sp"
            android:textStyle="bold"
            android:textColor="#F97316" />
    </LinearLayout>
    
    <!-- Primary Button -->
    <com.example.restaurantapp.ui.components.PrimaryButton
        android:id="@+id/placeOrderButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
    
</LinearLayout>
```

## 🔧 API Reference

### Methods

| Method | Description |
|--------|-------------|
| `setText(String text)` | Set button text |
| `setState(ButtonState state)` | Set button state (DEFAULT, PRESSED, DISABLED, LOADING) |
| `setLoading(boolean loading)` | Show/hide loading spinner |
| `setEnabled(boolean enabled)` | Enable/disable button |
| `setShowIcon(boolean show)` | Show/hide right arrow icon |
| `setButtonBackgroundColor(int color)` | Set custom background color |
| `setTextColor(int color)` | Set custom text color |
| `getState()` | Get current button state |
| `getText()` | Get button text |
| `setOnClickListener(OnClickListener l)` | Set click listener |

### States (ButtonState enum)

- `ButtonState.DEFAULT` - Normal, interactive state
- `ButtonState.PRESSED` - Temporary pressed state (automatic)
- `ButtonState.DISABLED` - Non-interactive state
- `ButtonState.LOADING` - Processing state with spinner

## 🎨 Accessibility

The button includes proper accessibility support:

- `accessibilityRole="button"` - Announced as button by screen readers
- `accessibilityState` - Indicates disabled/busy states
- Minimum touch target size: 52dp height
- High contrast colors (WCAG AA compliant)
- Clear, descriptive text

## 📊 Performance

- Lightweight: No external dependencies
- Smooth animations: Hardware-accelerated
- Efficient rendering: Custom drawing for icon
- Memory efficient: Reuses views

## 🐛 Troubleshooting

### Button not showing

Make sure you have the correct import:
```java
import com.example.restaurantapp.ui.components.PrimaryButton;
```

### Click not working

Check the button state:
```java
if (button.getState() != ButtonState.DEFAULT) {
    // Button is disabled or loading
}
```

### Custom colors not applying

Use the correct method:
```java
// ❌ Wrong
button.setBackgroundColor(color);

// ✅ Correct
button.setButtonBackgroundColor(color);
```

## 📚 Related Components

- **SecondaryButton** - For secondary actions
- **TextButton** - For tertiary actions
- **IconButton** - For icon-only actions

---

**Design System**: Minimal • Clean • Modern  
**Version**: 1.0  
**Last Updated**: April 2026

