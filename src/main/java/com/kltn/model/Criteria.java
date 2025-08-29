package com.kltn.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@Table(name = "criteria")
public class Criteria {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address")
    private String address;

    @Column(name = "first_class", length = 50)
    private String firstClass;

    @Column(name = "price")
    private Integer price;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    @Column(name = "second_class", length = 50)
    private String secondClass;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "material", length = 50)
    private String material;

    @Column(name = "original_price")
    private Integer originalPrice;
}