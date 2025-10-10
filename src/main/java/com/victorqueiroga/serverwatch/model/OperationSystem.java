package com.victorqueiroga.serverwatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "operation_systems")
public class OperationSystem implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

}
