package com.example.restaurantapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Store {
    private String storeName;
    private Double lat;
    private Double lon;
    private String foodCategory;
    private Integer stars;
    private Integer noOfVotes;
    private String logo;
    private double revenue;
    private transient List<Product> products;

    public Store() {
        this.products = new ArrayList<>();  // ✅ Always initialize an empty list
    }

    public Store(String storeName, double lat, double lon, String foodCategory, int stars, int noOfVotes, String logo, List<Product> products) {
        this.storeName = storeName;
        this.lat = lat;
        this.lon = lon;
        this.foodCategory = foodCategory;
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.logo = logo;
        this.products = (products != null) ? new ArrayList<>(products) : new ArrayList<>();
        this.revenue = 0.0;
    }

    // Getters and Setters
    public String getStoreName() { return storeName; }
    public void setStoreName(String name) { this.storeName = name; }
    public Double  getStoreLat() { return lat; }
    public void setStoreLat(Double  lat) { this.lat= lat; }
    public Double  getStoreLon() { return lon; }
    public void setStoreLon(Double  lon) { this.lon= lon; }
    public String getFoodCategory() { return foodCategory; }
    public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }
    public Integer getStoreStars() { return stars; }
    public void setStoreStars(Integer stars) { this.stars = stars; }
    public Integer getStorenoOfVotes() { return noOfVotes; }
    public void setStorenoOfVotes(Integer noOfVotes) { this.noOfVotes = noOfVotes; }
    public String getStoreLogo() { return logo; }
    public void setStoreLogo(String logo) { this.logo = logo; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
    //public void addProduct(Product product) { this.products.add(product); }
    public void removeProduct(Product product) { this.products.remove(product); }

    public Product findProductByName(String name) {
        for (Product p : products) {
            if (p.getProductName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    public double getRevenue() {
        return revenue;
    }

    public void addRevenue(double amount) {
        if (amount > 0) {
            this.revenue += amount;
        }
    }

    public synchronized void addProductThreadSafe(Product product) {
        products.add(product);
    }

    public synchronized boolean removeProduct(String productName) {
        Product toRemove = findProductByName(productName);
        if (toRemove == null) return false;
        return products.remove(toRemove);
    }



    @Override
    public String toString() {
        return "Store [Name=" + this.getStoreName() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equals(this.getStoreName(),store.getStoreName() );
    }

    public String getPriceCategory(){
        if (priceCategoryOverride != null) return priceCategoryOverride;
        String priceCategory;
        double totalPrice = 0;
        int count = 0;
        for (Product p : getProducts()) {
            totalPrice += p.getPrice();
            count++;
        }
        double avgPrice = (count == 0) ? 0 : totalPrice / count;

        if (avgPrice <= 5) {
            priceCategory = "$";
        } else if (avgPrice <= 15) {
            priceCategory = "$$";
        } else {
            priceCategory = "$$$";
        }
        return priceCategory;
    }
    public JSONObject toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("StoreName", storeName);
            obj.put("Latitude", lat);
            obj.put("Longitude", lon);
            obj.put("FoodCategory", foodCategory);
            obj.put("Stars", stars);
            obj.put("NoOfVotes", noOfVotes);
            obj.put("StoreLogo", logo);

            JSONArray productsArr = new JSONArray();
            for (Product p : products) {
                productsArr.put(p.toJson());
            }
            obj.put("Products", productsArr);

            return obj;
        } catch (org.json.JSONException e) {
            System.err.println("Error creating JSON: " + e.getMessage());
            return null;
        }
    }

    public static Store fromJson(JSONObject obj) throws JSONException {
        String storeName = obj.getString("StoreName");
        double latitude = obj.getDouble("Latitude");
        double longitude = obj.getDouble("Longitude");
        String foodCategory = obj.getString("FoodCategory");
        int stars = obj.getInt("Stars");
        int noOfVotes = obj.optInt("NoOfVotes", 0);
        String logo = obj.optString("StoreLogo", "");

        List<Product> productList = new ArrayList<>();
        JSONArray productsArr = obj.optJSONArray("Products");
        if (productsArr != null) {
            for (int i = 0; i < productsArr.length(); i++) {
                JSONObject pObj = productsArr.getJSONObject(i);
                productList.add(Product.fromJson(pObj));
            }
        }
        Store store = new Store(storeName, latitude, longitude, foodCategory, stars, noOfVotes, logo, productList);
        // If products are empty, try to set price category from JSON
        if (productList.isEmpty() && obj.has("PriceCategory")) {
            store.setPriceCategory(obj.getString("PriceCategory"));
        }
        return store;
    }


    private String priceCategoryOverride = null;
    public void setPriceCategory(String priceCategory) {
        this.priceCategoryOverride = priceCategory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getStoreName());
    }
}