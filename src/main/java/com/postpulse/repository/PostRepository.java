package com.postpulse.repository;

import com.postpulse.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCategoryId(long categoryId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long excludePostId);

    Optional<Post> findBySlug(String slug);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Post p WHERE p.id = :id")
    int deletePostById(@Param("id") Long id);
}
