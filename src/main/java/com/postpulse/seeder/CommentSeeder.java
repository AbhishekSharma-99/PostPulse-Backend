package com.postpulse.seeder;

import com.postpulse.entity.Comment;
import com.postpulse.entity.Post;
import com.postpulse.entity.User;
import com.postpulse.repository.CommentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Profile("dev")
public class CommentSeeder {

    private final CommentRepository commentRepository;

    public CommentSeeder(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    List<Comment> seed(List<User> users, List<Post> posts) {
        User priya = users.get(1);
        User rohan = users.get(2);

        // Map posts by slug for readable lookup
        Map<String, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getSlug, p -> p));

        record CommentData(String slug, String body, User author) {
        }

        List<CommentData> data = List.of(
                new CommentData(
                        "getting-started-with-spring-boot-3",
                        "The Jakarta EE namespace migration is something most tutorials skip entirely. Good that you called it out.",
                        priya),

                new CommentData(
                        "getting-started-with-spring-boot-3",
                        "Virtual threads with Spring Boot 3 are massively underrated. The throughput difference under load is real.",
                        rohan),

                new CommentData(
                        "how-jwt-authentication-actually-works",
                        "Most articles just paste the code. This explains what actually happens during signature verification.",
                        rohan),

                new CommentData(
                        "how-jwt-authentication-actually-works",
                        "The part about the server recomputing the signature on every request clicked something for me.",
                        priya),

                new CommentData(
                        "docker-for-java-developers",
                        "Multi-stage builds cut our image size from 600MB to under 80MB. The difference in pull times is significant.",
                        priya),

                new CommentData(
                        "docker-for-java-developers",
                        "Works on my machine has caused real production incidents. Containerisation is not optional anymore.",
                        rohan),

                new CommentData(
                        "building-a-backend-portfolio-that-gets-you-hired",
                        "Documenting tradeoffs instead of just code is exactly what was missing from my portfolio.",
                        rohan),

                new CommentData(
                        "building-a-backend-portfolio-that-gets-you-hired",
                        "Added an architecture section and a decision log to my README after reading this. Much stronger now.",
                        priya),

                new CommentData(
                        "spring-interview-questions-that-actually-get-asked",
                        "REQUIRES_NEW propagation cost me an interview last month. Wish I had read this beforehand.",
                        priya),

                new CommentData("spring-interview-questions-that-actually-get-asked",
                        "Bean lifecycle comes up in every Spring interview I have had. This covers the right depth.",
                        rohan),

                new CommentData(
                        "rajasthan-beyond-the-tourist-trail",
                        "Jaisalmer at sunset is one of the most striking things I have seen. The fort at dusk is something else.",
                        priya),

                new CommentData(
                        "rajasthan-beyond-the-tourist-trail",
                        "Bundi is criminally underrated. Step wells, murals, and almost no tourists.",
                        rohan),

                new CommentData(
                        "weekend-getaways-from-delhi-under-300km",
                        "Lansdowne in November — nearly empty, cold enough to matter, and genuinely peaceful.",
                        rohan),

                new CommentData(
                        "weekend-getaways-from-delhi-under-300km",
                        "Mandawa havelis are stunning. The painted facades are unlike anything else in North India.",
                        priya),

                new CommentData(
                        "the-perfect-chicken-biryani-a-technical-guide",
                        "The 70 percent parboil rule is the single most useful piece of biryani advice I have ever read.",
                        priya),

                new CommentData(
                        "the-perfect-chicken-biryani-a-technical-guide",
                        "Sealing with dough traps steam far more effectively than foil. The texture difference is noticeable.",
                        rohan),

                new CommentData(
                        "minimalism-as-a-developer-productivity-system",
                        "Eliminating morning decisions is something I implemented three months ago. The mental clarity gain is real.",
                        rohan),

                new CommentData(
                        "minimalism-as-a-developer-productivity-system",
                        "Six half-finished side projects, zero shipped ones. This article described my situation exactly.",
                        priya),

                new CommentData(
                        "building-a-consistent-workout-habit-as-a-developer",
                        "Hip flexor work resolved six months of lower back pain in under three weeks. Underrated fix.",
                        priya),

                new CommentData(
                        "building-a-consistent-workout-habit-as-a-developer",
                        "Consistency over intensity is the thing gym culture refuses to teach. Daily movement beats weekend sessions.",
                        rohan),

                new CommentData(
                        "sleep-deep-work-and-why-rest-is-a-developer-skill",
                        "The legally drunk analogy for sleep deprivation is the one I now use when people brag about all-nighters.",
                        rohan),

                new CommentData(
                        "sleep-deep-work-and-why-rest-is-a-developer-skill",
                        "Deep work being downstream of sleep quality is something I felt for years but never saw articulated.",
                        priya)
        );

        List<Comment> comments = data.stream().map(d -> {
            Comment c = new Comment();
            c.setBody(d.body());
            c.setUser(d.author());
            c.setPost(postMap.get(d.slug()));
            return c;
        }).toList();

        return commentRepository.saveAll(comments);
    }
}
