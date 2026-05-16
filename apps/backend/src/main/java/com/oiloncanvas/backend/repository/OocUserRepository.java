package com.oiloncanvas.backend.repository;

import com.oiloncanvas.backend.entity.OocUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * OocUser JPA repository.
 */
@Repository
public interface OocUserRepository extends JpaRepository<OocUser, Integer> {
    /**
     * Finds a user row by external user identifier.
     *
     * @param externalUserId external user id saved for session mapping
     * @return matching user row when present
     */
    Optional<OocUser> findByExternalUserId(String externalUserId);

    Optional<OocUser> findByCurrentSessionId(String currentSessionId);

    /**
     * Fallback for single-user / no-sessionId clients: most recently created session.
     */
    Optional<OocUser> findTopByOrderBySessionCreatedAtDesc();

    // inherits save(), findById(), findAll(), and deleteById(), among others,
    // which service will use.
}
