package ru.bgcrm.test.exp;

/*
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;

public class PDFStringReplace
{
	public static void main( String[] args ) 
		throws IOException, COSVisitorException
    {
		String fileIn = "/home/shamil/tmp/pdf-test.pdf";
		String fileOut = "/home/shamil/tmp/pdf-test_edited.pdf";
		
		 // the document
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load( fileIn );
            List pages = doc.getDocumentCatalog().getAllPages();
            for( int i=0; i<pages.size(); i++ )
            {
                PDPage page = (PDPage)pages.get( i );
                PDStream contents = page.getContents();
                PDFStreamParser parser = new PDFStreamParser(contents.getStream() );
                parser.parse();
                List tokens = parser.getTokens();
                for( int j=0; j<tokens.size(); j++ )
                {
                    Object next = tokens.get( j );
                    if( next instanceof PDFOperator )
                    {
                        PDFOperator op = (PDFOperator)next;
                        //Tj and TJ are the two operators that display
                        //strings in a PDF
                        if( op.getOperation().equals( "Tj" ) )
                        {
                            //Tj takes one operator and that is the string
                            //to display so lets update that operator
                            COSString previous = (COSString)tokens.get( j-1 );
                            String string = previous.getString();
                            string = string.replaceFirst( "PDF", " test test " );
                            previous.reset();
                            previous.append( string.getBytes("ISO-8859-1") );
                        }
                        else if( op.getOperation().equals( "TJ" ) )
                        {
                            COSArray previous = (COSArray)tokens.get( j-1 );
                            for( int k=0; k<previous.size(); k++ )
                            {
                                Object arrElement = previous.getObject( k );
                                if( arrElement instanceof COSString )
                                {
                                    COSString cosString = (COSString)arrElement;
                                    String string = cosString.getString();
                                    string = string.replaceFirst( "PDF", " test test " );
                                    cosString.reset();
                                    cosString.append( string.getBytes("ISO-8859-1") );
                                }
                            }
                        }
                    }
                }
                //now that the tokens are updated we will replace the
                //page content stream.
                PDStream updatedStream = new PDStream(doc);
                OutputStream out = updatedStream.createOutputStream();
                ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
                tokenWriter.writeTokens( tokens );
                page.setContents( updatedStream );
            }
            doc.save( fileOut );
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }

    }
}
*/