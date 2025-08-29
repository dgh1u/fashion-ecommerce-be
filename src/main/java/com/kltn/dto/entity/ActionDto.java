package com.kltn.dto.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kltn.model.enums.ActionName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionDto implements Serializable {
    private Long id;

    private String email;

    private String fullName;

    private ActionName action;

    private String productTitle;

    private Long productId;

    private String firstClass;

    private Boolean isRead;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private LocalDateTime time;

}