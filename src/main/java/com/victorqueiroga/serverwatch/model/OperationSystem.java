package com.victorqueiroga.serverwatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class OperationSystem implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "version", nullable = false, length = 50)
    private String version;

}
