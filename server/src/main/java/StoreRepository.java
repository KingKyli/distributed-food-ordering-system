import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class StoreRepository {
    private final Path dataDirectory;
    private final String databaseUrl;

    StoreRepository(Path dataDirectory, Path databasePath) {
        this.dataDirectory = dataDirectory;
        this.databaseUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath();
    }

    List<StoreData> bootstrap(List<StoreData> seedStores) {
        try {
            Files.createDirectories(dataDirectory);
            initializeDatabase();
            List<StoreData> persistedStores = loadStores();
            if (persistedStores.isEmpty()) {
                saveStores(seedStores);
                return new ArrayList<>(seedStores);
            }
            return persistedStores;
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Failed to bootstrap persisted store state", e);
        }
    }

    List<StoreData> loadStores() throws SQLException {
        Map<String, StoreData> storesByName = new LinkedHashMap<>();
        try (Connection connection = DriverManager.getConnection(databaseUrl)) {
            try (PreparedStatement storeStatement = connection.prepareStatement(
                    "SELECT store_name, latitude, longitude, food_category, stars, no_of_votes, store_logo FROM stores ORDER BY store_name")) {
                ResultSet resultSet = storeStatement.executeQuery();
                while (resultSet.next()) {
                    StoreData store = new StoreData(
                            resultSet.getString("store_name"),
                            resultSet.getDouble("latitude"),
                            resultSet.getDouble("longitude"),
                            resultSet.getString("food_category"),
                            resultSet.getInt("stars"),
                            resultSet.getInt("no_of_votes"),
                            resultSet.getString("store_logo")
                    );
                    storesByName.put(store.storeName, store);
                }
            }

            try (PreparedStatement productStatement = connection.prepareStatement(
                    "SELECT store_name, product_name, product_type, available_amount, price FROM products ORDER BY store_name, id")) {
                ResultSet resultSet = productStatement.executeQuery();
                while (resultSet.next()) {
                    StoreData store = storesByName.get(resultSet.getString("store_name"));
                    if (store != null) {
                        store.products.add(new ProductData(
                                resultSet.getString("product_name"),
                                resultSet.getString("product_type"),
                                resultSet.getInt("available_amount"),
                                resultSet.getDouble("price")
                        ));
                    }
                }
            }
        }
        return new ArrayList<>(storesByName.values());
    }

    void saveStores(List<StoreData> stores) {
        try (Connection connection = DriverManager.getConnection(databaseUrl)) {
            connection.setAutoCommit(false);
            try (Statement clearStatement = connection.createStatement()) {
                clearStatement.executeUpdate("DELETE FROM products");
                clearStatement.executeUpdate("DELETE FROM stores");
            }

            try (PreparedStatement storeStatement = connection.prepareStatement(
                    "INSERT INTO stores(store_name, latitude, longitude, food_category, stars, no_of_votes, store_logo) VALUES (?, ?, ?, ?, ?, ?, ?)");
                 PreparedStatement productStatement = connection.prepareStatement(
                         "INSERT INTO products(store_name, product_name, product_type, available_amount, price) VALUES (?, ?, ?, ?, ?)") ) {
                for (StoreData store : stores) {
                    storeStatement.setString(1, store.storeName);
                    storeStatement.setDouble(2, store.latitude);
                    storeStatement.setDouble(3, store.longitude);
                    storeStatement.setString(4, store.foodCategory);
                    storeStatement.setInt(5, store.stars);
                    storeStatement.setInt(6, store.noOfVotes);
                    storeStatement.setString(7, store.storeLogo);
                    storeStatement.addBatch();

                    for (ProductData product : store.products) {
                        productStatement.setString(1, store.storeName);
                        productStatement.setString(2, product.productName);
                        productStatement.setString(3, product.productType);
                        productStatement.setInt(4, product.availableAmount);
                        productStatement.setDouble(5, product.price);
                        productStatement.addBatch();
                    }
                }
                storeStatement.executeBatch();
                productStatement.executeBatch();
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to persist stores to SQLite", e);
        }
    }

    private void initializeDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS stores (" +
                            "store_name TEXT PRIMARY KEY," +
                            "latitude REAL NOT NULL," +
                            "longitude REAL NOT NULL," +
                            "food_category TEXT NOT NULL," +
                            "stars INTEGER NOT NULL," +
                            "no_of_votes INTEGER NOT NULL," +
                            "store_logo TEXT NOT NULL)"
            );
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS products (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "store_name TEXT NOT NULL," +
                            "product_name TEXT NOT NULL," +
                            "product_type TEXT NOT NULL," +
                            "available_amount INTEGER NOT NULL," +
                            "price REAL NOT NULL," +
                            "FOREIGN KEY(store_name) REFERENCES stores(store_name) ON DELETE CASCADE)"
            );
        }
    }
}