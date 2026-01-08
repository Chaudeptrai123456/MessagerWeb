package com.example.Messenger.Service.Implement;

import com.example.Messenger.Record.WarehouseRole;
import com.example.Messenger.Repository.UserWarehouseRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional()
public class WarehousePermissionService {

    private final UserWarehouseRoleRepository userWarehouseRoleRepository;

    public WarehousePermissionService(UserWarehouseRoleRepository userWarehouseRoleRepository) {
        this.userWarehouseRoleRepository = userWarehouseRoleRepository;
    }

    public void checkManagerPermission(
            String userId,
            String warehouseId
    ) throws AccessDeniedException {
        boolean allowed =
                userWarehouseRoleRepository
                        .existsByUserIdAndWarehouseIdAndRole(
                                userId,
                                warehouseId,
                                WarehouseRole.MANAGER
                        );

        if (!allowed) {
            throw new AccessDeniedException(
                    "Manager has no permission on this warehouse"
            );
        }
    }
}
