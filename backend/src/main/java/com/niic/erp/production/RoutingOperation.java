package com.niic.erp.production;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "routing_operations")
public class RoutingOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "routing_workstation_id", nullable = false)
    private RoutingWorkstation routingWorkstation;

    private int seq;

    @ManyToOne
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    // The dependency-flow DAG: this step can't accept more pieces than have
    // already cleared every step it depends on (which may sit on a different
    // workstation). See ProductionEntryService for the availability calc that
    // reads this graph.
    @ManyToMany
    @JoinTable(name = "routing_operation_dependencies",
            joinColumns = @JoinColumn(name = "routing_operation_id"),
            inverseJoinColumns = @JoinColumn(name = "depends_on_id"))
    private Set<RoutingOperation> dependsOn = new HashSet<>();

    protected RoutingOperation() {
    }

    public RoutingOperation(RoutingWorkstation routingWorkstation, int seq, Operation operation) {
        this.routingWorkstation = routingWorkstation;
        this.seq = seq;
        this.operation = operation;
    }

    public Long getId() {
        return id;
    }

    public RoutingWorkstation getRoutingWorkstation() {
        return routingWorkstation;
    }

    public int getSeq() {
        return seq;
    }

    public Operation getOperation() {
        return operation;
    }

    public Set<RoutingOperation> getDependsOn() {
        return dependsOn;
    }
}
