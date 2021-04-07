package org.bgerp.plugin.report.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.util.TimeUtils;

public class RecordTest {
    @Test
    public void testJsonSerialize() throws Exception {
        var data = new Data(new Columns(
            new Column.ColumnInteger("id", null, null),
            new Column.ColumnString("title", "Title", null),
            new Column.ColumnDateTime("time", "Time", null, TimeUtils.FORMAT_TYPE_YMDHM)
        ));

        final var time = "07.04.2021 18:52";

        var record = data.addRecord();

        boolean thrown = false;
        try {
            record.add(new Date());
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Incorrect object class: 'java.util.Date' for column: 'id'", e.getMessage());
            thrown = true;
        }
        Assert.assertTrue(thrown);

        record.add(1);
        record.add("Test");
        record.add(TimeUtils.parse(time, TimeUtils.FORMAT_TYPE_YMDHM));

        thrown = false;
        try {
            record.add(TimeUtils.parse(time, TimeUtils.FORMAT_TYPE_YMDHM));
        } catch (IndexOutOfBoundsException e) {
            Assert.assertEquals("Too many column values", e.getMessage());
            thrown = true;
        }
        Assert.assertTrue(thrown);

        Assert.assertEquals(1, record.get("id"));
        Assert.assertEquals("Test", record.get("title"));

        thrown = false;
        try {
            Assert.assertNull(record.get("zdss"));
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Column not found: zdss", e.getMessage());
            thrown = true;
        }
        Assert.assertTrue(thrown);

        Assert.assertEquals(time, record.getString("time"));

        var json = new ObjectMapper().writeValueAsString(List.of(record));
        Assert.assertEquals("[{\"title\":\"Test\",\"time\":\"" + time + "\"}]", json);
    }
}
