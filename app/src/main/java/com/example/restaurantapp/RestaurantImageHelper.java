package com.example.restaurantapp;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps every restaurant name to its pre-cropped drawable resource.
 * Images are individual tiles cropped from the unified spritesheet
 * (restaurant_spritesheet.png – 4 cols × 6 rows, 256×256 px each).
 */
public final class RestaurantImageHelper {

    // ── name  →  drawable resource-id ──────────────────────────────────
    private static final Map<String, Integer> IMAGE_MAP = new HashMap<>();

    static {
        // Row 0
        IMAGE_MAP.put("bbq nation",           R.drawable.restaurant_img_bbq_nation);
        IMAGE_MAP.put("sushi sun",             R.drawable.restaurant_img_sushi_sun);
        IMAGE_MAP.put("taco town",             R.drawable.restaurant_img_taco_town);
        IMAGE_MAP.put("steakhouse seven",      R.drawable.restaurant_img_steakhouse_seven);

        // Row 1
        IMAGE_MAP.put("pizza palace",          R.drawable.restaurant_img_pizza_palace);
        IMAGE_MAP.put("ramen republic",        R.drawable.restaurant_img_ramen_republic);
        IMAGE_MAP.put("noodle nook",           R.drawable.restaurant_img_noodle_nook);
        IMAGE_MAP.put("pasta paradise",        R.drawable.restaurant_img_pasta_paradise);

        // Row 2
        IMAGE_MAP.put("kebab house",           R.drawable.restaurant_img_kebab_house);
        IMAGE_MAP.put("le bistro paris",       R.drawable.restaurant_img_le_bistro_paris);
        IMAGE_MAP.put("gyros express",         R.drawable.restaurant_img_gyros_express);
        IMAGE_MAP.put("indian spice garden",   R.drawable.restaurant_img_indian_spice_garden);

        // Row 3
        IMAGE_MAP.put("falafel house",         R.drawable.restaurant_img_falafel_house);
        IMAGE_MAP.put("greek tavern",          R.drawable.restaurant_img_greek_tavern);
        IMAGE_MAP.put("burger barn",           R.drawable.restaurant_img_burger_barn);
        IMAGE_MAP.put("cookie corner",         R.drawable.restaurant_img_cookie_corner);

        // Row 4
        IMAGE_MAP.put("crepe cafe",            R.drawable.restaurant_img_crepe_cafe);
        IMAGE_MAP.put("dim sum dragon",        R.drawable.restaurant_img_dim_sum_dragon);
        IMAGE_MAP.put("the poke bowl bar",     R.drawable.restaurant_img_poke_bowl_bar);
        IMAGE_MAP.put("poke bowl bar",         R.drawable.restaurant_img_poke_bowl_bar);
        IMAGE_MAP.put("brunch club",           R.drawable.restaurant_img_brunch_club);

        // Row 5
        IMAGE_MAP.put("souvlaki square",       R.drawable.restaurant_img_souvlaki_square);
        IMAGE_MAP.put("thai palace",           R.drawable.restaurant_img_thai_palace);
        IMAGE_MAP.put("the sandwich bar",      R.drawable.restaurant_img_sandwich_bar);
        IMAGE_MAP.put("sandwich bar",          R.drawable.restaurant_img_sandwich_bar);
        IMAGE_MAP.put("the vegan garden",      R.drawable.restaurant_img_vegan_garden);
        IMAGE_MAP.put("vegan garden",          R.drawable.restaurant_img_vegan_garden);

        // Tex Mex Grill – reuse a suitable tile (gyros / grill food)
        IMAGE_MAP.put("tex mex grill",         R.drawable.restaurant_img_taco_town);
    }

    private RestaurantImageHelper() {}

    /**
     * Returns the drawable resource ID for the given restaurant name,
     * or {@link R.drawable#ic_restaurant} as a fallback.
     */
    public static int getImageRes(String storeName) {
        if (storeName == null) return R.drawable.ic_restaurant;
        Integer res = IMAGE_MAP.get(storeName.trim().toLowerCase(java.util.Locale.ROOT));
        return res != null ? res : R.drawable.ic_restaurant;
    }

    /** True if a specific photo exists for this restaurant. */
    public static boolean hasImage(String storeName) {
        if (storeName == null) return false;
        return IMAGE_MAP.containsKey(storeName.trim().toLowerCase(java.util.Locale.ROOT));
    }
}

