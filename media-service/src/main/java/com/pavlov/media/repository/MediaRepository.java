package com.pavlov.media.repository;

import com.pavlov.media.entity.Media;
import org.hibernate.annotations.processing.HQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

//    @Find
    @HQL("SELECT m.fileName FROM Media m WHERE m.id = :id")
    Optional<String> findFileNameById(Long id);
}
