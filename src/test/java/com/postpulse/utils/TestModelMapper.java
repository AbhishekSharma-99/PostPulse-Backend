package com.postpulse.utils;

import com.postpulse.entity.Post;
import com.postpulse.payload.post.PostResponse;
import com.postpulse.payload.post.PostSummary;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

/**
 * Provides a ModelMapper instance identical to the production
 * {@link com.postpulse.config.ModelMapperConfig} for use in unit tests.
 * This avoids duplicating the mapping configuration across test classes.
 */
public final class TestModelMapper {

    private static ModelMapper modelMapper;

    private TestModelMapper() {} // prevent instantiation

    public static synchronized ModelMapper getModelMapper() {
        if (modelMapper == null) {
            modelMapper = buildModelMapper();
        }
        return modelMapper;
    }

    private static ModelMapper buildModelMapper() {
        ModelMapper mapper = new ModelMapper();

        // Post → PostResponse (mirrors ModelMapperConfig)
        mapper.addMappings(new PropertyMap<Post, PostResponse>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    Post src = (Post) ctx.getSource();
                    return src.getCategory() != null ? src.getCategory().getId() : null;
                }).map(source).setCategoryId(null);

                using(ctx -> {
                    Post src = (Post) ctx.getSource();
                    return src.getCategory() != null ? src.getCategory().getName() : null;
                }).map(source).setCategoryName(null);

                using(ctx -> {
                    Post src = (Post) ctx.getSource();
                    return src.getUser() != null ? src.getUser().getName() : null;
                }).map(source).setAuthorName(null);
            }
        });

        // Post → PostSummary
        mapper.addMappings(new PropertyMap<Post, PostSummary>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    Post src = (Post) ctx.getSource();
                    return src.getCategory() != null ? src.getCategory().getName() : null;
                }).map(source).setCategoryName(null);

                using(ctx -> {
                    Post src = (Post) ctx.getSource();
                    return src.getUser() != null ? src.getUser().getName() : null;
                }).map(source).setAuthorName(null);
            }
        });

        return mapper;
    }
}