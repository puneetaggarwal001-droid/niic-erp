package com.niic.erp.backup;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backup")
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> export() {
        Map<String, Object> snapshot = backupService.export();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"niic-erp-backup.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(snapshot);
    }

    @PostMapping("/restore")
    public Map<String, Object> restore(@RequestBody Map<String, Object> snapshot) {
        return backupService.restore(snapshot);
    }
}
