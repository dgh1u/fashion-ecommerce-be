package com.kltn.dto.entity;

import lombok.Data;

@Data
public class UserDto {
    private Long id;

    private String fullName;

    private String email;

    private String address;

    private String phone;

    private boolean block;

    private String b64;

    private String fileType;

    private RoleDto role;



}
