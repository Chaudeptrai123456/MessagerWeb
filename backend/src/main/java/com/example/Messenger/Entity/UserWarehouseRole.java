package com.example.Messenger.Entity;

import com.example.Messenger.Record.WarehouseRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "user_warehouse_roles"
//        uniqueConstraints = @UniqueConstraint(
//                columnNames = {"user_id", "warehouse_id"}
//        )
)
public class UserWarehouseRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* USER */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* WAREHOUSE */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    /* ROLE TRONG WAREHOUSE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarehouseRole role;

    /* MANAGER LIMIT */
    private Integer maxStaff;
    private Integer maxWarehouses;

    /* STAFF PERMISSIONS */
    @OneToMany(mappedBy = "userWarehouseRole", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StaffPermission> permissions = new HashSet<>();

    private LocalDateTime assignedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public WarehouseRole getRole() {
        return role;
    }

    public void setRole(WarehouseRole role) {
        this.role = role;
    }

    public Integer getMaxStaff() {
        return maxStaff;
    }

    public void setMaxStaff(Integer maxStaff) {
        this.maxStaff = maxStaff;
    }

    public Integer getMaxWarehouses() {
        return maxWarehouses;
    }

    public void setMaxWarehouses(Integer maxWarehouses) {
        this.maxWarehouses = maxWarehouses;
    }

    public Set<StaffPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<StaffPermission> permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
