package com.example.restaurantapp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProtocolUtilsTest {

    @Test
    public void isOkResponse_acceptsOkAndSuccessPrefixes_caseInsensitive() {
        assertTrue(ProtocolUtils.isOkResponse("OK: done"));
        assertTrue(ProtocolUtils.isOkResponse(" success: created "));
    }

    @Test
    public void isOkResponse_rejectsNullAndErrorResponses() {
        assertFalse(ProtocolUtils.isOkResponse(null));
        assertFalse(ProtocolUtils.isOkResponse("ERROR: failed"));
    }

    @Test
    public void extractErrorMessage_returnsSpecificErrorMessage_whenErrorPrefixExists() {
        assertEquals("Invalid access code", ProtocolUtils.extractErrorMessage("ERROR: Invalid access code", "Fallback"));
    }

    @Test
    public void extractErrorMessage_returnsFallback_whenResponseIsBlank() {
        assertEquals("Fallback", ProtocolUtils.extractErrorMessage("   ", "Fallback"));
    }

    @Test
    public void extractErrorMessage_returnsTrimmedResponse_whenNoErrorPrefixExists() {
        assertEquals("Server said no", ProtocolUtils.extractErrorMessage("  Server said no  ", "Fallback"));
    }
}