package ru.bgcrm.test.exp;

/*
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFTextStripper;

import com.itextpdf.text.DocumentException;

public class PDFTextStringReplace
{
	*
	 * Main method.
	 * @param args no arguments needed
	 * @throws DocumentException 
	 * @throws IOException
	 * @throws COSVisitorException 
	 *
	public static void main( String[] args )
	    throws DocumentException, IOException, COSVisitorException
	{
		String fileIn = "/home/shamil/tmp/pdf-test.pdf";
		String fileOut = "/home/shamil/tmp/pdf-test_edited.pdf";

		// the document
		//PDDocument doc = null;
		try
		{

			PDFParser parser = new PDFParser( new FileInputStream( fileIn ) );

			parser.parse();

			COSDocument cosDoc = parser.getDocument();
			PDFTextStripper pdfStripper = new PDFTextStripper();
			PDDocument pdDoc = new PDDocument( cosDoc );
			String parsedText = pdfStripper.getText( pdDoc );

			System.out.println( parsedText );

			*        	
			
			PDFParser docParser = new PDFParser( new FileInputStream( fileIn ) );
			
			doc = docParser.getDocument();
			PDFTextStripper pdfStripper = new PDFTextStripper();
			pdDoc = new PDDocument( cosDoc );
			parsedText = pdfStripper.getText( pdDoc );
			
			
			//doc = PDDocument.load( fileIn );
			
			List<?> pages = pdDoc.getDocumentCatalog().getAllPages();
			for( int i = 0; i < pages.size(); i++ )
			{
				PDPage page = (PDPage)pages.get( i );
				PDStream contents = page.getContents();
				PDFStreamParser p = new PDFStreamParser( contents.getStream() );
				p.parse();
				List<?> tokens = p.getTokens();
				for( int j = 0; j < tokens.size(); j++ )
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
							COSString previous = (COSString)tokens.get( j - 1 );
							String string = previous.getString();
							string = string.replaceFirst( "Please", "test test" );
							previous.reset();
							previous.append( string.getBytes( "ISO-8859-1" ) );
						}
						else if( op.getOperation().equals( "TJ" ) )
						{
							COSArray previous = (COSArray)tokens.get( j - 1 );
							for( int k = 0; k < previous.size(); k++ )
							{
								Object arrElement = previous.getObject( k );
								if( arrElement instanceof COSString )
								{
									COSString cosString = (COSString)arrElement;
									String string = cosString.getString();
									string = string.replaceFirst( "Please", "test test" );
									cosString.reset();
									cosString.append( string.getBytes( "ISO-8859-1" ) );
								}
							}
						}
					}
				}
				//now that the tokens are updated we will replace the
				//page content stream.
				PDStream updatedStream = new PDStream( pdDoc );
				OutputStream out = updatedStream.createOutputStream();
				ContentStreamWriter tokenWriter = new ContentStreamWriter( out );
				tokenWriter.writeTokens( tokens );
				page.setContents( updatedStream );
			}
			//doc.save( fileOut );
		}
		finally
		{
			*if( doc != null )
			{
				doc.close();
			}*
		}

		*PdfReader reader = new PdfReader( fileIn );
		byte[] streamBytes = reader.getPageContent( 1 );
		StringBuffer buf = new StringBuffer();
		String contentStream = new String( streamBytes );
		String searchString = "ул.";
		int pos = contentStream.indexOf( searchString );
		buf.append( contentStream.substring( 0, pos ) );
		buf.append( "XXXX" );
		buf.append( contentStream.substring( pos + searchString.length() + 1 ) );
		String modifiedString = buf.toString();
		PdfStamper stamper = new PdfStamper( reader, new FileOutputStream( fileOut ) );
		reader.setPageContent( 1, modifiedString.getBytes() );
		stamper.close();*

		*PdfReader reader = new PdfReader( fileIn );
		PdfStamper stamper = new PdfStamper( reader, new FileOutputStream( fileOut ) );
		
		reader.getAcroFields();
		
		reader.get*

		/*PdfContentByte canvas = stamper.getOverContent( 1 );
		ColumnText.showTextAligned( canvas, Element.ALIGN_LEFT, new Phrase( "Hello people!" ), 36, 540, 0 );
		stamper.close();*	
	}

	*
	 * Manipulates a PDF file src with the file dest as result
	 * @param src the original PDF
	 * @param dest the resulting PDF
	 * @throws IOException
	 * @throws DocumentException
	 
	public static void stampIgnoreRotation( String src, String dest )
	    throws IOException, DocumentException
	{
		PdfReader reader = new PdfReader( src );
		PdfStamper stamper = new PdfStamper( reader, new FileOutputStream( dest ) );
		stamper.setRotateContents( false );
		PdfContentByte canvas = stamper.getOverContent( 1 );
		ColumnText.showTextAligned( canvas, Element.ALIGN_LEFT, new Phrase( "Hello people!" ), 36, 540, 0 );
		stamper.close();
	}
	*
}
*/