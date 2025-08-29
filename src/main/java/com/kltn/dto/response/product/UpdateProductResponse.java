package com.kltn.dto.response.product;

import com.kltn.dto.entity.CriteriaDto;
import com.kltn.dto.entity.UserDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateProductResponse {
    private long id;
    private String title;
    private String content;
    private boolean approved;
    private boolean notApproved;
    private LocalDateTime createAt;
    private LocalDateTime lastUpdate;
    private boolean del;
    private CriteriaDto criteriaDTO;
    private UserDto userDTO;
    private String type;
}
