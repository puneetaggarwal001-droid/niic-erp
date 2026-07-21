package com.niic.erp.store;

import com.niic.erp.common.NotFoundException;
import com.niic.erp.production.Job;
import com.niic.erp.production.JobColour;
import com.niic.erp.production.JobRepository;
import com.niic.erp.production.JobSize;
import com.niic.erp.store.dto.MrpResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Material Requirement Planning: explode a job's finished-goods BOM (RM directly,
 * plus SFG components exploded through their batch-based SFG BOM) across every
 * colour/size × planned quantity, then compare the required raw-material totals
 * against available stock to surface shortfalls.
 */
@Service
public class MrpService {

    private final JobRepository jobRepository;
    private final StoreItemRepository itemRepository;
    private final BomService bomService;
    private final StoreService storeService;

    public MrpService(JobRepository jobRepository, StoreItemRepository itemRepository,
                      BomService bomService, StoreService storeService) {
        this.jobRepository = jobRepository;
        this.itemRepository = itemRepository;
        this.bomService = bomService;
        this.storeService = storeService;
    }

    @Transactional(readOnly = true)
    public MrpResponse explode(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job " + jobId + " not found."));

        Map<Long, BigDecimal> required = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>();
        Long fgItemId = job.getFgItemId();

        if (fgItemId == null) {
            warnings.add("Job has no linked finished-goods item; cannot explode a BOM.");
            return new MrpResponse(job.getId(), job.getJobDisplayId(), List.of(), warnings);
        }

        for (JobColour colour : job.getColours()) {
            for (JobSize size : colour.getSizes()) {
                BigDecimal qty = BigDecimal.valueOf(size.getPlannedQty());
                var fgBom = bomService.resolveBom(fgItemId, colour.getName(), size.getSize());
                if (fgBom.isEmpty()) {
                    warnings.add("No BOM for " + colour.getName() + "/" + size.getSize());
                    continue;
                }
                Bom bom = fgBom.get();
                BigDecimal batch = bom.getBatchQty();
                for (BomComponent comp : bom.getComponents()) {
                    StoreItem item = comp.getComponentItem();
                    BigDecimal need = comp.getQuantity().multiply(qty)
                            .divide(batch, 6, RoundingMode.HALF_UP);
                    if (item.getItemType() == ItemType.SFG) {
                        // The SFG itself is a requirement, and its RM inputs explode further.
                        add(required, item.getId(), need);
                        explodeSfg(item, colour.getName(), size.getSize(), need, required, warnings);
                    } else {
                        add(required, item.getId(), need);
                    }
                }
            }
        }

        List<MrpResponse.Line> lines = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : required.entrySet()) {
            StoreItem item = itemRepository.findById(e.getKey()).orElse(null);
            if (item == null) {
                continue;
            }
            BigDecimal req = scale(e.getValue());
            BigDecimal avail = scale(storeService.available(item.getId()));
            BigDecimal shortfall = req.subtract(avail);
            if (shortfall.signum() < 0) {
                shortfall = BigDecimal.ZERO;
            }
            String status = shortfall.signum() == 0 ? "OK" : (avail.signum() > 0 ? "PARTIAL" : "SHORT");
            lines.add(new MrpResponse.Line(item.getId(), item.getItemCode(), item.getName(),
                    item.getItemType().name(), item.getUnit(), req, avail, shortfall, status));
        }
        return new MrpResponse(job.getId(), job.getJobDisplayId(), lines, warnings);
    }

    private void explodeSfg(StoreItem sfg, String colour, String size, BigDecimal sfgNeeded,
                            Map<Long, BigDecimal> required, List<String> warnings) {
        var sfgBom = bomService.resolveBom(sfg.getId(), colour, size);
        if (sfgBom.isEmpty()) {
            warnings.add("No SFG BOM for " + sfg.getItemCode());
            return;
        }
        Bom bom = sfgBom.get();
        BigDecimal batch = bom.getBatchQty();
        for (BomComponent comp : bom.getComponents()) {
            BigDecimal rmPerSfg = comp.getQuantity().divide(batch, 6, RoundingMode.HALF_UP);
            add(required, comp.getComponentItem().getId(), rmPerSfg.multiply(sfgNeeded));
        }
    }

    private static void add(Map<Long, BigDecimal> map, Long key, BigDecimal value) {
        map.merge(key, value, BigDecimal::add);
    }

    private static BigDecimal scale(BigDecimal v) {
        return v.setScale(3, RoundingMode.HALF_UP);
    }
}
