package com.pavlov.user.repository;

import com.pavlov.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(value = "SELECT r FROM Role r JOIN consumer_roles ur ON ur.role_id = r.id WHERE ur.consumer_id = :consumerId", nativeQuery = true)
    List<Role> findByConsumerId(long consumerId);

}
