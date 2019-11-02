package ru.bgcrm.test.exp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class PDFFormItext
{
	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main( String[] args )
	    throws IOException, DocumentException
	{
		String fileIn = "/home/shamil/tmp/kazan_zakaz_inet.pdf";
		String fileOut = "/home/shamil/tmp/kazan_zakaz_inet_edited.pdf";

		/*String fileIn = "/home/shamil/tmp/test_nk.pdf";
		String fileOut = "/home/shamil/tmp/test_nk_edited.pdf";*/

		//		  PrintStream out = new PrintStream(new FileOutputStream( fileI ));
		/*PdfReader reader = new PdfReader( fileIn );
		AcroFields form = reader.getAcroFields();
		XfaForm xfa = form.getXfa();
		System.out.println( xfa.isXfaPresent() ? "XFA form" : "AcroForm" );
		Set<String> fields = form.getFields().keySet();
		for( String key : fields )
		{
			System.out.println( "Name:" + key );
		}*/

		final BaseFont bf = BaseFont.createFont( "jar:" + PDFFormItext.class.getResource( "/ru/bitel/fonts/arial.ttf" ).getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED );

		PdfReader reader = new PdfReader( fileIn );
		PdfStamper stamper = new PdfStamper( reader, new FileOutputStream( fileOut ) );

		AcroFields fields = stamper.getAcroFields();
		fields.addSubstitutionFont( bf );
		/*for( String key : fields.getFields().keySet() )
		{
			System.out.println( key + " => " + fields.getField( key ) );
			fields.setField( key, "Пася" );
		}*/
		
		String states[] = fields.getAppearanceStates( "toggle_5" );
		System.out.println( Arrays.toString( states ) );
		
		//fields.getField( "toggle_5" );
		fields.setField( "toggle_5", "On" );
		

		//fields.setField( "P0.zp_23ob.F14b_4_1__l_", "Пася" );

		/*form.setField( "movies[0].movie[0].imdb[0]", "1075110" );
		form.setField( "movies[0].movie[0].duration[0]", "108" );
		form.setField( "movies[0].movie[0].title[0]", "The Misfortunates" );
		form.setField( "movies[0].movie[0].original[0]", "De helaasheid der dingen" );
		form.setField( "movies[0].movie[0].year[0]", "2009" );*/

		stamper.setFormFlattening( true );
		stamper.close();
	}

}
