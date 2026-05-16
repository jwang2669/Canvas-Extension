package com.oiloncanvas.backend.repository;

import com.oiloncanvas.backend.entity.CanvasConnection;
import com.oiloncanvas.backend.entity.OocUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * CanvasConnection JPA repository. Defines custom query method to find
 * canvas connections by an OocUser.
 */
@Repository
public interface CanvasConnectionRepository extends JpaRepository<CanvasConnection, Integer> {
    // find canvas connections by OocUser
    List<CanvasConnection> findByOocUser(OocUser oocUser);
    
    // save, findById, findAll, deleteById, etc. are inherited and will be used by service.
}
