package ru.bgcrm.model.process;

import org.junit.Assert;
import org.junit.Test;


public class TypePropertiesTest {
    @Test
    public void testTransactionPropertiesGet() {
        var props = transactionPropertiesCreate();
        transactionPropertiesCheck(props);
    }

    @Test
    public void testTransactionPropertiesSerialize() {
        var data = transactionPropertiesCreate().serializeToData();
        Assert.assertTrue(data.contains("transaction.1-2.enable=1"));
        Assert.assertTrue(data.contains("transaction.3-2.enable=0"));
        var props = new TypeProperties(data, null, null);
        transactionPropertiesCheck(props);
    }

    private TypeProperties transactionPropertiesCreate() {
        var props = new TypeProperties();
        props.setTransactionProperties(1, 2, true);
        props.setTransactionProperties(3, 2, false);
        return props;
    }

    private void transactionPropertiesCheck(TypeProperties props) {
        var tp = props.getTransactionProperties(1, 2);
        Assert.assertNotNull(tp);
        Assert.assertTrue(tp.isEnable());

        tp = props.getTransactionProperties(3, 2);
        Assert.assertNotNull(tp);
        Assert.assertFalse(tp.isEnable());

        // not defined properties - returns true
        tp = props.getTransactionProperties(7, 8);
        Assert.assertNotNull(tp);
        Assert.assertTrue(tp.isEnable());
    }
}
