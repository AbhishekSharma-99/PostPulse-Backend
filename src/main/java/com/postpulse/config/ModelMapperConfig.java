package com.postpulse.config;

import com.postpulse.entity.Post;
import com.postpulse.payload.post.PostResponse;
import com.postpulse.payload.post.PostSummary;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STANDARD);

        // Post → PostResponse
        modelMapper.addMappings(new PropertyMap<Post, PostResponse>() {
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
        modelMapper.addMappings(new PropertyMap<Post, PostSummary>() {
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

        return modelMapper;
    }
}