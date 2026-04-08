# Secondary Button Component

Clean, outlined button for secondary actions in the Foodie Express app.

## 🎯 Purpose

**Secondary actions** that are important but NOT primary:
- "View Menu"
- "Open"
- "See More"
- "Details"
- "View All"

## 📐 Design Specs

| Property | Value |
|----------|-------|
| Style | Outlined (border, no fill) |
| Height | 44dp |
| Border Radius | 12dp |
| Border Width | 1.5dp (~2dp) |
| Border Color | #1E293B (navy) |
| Text Color | #1E293B (navy) |
| Text Size | 14sp |
| Font Weight | Medium (500) |
| Padding | 16dp horizontal |
| Background | Transparent |
| Pressed State | rgba(15, 23, 42, 0.05) |

## 🎨 States

- **DEFAULT** - Outlined, transparent background
- **PRESSED** - Light navy background (5% opacity)
- **DISABLED** - 50% opacity

## 📦 Usage

### In XML Layout

```xml
<com.example.restaurantapp.ui.components.SecondaryButton
    android:id="@+id/viewMenuButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

### In Java Code

#### Basic Usage

```java
import com.example.restaurantapp.ui.components.SecondaryButton;

SecondaryButton button = findViewById(R.id.viewMenuButton);
button.setText("View Menu");
button.setOnClickListener(v -> {
    // Handle click
    openMenu();
});
```

#### With Icon

```java
SecondaryButton button = findViewById(R.id.viewMenuButton);
button.setText("View Menu");
button.setShowIcon(true);
button.setIconColor(0xFFF97316); // Orange
button.setOnClickListener(v -> openMenu());
```

#### Programmatic Creation

```java
SecondaryButton button = new SecondaryButton(context);
button.setText("See More");
button.setOnClickListener(v -> showMore());
yourLayout.addView(button);
```

## 🎯 Where to Use

### ✅ DO Use SecondaryButton For:

**Inside Restaurant Cards:**
```java
// "Open" button next to restaurant info
SecondaryButton openButton = findViewById(R.id.openButton);
openButton.setText("Open");
openButton.setOnClickListener(v -> openRestaurant());
```

**In Lists:**
```java
// "View All" at the end of a list
SecondaryButton viewAllButton = findViewById(R.id.viewAllButton);
viewAllButton.setText("View All");
viewAllButton.setOnClickListener(v -> showAllRestaurants());
```

**Details Actions:**
```java
// "Details" button for more info
SecondaryButton detailsButton = findViewById(R.id.detailsButton);
detailsButton.setText("Details");
detailsButton.setOnClickListener(v -> showDetails());
```

**Navigation:**
```java
// "See More" for expanding content
SecondaryButton seeMoreButton = findViewById(R.id.seeMoreButton);
seeMoreButton.setText("See More");
seeMoreButton.setOnClickListener(v -> expandContent());
```

### ❌ DON'T Use SecondaryButton For:

- **Primary actions** (use PrimaryButton)
  - ❌ "Checkout"
  - ❌ "Place Order"
  - ❌ "Add to Cart"

- **Destructive actions**
  - ❌ "Delete"
  - ❌ "Remove"

- **Cancel/Back**
  - Use text button or icon button

## 🎨 Pairing with PrimaryButton

**Rule:** Always pair with PrimaryButton when both actions are needed

### Example: Restaurant Card

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="end"
    android:padding="16dp">
    
    <!-- Secondary action: View menu -->
    <com.example.restaurantapp.ui.components.SecondaryButton
        android:id="@+id/viewMenuButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="12dp" />
    
    <!-- Primary action: Order now -->
    <com.example.restaurantapp.ui.components.PrimaryButton
        android:id="@+id/orderButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
</LinearLayout>
```

```java
// In your Activity/Fragment
SecondaryButton viewMenuButton = findViewById(R.id.viewMenuButton);
viewMenuButton.setText("View Menu");
viewMenuButton.setShowIcon(true);
viewMenuButton.setOnClickListener(v -> openMenu());

PrimaryButton orderButton = findViewById(R.id.orderButton);
orderButton.setText("ORDER NOW");
orderButton.setOnClickListener(v -> placeOrder());
```

## 📱 Real World Examples

### 1. MainActivity - Restaurant Card

```java
public class MainActivity extends AppCompatActivity {
    
    private void setupRestaurantCard(Store store, View cardView) {
        SecondaryButton openButton = cardView.findViewById(R.id.openButton);
        openButton.setText("Open");
        openButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantDetailsActivity.class);
            intent.putExtra("store_id", store.getId());
            startActivity(intent);
        });
        
        PrimaryButton orderButton = cardView.findViewById(R.id.orderButton);
        orderButton.setText("ORDER NOW");
        orderButton.setOnClickListener(v -> {
            // Quick order flow
            startQuickOrder(store);
        });
    }
}
```

### 2. OrderHistoryActivity - Reorder Actions

```java
public class OrderHistoryActivity extends AppCompatActivity {
    
    private void setupOrderCard(OrderRecord order, View cardView) {
        SecondaryButton detailsButton = cardView.findViewById(R.id.detailsButton);
        detailsButton.setText("Details");
        detailsButton.setOnClickListener(v -> showOrderDetails(order));
        
        PrimaryButton reorderButton = cardView.findViewById(R.id.reorderButton);
        reorderButton.setText("REORDER");
        reorderButton.setOnClickListener(v -> reorder(order));
    }
}
```

### 3. RestaurantDetailsActivity - View Menu

```java
public class RestaurantDetailsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
        
        SecondaryButton viewFullMenuButton = findViewById(R.id.viewFullMenuButton);
        viewFullMenuButton.setText("View Full Menu");
        viewFullMenuButton.setShowIcon(true);
        viewFullMenuButton.setOnClickListener(v -> {
            // Show full menu in a dialog or new screen
            showFullMenu();
        });
    }
}
```

## 🔧 API Reference

### Methods

| Method | Description |
|--------|-------------|
| `setText(String text)` | Set button text |
| `getText()` | Get button text |
| `setShowIcon(boolean show)` | Show/hide icon |
| `setIconColor(int color)` | Set icon color |
| `setBorderColor(int color)` | Set custom border color |
| `setTextColor(int color)` | Set custom text color |
| `setEnabled(boolean enabled)` | Enable/disable button |
| `setOnClickListener(OnClickListener l)` | Set click listener |

### Default Values

```java
// Colors
BORDER_COLOR = 0xFF1E293B  // #1E293B navy
TEXT_COLOR = 0xFF1E293B    // #1E293B navy
ICON_COLOR = 0xFFF97316    // #F97316 orange

// Dimensions
HEIGHT = 44dp
BORDER_RADIUS = 12dp
BORDER_WIDTH = 2dp (1.5dp rounded)
PADDING = 16dp horizontal
TEXT_SIZE = 14sp
```

## 🎨 Customization

### Custom Colors

```java
SecondaryButton button = findViewById(R.id.customButton);
button.setText("Custom");
button.setBorderColor(0xFF10B981); // Green border
button.setTextColor(0xFF10B981);   // Green text
```

### Without Icon

```java
SecondaryButton button = findViewById(R.id.button);
button.setText("See More");
button.setShowIcon(false); // No icon (default)
```

### With Custom Icon Color

```java
SecondaryButton button = findViewById(R.id.button);
button.setText("View Menu");
button.setShowIcon(true);
button.setIconColor(0xFF3B82F6); // Blue icon
```

## ✨ Design Principles

### Outlined & Minimal
- Clean border, no fill
- Transparent background
- Navy outline (#1E293B)

### Focused Color
- Navy for professional look
- Orange icon for brand accent
- Subtle pressed state

### Scalable & Legible
- 14sp text (readable)
- 44dp height (tap-friendly)
- Good contrast

## 🆚 Primary vs Secondary

| Aspect | PrimaryButton | SecondaryButton |
|--------|---------------|-----------------|
| **Style** | Filled (solid orange) | Outlined (navy border) |
| **Color** | #F97316 (orange) | #1E293B (navy) |
| **Use** | Main action | Supporting action |
| **Examples** | "Checkout", "Order" | "View Menu", "Open" |
| **Hierarchy** | High priority | Medium priority |

## 🎯 Button Hierarchy

```
1. PrimaryButton (orange, filled)
   └─ "Checkout", "Place Order", "Add to Cart"

2. SecondaryButton (navy, outlined)  ← YOU ARE HERE
   └─ "View Menu", "Open", "See More"

3. TextButton (text only, no border)
   └─ "Cancel", "Skip", "Learn More"
```

## 📊 Accessibility

- **Minimum touch target**: 44dp height ✅
- **High contrast**: Navy on white (WCAG AA) ✅
- **Clear labels**: Descriptive text ✅
- **Focus indicators**: Press state visible ✅

## 🐛 Troubleshooting

### Button not showing

Make sure you import correctly:
```java
import com.example.restaurantapp.ui.components.SecondaryButton;
```

### Click not working

Check if button is enabled:
```java
button.setEnabled(true);
```

### Text too long

Use concise labels (1-2 words):
```java
// ✅ Good
button.setText("View Menu");

// ❌ Too long
button.setText("Click here to view the full restaurant menu");
```

## 📚 Related Components

- **PrimaryButton** - For main actions
- **TextButton** - For tertiary actions (future)
- **IconButton** - For icon-only actions (future)

---

**Design System**: Minimal • Clean • Modern  
**Version**: 1.0  
**Last Updated**: April 2026

