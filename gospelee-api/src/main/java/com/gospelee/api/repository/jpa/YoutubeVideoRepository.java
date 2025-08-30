package com.gospelee.api.repository.jpa;

import com.gospelee.api.entity.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YoutubeVideoRepository extends JpaRepository<YoutubeVideo, Long> {
    
    @Query("SELECT y FROM YoutubeVideo y WHERE y.isActive = true ORDER BY y.sortOrder ASC, y.insertTime DESC")
    List<YoutubeVideo> findActiveVideosOrderBySortOrder();
}
