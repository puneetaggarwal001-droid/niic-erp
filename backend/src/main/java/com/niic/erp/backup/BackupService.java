package com.niic.erp.backup;

import com.niic.erp.common.BadRequestException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Whole-database JSON backup and restore. Works at the JDBC level (not through
 * JPA) so it captures every column of every table without lazy-loading or
 * entity-graph concerns. The legacy app dumped its localStorage/Firestore state;
 * this is the relational equivalent.
 *
 * <p>Restore is destructive: it deletes and re-inserts every table's rows inside
 * one transaction, with referential integrity temporarily disabled so table
 * order does not matter. It is admin-only and validates table names against the
 * live schema to avoid injection via a crafted payload.
 */
@Service
public class BackupService {

    // Never export/restore Flyway's own bookkeeping — it must reflect the running jars.
    private static final Set<String> EXCLUDED = Set.of("flyway_schema_history");

    private final JdbcTemplate jdbc;

    public BackupService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** A full snapshot: {version, exportedAt, tables: {tableName: [row, …]}}. */
    @Transactional(readOnly = true)
    public Map<String, Object> export() {
        Map<String, Object> tables = new LinkedHashMap<>();
        long totalRows = 0;
        for (String table : tableNames()) {
            List<Map<String, Object>> rows = jdbc.queryForList("select * from " + table);
            tables.put(table, rows);
            totalRows += rows.size();
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("version", 1);
        out.put("exportedAt", Instant.now().toString());
        out.put("tableCount", tables.size());
        out.put("rowCount", totalRows);
        out.put("tables", tables);
        return out;
    }

    /** Replace all data with the snapshot's rows. Destructive; wrapped in one transaction. */
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> restore(Map<String, Object> snapshot) {
        Object rawTables = snapshot != null ? snapshot.get("tables") : null;
        if (!(rawTables instanceof Map)) {
            throw new BadRequestException("Backup payload must contain a 'tables' object.");
        }
        Map<String, Object> tables = (Map<String, Object>) rawTables;
        Set<String> known = tableNames();

        // Reject unknown tables up front rather than partway through the restore.
        for (String name : tables.keySet()) {
            if (!known.contains(name.toLowerCase())) {
                throw new BadRequestException("Unknown table in backup: " + name);
            }
        }

        jdbc.execute("set referential_integrity false");
        try {
            int restored = 0;
            for (String table : known) {
                jdbc.update("delete from " + table);
                Object rawRows = tables.get(table);
                if (!(rawRows instanceof List<?> rows)) {
                    continue;
                }
                for (Object rawRow : rows) {
                    if (rawRow instanceof Map) {
                        insertRow(table, (Map<String, Object>) rawRow);
                        restored++;
                    }
                }
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("restoredTables", tables.size());
            result.put("restoredRows", restored);
            return result;
        } finally {
            jdbc.execute("set referential_integrity true");
        }
    }

    private void insertRow(String table, Map<String, Object> row) {
        if (row.isEmpty()) {
            return;
        }
        List<String> cols = row.keySet().stream().toList();
        String columnList = String.join(", ", cols);
        String placeholders = cols.stream().map(c -> "?").collect(Collectors.joining(", "));
        Object[] values = cols.stream().map(row::get).toArray();
        jdbc.update("insert into " + table + " (" + columnList + ") values (" + placeholders + ")", values);
    }

    /** Base tables in the current schema, lower-cased, excluding Flyway's history. */
    private Set<String> tableNames() {
        List<String> names = jdbc.queryForList(
                "select table_name from information_schema.tables "
                        + "where table_type = 'BASE TABLE' and table_schema in ('PUBLIC', 'public')",
                String.class);
        return names.stream()
                .map(String::toLowerCase)
                .filter(n -> !EXCLUDED.contains(n))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}
