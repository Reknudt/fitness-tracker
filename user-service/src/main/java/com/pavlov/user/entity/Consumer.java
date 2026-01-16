package com.pavlov.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Entity
@NoArgsConstructor
@Table(name = "consumer")
public class Consumer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 63)
    String username;

    @NotBlank
    @Size(max = 63)
    String email;

    @NotBlank
    @Size(max = 63)
    String passwordHash;

    LocalDate birthDate;

    @NotNull
    @Column(insertable = false, updatable = false)
    LocalDate joinedOn = LocalDate.now();

    @Positive
    @Digits(integer = 6, fraction = 3)
    BigDecimal weight;

    boolean enabled;
}
