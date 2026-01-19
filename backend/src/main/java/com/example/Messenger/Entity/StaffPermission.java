package com.example.Messenger.Entity;

import com.example.Messenger.Record.Type.Permission;
import jakarta.persistence.*;

@Entity
@Table(name = "staff_permission")
public class StaffPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_warehouse_role_id")
    private UserWarehouseRole userWarehouseRole;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public UserWarehouseRole getUserWarehouseRole() {
        return userWarehouseRole;
    }

    public void setUserWarehouseRole(UserWarehouseRole userWarehouseRole) {
        this.userWarehouseRole = userWarehouseRole;
    }
}
