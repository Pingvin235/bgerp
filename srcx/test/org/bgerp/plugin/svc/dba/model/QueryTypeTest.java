package org.bgerp.plugin.svc.dba.model;

import org.junit.Assert;
import org.junit.Test;

public class QueryTypeTest {
    @Test
    public void testOf() {
        Assert.assertEquals(QueryType.SELECT, QueryType.of("Select * from table"));
        Assert.assertEquals(QueryType.SELECT, QueryType.of(" SeLect * from table"));
        Assert.assertEquals(QueryType.INSERT, QueryType.of("insert into a SeLect * from b"));
        Assert.assertNull(QueryType.of(" SeeLect * from table"));
        Assert.assertEquals(QueryType.DROP, QueryType.of(" drop table1"));
    }
}
