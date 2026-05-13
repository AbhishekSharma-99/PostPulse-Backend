package com.postpulse.seeder;

import com.postpulse.entity.Category;
import com.postpulse.repository.CategoryRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
public class CategorySeeder {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    List<Category> seed() {
        record CategoryData(String name, String description) {
        }

        List<CategoryData> data = List.of(
                new CategoryData("Technology",       "Posts about software engineering, tools, and development practices"),
                new CategoryData("Career",           "Advice on professional growth, job hunting, and developer careers"),
                new CategoryData("Travel",           "Destinations, travel tips, and experiences across India and beyond"),
                new CategoryData("Food",             "Recipes, restaurant reviews, and culinary deep dives"),
                new CategoryData("Health & Fitness", "Wellness, fitness routines, sleep, and mental health for developers")
        );

        List<Category> categories = data.stream().map(d -> {
            Category c = new Category();
            c.setName(d.name());
            c.setDescription(d.description());
            return c;
        }).toList();

        return categoryRepository.saveAll(categories);
    }
}
