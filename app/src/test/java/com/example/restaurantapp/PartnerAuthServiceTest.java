package com.example.restaurantapp;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PartnerAuthServiceTest {

    @Test
    public void loginPartner_rejectsBlankFields() {
        PartnerAuthService service = new PartnerAuthService(new StubRestaurantRepository(), new FakeServerGateway());

        AppResult<Store> result = service.loginPartner("  ", " ");

        assertFalse(result.isSuccess());
        assertEquals("Please fill in all fields.", result.getMessage());
    }

    @Test
    public void loginPartner_returnsStore_whenLoginSucceeds() {
        StubRestaurantRepository repository = new StubRestaurantRepository();
        Store store = new Store("Pizza Palace", 1.0, 2.0, "Pizza", 5, 100, "", Collections.emptyList());
        repository.storeByNameResult = AppResult.success(store);

        PartnerAuthService service = new PartnerAuthService(
                repository,
                new FakeServerGateway().withPartnerLoginResponse("OK: Login successful")
        );

        AppResult<Store> result = service.loginPartner("Pizza Palace", "123456");

        assertTrue(result.isSuccess());
        assertEquals("Pizza Palace", result.getData().getStoreName());
    }

    @Test
    public void loginPartner_returnsProtocolError_whenServerRejectsLogin() {
        PartnerAuthService service = new PartnerAuthService(
                new StubRestaurantRepository(),
                new FakeServerGateway().withPartnerLoginResponse("ERROR: Invalid access code")
        );

        AppResult<Store> result = service.loginPartner("Pizza Palace", "000000");

        assertFalse(result.isSuccess());
        assertEquals("Invalid access code", result.getMessage());
    }

    @Test
    public void requestAccessCode_parsesCodeSentResponse() {
        PartnerAuthService service = new PartnerAuthService(
                new StubRestaurantRepository(),
                new FakeServerGateway().withAccessCodeResponse("CODE_SENT:op***@demo.com:123456:7")
        );

        AppResult<PartnerAccessCodeInfo> result = service.requestAccessCode("Pizza Palace");

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("Pizza Palace", result.getData().getStoreName());
        assertEquals("op***@demo.com", result.getData().getDeliveryDestination());
        assertEquals("123456", result.getData().getDemoCode());
        assertEquals(7, result.getData().getExpiresInMinutes());
    }

    @Test
    public void requestAccessCode_returnsError_whenResponseShapeIsInvalid() {
        PartnerAuthService service = new PartnerAuthService(
                new StubRestaurantRepository(),
                new FakeServerGateway().withAccessCodeResponse("CODE_SENT:broken")
        );

        AppResult<PartnerAccessCodeInfo> result = service.requestAccessCode("Pizza Palace");

        assertFalse(result.isSuccess());
        assertEquals("Invalid access code response from server.", result.getMessage());
    }
}