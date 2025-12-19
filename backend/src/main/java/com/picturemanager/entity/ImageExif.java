// src/main/java/com/picturemanager/entity/ImageExif.java
package com.picturemanager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "image_exif")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageExif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false, unique = true)
    private Image image;

    @Column(name = "camera_make", length = 100)
    private String cameraMake;

    @Column(name = "camera_model", length = 100)
    private String cameraModel;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "exposure_time", length = 20)
    private String exposureTime;

    @Column(name = "f_number", length = 20)
    private String fNumber;

    @Column(name = "iso_speed")
    private Integer isoSpeed;

    @Column(name = "focal_length", length = 20)
    private String focalLength;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}