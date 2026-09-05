package com.dailycodework.buynowdotcom.repository;

import com.dailycodework.buynowdotcom.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByProductId(Long productId);
}