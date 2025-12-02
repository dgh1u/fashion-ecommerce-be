package com.kltn.service.impl;

import com.kltn.exception.DataNotFoundException;
import com.kltn.model.OrderItems;
import com.kltn.model.Orders;
import com.kltn.model.ProductInventory;
import com.kltn.repository.OrderItemRepository;
import com.kltn.repository.ProductInventoryRepository;
import com.kltn.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final ProductInventoryRepository productInventoryRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public void updateInventoryAfterPayment(Orders order) {
        try {
            log.info("Bắt đầu cập nhật inventory cho order ID: {}, OrderCode: {}",
                    order.getId(), order.getOrderCode());

            // Lấy tất cả OrderItems của Order này với Product và Size được fetch
            List<OrderItems> orderItems = orderItemRepository.findByOrderIdWithProductAndSize(order.getId());

            if (orderItems.isEmpty()) {
                log.warn("Không tìm thấy OrderItems cho Order ID: {}", order.getId());
                return;
            }

            for (OrderItems orderItem : orderItems) {
                try {
                    Long productId = orderItem.getProduct().getId();
                    Long sizeId = orderItem.getSize().getId();
                    Integer quantityToBuy = orderItem.getQuantity();

                    log.info("Xử lý OrderItem - ProductID: {}, SizeID: {}, Quantity: {}",
                            productId, sizeId, quantityToBuy);

                    // Tìm ProductInventory tương ứng
                    ProductInventory inventory = productInventoryRepository
                            .findByProductIdAndSizeId(productId, sizeId)
                            .orElseThrow(() -> new DataNotFoundException(
                                    String.format("Không tìm thấy inventory cho ProductID: %d, SizeID: %d",
                                            productId, sizeId)));

                    // Kiểm tra số lượng tồn kho
                    if (inventory.getQuantity() < quantityToBuy) {
                        log.error("Không đủ tồn kho - ProductID: {}, SizeID: {}, Tồn kho: {}, Cần: {}",
                                productId, sizeId, inventory.getQuantity(), quantityToBuy);
                        throw new RuntimeException(
                                String.format("Không đủ tồn kho cho sản phẩm ID: %d, Size ID: %d",
                                        productId, sizeId));
                    }

                    // Trừ số lượng tồn kho
                    Integer newQuantity = inventory.getQuantity() - quantityToBuy;
                    inventory.setQuantity(newQuantity);
                    productInventoryRepository.save(inventory);

                    log.info("✅ Đã cập nhật inventory - ProductID: {}, SizeID: {}, Tồn kho cũ: {}, Tồn kho mới: {}",
                            productId, sizeId, inventory.getQuantity() + quantityToBuy, newQuantity);

                } catch (Exception e) {
                    log.error("❌ Lỗi khi cập nhật inventory cho OrderItem ID: {} - {}",
                            orderItem.getId(), e.getMessage());
                    throw new RuntimeException("Lỗi cập nhật inventory: " + e.getMessage());
                }
            }

            log.info("✅ Hoàn thành cập nhật inventory cho Order ID: {}", order.getId());

        } catch (Exception e) {
            log.error("❌ Lỗi tổng quát khi cập nhật inventory cho Order ID: {} - {}",
                    order.getId(), e.getMessage());
            throw new RuntimeException("Lỗi cập nhật inventory: " + e.getMessage());
        }
    }
}