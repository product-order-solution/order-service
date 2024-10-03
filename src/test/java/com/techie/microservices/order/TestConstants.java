package com.techie.microservices.order;

public class TestConstants {

    public static final String REQUEST_BODY = """
        {
            "skuCode": "iPhone_15",
            "price": 1000,
            "quantity": 1
        }""";

    /**
     * The JWT header and payload are just Base64-encoded JSON. This header indicates no signature algorithm:
     * {
     *   "alg": "none"
     * }
     * This payload contains the claims you'll use in your test, such as email, given_name, and family_name:
     * {
     *   "email": "cd@gmail.com",
     *   "given_name": "Carlos",
     *   "family_name": "D",
     *   "sub": "1234567890",
     *   "preferred_username": "carlosd"
     * }
     * You can use an online Base64 encoder to encode these parts. ConcatenatE the Base64-encoded header and payload
     * with a period (.), and ignoring the signature part (since you don't need it for testing)
     */
    public static final String MOCK_JWT = "eyJhbGciOiJub25lIn0=" +
            ".eyJlbWFpbCI6ImNkQGdtYWlsLmNvbSIsImdpdmVuX25hbWUiOiJDYXJsb3MiLCJmYW1pbHlfbmFtZSI6IkQiLCJzdWIiOiIxMjM0NTY3ODkwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiY2FybG9zZCJ9" +
            ".xyz";

    public static final String EXPECTED_RESPONSE = "Order placed successfully";
}
