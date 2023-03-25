package ru.kpfu.machinemetrics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String photoPath;

    private String inventoryNumber;

    private String name;

    private BigDecimal cost;

    private String source;

    // todo many to one ?
    private String department;

    // todo many to one ?
    private String responsiblePerson;

    // todo many to one ?
    private String status;

    private Instant receiptDate;

    private Instant lastOperationDate;

    // todo ... another params

    private boolean deleted;
}
