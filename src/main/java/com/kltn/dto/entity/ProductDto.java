package com.kltn.dto.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDto {
    private long id;

    private String title;

    private String content;

    private boolean approved;

    private boolean notApproved;

    private LocalDateTime createAt;

    private LocalDateTime lastUpdate;

    private boolean del;

    private UserDto userDTO;

    private CriteriaDto criteriaDTO;

    private List<CommentDto> commentDTOS;

    private List<String> imageStrings;

    private List<ProductInventoryDto> inventories;

    private String type;
}
