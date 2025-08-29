package com.kltn.dto.entity;

import com.kltn.model.Comment;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for {@link Comment}
 */
@Data
public class CommentDto {
    private long id;

    private String content;

    private LocalDateTime lastUpdate;

    private Long idProduct;

    private UserDto userDTO;

    private Long rate;
}