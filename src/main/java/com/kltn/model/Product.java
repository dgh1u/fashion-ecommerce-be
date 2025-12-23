package com.kltn.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "product")
@ToString
public class Product {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "del")
    private Boolean del;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "title")
    private String title;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "criteria_id")
    private Criteria criteria;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "type")
    private String type;



    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;



    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductInventory> inventories;
}
