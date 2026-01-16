package com.pavlov.user.service;

import com.pavlov.user.entity.Role;
import com.pavlov.user.entity.Consumer;
import com.pavlov.user.repository.RoleRepository;
import com.pavlov.user.repository.ConsumerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@RequiredArgsConstructor
@Service
public class ConsumerService {          // todo for auth ConsumerDetailsService + BCrypt ?

    private final ConsumerRepository consumerRepository;
    private final RoleRepository roleRepository;

    public Page<Consumer> getSlice(Pageable pageable, LocalDate birthDate, BigDecimal weight) {
        return consumerRepository.findAll(pageable, birthDate, weight);
    }

    public Consumer findById(long id) {
        return consumerRepository.findById(id).orElseThrow();
    }

    public List<Role> findRolesByConsumerId(long id) {
        return roleRepository.findByConsumerId(id);
    }

    public Consumer createConsumer(Consumer consumer) {
        if (consumerRepository.existsByUsername(consumer.getUsername()))
            throw new ResponseStatusException(CONFLICT, "Username  " + consumer.getUsername() + " is already in used");
        return consumerRepository.save(consumer);
    }

    @Transactional
    public void assignRolesToConsumer(long id, List<Long> roleIds) {
        for (Long roleId: roleIds) {
            consumerRepository.assignRolesToConsumer(id, roleId);
        }
    }

    public Consumer updateConsumer(long id, Consumer consumer) {
        if (!Objects.equals(id, consumer.getId()))
            throw new ResponseStatusException(BAD_REQUEST, "IDs are not equal in request url and body");
        if (consumerRepository.existsByUsername(consumer.getUsername()))
            throw new ResponseStatusException(CONFLICT, "Username  " + consumer.getUsername() + " is already in used");
        return consumerRepository.save(consumer);
    }

    public void deleteConsumerById(long id) {
        consumerRepository.deleteById(id);
    }

    @Transactional
    public void removeRolesFromConsumer(long id, List<Long> roleIds) {
        for (Long roleId: roleIds) {
            consumerRepository.removeRolesFromConsumer(id, roleId);
        }
    }
}
