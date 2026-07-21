package com.niic.erp.production;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "job_colours")
public class JobColour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int seq;

    @OneToMany(mappedBy = "jobColour", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seq asc")
    private List<JobSize> sizes = new ArrayList<>();

    protected JobColour() {
    }

    public JobColour(Job job, String name, int seq) {
        this.job = job;
        this.name = name;
        this.seq = seq;
    }

    public Long getId() {
        return id;
    }

    public Job getJob() {
        return job;
    }

    public String getName() {
        return name;
    }

    public int getSeq() {
        return seq;
    }

    public List<JobSize> getSizes() {
        return sizes;
    }
}
