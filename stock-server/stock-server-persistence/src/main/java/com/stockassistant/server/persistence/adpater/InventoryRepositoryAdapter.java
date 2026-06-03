package com.stockassistant.server.persistence.adpater;

import com.stockassistant.server.domain.feature.inventory.port.out.InventoryRepositoryPort;
import com.stockassistant.server.domain.model.InventoryItem;
import com.stockassistant.server.domain.model.excpetion.ObjectNotFoundException;
import com.stockassistant.server.domain.model.excpetion.OperationFailedException;
import com.stockassistant.server.persistence.entity.InventoryItemEntity;
import com.stockassistant.server.persistence.mapper.InventoryEntityMapper;
import com.stockassistant.server.persistence.repository.InventoryRepository;
import com.stockassistant.server.persistence.specification.InventorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {
    private final InventoryRepository inventoryRepository;
    private final InventoryEntityMapper inventoryEntityMapper;

    @Override
    public Page<InventoryItem> findAll(Pageable pageable, UUID warehouseUUID, UUID productUUID) {
        Specification<InventoryItemEntity> specification = Specification.where(null);

        if (Objects.nonNull(warehouseUUID)) {
            specification = specification.and(InventorySpecification.hasWarehouseUUID(warehouseUUID));
        }
        if (Objects.nonNull(productUUID)) {
            specification = specification.and(InventorySpecification.hasProductUUID(productUUID));
        }

        return inventoryRepository.findAll(
                        specification,
                        pageable)
                .map(inventoryEntityMapper::toInventoryItem);

    }

    @Override
    public InventoryItem save(InventoryItem item) {
        return inventoryEntityMapper.toInventoryItem(inventoryRepository.save(inventoryEntityMapper.toInventoryItemEntity(item)));
    }

    @Override
    @Transactional
    public InventoryItem update(UUID warehouseId, UUID productId, int quantity) {
        InventoryItemEntity entity = inventoryRepository
                .findByWarehouseAndProduct(warehouseId, productId)
                .orElseThrow(ObjectNotFoundException::new);

        entity.setQuantity(quantity);
        entity.setLastStockUpdate(LocalDateTime.now());

        try {
            InventoryItemEntity saved = inventoryRepository.save(entity);
            return inventoryEntityMapper.toInventoryItem(saved);
        } catch (OptimisticLockingFailureException e) {
            throw new OperationFailedException(
                    "库存更新失败，该记录已被其他操作修改，请重试");
        }
    }

    @Override
    public List<InventoryItem> findAllBelowThreshold(int threshold) {
        return inventoryRepository.findItemsBelowThreshold(threshold)
                .stream()
                .map(inventoryEntityMapper::toInventoryItem)
                .toList();
    }

}
