package com.kltn.dto.entity;

import com.kltn.model.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link Size}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SizeDto implements Serializable {
    private Long id;

    private String name;
}