package com.niic.erp.sampling;

import com.niic.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * An admin request to the sampler: create a new sample (NEW) or change an
 * existing one (CHANGE). The sampler completes it by linking the produced
 * sample; the admin later resolves it (select / reject / change / new revision).
 */
@Entity
@Table(name = "sample_requests")
public class SampleRequest extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "req_type", nullable = false, length = 10)
    private RequestType reqType;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 10)
    private String priority = "NORMAL";

    @Column(name = "ref_sample_id")
    private Long refSampleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "completed_sample_id")
    private Long completedSampleId;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    @Column(name = "admin_resolved_at")
    private Instant adminResolvedAt;

    @Column(name = "admin_resolved_by", length = 100)
    private String adminResolvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_resolved_remark", length = 20)
    private ClosedRemark adminResolvedRemark;

    protected SampleRequest() {
    }

    public SampleRequest(RequestType reqType, String title, String description, String priority,
                         Long refSampleId, String requestedBy) {
        this.reqType = reqType;
        this.title = title;
        this.description = description;
        this.priority = priority != null ? priority : "NORMAL";
        this.refSampleId = refSampleId;
        this.requestedBy = requestedBy;
    }

    public RequestType getReqType() {
        return reqType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getRefSampleId() {
        return refSampleId;
    }

    public void setRefSampleId(Long refSampleId) {
        this.refSampleId = refSampleId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Long getCompletedSampleId() {
        return completedSampleId;
    }

    public void setCompletedSampleId(Long completedSampleId) {
        this.completedSampleId = completedSampleId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public Instant getAdminResolvedAt() {
        return adminResolvedAt;
    }

    public void setAdminResolvedAt(Instant adminResolvedAt) {
        this.adminResolvedAt = adminResolvedAt;
    }

    public String getAdminResolvedBy() {
        return adminResolvedBy;
    }

    public void setAdminResolvedBy(String adminResolvedBy) {
        this.adminResolvedBy = adminResolvedBy;
    }

    public ClosedRemark getAdminResolvedRemark() {
        return adminResolvedRemark;
    }

    public void setAdminResolvedRemark(ClosedRemark adminResolvedRemark) {
        this.adminResolvedRemark = adminResolvedRemark;
    }
}
