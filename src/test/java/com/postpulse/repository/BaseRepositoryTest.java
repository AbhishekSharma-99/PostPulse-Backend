package com.postpulse.repository;

import com.postpulse.config.JpaAuditingConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
public abstract class BaseRepositoryTest {
    // This class is intentionally left blank. It serves as a common base for all repository tests,
    // providing shared configuration and setup (like enabling JPA auditing) without duplicating code.
}

