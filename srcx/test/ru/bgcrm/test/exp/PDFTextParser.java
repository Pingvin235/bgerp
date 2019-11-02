package ru.bgcrm.test.exp;

/*
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

public class PDFTextParser
{
	// PDFTextParser Constructor 
	public PDFTextParser()
	{}

	// Extract text from PDF Document
	String pdftoText( String fileName )
	{
		try
		{
			PDFParser parser = new PDFParser( new FileInputStream( fileName ) );
			parser.parse();
			
			COSDocument cosDoc = parser.getDocument();
			PDFTextStripper pdfStripper = new PDFTextStripper();
			PDDocument pdDoc = new PDDocument( cosDoc );
			
			return pdfStripper.getText( pdDoc );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return null;
	}

	// Write the parsed text from PDF to a file
	void writeTexttoFile( String pdfText, String fileName )
	{

		System.out.println( "\nWriting PDF text to output text file " + fileName + "...." );
		try
		{
			PrintWriter pw = new PrintWriter( fileName );
			pw.print( pdfText );
			pw.close();
		}
		catch( Exception e )
		{
			System.out.println( "An exception occured in writing the pdf text to file." );
			e.printStackTrace();
		}
		System.out.println( "Done." );
	}

	//Extracts text from a PDF Document and writes it to a text file
	public static void main( String args[] )
	{
		args = new String[]{ "/home/shamil/tmp/test.pdf", "/home/shamil/tmp/test.pdf.txt" };
		
		if( args.length != 2 )
		{
			System.out.println( "Usage: java PDFTextParser  " );
			System.exit( 1 );
		}

		PDFTextParser pdfTextParserObj = new PDFTextParser();
		String pdfToText = pdfTextParserObj.pdftoText( args[0] );

		if( pdfToText == null )
		{
			System.out.println( "PDF to Text Conversion failed." );
		}
		else
		{
			System.out.println( "\nThe text parsed from the PDF Document....\n" + pdfToText );
			pdfTextParserObj.writeTexttoFile( pdfToText, args[1] );
		}
	}
}
*/