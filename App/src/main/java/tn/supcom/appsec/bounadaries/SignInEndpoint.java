package tn.supcom.appsec.bounadaries;

import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Base64;
import tn.supcom.appsec.controllers.UserManager;
import tn.supcom.appsec.Utilities.Identity;
import org.json.JSONObject;
import jakarta.ejb.EJBException;
import java.net.URI;


@Path("/")
@RequestScoped
public class SignInEndpoint {
    private static final String COOKIE_NAME = "xssCookie";
    private static final String AUTHENTICATION_SCHEME_PREFIX = "Bearer ";

    @Inject
    private UserManager identityController;
    @Context
    private UriInfo uriInfo;
    @Context
    private HttpHeaders headers;

    // Utility method to get cookie value
    private String getCookieValue(String cookieName) {
        Cookie cookie = headers.getCookies().get(cookieName);
        return (cookie != null) ? cookie.getValue() : null;
    }

    @POST
    @Path("/authorize")
    @Produces(MediaType.APPLICATION_JSON)
    public Response preSignIn(@HeaderParam("Pre-Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith(AUTHENTICATION_SCHEME_PREFIX)) {
            System.err.println("Authorization header is missing or does not start with the expected prefix.");
            URI signinUri = uriInfo.getBaseUriBuilder()
                    .path("signin.html")
                    .build();
            return Response.seeOther(signinUri).build();
        }

        try {
            // Log the received authorization header
            System.out.println("Authorization Header: " + authorization);

            // Extract and decode the Base64 part of the header
            String encodedPart = authorization.substring(AUTHENTICATION_SCHEME_PREFIX.length());
            System.out.println("Encoded Part: " + encodedPart);

            byte[] decodedBytes = Base64.getDecoder().decode(encodedPart);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            System.out.println("Decoded String: " + decoded);

            // Split the decoded string into credentials
            String[] credentials = decoded.split("#");

            // Log the credentials
            System.out.println("Credentials Length: " + credentials.length);
            System.out.println("Client ID: " + (credentials.length > 0 ? credentials[0] : "null"));
            System.out.println("Code Challenge: " + (credentials.length > 1 ? credentials[1] : "null"));

            if (credentials.length != 2) {
                System.err.println("Decoded credentials are invalid. Expected format: 'clientId#codeChallenge'.");
                URI signinUri = uriInfo.getBaseUriBuilder()
                        .path("signin.html")
                        .build();
                return Response.seeOther(signinUri).build();
            }

            String clientId = credentials[0];
            String codeChallenge = credentials[1];

            // Generate the cookie value
            String cookieValue = Base64.getEncoder().encodeToString((clientId + "#" + codeChallenge).getBytes(StandardCharsets.UTF_8));
            System.out.println("Generated Cookie Value: " + cookieValue);

            // Create the cookie
            NewCookie codeChallengeCookie = new NewCookie(
                    COOKIE_NAME,           // Cookie name
                    cookieValue,           // Value
                    "/",                   // Path (available across the app)
                    null,                  // Domain (default to server domain)
                    "Code Challenge Cookie", // Comment for the cookie
                    86400,                 // Cookie lifetime (1 day in seconds)
                    true,                  // Secure cookie (only sent over HTTPS)
                    true                   // HttpOnly (accessible only by the server)
            );

            System.out.println("Cookie successfully created: " + codeChallengeCookie);

            return Response.ok("{\"message\":\"Code challenge set in cookie.\"}")
                    .cookie(codeChallengeCookie)
                    .build();
        } catch (IllegalArgumentException e) {
            System.err.println("Error decoding the Pre-Authorization header: " + e.getMessage());
            URI signinUri = uriInfo.getBaseUriBuilder()
                    .path("signin.html")
                    .build();
            return Response.seeOther(signinUri).build();
        }
    }

    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signIn(String json) {
        StringBuilder debugLogs = new StringBuilder();
        
        try {
            // Parse and log the input JSON
            System.out.println("Received JSON: " + json);
            JSONObject obj = new JSONObject(json);
            String email = obj.optString("email", null);
            String password = obj.optString("password", null);


            // Log parsed email and password
            System.out.println("Parsed email: " + email);
            System.out.println("Parsed password: " + password);

            // Validate email and password
            if (email == null || email.isEmpty() || email.length() < 4 || email.length() > 30 || password == null || password.length() < 6) {
                debugLogs.append("Validation failed: Invalid credentials.\n");
                return Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity("{\"message\":\"Invalid credentials.\", \"debug_logs\":\"" + debugLogs.toString() + "\"}")
                        .build();
            }

            // Authenticate the user
            debugLogs.append("Attempting authentication...\n");
            Identity identity = identityController.authenticate(email, password);
            debugLogs.append("Authentication successful for email: ").append(email).append("\n");

            // Retrieve the cookie value
            String cookieValue = getCookieValue(COOKIE_NAME);

            if (cookieValue == null) {
                System.out.println("Cookie missing or invalid for email: " + email);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\":\"Cookie missing or invalid.\", \"debug_logs\":\"" + debugLogs.toString() + "\"}")
                        .build();
            }

            // Decode the cookie value (Base64 encoded)
            String decodedCookie = new String(Base64.getDecoder().decode(cookieValue), StandardCharsets.UTF_8);
            System.out.println("Decoded cookie: " + decodedCookie);

            String[] parts = decodedCookie.split("#");
            if (parts.length != 2) {
                System.out.println("Error: Invalid cookie format");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"message\":\"Invalid cookie format.\", \"debug_logs\":\"" + debugLogs.toString() + "\"}")
                        .build();
            }

            String clientId = parts[0];
            String codeChallenge = parts[1];
            System.out.println("Client ID: " + clientId);
            System.out.println("Code Challenge: " + codeChallenge);

            // Verify clientId and codeChallenge
            if (clientId == null || codeChallenge == null) {
                System.out.println("Client ID or Code Challenge missing for email: " + email);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"message\":\"Client ID or Code Challenge missing.\", \"debug_logs\":\"" + debugLogs.toString() + "\"}")
                        .build();
            }

            // Create authorization code
            String authorizationCode = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString((codeChallenge + ":" + clientId).getBytes(StandardCharsets.UTF_8));
            System.out.println("Generated authorization code for email: " + email);

            // Return authorization code
            return Response.ok("{\"authorization_code\":\"" + authorizationCode + "\"}")
                    .build();

        } catch (EJBException e) {
            // Log the full stack trace
            debugLogs.append("EJBException occurred: ").append(e.getMessage()).append("\n");
            e.printStackTrace();

            // Include exception message in response
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Authentication failed.\", \"debug_logs\":\"" + debugLogs.toString() + "\"}")
                    .build();
            } catch (Exception e) {
                // Log unexpected errors
                debugLogs.append("Unexpected error: ").append(e.getMessage()).append("\n");
                e.printStackTrace();

                // Include unexpected error details in response
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"message\":\"An unexpected error occurred.\", \"debug_logs\":\"" + debugLogs.toString() + "\"}")
                        .build();
            }
        }

    }

