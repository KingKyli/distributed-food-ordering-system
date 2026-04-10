import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

final class PartnerAccessManager {
    private static final int ACCESS_CODE_EXPIRY_MINUTES = 5;
    private static final long ACCESS_CODE_EXPIRY_MILLIS = ACCESS_CODE_EXPIRY_MINUTES * 60_000L;

    private final Map<String, String> partnerContacts;
    private final Map<String, String> activeCodes = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Long> activeCodeExpiry = Collections.synchronizedMap(new HashMap<>());
    private final Random random = new Random();

    PartnerAccessManager(Map<String, String> partnerContacts) {
        this.partnerContacts = Map.copyOf(partnerContacts);
    }

    synchronized AccessCodeIssueResult requestAccessCode(String storeName) {
        String trimmedStoreName = storeName.trim();
        String deliveryDestination = partnerContacts.get(trimmedStoreName);
        if (deliveryDestination == null) {
            return AccessCodeIssueResult.storeNotFound();
        }

        String existingCode = activeCodes.get(trimmedStoreName);
        Long existingExpiry = activeCodeExpiry.get(trimmedStoreName);
        long now = System.currentTimeMillis();
        if (existingCode != null && existingExpiry != null) {
            if (now <= existingExpiry) {
                return AccessCodeIssueResult.alreadySent(
                        maskEmail(deliveryDestination),
                        existingCode,
                        remainingMinutes(existingExpiry, now)
                );
            }
            activeCodes.remove(trimmedStoreName);
            activeCodeExpiry.remove(trimmedStoreName);
        }

        String accessCode = generateSixDigitCode();
        activeCodes.put(trimmedStoreName, accessCode);
        activeCodeExpiry.put(trimmedStoreName, now + ACCESS_CODE_EXPIRY_MILLIS);
        return AccessCodeIssueResult.success(maskEmail(deliveryDestination), accessCode, ACCESS_CODE_EXPIRY_MINUTES);
    }

    synchronized LoginValidationResult validateLogin(String storeName, String accessCode) {
        String expectedCode = activeCodes.get(storeName);
        Long expiresAt = activeCodeExpiry.get(storeName);
        if (!partnerContacts.containsKey(storeName)) {
            return LoginValidationResult.STORE_NOT_FOUND;
        }
        if (expectedCode == null || expiresAt == null) {
            return LoginValidationResult.NO_ACTIVE_CODE;
        }
        if (System.currentTimeMillis() > expiresAt) {
            activeCodes.remove(storeName);
            activeCodeExpiry.remove(storeName);
            return LoginValidationResult.EXPIRED_CODE;
        }
        if (!expectedCode.equals(accessCode)) {
            return LoginValidationResult.INVALID_CODE;
        }

        activeCodes.remove(storeName);
        activeCodeExpiry.remove(storeName);
        return LoginValidationResult.SUCCESS;
    }

    String getMaskedDestination(String storeName) {
        String contact = partnerContacts.get(storeName.trim());
        return contact == null ? null : maskEmail(contact);
    }

    private String generateSixDigitCode() {
        int value = 100000 + random.nextInt(900000);
        return String.valueOf(value);
    }

    private int remainingMinutes(long expiresAt, long now) {
        long remainingMillis = Math.max(0L, expiresAt - now);
        long roundedUp = (remainingMillis + 60_000L - 1L) / 60_000L;
        return (int) Math.max(1L, roundedUp);
    }

    static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.substring(0, 2) + "***" + domain;
    }

    enum LoginValidationResult {
        SUCCESS,
        STORE_NOT_FOUND,
        NO_ACTIVE_CODE,
        EXPIRED_CODE,
        INVALID_CODE
    }

    static final class AccessCodeIssueResult {
        final boolean success;
        final boolean alreadyActive;
        final String maskedDestination;
        final String accessCode;
        final int expiresInMinutes;

        private AccessCodeIssueResult(boolean success, boolean alreadyActive, String maskedDestination, String accessCode, int expiresInMinutes) {
            this.success = success;
            this.alreadyActive = alreadyActive;
            this.maskedDestination = maskedDestination;
            this.accessCode = accessCode;
            this.expiresInMinutes = expiresInMinutes;
        }

        static AccessCodeIssueResult success(String maskedDestination, String accessCode, int expiresInMinutes) {
            return new AccessCodeIssueResult(true, false, maskedDestination, accessCode, expiresInMinutes);
        }

        static AccessCodeIssueResult alreadySent(String maskedDestination, String accessCode, int expiresInMinutes) {
            return new AccessCodeIssueResult(false, true, maskedDestination, accessCode, expiresInMinutes);
        }

        static AccessCodeIssueResult storeNotFound() {
            return new AccessCodeIssueResult(false, false, null, null, 0);
        }
    }
}