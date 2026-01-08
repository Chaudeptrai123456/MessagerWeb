package com.example.Messenger.Controller.Staff;

import com.example.Messenger.Service.Implement.UserService;
import com.example.Messenger.Service.Implement.WarehouseEconomicService;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/staff")
public class ManageWarehouse {
    private final WarehouseEconomicService warehouseEconomicService;

    public ManageWarehouse(WarehouseEconomicService warehouseEconomicService) {
        this.warehouseEconomicService = warehouseEconomicService;
    }


}
