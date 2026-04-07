package com.example.restaurantapp;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RestaurantRepositoryTest {

    @Test
    public void searchStores_returnsParsedStores_whenGatewayReturnsValidJson() {
        RestaurantRepository repository = new RestaurantRepository(new FakeServerGateway()
                .withSearchResponse("[{\"StoreName\":\"Pizza Palace\",\"Latitude\":1.0,\"Longitude\":2.0,\"FoodCategory\":\"Pizza\",\"Stars\":5,\"NoOfVotes\":10,\"StoreLogo\":\"\",\"Products\":[]}]"));

        AppResult<List<Store>> result = repository.searchStores("", "", "", 0, "");

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("Pizza Palace", result.getData().get(0).getStoreName());
    }

    @Test
    public void searchStores_returnsServerErrorMessage_whenGatewayReturnsErrorResponse() {
        RestaurantRepository repository = new RestaurantRepository(new FakeServerGateway()
                .withSearchResponse("ERROR: Backend unavailable"));

        AppResult<List<Store>> result = repository.searchStores("", "", "", 0, "");

        assertFalse(result.isSuccess());
        assertEquals("Backend unavailable", result.getMessage());
    }

    @Test
    public void searchStores_returnsTransportError_whenGatewayFails() {
        RestaurantRepository repository = new RestaurantRepository(new FakeServerGateway()
                .withSearchResult(AppResult.error("Connection failed")));

        AppResult<List<Store>> result = repository.searchStores("", "", "", 0, "");

        assertFalse(result.isSuccess());
        assertEquals("Connection failed", result.getMessage());
    }
}