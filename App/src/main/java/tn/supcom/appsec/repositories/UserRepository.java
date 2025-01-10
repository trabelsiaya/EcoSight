package tn.supcom.appsec.repositories;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

import java.util.Optional;
import java.util.stream.Stream;
import tn.supcom.appsec.entities.User;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    // Find all users as a stream
    Stream<User> findAll();

    // Find users by their userName 
    Stream<User> findByUserNameIn(String userName);

    // Find a user by their email
    Optional<User> findByEmail(String email);
}

