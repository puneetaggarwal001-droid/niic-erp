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
@Table(name = "job_requests")
public class JobRequest extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "style", referencedColumnName = "code", nullable = false)
    private Style style;

    @Column(nullable = false)
    private String modelNo;

    private Long fgItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    // Serialized List<JobColourRequest> — see production package-info for why
    // this staging data isn't modeled relationally like the live Job is. Mapped
    // to a plain text column (not @Lob/CLOB) so schema validation passes on both
    // H2 and Postgres — Postgres maps @Lob String to oid, which the text column
    // in the migration would not match.
    @Column(nullable = false)
    private String coloursJson;

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

    protected JobRequest() {
    }

    public JobRequest(Style style, String modelNo, Long fgItemId, Unit unit, String coloursJson, User requestedBy) {
        this.style = style;
        this.modelNo = modelNo;
        this.fgItemId = fgItemId;
        this.unit = unit;
        this.coloursJson = coloursJson;
        this.requestedBy = requestedBy;
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

    public String getColoursJson() {
        return coloursJson;
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
