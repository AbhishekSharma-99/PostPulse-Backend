package com.postpulse.repository;

import com.postpulse.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//@Repository is already inherited in simple repoository from JPARepository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(long postId);
}
