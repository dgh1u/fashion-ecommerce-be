package com.kltn.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for ProductInventory information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryDto implements Serializable {
    private Long id;
    
    private SizeDto size;
    
    private Integer quantity;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
