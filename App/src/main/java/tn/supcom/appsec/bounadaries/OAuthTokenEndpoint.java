package tn.supcom.appsec.bounadaries;

import java.nio.charset.StandardCharsets;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import tn.supcom.appsec.security.JwtManager;

import java.security.MessageDigest;
import java.util.Base64;

@Path("/oauth/token")
public class OAuthTokenEndpoint {

    @Inject
    private JwtManager jwtManager;

    @POST
    public Response generateToken(@QueryParam("authorization_code") String authorizationCode,
                                  @QueryParam("code_verifier") String codeVerifier) {
        if (authorizationCode == null || codeVerifier == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Missing required parameters: authorization_code, code_verifier.\"}")
                    .build();
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(authorizationCode), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");

            if (parts.length != 2) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\":\"Invalid authorization code format.\"}")
                        .build();
            }

            String originalCodeChallenge = parts[0];
            String clientId = parts[1];
            String recalculatedCodeChallenge = generateCodeChallenge(codeVerifier);

            if (!originalCodeChallenge.equals(recalculatedCodeChallenge)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\":\"Code verifier does not match.\"}")
                        .build();
            }

            String token = jwtManager.generateToken("1", "john.doe",
                    "resource:read,resource:write", new String[]{"Moderator", "Client"});

            return Response.ok(Json.createObjectBuilder()
                            .add("access_token", token)
                            .add("token_type", "Bearer")
                            .add("expires_in", 3600)
                            .build())
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\":\"Internal error occurred.\"}")
                    .build();
        }
    }

    private String generateCodeChallenge(String codeVerifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hashedBytes);
    }

}
