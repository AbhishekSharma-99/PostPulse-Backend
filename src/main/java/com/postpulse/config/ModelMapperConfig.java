package com.postpulse.config;

import com.postpulse.entity.Post;
import com.postpulse.payload.PostResponseDto;
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

        modelMapper.addMappings(new PropertyMap<Post, PostResponseDto>() {
            @Override
            protected void configure() {

                using(ctx -> {
                    Post source = (Post) ctx.getSource();
                    return source.getCategory() != null
                            ? source.getCategory().getId()
                            : null;
                }).map(source).setCategoryId(0L);
            }
        });

        return modelMapper;
    }
}