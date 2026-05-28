package com.postpulse.repository;

import com.postpulse.entity.Role;
import com.postpulse.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    // ─── Shared Fixtures ────────────────────────────────────────────────────
    // All three custom methods operate on User rows. The fixture below
    // persists one Role and one User with that Role assigned, so that:
    //
    //   - findByUsernameOrEmailWithRoles can assert the JOIN FETCH populated
    //     u.roles, not just that the User record was found
    //   - existsByUsername / existsByEmail have a known username and email
    //     to test both the true and false branches
    //
    // Role must be persisted first and then assigned to the User via the
    // owning side of the @ManyToMany (User.roles) with CascadeType.PERSIST,
    // so a single userRepository.save(user) writes both the user row and the
    // join table row in one operation.

    @BeforeEach
    void setUp() {
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");
        roleUser = roleRepository.save(roleUser);

        User abhishek = new User();
        abhishek.setName("Abhishek Sharma");
        abhishek.setUsername("abhishek");
        abhishek.setEmail("abhishek@postpulse.com");
        abhishek.setPassword("hashed_password_bcrypt");
        abhishek.setEnabled(true);
        abhishek.setRoles(Set.of(roleUser));
        userRepository.save(abhishek);
    }

    // =====================================================================
    // findByUsernameOrEmailWithRoles — JOIN FETCH
    // JPQL: SELECT u FROM User u JOIN FETCH u.roles
    //       WHERE u.username = :username OR u.email = :email
    //
    // This query powers Spring Security's UserDetailsService. Two contracts
    // must hold simultaneously:
    //   1. The OR predicate — both username-only and email-only lookups
    //      must resolve to the correct User
    //   2. The JOIN FETCH — u.roles must be fully populated in the returned
    //      entity so GrantedAuthority mapping works outside any transaction
    //      without triggering LazyInitializationException
    //
    // Why assert role fields and not just roles.size() > 0?
    // @DataJpaTest wraps each test in a transaction, so LAZY loading would
    // technically work even without the JOIN FETCH. Asserting the role name
    // confirms the JOIN actually ran and returned the correct data, not that
    // a lazy proxy was transparently resolved by the open transaction.
    // =====================================================================

    @Test
    @DisplayName("findByUsernameOrEmailWithRoles — should find user by username and return roles eagerly loaded")
    void findByUsernameOrEmailWithRoles_FindsByUsername_AndReturnsRolesPopulated() {

        // --- ARRANGE ---
        // Pass the real username, use a dummy for the email param —
        // the OR clause should match on username alone.

        // --- ACT ---
        Optional<User> result = userRepository.findByUsernameOrEmailWithRoles(
                "abhishek", "no-match@example.com"
        );

        // --- ASSERT ---
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("abhishek");

        // JOIN FETCH contract — roles must be populated with actual data.
        // A wrong implementation (no JOIN FETCH) would leave this as an
        // uninitialized proxy that only resolves because the transaction
        // is still open — but the role name check confirms real data was loaded.
        assertThat(result.get().getRoles()).isNotEmpty();
        assertThat(result.get().getRoles())
                .extracting(Role::getName)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("findByUsernameOrEmailWithRoles — should find user by email and return roles eagerly loaded")
    void findByUsernameOrEmailWithRoles_FindsByEmail_AndReturnsRolesPopulated() {

        // --- ARRANGE ---
        // Pass a dummy for the username param, real email — OR clause must
        // match on email alone. Tests the right-hand side of the OR predicate.

        // --- ACT ---
        Optional<User> result = userRepository.findByUsernameOrEmailWithRoles(
                "no-match-username", "abhishek@postpulse.com"
        );

        // --- ASSERT ---
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("abhishek@postpulse.com");

        assertThat(result.get().getRoles()).isNotEmpty();
        assertThat(result.get().getRoles())
                .extracting(Role::getName)
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("findByUsernameOrEmailWithRoles — should return Optional.empty when neither username nor email matches")
    void findByUsernameOrEmailWithRoles_ReturnsEmpty_WhenNeitherUsernameNorEmailMatches() {

        // --- ARRANGE ---
        // Both params are unknown — verifies Optional.empty() is returned
        // rather than throwing an exception or returning a null-wrapped Optional.

        // --- ACT ---
        Optional<User> result = userRepository.findByUsernameOrEmailWithRoles(
                "ghost-user", "ghost@nowhere.com"
        );

        // --- ASSERT ---
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsernameOrEmailWithRoles — should return Optional.empty when user has no assigned roles")
    void findByUsernameOrEmailWithRoles_ReturnsEmpty_WhenUserHasNoRoles() {

        // --- ARRANGE ---
        // A User with an empty roles Set. The JPQL uses JOIN FETCH (inner join)
        // — a user with zero roles produces no joined rows, so the query returns
        // no result. This is the correct contract: a roleless user cannot log in.
        // If this returns present(), the JOIN FETCH has been changed to LEFT JOIN
        // FETCH — a security regression that must be caught immediately.
        User roleless = new User();
        roleless.setName("Roleless User");
        roleless.setUsername("roleless");
        roleless.setEmail("roleless@postpulse.com");
        roleless.setPassword("hashed_password_bcrypt");
        roleless.setEnabled(true);
        // roles left as empty HashSet — no join table rows inserted
        userRepository.save(roleless);

        // --- ACT ---
        Optional<User> result = userRepository.findByUsernameOrEmailWithRoles(
                "roleless", "roleless@postpulse.com"
        );

        // --- ASSERT ---
        // Inner JOIN FETCH on an empty roles set produces no rows —
        // a LEFT JOIN FETCH would incorrectly return the user here.
        assertThat(result).isEmpty();
    }

    // =====================================================================
    // existsByUsername
    // Used by AuthServiceImpl to reject duplicate usernames on registration.
    // Both branches must be pinned — a broken derived query could always
    // return true (blocking all registrations) or always false (allowing
    // unlimited duplicate usernames).
    // =====================================================================

    @Test
    @DisplayName("existsByUsername — should return true when a user with that username exists")
    void existsByUsername_ReturnsTrue_WhenUsernameExists() {

        // --- ACT ---
        Boolean exists = userRepository.existsByUsername("abhishek");

        // --- ASSERT ---
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername — should return false when no user has that username")
    void existsByUsername_ReturnsFalse_WhenUsernameDoesNotExist() {

        // --- ACT ---
        Boolean exists = userRepository.existsByUsername("phantom_user");

        // --- ASSERT ---
        assertThat(exists).isFalse();
    }

    // =====================================================================
    // existsByEmail
    // Used by AuthServiceImpl to reject duplicate emails on registration.
    // Same criticality as existsByUsername — both true and false paths
    // must be verified. The email column has a unique constraint in the
    // schema; this query guards against constraint violations at the
    // service layer before a DB exception can propagate.
    // =====================================================================

    @Test
    @DisplayName("existsByEmail — should return true when a user with that email exists")
    void existsByEmail_ReturnsTrue_WhenEmailExists() {

        // --- ACT ---
        Boolean exists = userRepository.existsByEmail("abhishek@postpulse.com");

        // --- ASSERT ---
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail — should return false when no user has that email")
    void existsByEmail_ReturnsFalse_WhenEmailDoesNotExist() {

        // --- ACT ---
        Boolean exists = userRepository.existsByEmail("unknown@nowhere.com");

        // --- ASSERT ---
        assertThat(exists).isFalse();
    }
}