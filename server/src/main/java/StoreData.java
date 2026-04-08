import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class StoreData {
    final String storeName;
    final double latitude;
    final double longitude;
    final String foodCategory;
    final int stars;
    final int noOfVotes;
    final String storeLogo;
    final List<ProductData> products = new ArrayList<>();

    StoreData(String storeName, double latitude, double longitude, String foodCategory, int stars, int noOfVotes, String storeLogo, ProductData... products) {
        this(storeName, latitude, longitude, foodCategory, stars, noOfVotes, storeLogo);
        this.products.addAll(Arrays.asList(products));
    }

    StoreData(String storeName, double latitude, double longitude, String foodCategory, int stars, int noOfVotes, String storeLogo) {
        this.storeName = Objects.requireNonNull(storeName, "storeName");
        this.latitude = latitude;
        this.longitude = longitude;
        this.foodCategory = Objects.requireNonNull(foodCategory, "foodCategory");
        this.stars = stars;
        this.noOfVotes = noOfVotes;
        this.storeLogo = Objects.requireNonNull(storeLogo, "storeLogo");
    }

    ProductData findProduct(String productName) {
        for (ProductData product : products) {
            if (product.productName.equalsIgnoreCase(productName)) {
                return product;
            }
        }
        return null;
    }

    String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append('{')
            .append("\"StoreName\":\"").append(JsonSupport.escapeJson(storeName)).append("\",")
                .append("\"Latitude\":").append(latitude).append(',')
                .append("\"Longitude\":").append(longitude).append(',')
            .append("\"FoodCategory\":\"").append(JsonSupport.escapeJson(foodCategory)).append("\",")
                .append("\"Stars\":").append(stars).append(',')
                .append("\"NoOfVotes\":").append(noOfVotes).append(',')
            .append("\"StoreLogo\":\"").append(JsonSupport.escapeJson(storeLogo)).append("\",")
                .append("\"Products\":[");
        for (int index = 0; index < products.size(); index++) {
            if (index > 0) {
                sb.append(',');
            }
            sb.append(products.get(index).toJson());
        }
        sb.append("]}");
        return sb.toString();
    }
}