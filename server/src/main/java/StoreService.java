import java.util.Iterator;
import java.util.List;

final class StoreService {
    private final List<StoreData> stores;
    private final Runnable persistCallback;

    StoreService(List<StoreData> stores, Runnable persistCallback) {
        this.stores = stores;
        this.persistCallback = persistCallback;
    }

    MutationResult buyProduct(String storeName, String productName, int quantity) {
        synchronized (stores) {
            StoreData store = findStore(storeName);
            if (store == null) {
                return MutationResult.STORE_NOT_FOUND;
            }
            ProductData product = store.findProduct(productName);
            if (product == null) {
                return MutationResult.PRODUCT_NOT_FOUND;
            }
            if (quantity <= 0) {
                return MutationResult.INVALID_QUANTITY;
            }
            if (product.availableAmount < quantity) {
                return MutationResult.INSUFFICIENT_INVENTORY;
            }

            product.availableAmount -= quantity;
            persistCallback.run();
            return MutationResult.SUCCESS;
        }
    }

    MutationResult addStore(StoreData store) {
        synchronized (stores) {
            if (findStore(store.storeName) != null) {
                return MutationResult.STORE_ALREADY_EXISTS;
            }
            stores.add(store);
            persistCallback.run();
            return MutationResult.SUCCESS;
        }
    }

    MutationResult removeStore(String storeName) {
        synchronized (stores) {
            Iterator<StoreData> iterator = stores.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().storeName.equalsIgnoreCase(storeName)) {
                    iterator.remove();
                    persistCallback.run();
                    return MutationResult.SUCCESS;
                }
            }
            return MutationResult.STORE_NOT_FOUND;
        }
    }

    MutationResult addProduct(String storeName, ProductData product) {
        synchronized (stores) {
            StoreData store = findStore(storeName);
            if (store == null) {
                return MutationResult.STORE_NOT_FOUND;
            }
            if (store.findProduct(product.productName) != null) {
                return MutationResult.PRODUCT_ALREADY_EXISTS;
            }
            store.products.add(product);
            persistCallback.run();
            return MutationResult.SUCCESS;
        }
    }

    MutationResult removeProduct(String storeName, String productName) {
        synchronized (stores) {
            StoreData store = findStore(storeName);
            if (store == null) {
                return MutationResult.STORE_NOT_FOUND;
            }
            Iterator<ProductData> iterator = store.products.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().productName.equalsIgnoreCase(productName)) {
                    iterator.remove();
                    persistCallback.run();
                    return MutationResult.SUCCESS;
                }
            }
            return MutationResult.PRODUCT_NOT_FOUND;
        }
    }

    MutationResult updateProduct(String storeName, String productName, double newPrice, int newAmount) {
        synchronized (stores) {
            StoreData store = findStore(storeName);
            if (store == null) {
                return MutationResult.STORE_NOT_FOUND;
            }
            ProductData product = store.findProduct(productName);
            if (product == null) {
                return MutationResult.PRODUCT_NOT_FOUND;
            }
            product.price = newPrice;
            product.availableAmount = newAmount;
            persistCallback.run();
            return MutationResult.SUCCESS;
        }
    }

    String buildStoresJson() {
        StringBuilder sb = new StringBuilder("[");
        synchronized (stores) {
            for (int index = 0; index < stores.size(); index++) {
                if (index > 0) {
                    sb.append(',');
                }
                sb.append(stores.get(index).toJson());
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private StoreData findStore(String storeName) {
        for (StoreData store : stores) {
            if (store.storeName.equalsIgnoreCase(storeName)) {
                return store;
            }
        }
        return null;
    }

    enum MutationResult {
        SUCCESS,
        STORE_NOT_FOUND,
        STORE_ALREADY_EXISTS,
        PRODUCT_NOT_FOUND,
        PRODUCT_ALREADY_EXISTS,
        INVALID_QUANTITY,
        INSUFFICIENT_INVENTORY
    }
}