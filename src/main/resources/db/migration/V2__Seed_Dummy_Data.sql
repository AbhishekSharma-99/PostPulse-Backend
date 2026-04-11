-- =================================================================
-- V2__Seed_Dummy_Data.sql
-- Purpose : Populate demo data for portfolio presentation
-- Password: All users have password → password@123
-- =================================================================


-- -----------------------------------------------------------------
-- 1. USERS
-- Seeding before posts because posts have no user FK yet,
-- but comments reference users via email. Good habit to seed
-- users first always.
-- -----------------------------------------------------------------
INSERT INTO `users` (id, name, username, email, password) VALUES
                                                              (1, 'Abhishek Sharma',   'abhishek_dev',     'abhishek@postpulse.com',   '$2a$10$WHXkXydaHJPAOhfgDNToBuplRKUVEY5c9FQwa.a33zLmr/MWYDQiW'),
                                                              (2, 'Priya Mehta',    'priya_writes',  'priya@postpulse.com',   '$2a$10$WHXkXydaHJPAOhfgDNToBuplRKUVEY5c9FQwa.a33zLmr/MWYDQiW'),
                                                              (3, 'Rohan Verma',    'rohan_tech',    'rohan@postpulse.com',   '$2a$10$WHXkXydaHJPAOhfgDNToBuplRKUVEY5c9FQwa.a33zLmr/MWYDQiW');


-- -----------------------------------------------------------------
-- 2. ASSIGN ROLES TO USERS
-- abhishek  → ADMIN (id:1) + USER (id:2)  — can manage everything
-- priya  → USER only                   — regular contributor
-- rohan  → USER only                   — regular contributor
-- -----------------------------------------------------------------
INSERT INTO `users_roles` (user_id, role_id) VALUES
                                                 (1, 1),   -- abhishek  → ROLE_ADMIN
                                                 (1, 2),   -- abhishek  → ROLE_USER
                                                 (2, 2),   -- priya  → ROLE_USER
                                                 (3, 2);   -- rohan  → ROLE_USER


-- -----------------------------------------------------------------
-- 3. CATEGORIES
-- 5 categories covering broad topics — makes the app look real
-- -----------------------------------------------------------------
INSERT INTO `categories` (id, name, description) VALUES
                                                     (1, 'Technology',       'Latest trends in software, AI, and the digital world'),
                                                     (2, 'Career & Growth',  'Tips on landing jobs, growing skills, and navigating tech careers'),
                                                     (3, 'Travel',           'Destinations, travel stories, and tips from around the world'),
                                                     (4, 'Food & Lifestyle', 'Recipes, food culture, and everyday living'),
                                                     (5, 'Health & Fitness', 'Workouts, mental health, nutrition, and wellness advice');


-- -----------------------------------------------------------------
-- 4. POSTS
-- 2-3 posts per category = 12 posts total
-- Enough to show pagination works (if you have it)
-- Enough to show category filtering works
-- -----------------------------------------------------------------

-- Technology (category_id: 1)
INSERT INTO `posts` (id, title, description, content, category_id) VALUES
                                                                       (1,
                                                                        'Getting Started with Spring Boot 3',
                                                                        'A complete beginner guide to building REST APIs with Spring Boot 3 and Java 21',
                                                                        'Spring Boot 3 introduced several exciting changes including native compilation support via GraalVM, upgraded to Jakarta EE 10 from javax, and requires Java 17 as minimum. In this post we explore setting up your first project, understanding auto-configuration, and building your first REST controller. We also look at how Spring Boot 3 integrates with the new virtual threads feature in Java 21 for massively concurrent applications.',
                                                                        1),

                                                                       (2,
                                                                        'Understanding JWTs — How Token Authentication Really Works',
                                                                        'Breaking down JSON Web Tokens — header, payload, signature, and how Spring Security validates them',
                                                                        'A JWT consists of three Base64URL encoded parts separated by dots. The header declares the algorithm (typically HS256 or RS256). The payload carries claims — both standard ones like sub and exp and your custom ones like roles. The signature is computed by hashing header + payload with a secret key. Spring Security validates the token on every request by recomputing the signature and comparing it — if it matches, the request is trusted. Understanding this makes debugging auth issues dramatically easier.',
                                                                        1),

                                                                       (3,
                                                                        'Why Every Java Developer Should Learn Docker',
                                                                        'Containerise your Spring Boot app and never hear it works on my machine again',
                                                                        'Docker solves the classic environment mismatch problem. You define your application environment in a Dockerfile, build it into an image, and that exact same image runs on your laptop, your teammates machine, and your production server. For Java developers this means consistent JVM versions, consistent database versions, and predictable deployments. In this post we walk through writing a multi-stage Dockerfile for a Spring Boot app that keeps the final image lean by separating the build stage from the runtime stage.',
                                                                        1),

-- Career & Growth (category_id: 2)
                                                                       (4,
                                                                        'How I Structured My Portfolio to Get Noticed by Recruiters',
                                                                        'Practical advice on building a developer portfolio that actually gets callbacks',
                                                                        'Most developer portfolios make the same mistakes — todo apps, weather apps, and no context on what problem was solved. Recruiters spend less than two minutes on a portfolio. What they look for is evidence of real decisions: why did you choose this database, why this architecture, what tradeoffs did you make. Document your thinking, not just your code. A blog REST API with Flyway migrations, JWT auth, Docker deployment, and a clean README tells a far more compelling story than five todo apps.',
                                                                        2),

                                                                       (5,
                                                                        'Cracking the Spring Boot Interview — Topics That Actually Come Up',
                                                                        'From bean lifecycle to transaction management — a focused preparation guide',
                                                                        'Spring Boot interviews consistently cover a core set of topics. Bean lifecycle and scopes — know singleton vs prototype and when to use each. Auto-configuration — understand how @EnableAutoConfiguration and META-INF/spring.factories work. Transaction management — know @Transactional propagation levels, especially REQUIRED vs REQUIRES_NEW. Spring Security filter chain — understand how requests flow through filters before reaching your controller. Knowing these deeply, with examples from your own project, is far more impressive than memorising definitions.',
                                                                        2),

-- Travel (category_id: 3)
                                                                       (6,
                                                                        'Exploring Rajasthan — The Land of Forts and Colours',
                                                                        'A travel guide through Jaipur, Jodhpur, and Jaisalmer',
                                                                        'Rajasthan is unlike anywhere else in India. Jaipur greets you with its rose-pink architecture and the magnificent Amber Fort perched on the Aravalli hills. Jodhpur, the Blue City, is best experienced by getting lost in the old city lanes around Mehrangarh Fort. Jaisalmer feels like stepping into a sand-coloured dream — the golden fort, the havelis, and the Sam Sand Dunes at sunset are unforgettable. Best time to visit is October through March when the heat is manageable and the colours are at their most vivid.',
                                                                        3),

                                                                       (7,
                                                                        'Weekend Getaways from Delhi — Hidden Gems Within 300km',
                                                                        'Escape the capital without a long journey — underrated destinations worth your weekend',
                                                                        'Most Delhi residents know Agra and Jaipur but miss some genuinely special destinations nearby. Lansdowne in Uttarakhand is a quiet hill station with colonial-era churches and pine forests that see a fraction of the crowds Mussoorie gets. Neemrana Fort Palace in Rajasthan is a 15th century fort converted into a heritage hotel — day visits are possible. Mandawa in the Shekhawati region is an open-air museum of painted havelis that most tourists skip entirely.',
                                                                        3),

-- Food & Lifestyle (category_id: 4)
                                                                       (8,
                                                                        'The Science of Making Perfect Biryani',
                                                                        'Breaking down every layer of biryani — rice, dum cooking, and the spice balance',
                                                                        'Perfect biryani is not a recipe, it is a technique. The rice must be parboiled to exactly 70 percent doneness before layering — fully cooked rice turns mushy during dum. The meat must be marinated a minimum of four hours, ideally overnight, to allow the yoghurt acids to tenderise the fibres. Dum cooking — sealing the pot with dough and cooking on a low flame — creates a pressure that forces the flavours of the bottom layer upward through the rice while the saffron milk and fried onions melt downward. The result is every grain carrying the full story.',
                                                                        4),

                                                                       (9,
                                                                        'Minimalist Living — What Owning Less Taught Me About Focus',
                                                                        'How reducing physical clutter changed the way I approach work and creativity',
                                                                        'The connection between physical environment and mental clarity is well documented in productivity research. When I reduced my possessions to what I genuinely used, something unexpected happened — decision fatigue dropped noticeably. Fewer choices about trivial things meant more mental energy for things that matter. For developers this translates directly — a clean workspace, a focused tool set, and deliberately limited side projects produces better output than spreading attention across everything.',
                                                                        4),

-- Health & Fitness (category_id: 5)
                                                                       (10,
                                                                        'Building a Consistent Workout Habit as a Developer',
                                                                        'Practical fitness advice for people who sit at a desk for 8+ hours a day',
                                                                        'Sedentary work creates specific physical problems — tight hip flexors, weakened glutes, forward head posture, and compressed spinal discs. A developer fitness routine should specifically counter these patterns. Daily hip flexor stretches, glute activation exercises, and thoracic spine mobility work address the root causes rather than just the symptoms. Consistency matters far more than intensity. Twenty minutes of targeted movement every day produces better long-term results than two hour gym sessions twice a week.',
                                                                        5),

                                                                       (11,
                                                                        'Sleep, Deep Work, and Why Rest is a Developer Skill',
                                                                        'The productivity case for taking sleep and recovery seriously',
                                                                        'Sleep deprivation impairs the prefrontal cortex — exactly the brain region responsible for the logical reasoning, pattern recognition, and creative problem solving that coding demands. Research consistently shows that six hours of sleep produces cognitive performance equivalent to being legally drunk. Deep work — sustained, focused, distraction-free concentration — is only accessible to a rested brain. Treating sleep as a productivity tool rather than a sacrifice is one of the highest leverage changes a developer can make.',
                                                                        5);


-- -----------------------------------------------------------------
-- 5. COMMENTS
-- 2 comments per post minimum — shows the social aspect of the app
-- Using realistic names and emails, not "user1@test.com"
-- -----------------------------------------------------------------
INSERT INTO `comment` (post_id, name, email, body) VALUES

-- Post 1: Spring Boot
(1, 'Sneha Kapoor',   'sneha@gmail.com',    'This is exactly what I needed as someone just starting out with Spring. The Jakarta EE migration point was something I had missed completely.'),
(1, 'Dev Anand',      'devanand@gmail.com', 'Virtual threads with Spring Boot 3 is such an underrated feature. Would love a dedicated post on that topic.'),

-- Post 2: JWT
(2, 'Rahul Nair',     'rahul@gmail.com',    'Best JWT explanation I have read. Most articles just show you how to copy paste the code. This actually explains what is happening under the hood.'),
(2, 'Meera Joshi',    'meera@gmail.com',    'The signature recomputation part clicked something for me. I never understood why you cannot tamper with a JWT payload until now.'),

-- Post 3: Docker
(3, 'Karan Singh',    'karan@gmail.com',    'Multi-stage Dockerfile for Spring Boot is something I have been meaning to learn. The final image size difference is massive.'),
(3, 'Anita Sharma',   'anita@gmail.com',    'Works on my machine has caused me so many production issues. Bookmarking this.'),

-- Post 4: Portfolio
(4, 'Vikram Reddy',   'vikram@gmail.com',   'The point about documenting tradeoffs rather than just code really hit. My portfolio is full of apps with zero context.'),
(4, 'Pooja Iyer',     'pooja@gmail.com',    'Just restructured my README after reading this. Added an architecture section and a why I made these choices section. Feels much stronger now.'),

-- Post 5: Interview
(5, 'Arjun Pillai',   'arjun@gmail.com',    'REQUIRES_NEW propagation tripped me up in an interview last month. Wish I had read this beforehand.'),
(5, 'Nisha Gupta',    'nisha@gmail.com',    'Bean lifecycle questions come up in literally every Spring interview I have had. Solid list.'),

-- Post 6: Rajasthan
(6, 'Tanvi Shah',     'tanvi@gmail.com',    'Jaisalmer at sunset is genuinely one of the most beautiful things I have ever seen. Great writeup.'),
(6, 'Rohit Das',      'rohit@gmail.com',    'Would add Udaipur to this list. The Lake Palace view is something else entirely.'),

-- Post 7: Delhi Getaways
(7, 'Simi Arora',     'simi@gmail.com',     'Lansdowne is so underrated. Went last November and had the whole place nearly to ourselves.'),
(7, 'Kabir Malhotra', 'kabir@gmail.com',    'Mandawa is a hidden gem. The painted havelis are stunning and zero tourist crowds.'),

-- Post 8: Biryani
(8, 'Deepa Nair',     'deepa@gmail.com',    'The 70 percent parboil rule is the thing nobody talks about but it is everything. Ruined so many biryanis before I learned this.'),
(8, 'Faisal Khan',    'faisal@gmail.com',   'Sealing with dough instead of foil makes a real difference. The steam stays trapped much more effectively.'),

-- Post 9: Minimalism
(9, 'Lakshmi Rao',    'lakshmi@gmail.com',  'Decision fatigue is real. Reduced my morning choices to almost zero and the mental clarity difference in the first hour of work is noticeable.'),
(9, 'Aditya Bose',    'aditya@gmail.com',   'The side project point is something I needed to hear. I have six half-finished projects and zero finished ones.'),

-- Post 10: Fitness
(10, 'Priya Mehta',   'priya@postpulse.com','Hip flexor stretches changed my lower back situation completely. Six months of back pain gone in three weeks.'),
(10, 'Suresh Kumar',  'suresh@gmail.com',   'The consistency over intensity point is something gym culture really does not talk about enough.'),

-- Post 11: Sleep
(11, 'Rohan Verma',   'rohan@postpulse.com','The legally drunk comparison for sleep deprivation is one I am going to start using when people brag about pulling all-nighters.'),
(11, 'Neha Tiwari',   'neha@gmail.com',     'Deep work being gated behind sleep quality is something I have felt but never seen articulated this clearly.');