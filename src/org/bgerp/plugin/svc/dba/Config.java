package org.bgerp.plugin.svc.dba;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.svc.dba.model.TableStatus;

public class Config extends org.bgerp.app.cfg.Config {
    private static final DateTimeFormatter PATTERN_YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    private final int cleanupMonthTablesOlderThanMonths;

    protected Config(ConfigMap config, boolean validate) {
        super(null);
        config = config.sub(Plugin.ID + ":");
        this.cleanupMonthTablesOlderThanMonths = config.getInt("cleanup.month.tables.older.than.months", 12);
    }

    /**
     * Sets drop candidates for table list.
     * @param tables
     * @param now
     */
    public void dropCandidates(List<TableStatus> tables, YearMonth now) {
        YearMonth borderYearMonth = now.minusMonths(cleanupMonthTablesOlderThanMonths);
        for (TableStatus table : tables)
            table.setDropCandidate(isDropCandidate(table.getName(), borderYearMonth));
    }

    private boolean isDropCandidate(String name, YearMonth borderYearMonth) {
        if (name.startsWith("_"))
            return true;

        try {
            YearMonth yearMonth = YearMonth.parse(StringUtils.substringAfterLast(name, "_"), PATTERN_YYYYMM);
            return yearMonth.compareTo(borderYearMonth) < 0;

        } catch (DateTimeParseException e) {}

        return false;
    }
}
