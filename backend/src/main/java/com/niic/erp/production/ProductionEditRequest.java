package com.niic.erp.production;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.common.RequestStatus;
import com.niic.erp.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "production_edit_requests")
public class ProductionEditRequest extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "colour_id")
    private JobColour colour;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private JobSize size;

    @ManyToOne
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Column(nullable = false)
    private Instant requestedAt;

    @Column(nullable = false, length = 2000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    // Once approved, the request can be consumed exactly once to reopen the
    // (job, colour, size) combo for a single consolidated re-entry.
    @Column(nullable = false)
    private boolean used = false;

    private Instant usedAt;

    @ManyToOne
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    private Instant resolvedAt;

    @Column(length = 2000)
    private String resolution;

    protected ProductionEditRequest() {
    }

    public ProductionEditRequest(Job job, JobColour colour, JobSize size, User requestedBy, String reason) {
        this.job = job;
        this.colour = colour;
        this.size = size;
        this.requestedBy = requestedBy;
        this.requestedAt = Instant.now();
        this.reason = reason;
    }

    public Job getJob() {
        return job;
    }

    public JobColour getColour() {
        return colour;
    }

    public JobSize getSize() {
        return size;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public String getReason() {
        return reason;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}
