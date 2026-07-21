package com.niic.erp.production;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routing_workstations")
public class RoutingWorkstation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "routing_id", nullable = false)
    private Routing routing;

    private int seq;

    @ManyToOne
    @JoinColumn(name = "workstation_id", nullable = false)
    private Workstation workstation;

    @OneToMany(mappedBy = "routingWorkstation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq asc")
    private List<RoutingOperation> operations = new ArrayList<>();

    protected RoutingWorkstation() {
    }

    public RoutingWorkstation(Routing routing, int seq, Workstation workstation) {
        this.routing = routing;
        this.seq = seq;
        this.workstation = workstation;
    }

    public Long getId() {
        return id;
    }

    public Routing getRouting() {
        return routing;
    }

    public int getSeq() {
        return seq;
    }

    public Workstation getWorkstation() {
        return workstation;
    }

    public List<RoutingOperation> getOperations() {
        return operations;
    }
}
