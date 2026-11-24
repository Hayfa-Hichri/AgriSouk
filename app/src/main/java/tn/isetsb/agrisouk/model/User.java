package tn.isetsb.agrisouk.model;

/**
 * Utilisateur de l'application. Le rôle détermine les écrans accessibles.
 */
public class User {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    private long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;

    public User(long id, String fullName, String email, String phone, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(role);
    }
}
