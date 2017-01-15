import java.io.OutputStream;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.text.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.PDResources;

import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.IntegerValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

/**
 * This is an example on how to merge PDF documents.
 *
 * @author Christian H <chadilukito@gmail.com>
 */
public final class MergePdf
{
    /**
     * Default constructor.
     */
    private MergePdf()
    {
        //example class should not be instantiated
    }

    /**
     *
     * @param args The command line arguments.
     *
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException
    {
        if( args.length != 2 )
        {
            usage();
        }
        else
        {
            try
            {
                String[] infiles = args[0].split(";");
                if (infiles.length < 2) {
                    System.err.println( "Error: Need more than 1 file to combine." );
                    System.exit( 1 );
                } else {
                    _Merge(infiles, args[1]);
                }
            }
            finally { }
        }
    }
    
        
    private static void _Merge(String[] infiles, String outfile) throws IOException
    {
        PDDocument[] document = new PDDocument[infiles.length];
        PDDocument documentOut = document[0];
        
        PDFMergerUtility ut = new PDFMergerUtility();        
        for (int i=1; i < infiles.length; i++) {
            ut.appendDocument(documentOut, document[i]);
        }

        documentOut.save(outfile);
    }
    
    /**
     * This will print the usage for this document.
     */
    private static void usage()
    {
        System.err.println( "Usage: java " + MergePdf.class.getName() + " <input-pdf[;input-pdf...]> <output-pdf>" );
    }

}