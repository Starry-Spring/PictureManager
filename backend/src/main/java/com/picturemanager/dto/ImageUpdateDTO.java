// src/main/java/com/picturemanager/dto/ImageUpdateDTO.java
package com.picturemanager.dto;

import lombok.Data;
import java.util.Set;

@Data
public class ImageUpdateDTO {

    private String title;

    private String description;

    private Set<String> tags;

    // 裁剪参数
    private Integer cropLeft;   // 从左边保留多少像素（负数表示从右边）
    private Integer cropTop;    // 从上边保留多少像素（负数表示从下边）

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Integer getCropLeft() {
        return cropLeft;
    }

    public void setCropLeft(Integer cropLeft) {
        this.cropLeft = cropLeft;
    }

    public Integer getCropTop() {
        return cropTop;
    }

    public void setCropTop(Integer cropTop) {
        this.cropTop = cropTop;
    }
}
