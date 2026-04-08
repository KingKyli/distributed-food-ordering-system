import java.util.Objects;

final class ProductData {
    final String productName;
    final String productType;
    int availableAmount;
    double price;

    ProductData(String productName, String productType, int availableAmount, double price) {
        this.productName = Objects.requireNonNull(productName, "productName");
        this.productType = Objects.requireNonNull(productType, "productType");
        this.availableAmount = availableAmount;
        this.price = price;
    }

    String toJson() {
        return "{" +
                "\"ProductName\":\"" + JsonSupport.escapeJson(productName) + "\"," +
                "\"ProductType\":\"" + JsonSupport.escapeJson(productType) + "\"," +
                "\"AvailableAmount\":" + availableAmount + "," +
                "\"Price\":" + price +
                "}";
    }
}