package ru.bgcrm.plugin.document.docgen;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Value;

public class MathBitSetExtensionFunction
	extends ExtensionFunctionDefinition
{
	public MathBitSetExtensionFunction()
	{}
	
	@Override
	public StructuredQName getFunctionQName()
	{
		return new StructuredQName( "bgcrm", "http://bgcrm.ru/saxon-extension-math", "isbitset" );
	}

	@Override
    public SequenceType[] getArgumentTypes()
    {
		return new SequenceType[] { SequenceType.SINGLE_INTEGER, SequenceType.SINGLE_INTEGER };
    }

	@Override
    public SequenceType getResultType( SequenceType[] suppliedArgumentTypes )
    {
		return SequenceType.SINGLE_BOOLEAN;
    }

	@SuppressWarnings("serial")
	@Override
	public ExtensionFunctionCall makeCallExpression()
	{
		return new ExtensionFunctionCall()
		{
			@SuppressWarnings({ "rawtypes", "unchecked" })
            public SequenceIterator call( SequenceIterator[] arguments, XPathContext ctx )
			    throws XPathException
			{
				try
                {
					long value = ((IntegerValue)arguments[0].next()).longValue();
					int bitPos = (int)((IntegerValue)arguments[1].next()).longValue();
					
					return Value.asIterator( BooleanValue.get( ( value & ( 1L << bitPos ) ) > 0 ) );
                }
                catch( Exception e )
                {
	                throw new XPathException( e );
                }
			}
		};
	}
}