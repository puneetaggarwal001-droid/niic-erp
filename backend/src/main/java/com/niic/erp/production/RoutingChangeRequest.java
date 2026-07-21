package com.niic.erp.production;

import com.niic.erp.common.BaseEntity;
import com.niic.erp.common.RequestStatus;
import com.niic.erp.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "routing_change_requests")
public class RoutingChangeRequest extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Serialized List<RoutingWorkstationRequest> — same reasoning as JobRequest.coloursJson.
    @Lob
    @Column(nullable = false)
    private String proposedRoutingJson;

    @Column(nullable = false, length = 2000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @ManyToOne
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    private Instant reviewedAt;

    @Column(length = 2000)
    private String adminRemark;

    protected RoutingChangeRequest() {
    }

    public RoutingChangeRequest(Job job, String proposedRoutingJson, String reason, User requestedBy) {
        this.job = job;
        this.proposedRoutingJson = proposedRoutingJson;
        this.reason = reason;
        this.requestedBy = requestedBy;
    }

    public Job getJob() {
        return job;
    }

    public String getProposedRoutingJson() {
        return proposedRoutingJson;
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

    public User getRequestedBy() {
        return requestedBy;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getAdminRemark() {
        return adminRemark;
    }

    public void setAdminRemark(String adminRemark) {
        this.adminRemark = adminRemark;
    }
}
