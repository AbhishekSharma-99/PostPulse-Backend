package com.postpulse.utils;

import com.postpulse.repository.PostRepository;
import org.springframework.stereotype.Component;

@Component
public class SlugUtils {

    private final PostRepository postRepository;

    public SlugUtils(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public String generateUniqueSlug(String title, Long excludePostId) {
        String baseSlug = title.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")   // remove special chars
                .replaceAll("\\s+", "-")           // spaces to hyphens
                .replaceAll("-+", "-")             // multiple hyphens to single
                .replaceAll("^-|-$", "");

        String slug = baseSlug;
        int counter = 1;

        while (isSlugTaken(slug, excludePostId)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    public boolean isSlugTaken(String slug, Long excludePostId) {
        if (excludePostId == null) {
            return postRepository.existsBySlug(slug);
        }
        return postRepository.existsBySlugAndIdNot(slug, excludePostId);
    }
}
