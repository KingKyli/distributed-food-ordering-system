# 🔗 Integration Guide - Primary Button

Οδηγός για να προσθέσεις το PrimaryButton στα existing Activities του Foodie Express app.

---

## 📱 1. BasketActivity - Checkout Button

### Τι να αλλάξεις:

**Πριν (Old XML):**
```xml
<Button
    android:id="@+id/checkoutButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Checkout" />
```

**Μετά (New Component):**
```xml
<com.example.restaurantapp.ui.components.PrimaryButton
    android:id="@+id/checkoutButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

**Java Code:**
```java
// In BasketActivity.java

// Add import
import com.example.restaurantapp.ui.components.PrimaryButton;

// In onCreate()
PrimaryButton checkoutButton = findViewById(R.id.checkoutButton);
checkoutButton.setText("CHECKOUT");
checkoutButton.setOnClickListener(v -> handleCheckout());

// Update button state based on basket
private void updateCheckoutButton() {
    boolean hasItems = basket != null && !basket.getItems().isEmpty();
    checkoutButton.setEnabled(hasItems);
}

// In checkout handler
private void handleCheckout() {
    checkoutButton.setLoading(true);
    
    orderService.placeOrder(basket, new OrderCallback() {
        @Override
        public void onSuccess(OrderRecord order) {
            runOnUiThread(() -> {
                checkoutButton.setLoading(false);
                Toast.makeText(BasketActivity.this, 
                    "Order placed successfully!", 
                    Toast.LENGTH_SHORT).show();
                basket.clear();
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
```

---

## 🍕 2. RestaurantDetailsActivity - Add to Basket Button

### XML Layout (activity_restaurant_details.xml):
```xml
<com.example.restaurantapp.ui.components.PrimaryButton
    android:id="@+id/addToBasketButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    android:layout_gravity="bottom" />
```

### Java Code:
```java
// In RestaurantDetailsActivity.java

import com.example.restaurantapp.ui.components.PrimaryButton;

private PrimaryButton addToBasketButton;
private Product selectedProduct;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_restaurant_details);
    
    addToBasketButton = findViewById(R.id.addToBasketButton);
    addToBasketButton.setText("ADD TO BASKET");
    addToBasketButton.setOnClickListener(v -> addToBasket());
    
    updateAddToBasketButton();
}

private void updateAddToBasketButton() {
    // Disable if no product selected
    boolean canAdd = selectedProduct != null;
    addToBasketButton.setEnabled(canAdd);
}

private void addToBasket() {
    if (selectedProduct == null) {
        Toast.makeText(this, "Please select a product", 
            Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Add to basket
    Basket basket = Basket.getInstance();
    basket.addItem(new BasketItem(selectedProduct, 1));
    
    // Visual feedback
    Toast.makeText(this, 
        selectedProduct.getName() + " added to basket", 
        Toast.LENGTH_SHORT).show();
    
    // Optional: Navigate to basket
    // Intent intent = new Intent(this, BasketActivity.class);
    // startActivity(intent);
}
```

---

## 📜 3. OrderHistoryActivity - Reorder Button

### Σε RecyclerView Item (order_history_item.xml):
```xml
<com.example.restaurantapp.ui.components.PrimaryButton
    android:id="@+id/reorderButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="12dp" />
```

### In OrderHistoryAdapter.java:
```java
import com.example.restaurantapp.ui.components.PrimaryButton;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // ... other views
        PrimaryButton reorderButton;
        
        public ViewHolder(View view) {
            super(view);
            // ... other view bindings
            reorderButton = view.findViewById(R.id.reorderButton);
        }
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OrderRecord order = orders.get(position);
        
        // ... bind other data
        
        // Setup reorder button
        holder.reorderButton.setText("REORDER");
        holder.reorderButton.setOnClickListener(v -> {
            holder.reorderButton.setLoading(true);
            
            orderService.reorder(order.getOrderId(), new OrderCallback() {
                @Override
                public void onSuccess(OrderRecord newOrder) {
                    holder.reorderButton.setLoading(false);
                    Toast.makeText(context, 
                        "Order placed successfully!", 
                        Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onError(Exception e) {
                    holder.reorderButton.setLoading(false);
                    Toast.makeText(context, 
                        "Failed to reorder: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
```

---

## 🎯 4. ManagerConsoleActivity - Place Order Button

### XML:
```xml
<com.example.restaurantapp.ui.components.PrimaryButton
    android:id="@+id/placeOrderButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp" />
```

### Java:
```java
import com.example.restaurantapp.ui.components.PrimaryButton;

private PrimaryButton placeOrderButton;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_manager_console);
    
    placeOrderButton = findViewById(R.id.placeOrderButton);
    placeOrderButton.setText("PLACE ORDER");
    placeOrderButton.setOnClickListener(v -> placeOrder());
}

private void placeOrder() {
    placeOrderButton.setLoading(true);
    
    // ... order logic
    
    new Handler().postDelayed(() -> {
        placeOrderButton.setLoading(false);
        Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
    }, 1500);
}
```

---

## 🔐 5. PartnerLoginActivity - Login Button

### XML:
```xml
<com.example.restaurantapp.ui.components.PrimaryButton
    android:id="@+id/loginButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="24dp" />
```

### Java:
```java
import com.example.restaurantapp.ui.components.PrimaryButton;

private PrimaryButton loginButton;
private EditText accessCodeInput;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_partner_login);
    
    loginButton = findViewById(R.id.loginButton);
    loginButton.setText("LOGIN");
    loginButton.setOnClickListener(v -> handleLogin());
    
    // Disable button if access code is empty
    accessCodeInput.addTextChangedListener(new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            loginButton.setEnabled(!s.toString().trim().isEmpty());
        }
        // ... other methods
    });
}

private void handleLogin() {
    String accessCode = accessCodeInput.getText().toString().trim();
    
    if (accessCode.isEmpty()) {
        Toast.makeText(this, "Please enter access code", 
            Toast.LENGTH_SHORT).show();
        return;
    }
    
    loginButton.setLoading(true);
    
    partnerAuthService.login(accessCode, new AuthCallback() {
        @Override
        public void onSuccess(PartnerAccessCodeInfo info) {
            runOnUiThread(() -> {
                loginButton.setLoading(false);
                Toast.makeText(PartnerLoginActivity.this, 
                    "Login successful!", 
                    Toast.LENGTH_SHORT).show();
                // Navigate to manager console
                startActivity(new Intent(
                    PartnerLoginActivity.this, 
                    ManagerConsoleActivity.class
                ));
                finish();
            });
        }
        
        @Override
        public void onError(Exception e) {
            runOnUiThread(() -> {
                loginButton.setLoading(false);
                Toast.makeText(PartnerLoginActivity.this, 
                    "Login failed: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
        }
    });
}
```

---

## ⚙️ 6. SettingsActivity - Save Button

### XML:
```xml
<com.example.restaurantapp.ui.components.PrimaryButton
    android:id="@+id/saveButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp" />
```

### Java:
```java
import com.example.restaurantapp.ui.components.PrimaryButton;

private PrimaryButton saveButton;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    
    saveButton = findViewById(R.id.saveButton);
    saveButton.setText("SAVE CHANGES");
    saveButton.setOnClickListener(v -> saveSettings());
    
    // Initially disable until changes are made
    saveButton.setEnabled(false);
}

private void saveSettings() {
    saveButton.setLoading(true);
    
    // Save settings logic
    SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    // ... save settings
    editor.apply();
    
    new Handler().postDelayed(() -> {
        saveButton.setLoading(false);
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        saveButton.setEnabled(false); // Disable until next change
    }, 500);
}
```

---

## 🎨 Common Patterns

### Pattern 1: Form Validation
```java
// Disable button until form is valid
private void validateForm() {
    boolean isValid = /* validation logic */;
    submitButton.setEnabled(isValid);
}

// Call on text change
editText.addTextChangedListener(new TextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
        validateForm();
    }
    // ...
});
```

### Pattern 2: Async Operation with Error Handling
```java
private void performAsyncAction() {
    button.setLoading(true);
    
    asyncService.doSomething(new Callback() {
        @Override
        public void onSuccess(Result result) {
            runOnUiThread(() -> {
                button.setLoading(false);
                // Handle success
            });
        }
        
        @Override
        public void onError(Exception e) {
            runOnUiThread(() -> {
                button.setLoading(false);
                // Show error
                Toast.makeText(context, 
                    "Error: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
        }
    });
}
```

### Pattern 3: Conditional Button State
```java
private void updateButtonState() {
    boolean canProceed = /* condition */;
    
    if (canProceed) {
        button.setEnabled(true);
        button.setText("CONTINUE");
    } else {
        button.setEnabled(false);
        button.setText("COMPLETE FORM FIRST");
    }
}
```

---

## ✅ Migration Checklist

Για κάθε Activity που θέλεις να ενημερώσεις:

- [ ] Import το PrimaryButton class
- [ ] Άλλαξε το XML layout (Button → PrimaryButton)
- [ ] Update το findViewById call
- [ ] Set το text με setText()
- [ ] Implement loading state για async operations
- [ ] Add validation logic (αν χρειάζεται)
- [ ] Test όλες τις καταστάσεις (enabled, disabled, loading)
- [ ] Check accessibility με TalkBack

---

## 🚨 Common Mistakes to Avoid

❌ **DON'T:**
```java
// Setting background directly (breaks the component)
button.setBackgroundColor(Color.RED);

// Using android:text in XML (use setText() in Java)
<PrimaryButton android:text="Click me" />
```

✅ **DO:**
```java
// Use the proper method
button.setButtonBackgroundColor(Color.RED);

// Set text in Java
button.setText("CLICK ME");
```

---

## 📊 Testing

### Test Cases:
1. **Default State**: Button is clickable and shows text + icon
2. **Pressed State**: Visual feedback (darker color + scale)
3. **Disabled State**: Button is grayed out and not clickable
4. **Loading State**: Shows spinner, text hidden, not clickable
5. **State Transitions**: Loading → Default, Disabled → Enabled
6. **Long Text**: Button handles long text gracefully
7. **Rapid Clicks**: Button prevents double-submission

### Manual Test:
```java
// Run this in your Activity
private void testAllStates() {
    PrimaryButton test = findViewById(R.id.testButton);
    
    // Test 1: Default
    test.setText("DEFAULT");
    test.setState(ButtonState.DEFAULT);
    
    // Test 2: Loading (after 2s)
    new Handler().postDelayed(() -> {
        test.setText("LOADING");
        test.setLoading(true);
    }, 2000);
    
    // Test 3: Disabled (after 4s)
    new Handler().postDelayed(() -> {
        test.setLoading(false);
        test.setText("DISABLED");
        test.setEnabled(false);
    }, 4000);
    
    // Test 4: Back to default (after 6s)
    new Handler().postDelayed(() -> {
        test.setText("ENABLED");
        test.setEnabled(true);
    }, 6000);
}
```

---

## 📚 Resources

- **Full Guide**: `PRIMARY_BUTTON_GUIDE.md`
- **Visual Diagram**: `../docs/PRIMARY_BUTTON_VISUAL_GUIDE.txt`
- **Examples**: `PrimaryButtonExampleActivity.java`
- **Source Code**: `PrimaryButton.java`

---

## 🆘 Need Help?

Αν έχεις πρόβλημα:
1. Check το `PRIMARY_BUTTON_GUIDE.md` για API reference
2. Run το `PrimaryButtonExampleActivity` για examples
3. Check τα compile errors με το IDE
4. Βεβαιώσου ότι έχεις το σωστό import

---

**Happy Coding! 🚀**

*Design System: Minimal • Clean • Modern*  
*Version: 1.0 • April 2026*

