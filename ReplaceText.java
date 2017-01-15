import java.io.OutputStream;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.contentstream.operator.Operator;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.*;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

/**
 * This is an example on how to remove text from PDF document.
 *
 * Note: 
 * ------------
 * Because of nature of the PDF structure itself, actually this will not work 100% able to find text that need to be replaced.
 * There are other solutions for that, for example using PDFTextStripper.
 *
 * @author Christian H <chadilukito@gmail.com>
 */
public final class ReplaceText
{
    /**
     * Default constructor.
     */
    private ReplaceText()
    {
        //example class should not be instantiated
    }

    /**
     * This will remove all text from a PDF document.
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException
    {
        if( args.length != 4 )
        {
            usage();
        }
        else
        {
            PDDocument document = null;
            try
            {
                document = PDDocument.load( new File(args[2]) );
                if( document.isEncrypted() )
                {
                    System.err.println( "Error: Encrypted documents are not supported for this example." );
                    System.exit( 1 );
                }
                
                System.out.println(args[0] + " => "+ args[1]);
                
                document = _ReplaceText(document, args[0], args[1]);                
                document.save( args[3] );
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }
    
        
    private static PDDocument _ReplaceText(PDDocument document, String searchString, String replacement) throws IOException
    {
        if (StringUtils.isEmpty(searchString) || StringUtils.isEmpty(replacement)) {
            return document;
        }
        
        for ( PDPage page : document.getPages() )
        {
            PDFStreamParser parser = new PDFStreamParser(page);
            parser.parse();
            List tokens = parser.getTokens();
            
            for (int j = 0; j < tokens.size(); j++) 
            {
                Object next = tokens.get(j);
                if (next instanceof Operator) 
                {
                    Operator op = (Operator) next;
                    
                    String pstring = "";
                    int prej = 0;
                    
                    //Tj and TJ are the two operators that display strings in a PDF
                    if (op.getName().equals("Tj")) 
                    {
                        // Tj takes one operator and that is the string to display so lets update that operator
                        COSString previous = (COSString) tokens.get(j - 1);
                        String string = previous.getString();
                        string = string.replaceFirst(searchString, replacement);
                        previous.setValue(string.getBytes());
                    } else 
                    if (op.getName().equals("TJ")) 
                    {
                        COSArray previous = (COSArray) tokens.get(j - 1);
                        for (int k = 0; k < previous.size(); k++) 
                        {
                            Object arrElement = previous.getObject(k);
                            if (arrElement instanceof COSString) 
                            {
                                COSString cosString = (COSString) arrElement;
                                String string = cosString.getString();
                                
                                if (j == prej) {
                                    pstring += string;
                                } else {
                                    prej = j;
                                    pstring = string;
                                }
                            }                       
                        }                        
                        

                        if (searchString.equals(pstring.trim())) 
                        {                            
                            COSString cosString2 = (COSString) previous.getObject(0);
                            cosString2.setValue(replacement.getBytes());                           

                            int total = previous.size()-1;    
                            for (int k = total; k > 0; k--) {
                                previous.remove(k);
                            }                            
                        }
                    }
                }
            }
            
            // now that the tokens are updated we will replace the page content stream.
            PDStream updatedStream = new PDStream(document);
            OutputStream out = updatedStream.createOutputStream(COSName.FLATE_DECODE);
            ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
            tokenWriter.writeTokens(tokens);            
            out.close();
            page.setContents(updatedStream);
        }

        return document;
    }
    
    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java " + ReplaceText.class.getName() + " <old-text> <new-text> <input-pdf> <output-pdf>" );
    }

}