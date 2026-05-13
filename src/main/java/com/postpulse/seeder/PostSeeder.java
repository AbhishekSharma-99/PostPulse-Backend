package com.postpulse.seeder;

import com.postpulse.entity.Category;
import com.postpulse.entity.Post;
import com.postpulse.entity.User;
import com.postpulse.repository.PostRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
public class PostSeeder {

    private final PostRepository postRepository;

    public PostSeeder(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    List<Post> seed(List<User> users, List<Category> categories) {
        User admin = users.getFirst(); // abhishek — primary author

        Category tech    = categories.get(0);
        Category career  = categories.get(1);
        Category travel  = categories.get(2);
        Category food    = categories.get(3);
        Category health  = categories.get(4);

        record PostData(String title, String slug, String description, String content, User author, Category category) {}

        List<PostData> data = List.of(
                new PostData(
                        "Getting Started with Spring Boot 3",
                        "getting-started-with-spring-boot-3",
                        "A practical introduction to building production-ready APIs with Spring Boot 3",
                        "Spring Boot 3 requires Java 17 as a baseline and migrates from javax to jakarta namespaces...",
                        admin, tech),  // ← admin

                new PostData(
                        "How JWT Authentication Actually Works",
                        "how-jwt-authentication-actually-works",
                        "Understanding the internals of JWT — signing, verification, and why tampering fails",
                        "A JWT consists of three Base64URL-encoded segments: header, payload, and signature...",
                        admin, tech),

                new PostData(
                        "Docker for Java Developers",
                        "docker-for-java-developers",
                        "Containerising Spring Boot applications with multi-stage Dockerfiles",
                        "Multi-stage Dockerfiles separate the build environment from the runtime image...",
                        admin, tech),

                new PostData(
                        "Building a Backend Portfolio That Gets You Hired",
                        "building-a-backend-portfolio-that-gets-you-hired",
                        "What hiring managers actually look for in a backend developer portfolio",
                        "A strong backend portfolio demonstrates decision-making, not just code...",
                        admin, career),

                new PostData(
                        "Spring Interview Questions That Actually Get Asked",
                        "spring-interview-questions-that-actually-get-asked",
                        "Focused preparation on the Spring topics interviewers probe most",
                        "Transaction propagation, bean lifecycle, and @Component vs @Service...",
                        admin, career),

                new PostData(
                        "Rajasthan: Beyond the Tourist Trail",
                        "rajasthan-beyond-the-tourist-trail",
                        "Hidden gems and underrated destinations in the desert state",
                        "Most visitors stick to Jaipur, Jodhpur, and Udaipur. The state has far more...",
                        admin, travel),

                new PostData(
                        "Weekend Getaways from Delhi Under 300km",
                        "weekend-getaways-from-delhi-under-300km",
                        "Curated escapes within a comfortable drive from the capital",
                        "Lansdowne, Mandawa, and Alwar are consistently overlooked by travellers...",
                        admin, travel),

                new PostData(
                        "The Perfect Chicken Biryani: A Technical Guide",
                        "the-perfect-chicken-biryani-a-technical-guide",
                        "Treating biryani as an engineering problem — variables, technique, reproducibility",
                        "The most common biryani failure is overcooked rice. The 70 percent parboil rule...",
                        admin, food),

                new PostData(
                        "Minimalism as a Developer Productivity System",
                        "minimalism-as-a-developer-productivity-system",
                        "Reducing decision fatigue and cognitive overhead through intentional constraints",
                        "Decision fatigue is measurable. Every choice depletes the same cognitive resource pool...",
                        admin, health),

                new PostData(
                        "Building a Consistent Workout Habit as a Developer",
                        "building-a-consistent-workout-habit-as-a-developer",
                        "Practical fitness advice for people who sit at a desk for 8 or more hours a day",
                        "Sedentary work creates specific physical problems — tight hip flexors, weakened glutes...",
                        admin, health),

                new PostData(
                        "Sleep, Deep Work, and Why Rest is a Developer Skill",
                        "sleep-deep-work-and-why-rest-is-a-developer-skill",
                        "The productivity case for taking sleep and recovery seriously",
                        "Sleep deprivation impairs the prefrontal cortex — the region responsible for reasoning...",
                        admin, health)
        );

        List<Post> posts = data.stream().map(d -> {
            Post p = new Post();
            p.setTitle(d.title());
            p.setSlug(d.slug());
            p.setDescription(d.description());
            p.setContent(d.content());
            p.setUser(d.author());
            p.setCategory(d.category());
            return p;
        }).toList();

        return postRepository.saveAll(posts);
    }

}
