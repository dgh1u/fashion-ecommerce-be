package com.kltn.dto.entity;

import com.kltn.model.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for {@link Size}
 */
@Data
public class SizeDto implements Serializable {
    private Long id;

    private String name;
}