package com.niic.erp.production;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
public class Job extends BaseEntity {

    // Format "NIIC / <STYLE> / <NNN>" — see JobNumberAllocator.
    @Column(nullable = false, unique = true)
    private String jobDisplayId;

    @ManyToOne
    @JoinColumn(name = "style", referencedColumnName = "code", nullable = false)
    private Style style;

    @Column(nullable = false)
    private String modelNo;

    // FK into the not-yet-built Store module's FG item master; left unenforced for now.
    private Long fgItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    @Column(nullable = false)
    private int totalPlannedQty;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobSource source;

    @Column(nullable = false)
    private boolean routingAssigned = false;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq asc")
    private List<JobColour> colours = new ArrayList<>();

    protected Job() {
    }

    public Job(String jobDisplayId, Style style, String modelNo, Long fgItemId, Unit unit, JobSource source,
               User createdBy) {
        this.jobDisplayId = jobDisplayId;
        this.style = style;
        this.modelNo = modelNo;
        this.fgItemId = fgItemId;
        this.unit = unit;
        this.source = source;
        this.createdBy = createdBy;
    }

    public String getJobDisplayId() {
        return jobDisplayId;
    }

    public Style getStyle() {
        return style;
    }

    public String getModelNo() {
        return modelNo;
    }

    public Long getFgItemId() {
        return fgItemId;
    }

    public Unit getUnit() {
        return unit;
    }

    public int getTotalPlannedQty() {
        return totalPlannedQty;
    }

    public void setTotalPlannedQty(int totalPlannedQty) {
        this.totalPlannedQty = totalPlannedQty;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public JobSource getSource() {
        return source;
    }

    public boolean isRoutingAssigned() {
        return routingAssigned;
    }

    public void setRoutingAssigned(boolean routingAssigned) {
        this.routingAssigned = routingAssigned;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public List<JobColour> getColours() {
        return colours;
    }
}
