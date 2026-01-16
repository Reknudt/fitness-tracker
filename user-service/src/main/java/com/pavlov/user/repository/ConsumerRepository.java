package com.pavlov.user.repository;

import com.pavlov.user.entity.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, Long> {

    @Query("""
        SELECT u FROM Consumer u
            WHERE u.birthDate = coalesce(:birthDate, u.birthDate)
                AND u.weight = coalesce(:weight, u.weight)""")
    Page<Consumer> findAll(Pageable pageable, LocalDate birthDate, BigDecimal weight);

    boolean existsByUsername(String username);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO consumer_roles (consumer_id, role_id) VALUES (:id, :roleId)", nativeQuery = true)
    void assignRolesToConsumer(Long id, Long roleId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_roles ur WHERE user_id = :id AND role_id = :roleId", nativeQuery = true)
    void removeRolesFromConsumer(Long id, Long roleId);
}
