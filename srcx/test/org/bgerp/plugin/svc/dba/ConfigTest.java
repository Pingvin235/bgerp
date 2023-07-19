package org.bgerp.plugin.svc.dba;

import java.time.YearMonth;
import java.util.List;

import org.bgerp.app.cfg.SimpleConfigMap;
import org.bgerp.plugin.svc.dba.model.TableStatus;
import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {
    @Test
    public void testDropCandidates() {
        var table1 = new TableStatus();
        table1.setName("_some_old_table");

        var table2 = new TableStatus();
        table2.setName("normal_table");

        var table3 = new TableStatus();
        table3.setName("log_table_202203");

        var table4 = new TableStatus();
        table4.setName("log_table_202111");

        var table5 = new TableStatus();
        table5.setName("log_table_202102");

        var tables = List.of(table1, table2, table3, table4, table5);

        // default configuration, 12 months
        SimpleConfigMap.of().getConfig(Config.class).dropCandidates(tables, YearMonth.parse("2022-04"));
        Assert.assertTrue(table1.isDropCandidate());
        Assert.assertFalse(table2.isDropCandidate());
        Assert.assertFalse(table3.isDropCandidate());
        Assert.assertFalse(table4.isDropCandidate());
        Assert.assertTrue(table5.isDropCandidate());

        // 1 month
        SimpleConfigMap.of(Plugin.ID + ":cleanup.month.tables.older.than.months", 1).getConfig(Config.class).dropCandidates(tables, YearMonth.parse("2022-04"));
        Assert.assertTrue(table1.isDropCandidate());
        Assert.assertFalse(table2.isDropCandidate());
        Assert.assertFalse(table3.isDropCandidate());
        Assert.assertTrue(table4.isDropCandidate());
        Assert.assertTrue(table5.isDropCandidate());
    }
}
