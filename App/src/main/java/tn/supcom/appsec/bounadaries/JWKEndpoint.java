package tn.supcom.appsec.bounadaries;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.security.KeyPair;
import java.util.Map;
import java.util.HashMap;

@Path("/jwk")
public class JWKEndpoint {

    // Assuming cachedKeyPairs is a map holding the key pair for each kid
    // Replace this with your actual implementation
    private static final Map<String, KeyPair> cachedKeyPairs = new HashMap<>();

    // This method constructs the JWK from the key pair
    private JsonObject getPublicKeyAsJWK(String kid) {
        KeyPair keyPair = cachedKeyPairs.get(kid);
        if (keyPair == null) {
            throw new EJBException("Invalid kid: " + kid);
        }

        // Extract and encode the public key
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPublic().getEncoded());

        // You might need to change the curve depending on the algorithm used
        String curve = "P-256";  // Example for EC (Elliptic Curve) keys

        // Build the JWK JSON object
        return Json.createObjectBuilder()
                .add("kty", "EC")  // Assuming EC (Elliptic Curve) keys
                .add("crv", curve)
                .add("kid", kid)
                .add("x", encoded)  // EC "x" coordinate
                .build();
    }

    // Endpoint to get the JWK for a specific kid
    @GET
    @Path("/{kid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJWK(@PathParam("kid") String kid) {
        try {
            // Fetch the public key in JWK format
            JsonObject jwk = getPublicKeyAsJWK(kid);
            return Response.ok(jwk).build();  // Return the JWK as a JSON response
        } catch (EJBException e) {
            // Handle invalid kid exception
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder().add("error", e.getMessage()).build())
                    .build();
        } catch (Exception e) {
            // General error handling
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Json.createObjectBuilder().add("error", "An unexpected error occurred").build())
                    .build();
        }
    }
}
