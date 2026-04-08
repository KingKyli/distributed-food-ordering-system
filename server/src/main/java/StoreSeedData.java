import java.util.ArrayList;
import java.util.List;

final class StoreSeedData {
    private StoreSeedData() {
    }

    static List<StoreData> createSeedStores() {
        List<StoreData> stores = new ArrayList<>();

        stores.add(new StoreData("Sushi Sun", 37.9900, 23.7200, "Sushi", 5, 312, "sushi_logo",
                new ProductData("Salmon Roll", "Sushi", 25, 12.99),
                new ProductData("Tuna Sashimi", "Sushi", 20, 14.99),
                new ProductData("Dragon Roll", "Sushi", 15, 16.99),
                new ProductData("Miso Soup", "Soup", 40, 3.99)));

        stores.add(new StoreData("Pasta Paradise", 37.9820, 23.7150, "Italian", 5, 210, "pasta_logo",
                new ProductData("Spaghetti Carbonara", "Pasta", 40, 11.99),
                new ProductData("Lasagna", "Pasta", 30, 13.99),
                new ProductData("Fettuccine Alfredo", "Pasta", 35, 12.99),
                new ProductData("Tiramisu", "Dessert", 50, 5.99)));

        stores.add(new StoreData("Ramen Republic", 37.9910, 23.7350, "Japanese", 5, 178, "ramen_logo",
                new ProductData("Tonkotsu Ramen", "Ramen", 30, 13.50),
                new ProductData("Shoyu Ramen", "Ramen", 28, 12.50),
                new ProductData("Gyoza (6 pcs)", "Starter", 40, 6.99),
                new ProductData("Edamame", "Starter", 60, 3.50)));

        stores.add(new StoreData("Le Bistro Paris", 37.9760, 23.7180, "French", 5, 145, "bistro_logo",
                new ProductData("Croque Monsieur", "Sandwich", 20, 14.99),
                new ProductData("French Onion Soup", "Soup", 15, 11.99),
                new ProductData("Beef Bourguignon", "Main", 12, 22.99),
                new ProductData("Creme Brulee", "Dessert", 25, 9.99)));

        stores.add(new StoreData("Greek Tavern", 37.9830, 23.7050, "Greek", 5, 289, "tavern_logo",
                new ProductData("Moussaka", "Main", 35, 14.99),
                new ProductData("Lamb Chops", "Main", 20, 19.99),
                new ProductData("Spanakopita", "Starter", 50, 7.99),
                new ProductData("Baklava", "Dessert", 80, 4.99)));

        stores.add(new StoreData("Indian Spice Garden", 37.9870, 23.7420, "Indian", 5, 196, "indian_logo",
                new ProductData("Chicken Tikka Masala", "Curry", 30, 13.99),
                new ProductData("Lamb Biryani", "Rice", 25, 14.99),
                new ProductData("Garlic Naan", "Bread", 80, 2.99),
                new ProductData("Mango Lassi", "Drink", 60, 3.99)));

        stores.add(new StoreData("The Poke Bowl Bar", 37.9770, 23.7310, "Hawaiian", 5, 167, "poke_logo",
                new ProductData("Ahi Tuna Bowl", "Bowl", 25, 14.99),
                new ProductData("Salmon Poke Bowl", "Bowl", 30, 13.99),
                new ProductData("Veggie Poke Bowl", "Bowl", 35, 11.99),
                new ProductData("Edamame Side", "Side", 50, 3.99)));

        stores.add(new StoreData("Pizza Palace", 37.9838, 23.7275, "Pizza", 4, 120, "pizza_logo",
                new ProductData("Margherita", "Pizza", 50, 8.99),
                new ProductData("Pepperoni", "Pizza", 40, 10.99),
                new ProductData("Hawaiian", "Pizza", 30, 11.99),
                new ProductData("Garlic Bread", "Side", 70, 3.99)));

        stores.add(new StoreData("Burger Barn", 37.9750, 23.7350, "Burgers", 4, 85, "burger_logo",
                new ProductData("Classic Burger", "Burger", 60, 7.99),
                new ProductData("Cheese Burger", "Burger", 45, 8.99),
                new ProductData("Bacon Burger", "Burger", 35, 9.99),
                new ProductData("Fries", "Side", 90, 2.99)));

        stores.add(new StoreData("Gyros Express", 37.9860, 23.7300, "Greek", 4, 95, "gyros_logo",
                new ProductData("Pork Gyros", "Gyros", 70, 4.99),
                new ProductData("Chicken Souvlaki", "Souvlaki", 60, 5.99),
                new ProductData("Greek Salad", "Salad", 40, 6.99),
                new ProductData("Tzatziki", "Side", 100, 2.50)));

        stores.add(new StoreData("Souvlaki Square", 37.9895, 23.7260, "Greek Street Food", 4, 143, "souvlaki_logo",
                new ProductData("Pita Wrap", "Wrap", 90, 3.99),
                new ProductData("Kalamaki Pork", "Skewer", 70, 4.50),
                new ProductData("Kalamaki Chicken", "Skewer", 65, 4.50),
                new ProductData("Chips", "Side", 120, 2.00)));

        stores.add(new StoreData("Noodle Nook", 37.9805, 23.7220, "Asian Fusion", 4, 112, "noodle_logo",
                new ProductData("Pad Thai", "Noodles", 40, 10.99),
                new ProductData("Spring Rolls", "Starter", 60, 5.99),
                new ProductData("Fried Rice", "Rice", 50, 9.99),
                new ProductData("Wonton Soup", "Soup", 35, 6.99)));

        stores.add(new StoreData("BBQ Nation", 37.9745, 23.7390, "BBQ", 4, 98, "bbq_logo",
                new ProductData("BBQ Ribs Half", "BBQ", 20, 16.99),
                new ProductData("Pulled Pork Sandwich", "Sandwich", 40, 9.99),
                new ProductData("Corn on the Cob", "Side", 60, 3.50),
                new ProductData("Coleslaw", "Side", 80, 2.50)));

        stores.add(new StoreData("Thai Palace", 37.9850, 23.7470, "Thai", 4, 134, "thai_logo",
                new ProductData("Green Curry", "Curry", 35, 12.99),
                new ProductData("Tom Yum Soup", "Soup", 30, 9.99),
                new ProductData("Mango Sticky Rice", "Dessert", 40, 5.99),
                new ProductData("Jasmine Rice", "Rice", 90, 2.50)));

        stores.add(new StoreData("Dim Sum Dragon", 37.9920, 23.7155, "Chinese", 4, 156, "dimsum_logo",
                new ProductData("Har Gow (4 pcs)", "Dim Sum", 35, 7.99),
                new ProductData("Siu Mai (4 pcs)", "Dim Sum", 40, 7.50),
                new ProductData("Char Siu Bao", "Bun", 50, 5.99),
                new ProductData("Egg Tart", "Dessert", 70, 2.99)));

        stores.add(new StoreData("The Vegan Garden", 37.9780, 23.7195, "Vegan", 4, 88, "vegan_logo",
                new ProductData("Falafel Wrap", "Wrap", 45, 8.99),
                new ProductData("Quinoa Bowl", "Bowl", 35, 11.99),
                new ProductData("Avocado Toast", "Toast", 40, 7.99),
                new ProductData("Fruit Smoothie", "Drink", 60, 4.99)));

        stores.add(new StoreData("Steakhouse Seven", 37.9710, 23.7280, "Steakhouse", 4, 76, "steak_logo",
                new ProductData("Ribeye 250g", "Steak", 15, 26.99),
                new ProductData("Sirloin 200g", "Steak", 18, 22.99),
                new ProductData("Caesar Salad", "Salad", 30, 8.99),
                new ProductData("Mashed Potatoes", "Side", 60, 4.99)));

        stores.add(new StoreData("Brunch Club", 37.9930, 23.7340, "Brunch", 4, 201, "brunch_logo",
                new ProductData("Eggs Benedict", "Breakfast", 30, 11.99),
                new ProductData("Avocado Shakshuka", "Breakfast", 25, 12.99),
                new ProductData("Pancake Stack", "Breakfast", 50, 8.99),
                new ProductData("Fresh Orange Juice", "Drink", 80, 4.50)));

        stores.add(new StoreData("Crepe Cafe", 37.9800, 23.7440, "Crepes", 4, 119, "crepe_logo",
                new ProductData("Nutella Crepe", "Crepe", 60, 5.99),
                new ProductData("Ham & Cheese Crepe", "Crepe", 50, 6.99),
                new ProductData("Strawberry Crepe", "Crepe", 55, 6.50),
                new ProductData("Hot Chocolate", "Drink", 80, 3.50)));

        stores.add(new StoreData("Taco Town", 37.9780, 23.7400, "Mexican", 3, 65, "taco_logo",
                new ProductData("Beef Taco", "Taco", 100, 3.99),
                new ProductData("Chicken Burrito", "Burrito", 80, 6.99),
                new ProductData("Nachos", "Appetizer", 50, 5.99),
                new ProductData("Guacamole", "Side", 60, 2.99)));

        stores.add(new StoreData("Kebab House", 37.9715, 23.7320, "Middle Eastern", 3, 74, "kebab_logo",
                new ProductData("Doner Kebab", "Kebab", 80, 5.50),
                new ProductData("Shish Kebab", "Kebab", 60, 7.99),
                new ProductData("Hummus & Pita", "Starter", 70, 4.50),
                new ProductData("Baklava", "Dessert", 50, 2.99)));

        stores.add(new StoreData("Falafel House", 37.9940, 23.7380, "Mediterranean", 3, 91, "falafel_logo",
                new ProductData("Falafel Plate", "Main", 50, 6.99),
                new ProductData("Shawarma Wrap", "Wrap", 60, 5.99),
                new ProductData("Hummus", "Side", 80, 3.50),
                new ProductData("Lemonade", "Drink", 100, 2.50)));

        stores.add(new StoreData("Tex Mex Grill", 37.9730, 23.7450, "Mexican", 3, 52, "texmex_logo",
                new ProductData("Quesadilla", "Mexican", 45, 7.99),
                new ProductData("Beef Nachos", "Mexican", 40, 8.99),
                new ProductData("Chicken Fajitas", "Mexican", 30, 11.99),
                new ProductData("Salsa & Chips", "Starter", 60, 4.50)));

        stores.add(new StoreData("The Sandwich Bar", 37.9885, 23.7100, "Sandwiches", 3, 83, "sandwich_logo",
                new ProductData("Club Sandwich", "Sandwich", 50, 7.50),
                new ProductData("BLT", "Sandwich", 40, 5.99),
                new ProductData("Grilled Chicken", "Sandwich", 35, 6.99),
                new ProductData("Iced Coffee", "Drink", 100, 3.50)));

        stores.add(new StoreData("Cookie Corner", 37.9960, 23.7230, "Desserts", 3, 108, "cookie_logo",
                new ProductData("Choc Chip Cookie", "Cookie", 200, 1.99),
                new ProductData("Brownie", "Cake", 150, 2.99),
                new ProductData("Cheesecake Slice", "Cake", 80, 4.99),
                new ProductData("Milkshake", "Drink", 60, 5.99)));

        return stores;
    }
}