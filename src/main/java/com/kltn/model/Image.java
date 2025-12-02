package com.kltn.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Getter
@Setter
@Entity
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String fileName;

    private String fileType;

    @Lob
    @Column(name = "data", columnDefinition = "LONGBLOB")
    private byte[] data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    public Image() {
    }

    public Image(String fileName, String fileType, byte[] data, Product product) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.product = product;
        this.orderIndex = 0;
    }

    public Image(String fileName, String fileType, byte[] data, Product product, Integer orderIndex) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.product = product;
        this.orderIndex = orderIndex;
    }
}