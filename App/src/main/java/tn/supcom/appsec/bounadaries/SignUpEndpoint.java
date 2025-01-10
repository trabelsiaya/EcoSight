package tn.supcom.appsec.bounadaries;

import jakarta.ws.rs.core.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tn.supcom.appsec.Utilities.Argon2Utility;
import tn.supcom.appsec.entities.User;
import tn.supcom.appsec.repositories.UserRepository;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
@Path("user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SignUpEndpoint {

    @Inject
    private UserRepository repository;

    @POST 
    public Response save(User user) {
        try {
            repository.findByEmail(user.getemail()).orElseThrow(); 
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"User already exists!\"}")
                    .build();
        } catch (Exception e) {
            // Hash the password before saving it to the database
            String password = user.getPassword();
            String passwordHash = Argon2Utility.hash(password.toCharArray());

            // Create a new User entity with the hashed password
            User newUser = new User(user.getemail(), user.getUserName(), passwordHash, user.getPermissionLevel());

            // Save the user in the repository
            try {
                repository.save(newUser);
                System.out.println("User successfully saved: " + user);
            } catch (Exception er) {
                System.err.println("Error saving user: " + er.getMessage());
            }
            


            // Return a response indicating success with the created username
            return Response.status(Response.Status.CREATED)
                    .entity("{\"username created\": \"" + newUser.getUserName() + "\"}")
                    .build();
        }
    }
}

