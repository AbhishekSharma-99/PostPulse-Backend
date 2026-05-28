package com.postpulse.repository;

import com.postpulse.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest spins up an H2 in-memory database, configures Spring Data JPA,
// and rolls back every test in a transaction — no state leaks between tests.
// Only the JPA slice is loaded; the full ApplicationContext is never started.

class CategoryRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    // ─── Shared Fixtures ────────────────────────────────────────────────────
    // Both custom methods on CategoryRepository are uniqueness guards:
    // existsByName and existsByNameAndIdNot. The fixture below pre-persists
    // one category so every test starts from a consistent, non-empty state
    // without repeating the same save() boilerplate in each method.

    private Category technology;

    @BeforeEach
    void setUp() {
        technology = new Category();
        technology.setName("Technology");
        technology.setDescription("Posts about software engineering and tech");
        technology = categoryRepository.save(technology);
    }

    // =====================================================================
    // existsByName
    // Contract: returns true when any row in the categories table has the
    // given name. Used by CategoryServiceImpl to reject duplicate names on
    // create. Both branches must be pinned — if the derived query is wrong
    // (e.g. case-sensitive mismatch on H2), the false-positive case would
    // expose it immediately.
    // =====================================================================

    @Test
    @DisplayName("existsByName — should return true when a category with that name already exists")
    void existsByName_ReturnsTrue_WhenNameExists() {

        // --- ARRANGE ---
        // "Technology" was persisted in @BeforeEach. No additional setup needed.

        // --- ACT ---
        boolean exists = categoryRepository.existsByName("Technology");

        // --- ASSERT ---
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByName — should return false when no category has that name")
    void existsByName_ReturnsFalse_WhenNameDoesNotExist() {

        // --- ARRANGE ---
        // "Finance" was never inserted — this confirms the query returns false
        // rather than defaulting to true or throwing.

        // --- ACT ---
        boolean exists = categoryRepository.existsByName("Finance");

        // --- ASSERT ---
        assertThat(exists).isFalse();
    }

    // =====================================================================
    // existsByNameAndIdNot
    // Contract: returns true when a DIFFERENT row has the same name (genuine
    // duplicate on update). Returns false when the only row with that name
    // IS the excluded ID (self-reference — user is re-saving their own name,
    // which is not a conflict).
    //
    // This is the more critical test — if the "AND id != :excludeId" clause
    // is missing from the derived query, the self-exclusion case would return
    // true and the service would incorrectly reject a valid update.
    // =====================================================================

    @Test
    @DisplayName("existsByNameAndIdNot — should return true when a DIFFERENT category has the same name")
    void existsByNameAndIdNot_ReturnsTrue_WhenDifferentCategoryHasSameName() {

        // --- ARRANGE ---
        // A second category takes the name "Technology" under a different ID.
        // Simulates a POST /categories request where name collides with an
        // existing record that is not the one being updated.
        Category sports = new Category();
        sports.setName("Sports");
        sports.setDescription("Posts about sports");
        sports = categoryRepository.save(sports);

        // Now pretend "Sports" is being renamed to "Technology" via PUT.
        // sports.getId() is the ID being updated — technology.getId() is the
        // conflicting record. The query should detect the collision.

        // --- ACT ---
        boolean conflict = categoryRepository.existsByNameAndIdNot("Technology", sports.getId());

        // --- ASSERT ---
        assertThat(conflict).isTrue();
    }

    @Test
    @DisplayName("existsByNameAndIdNot — should return false when the only match IS the excluded ID (self-reference)")
    void existsByNameAndIdNot_ReturnsFalse_WhenOnlyMatchIsExcludedId() {

        // --- ARRANGE ---
        // "Technology" exists with technology.getId(). The service calls this
        // when updating that same category with the same name — it should NOT
        // be flagged as a duplicate because the only matching row is itself.

        // --- ACT ---
        boolean conflict = categoryRepository.existsByNameAndIdNot("Technology", technology.getId());

        // --- ASSERT ---
        // Self-exclusion must pass — no conflict when the sole matching row
        // is the record currently being updated.
        assertThat(conflict).isFalse();
    }

    @Test
    @DisplayName("existsByNameAndIdNot — should return false when name does not exist at all")
    void existsByNameAndIdNot_ReturnsFalse_WhenNameDoesNotExistAtAll() {

        // --- ARRANGE ---
        // "Finance" is absent from the table entirely. Verifies the query
        // handles a zero-row match gracefully regardless of the excludeId.

        // --- ACT ---
        boolean conflict = categoryRepository.existsByNameAndIdNot("Finance", technology.getId());

        // --- ASSERT ---
        assertThat(conflict).isFalse();
    }
}