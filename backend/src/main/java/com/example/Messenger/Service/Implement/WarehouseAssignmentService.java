package com.example.Messenger.Service.Implement;

import com.example.Messenger.Entity.User;
import com.example.Messenger.Entity.UserWarehouseRole;
import com.example.Messenger.Entity.Warehouse;
import com.example.Messenger.Record.Response.UserWarehouseRoleResponse;
import com.example.Messenger.Record.Type.WarehouseRole;
import com.example.Messenger.Repository.UserRepository;
import com.example.Messenger.Repository.UserWarehouseRoleRepository;
import com.example.Messenger.Repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseAssignmentService {

    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserWarehouseRoleRepository userWarehouseRoleRepository;
    @Autowired
    public WarehouseAssignmentService(UserRepository userRepository, WarehouseRepository warehouseRepository, UserWarehouseRoleRepository userWarehouseRoleRepository) {
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.userWarehouseRoleRepository = userWarehouseRoleRepository;
    }
    public UserWarehouseRoleResponse assignManagerToWarehouse(
            String email,
            String warehouseId,
            Integer maxStaff,
            Integer maxWarehouses
    ) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isManager = user.getAuthorities().stream()
                .anyMatch(a -> a.getName().equals("ROLE_MANAGER"));

        if (!isManager) {
            throw new RuntimeException("User is not a MANAGER");
        }

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        if (userWarehouseRoleRepository.existsByUserAndWarehouse(user, warehouse)) {
            throw new RuntimeException("User already assigned to this warehouse");
        }

        UserWarehouseRole uwr = new UserWarehouseRole();
        uwr.setUser(user);
        uwr.setWarehouse(warehouse);
        uwr.setRole(WarehouseRole.MANAGER);
        uwr.setMaxStaff(maxStaff);
        uwr.setMaxWarehouses(maxWarehouses);

        UserWarehouseRole saved = userWarehouseRoleRepository.save(uwr);

        return new UserWarehouseRoleResponse(
                saved.getId(),
                user.getId(),
                user.getEmail(),
                warehouse.getId(),
                warehouse.getName(),
                saved.getRole(),
                saved.getMaxStaff(),
                saved.getMaxWarehouses()
        );
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }
    public Page<User> getUsersByRole(
            String roleName,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "registrationDate")
        );

        return userRepository.findAllByRole(roleName, pageable);
    }
}