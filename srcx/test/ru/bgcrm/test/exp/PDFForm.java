package ru.bgcrm.test.exp;

/*
import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextbox;

import com.itextpdf.text.pdf.AcroFields;

public class PDFForm
{
	public static void main( String[] args )
	    throws IOException, COSVisitorException
	{
		String fileIn = "/home/shamil/tmp/test_nk.pdf";
		String fileOut = "/home/shamil/tmp/test_nk_edited.pdf";
		
		String name = "";
		String value = "";
		
		try
		{
			PDDocument pdf = PDDocument.load( fileIn );

			PDDocumentCatalog docCatalog = pdf.getDocumentCatalog();
			
			PDAcroForm acroForm = docCatalog.getAcroForm();
			for( Object field : acroForm.getFields() )
			{
				if( field instanceof PDTextbox )
				{
					((PDTextbox)field).setValue( "тест" );
				}
				System.out.println( field + " " + field.getClass().getName() );  
			}
			
			//acroForm.getFields();
			
			//.setValue( value );

			pdf.save( fileOut );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
*/