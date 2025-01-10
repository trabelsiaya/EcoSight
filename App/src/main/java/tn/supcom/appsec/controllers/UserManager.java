package tn.supcom.appsec.controllers;

import jakarta.ejb.EJBException;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import tn.supcom.appsec.entities.User;
import tn.supcom.appsec.repositories.UserRepository;
import tn.supcom.appsec.Utilities.Argon2Utility;

@ApplicationScoped
public class UserManager {

    @Inject
    private UserRepository userRepository; // Repository to interact with the user database

    // Finds a user by email (email)
    public User findByUsername(String email) {
        return userRepository.findById(email).orElseThrow(() ->
                new EJBException("User with email: " + email + " not found.") // Return user or throw exception if not found
        );
    }

    // Authenticate method for users (sign in)
    public User authenticate(final String email, final String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new EJBException("Failed sign in with email: " + email + " [Unknown email]") // If email is not found
        );

        // Check if the password matches the stored password (hashed using Argon2)
        if (Argon2Utility.check(user.getPassword(), password.toCharArray())) {
            return user; // Authentication successful
        }

        // If the password is incorrect
        throw new EJBException("Failed sign in with email: " + email + " [Wrong password]");
    }
}

