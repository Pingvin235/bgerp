package ru.bgcrm.model.param;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ru.bgcrm.model.param.Parameter.Type;

public class TestEnum {
    @Test
    public void testEnum() {
        assertEquals(Type.BLOB, Parameter.Type.of(Parameter.TYPE_BLOB));
    }
}
