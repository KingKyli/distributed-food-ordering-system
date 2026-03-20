package com.example.restaurantapp;

import android.content.Context;

/**
 * @deprecated This class was an early prototype that generated store passwords client-side
 * and persisted them in SharedPreferences — a design that is inherently insecure and
 * unsuitable for any real deployment.
 *
 * <p><strong>It is fully superseded by the server-driven access-code flow:</strong>
 * <ol>
 *   <li>Partner taps "Request access code" in {@link PartnerLoginActivity}.</li>
 *   <li>{@link PartnerAuthService#requestAccessCode(String)} sends
 *       {@code REQUEST_PARTNER_ACCESS_CODE:<storeName>} over the socket.</li>
 *   <li>The master server responds with a one-time {@code CODE_SENT:<dest>:<code>:<ttl>}
 *       message; the code is shown in the UI hint only for demo environments.</li>
 *   <li>The partner submits the code via {@code PARTNER_LOGIN:<storeName>:<code>},
 *       and the server validates it server-side.</li>
 * </ol>
 *
 * <p>No password is ever generated, stored, or visible on the client.
 * This class is retained as an empty stub so that any stale build references compile;
 * it must not be instantiated or called.
 */
@Deprecated
public final class PartnerLoginManager {
    private PartnerLoginManager() {
        throw new UnsupportedOperationException(
                "PartnerLoginManager is deprecated. Use PartnerAuthService instead.");
    }

    /** @deprecated Use {@link PartnerAuthService#requestAccessCode(String)}. */
    @Deprecated
    public static PartnerLoginManager getInstance() {
        throw new UnsupportedOperationException(
                "PartnerLoginManager is deprecated. Use PartnerAuthService instead.");
    }

    /** @deprecated No-op stub. Context initialisation is no longer required. */
    @Deprecated
    public void init(Context context) {
        // No-op: superseded by server-driven auth.
    }
}
