package com.example.Messenger.Entity;

import com.example.Messenger.Record.Type.Permission;
import com.example.Messenger.Record.Type.WarehouseRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name = "users")
@Entity
public class User {
    @Id
    private String id;
    @Column(name="username")
    private String username;
    private String password;
    @Column(unique = true,nullable = false)
    private String email;
    private String avatar;
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_authorities",
            joinColumns = @JoinColumn(name = "userid"),
            inverseJoinColumns = @JoinColumn(name = "authorityid")
    )
    private List<Authority> authorities;
//    @OneToMany(
//            mappedBy = "userWarehouseRole", // 👈
//            Hibernate tìm field này
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private Set<StaffPermission> permissions = new HashSet<>();
    /* ====== ORDER ====== */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Order> orders = new HashSet<>();

    /* ====== WAREHOUSE RELATION ====== */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserWarehouseRole> warehouseRoles = new HashSet<>();

//    public boolean hasRole(String role) {
//        return authorities.stream()
//                .anyMatch(a -> a.getName().equals("ROLE_" + role));
//    }

    /* ---------- WAREHOUSE CHECK ---------- */
    public boolean belongsToWarehouse(String warehouseId) {
        return warehouseRoles.stream()
                .anyMatch(wr -> wr.getWarehouse().getId().equals(warehouseId));
    }

    public boolean isManager(String warehouseId) {
        return warehouseRoles.stream()
                .anyMatch(wr ->
                        wr.getWarehouse().getId().equals(warehouseId)
                                && wr.getRole() == WarehouseRole.MANAGER
                );
    }

    public boolean isStaff(String warehouseId) {
        return warehouseRoles.stream()
                .anyMatch(wr ->
                        wr.getWarehouse().getId().equals(warehouseId)
                                && wr.getRole() == WarehouseRole.STAFF
                );
    }

    /* ---------- PERMISSION ---------- */
    public boolean hasPermission(String warehouseId, Permission permission) {
        return warehouseRoles.stream()
                .filter(wr ->
                        wr.getWarehouse().getId().equals(warehouseId)
                                && wr.getRole() == WarehouseRole.STAFF
                )
                .flatMap(wr -> wr.getPermissions().stream())
                .anyMatch(p -> p.getPermission() == permission);
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public boolean hasRole(String role) {
        return authorities.stream()
                .anyMatch(a -> a.getName().equals(role));
    }
}
