package tn.supcom.appsec.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import jakarta.json.bind.annotation.JsonbVisibility;
import tn.supcom.appsec.Utilities.Identity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@JsonbVisibility(FieldPropertyVisibilityStrategy.class)
public class User implements Serializable, Identity { // User entity for database

    @Id
    private String id; 

    @Column
    private String email;

    @Column
    private String userName;

    @Column
    private String password;

    @Column
    private Long permissionLevel;

    public User() {
        
        // Générer un ID unique lors de la création de l'objet
        this.id = UUID.randomUUID().toString();  // Générer un UUID unique sous forme de chaîne
    }

    public User(String email, String userName, String password, Long permissionLevel) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.userName = userName;
        this.password = password;
        this.permissionLevel = permissionLevel;
    }

    // Getters et setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getemail() {  // Corrected getter method name
        return email;
    }

    public String getUserName() {  // Corrected getter method name
        return userName;
    }

    public String getPassword() {  // Corrected getter method name
        return password;
    }

    public Long getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(Long permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }

    @Override
    public String getName() {
        return getemail();
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + email + '\'' +
                ", fullname=" + userName +
                '}';
    }
}
