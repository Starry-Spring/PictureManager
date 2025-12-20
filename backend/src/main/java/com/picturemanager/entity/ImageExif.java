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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getCameraMake() {
        return cameraMake;
    }

    public void setCameraMake(String cameraMake) {
        this.cameraMake = cameraMake;
    }

    public String getCameraModel() {
        return cameraModel;
    }

    public void setCameraModel(String cameraModel) {
        this.cameraModel = cameraModel;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public String getExposureTime() {
        return exposureTime;
    }

    public void setExposureTime(String exposureTime) {
        this.exposureTime = exposureTime;
    }

    public String getFNumber() {
        return fNumber;
    }

    public void setFNumber(String fNumber) {
        this.fNumber = fNumber;
    }

    public Integer getIsoSpeed() {
        return isoSpeed;
    }

    public void setIsoSpeed(Integer isoSpeed) {
        this.isoSpeed = isoSpeed;
    }

    public String getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(String focalLength) {
        this.focalLength = focalLength;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}