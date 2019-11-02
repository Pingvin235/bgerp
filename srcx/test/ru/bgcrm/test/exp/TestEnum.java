package ru.bgcrm.test.exp;

import org.junit.Test;
import static org.junit.Assert.*;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.Parameter.Type;

public class TestEnum
{
	@Test
	public void testEnum() {
		assertEquals( Type.BLOB, Parameter.Type.fromString( Parameter.TYPE_BLOB ) );
	}
}
