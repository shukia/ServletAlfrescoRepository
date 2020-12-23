/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.content.transform;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * @see org.alfresco.repo.content.transform.HtmlParserContentTransformer
 * 
 * @author Derek Hulley
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class HtmlParserContentTransformerTest extends AbstractContentTransformerTest
{
    private HtmlParserContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        transformer = new HtmlParserContentTransformer();
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
        transformer.afterPropertiesSet();
    }
    
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }

    public void testSetUp() throws Exception
    {
        assertNotNull(transformer);
    }
    
    public void checkIsTransformable() throws Exception
    {
        // check reliability
        boolean reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_HTML, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions());
        assertTrue(reliability);   // plain text to plain text is supported
        
        // check other way around
        reliability = transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_HTML, new TransformationOptions());
        assertFalse(reliability);   // plain text to plain text is not supported
    }
 
    /**
     * Checks that we correctly handle text in different encodings,
     *  no matter if the encoding is specified on the Content Property
     *  or in a meta tag within the HTML itself. (ALF-10466)
     *  
     *  On Windows, org.htmlparser.beans.StringBean.carriageReturn() appends a new system dependent new line
     *  so we must be careful when checking the returned text
     */
    public void testEncodingHandling() throws Exception
    {
        final String NEWLINE = System.getProperty ("line.separator");
        final String TITLE = "Testing!";
        final String TEXT_P1 = "This is some text in English";
        final String TEXT_P2 = "This is more text in English";
        final String TEXT_P3 = "C'est en Fran\u00e7ais et Espa\u00f1ol";
        String partA = "<html><head><title>" + TITLE + "</title></head>" + NEWLINE;
        String partB = "<body><p>" + TEXT_P1 + "</p>" + NEWLINE + 
                             "<p>" + TEXT_P2 + "</p>" + NEWLINE + 
                             "<p>" + TEXT_P3 + "</p>" + NEWLINE;
        String partC = "</body></html>";
        final String expected = TITLE + NEWLINE + TEXT_P1 + NEWLINE + TEXT_P2 + NEWLINE + TEXT_P3 + NEWLINE;
        
        ContentWriter content;
        ContentWriter dest;
        File tmpS = null;
        File tmpD = null;

        try
        {
            // Content set to ISO 8859-1
            tmpS = File.createTempFile("test", ".html");
            content = new FileContentWriter(tmpS);
            content.setEncoding("ISO-8859-1");
            content.setMimetype(MimetypeMap.MIMETYPE_HTML);
            content.putContent(partA+partB+partC);
            
            tmpD = File.createTempFile("test", ".txt");
            dest = new FileContentWriter(tmpD);
            dest.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            
            transformer.transform(content.getReader(), dest);
           
            assertEquals(expected, dest.getReader().getContentString());
            tmpS.delete();
            tmpD.delete();
        
            
            // Content set to UTF-8
            tmpS = File.createTempFile("test", ".html");
            content = new FileContentWriter(tmpS);
            content.setEncoding("UTF-8");
            content.setMimetype(MimetypeMap.MIMETYPE_HTML);
            content.putContent(partA+partB+partC);
            
            tmpD = File.createTempFile("test", ".txt");
            dest = new FileContentWriter(tmpD);
            dest.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            
            transformer.transform(content.getReader(), dest);
            assertEquals(expected, dest.getReader().getContentString());
            tmpS.delete();
            tmpD.delete();
            
            
            // Content set to UTF-16
            tmpS = File.createTempFile("test", ".html");
            content = new FileContentWriter(tmpS);
            content.setEncoding("UTF-16");
            content.setMimetype(MimetypeMap.MIMETYPE_HTML);
            content.putContent(partA+partB+partC);
            
            tmpD = File.createTempFile("test", ".txt");
            dest = new FileContentWriter(tmpD);
            dest.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            
            transformer.transform(content.getReader(), dest);
            assertEquals(expected, dest.getReader().getContentString());
            tmpS.delete();
            tmpD.delete();
            
            // Note - since HTML Parser 2.0 META tags specifying the
            // document encoding will ONLY be respected if the original
            // content type was set to ISO-8859-1.
            //
            // This means there is now only one test which we can perform
            // to ensure that this now-limited overriding of the encoding
            // takes effect.
            
            // Content set to ISO 8859-1, meta set to UTF-8
            tmpS = File.createTempFile("test", ".html");
            content = new FileContentWriter(tmpS);
            content.setMimetype(MimetypeMap.MIMETYPE_HTML);
            String str = partA+
                         "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                         partB+partC;
            content.putContent(new ByteArrayInputStream(str.getBytes("UTF-8")));
            content.setEncoding("ISO-8859-1");
            
            tmpD = File.createTempFile("test", ".txt");
            dest = new FileContentWriter(tmpD);
            dest.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            
            transformer.transform(content.getReader(), dest);
            assertEquals(expected, dest.getReader().getContentString());
            tmpS.delete();
            tmpD.delete();
            
            
            // Note - we can't test UTF-16 with only a meta encoding,
            //  because without that the parser won't know about the 
            //  2 byte format so won't be able to identify the meta tag
        }
        finally
        {
            if (tmpS != null && tmpS.exists()) tmpS.delete();
            if (tmpD != null && tmpD.exists()) tmpD.delete();
        }
    }
}
