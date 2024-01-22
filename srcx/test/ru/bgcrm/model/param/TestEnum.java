package ru.bgcrm.model.param;

import static org.junit.Assert.assertEquals;

import org.bgerp.model.param.Parameter;
import org.bgerp.model.param.Parameter.Type;
import org.junit.Test;

public class TestEnum {
    @Test
    public void testEnum() {
        assertEquals(Type.BLOB, Parameter.Type.of(Parameter.TYPE_BLOB));
    }
}
