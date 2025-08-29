package com.kltn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Getter
@Setter
@Entity
@Table(name = "permission")
@ToString
public class Permission {
    @Id
    @Column(name = "permission_id")
    private String permissionId;
    @NotBlank
    @Size(max = 100)
    private String name;
    @NotBlank
    @Size(max = 200)
    private String description;
    @ManyToMany(mappedBy = "permissions")
    private Collection<Role> roles;
}