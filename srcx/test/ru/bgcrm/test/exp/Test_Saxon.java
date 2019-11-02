package ru.bgcrm.test.exp;

import java.io.File;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Value;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.plugin.document.docgen.MathBitSetExtensionFunction;
import ru.bgcrm.util.XMLUtils;

public class Test_Saxon
{
	public static void main( String[] args )
	{
		try
		{
			Processor proc = new Processor( false );
			proc.registerExtensionFunction( new TestExtension() );
			proc.registerExtensionFunction( new TestExtensionXml() );
			proc.registerExtensionFunction( new MathBitSetExtensionFunction() );

			XsltCompiler comp = proc.newXsltCompiler();
			XsltExecutable templates = comp.compile( new StreamSource( new File( "docpattern/test.xsl" ) ) );
			XsltTransformer transformer = templates.load();

			XdmNode source = proc.newDocumentBuilder().build( new StreamSource( new File( "docpattern/test.xml" ) ) );
			Serializer out = proc.newSerializer( System.out );
			transformer.setDestination( out );
			proc.writeXdmValue( source, transformer );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("serial")
    private static class TestExtension
	    extends ExtensionFunctionDefinition
	{
		@Override
		public StructuredQName getFunctionQName()
		{
			return new StructuredQName( "bgcrm", "http://bgcrm.ru/saxon-extension", "test" );
		}

		@Override
		public SequenceType[] getArgumentTypes()
		{
			return new SequenceType[] { SequenceType.SINGLE_INTEGER, SequenceType.SINGLE_INTEGER };
		}

		@Override
		public SequenceType getResultType( SequenceType[] suppliedArgumentTypes )
		{
			return SequenceType.SINGLE_INTEGER;
		}

		@Override
		public ExtensionFunctionCall makeCallExpression()
		{
			return new ExtensionFunctionCall()
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
                public SequenceIterator call( SequenceIterator[] arguments, XPathContext context )
				    throws XPathException
				{
					long v0 = ((IntegerValue)arguments[0].next()).longValue();
					long v1 = ((IntegerValue)arguments[1].next()).longValue();
					long result = v0 << v1;
					return Value.asIterator( Int64Value.makeIntegerValue( result ) );
				}
			};
		}
	}
	
	@SuppressWarnings("serial")
    private static class TestExtensionXml
	    extends ExtensionFunctionDefinition
	{
		@Override
		public StructuredQName getFunctionQName()
		{
			return new StructuredQName( "bgcrm", "http://bgcrm.ru/saxon-extension", "testXml" );
		}
	
		@Override
		public SequenceType[] getArgumentTypes()
		{
			return new SequenceType[] { SequenceType.SINGLE_INTEGER };
		}
	
		@Override
		public SequenceType getResultType( SequenceType[] suppliedArgumentTypes )
		{
			return SequenceType.SINGLE_NODE;
		}
	
		@Override
		public ExtensionFunctionCall makeCallExpression()
		{
			return new ExtensionFunctionCall()
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
                public SequenceIterator call( SequenceIterator[] arguments, XPathContext ctx )
				    throws XPathException
				{
					/*long v0 = ((IntegerValue)arguments[0].next()).longValue();
					long v1 = ((IntegerValue)arguments[1].next()).longValue();
					long result = v0 << v1;*/
					
					Document doc = XMLUtils.newDocument();
					Element testEl = doc.createElement( "test" );
					doc.appendChild( testEl );
					
					Element tttEl = doc.createElement( "ttt" );
					testEl.appendChild( tttEl );
					
					tttEl.setAttribute( "attr", "value" );
					
					
					return SingletonIterator.makeIterator( new DocumentWrapper( doc, "", new Configuration() ) );
					                                                        
					//return Value.asIterator( Int64Value.makeIntegerValue( result ) );
				}
			};
		}
	}
}
