package com.stockassistant.server.domain.feature.inventory;

import com.stockassistant.server.domain.feature.inventory.port.out.InventoryRepositoryPort;
import com.stockassistant.server.domain.model.InventoryItem;
import com.stockassistant.server.domain.model.Product;
import com.stockassistant.server.domain.model.Warehouse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service class for handling inventory-related business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAlertService {

    private final InventoryRepositoryPort inventoryRepositoryPort;

    /**
     * Default low stock threshold (configurable).
     */
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    /**
     * Checks all inventory items for low stock and returns those below the threshold.
     *
     * @param threshold the minimum quantity before an alert is triggered
     * @return list of inventory items that are below the threshold
     */
    public List<InventoryItem> checkLowStock(int threshold) {
        log.info("Checking inventory items below threshold: {}", threshold);
        List<InventoryItem> lowStockItems = inventoryRepositoryPort.findAllBelowThreshold(threshold);
        if (!lowStockItems.isEmpty()) {
            log.warn("Found {} items with low stock (threshold: {})", lowStockItems.size(), threshold);
            lowStockItems.forEach(item ->
                    log.warn("LOW STOCK ALERT - Product: {}, Warehouse: {}, Current: {}",
                            item.product().name(),
                            item.warehouse().name(),
                            item.quantity())
            );
        } else {
            log.info("All inventory items are above threshold: {}", threshold);
        }
        return lowStockItems;
    }

    /**
     * Checks with default threshold.
     */
    public List<InventoryItem> checkLowStock() {
        return checkLowStock(DEFAULT_LOW_STOCK_THRESHOLD);
    }

    /**
     * Checks if a specific product in a warehouse is low on stock.
     */
    public boolean isLowStock(InventoryItem item) {
        return item.quantity() < DEFAULT_LOW_STOCK_THRESHOLD;
    }
}
