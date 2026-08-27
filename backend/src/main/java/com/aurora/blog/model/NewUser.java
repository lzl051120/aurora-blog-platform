package com.aurora.blog.model;

public class NewUser {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String displayName;
    private String role;

    public NewUser(String username, String email, String passwordHash, String displayName, String role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public String getRole() { return role; }
}
