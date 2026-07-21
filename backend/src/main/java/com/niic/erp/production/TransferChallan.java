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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfer_challans")
public class TransferChallan extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String challanNo;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "from_workstation_id", nullable = false)
    private Workstation fromWorkstation;

    @ManyToOne
    @JoinColumn(name = "to_workstation_id", nullable = false)
    private Workstation toWorkstation;

    @Column(length = 2000)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallanStatus status = ChallanStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "received_by_id")
    private User receivedBy;

    private Instant receivedAt;

    @ManyToOne
    @JoinColumn(name = "rejected_by_id")
    private User rejectedBy;

    private Instant rejectedAt;

    @Column(length = 2000)
    private String rejectionReason;

    @OneToMany(mappedBy = "transferChallan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransferChallanItem> items = new ArrayList<>();

    protected TransferChallan() {
    }

    public TransferChallan(String challanNo, Job job, Workstation fromWorkstation, Workstation toWorkstation,
                            String remarks, User createdBy) {
        this.challanNo = challanNo;
        this.job = job;
        this.fromWorkstation = fromWorkstation;
        this.toWorkstation = toWorkstation;
        this.remarks = remarks;
        this.createdBy = createdBy;
    }

    public String getChallanNo() {
        return challanNo;
    }

    public Job getJob() {
        return job;
    }

    public Workstation getFromWorkstation() {
        return fromWorkstation;
    }

    public Workstation getToWorkstation() {
        return toWorkstation;
    }

    public String getRemarks() {
        return remarks;
    }

    public ChallanStatus getStatus() {
        return status;
    }

    public void setStatus(ChallanStatus status) {
        this.status = status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public User getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(User receivedBy) {
        this.receivedBy = receivedBy;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public User getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(User rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public List<TransferChallanItem> getItems() {
        return items;
    }
}
