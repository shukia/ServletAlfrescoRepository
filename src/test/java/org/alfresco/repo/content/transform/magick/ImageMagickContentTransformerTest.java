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
package org.alfresco.repo.content.transform.magick;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ProxyContentTransformer;
import org.alfresco.repo.content.transform.RemoteTransformerClient;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.PagedSourceOptions;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.TransformationSourceOptions;
import org.alfresco.util.TempFileProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @see org.alfresco.repo.content.transform.magick.ImageMagickContentTransformerWorker
 * 
 * @author Derek Hulley
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class ImageMagickContentTransformerTest extends AbstractContentTransformerTest
{
    private ProxyContentTransformer transformer;

    @BeforeClass
    public static void before()
    {
        // Use the docker images for transforms (legacy)
        System.setProperty("alfresco-pdf-renderer.url", "http://localhost:8090/");
        System.setProperty("img.url", "http://localhost:8090/");
        System.setProperty("jodconverter.url", "http://localhost:8090/");
        System.setProperty("tika.url", "http://localhost:8090/");
        System.setProperty("transform.misc.url", "http://localhost:8090/");
    }

    @AfterClass
    public static void after()
    {
        System.clearProperty("alfresco-pdf-renderer.url");
        System.clearProperty("img.url");
        System.clearProperty("jodconverter.url");
        System.clearProperty("tika.url");
        System.clearProperty("transform.misc.url");
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        // Setup a mimetype service that will return a truncated set of mimetypes
        MockMimetypeService testMimetypeService = new MockMimetypeService();
        testMimetypeService.setConfigService(((MimetypeMap) mimetypeService).getConfigService());
        testMimetypeService.setContentCharsetFinder(((MimetypeMap) mimetypeService).getContentCharsetFinder());
        testMimetypeService.init();
        this.mimetypeService = testMimetypeService;
        
        transformer = (ProxyContentTransformer) ctx.getBean("transformer.ImageMagick");
        RemoteTransformerClient remoteTransformerClient = new RemoteTransformerClient("miscRemoteTransformerClient", "http://localhost:8090/");
        transformer.getWorker().setRemoteTransformerClient(remoteTransformerClient);
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testReliability() throws Exception
    {
        if (!this.transformer.getWorker().isAvailable())
        {
            return;
        }
        boolean reliability = transformer.isTransformable(
                MimetypeMap.MIMETYPE_IMAGE_GIF, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions());
        assertEquals("Mimetype should not be supported", false, reliability);
        reliability = transformer.isTransformable(
                MimetypeMap.MIMETYPE_IMAGE_GIF, -1, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions());
        assertEquals("Mimetype should be supported", true, reliability);
    }
    
    protected long transform(String sourceMimetype, String targetMimetype, TransformationOptions options) throws IOException
    {
        long size = -1;
        String[] quickFiles = getQuickFilenames(sourceMimetype);
        for (String quickFile : quickFiles)
        {
            String sourceExtension = quickFile.substring(quickFile.lastIndexOf('.')+1);
            String targetExtension = mimetypeService.getExtension(targetMimetype);
            
            // is there a test file for this conversion?
            File sourceFile = AbstractContentTransformerTest.loadNamedQuickTestFile(quickFile);
            if (sourceFile == null)
            {
                continue;  // no test file available for that extension
            }
            ContentReader sourceReader = new FileContentReader(sourceFile);
            
            // make a writer for the target file
            File targetFile = TempFileProvider.createTempFile(
                    getClass().getSimpleName() + "_" + getName() + "_" + sourceExtension + "_",
                    "." + targetExtension);
            ContentWriter targetWriter = new FileContentWriter(targetFile);
            
            // do the transformation
            sourceReader.setMimetype(sourceMimetype);
            targetWriter.setMimetype(targetMimetype);
            transformer.transform(sourceReader.getReader(), targetWriter, options);
            ContentReader targetReader = targetWriter.getReader();
            size = targetReader.getSize();
            assertTrue(size > 0);
        }
        return size;
    }
    
    public void testGifToPng() throws Exception
    {
        ImageTransformationOptions options = new ImageTransformationOptions();
        transform(MimetypeMap.MIMETYPE_IMAGE_GIF, MimetypeMap.MIMETYPE_IMAGE_PNG, options);
    }

    public void testJpegToPng() throws Exception
    {
        ImageTransformationOptions options = new ImageTransformationOptions();
        transform(MimetypeMap.MIMETYPE_IMAGE_JPEG, MimetypeMap.MIMETYPE_IMAGE_PNG, options);
    }

    public void testDeprecatedCommandOptions() throws Exception
    {
        // The commandOption should be removed, but is currently needed for custom renditions.
        // This test uses it to resize the file. This happens to be one of the standard options.
        ImageTransformationOptions options = new ImageTransformationOptions();
        long defaultSize = transform(MimetypeMap.MIMETYPE_IMAGE_JPEG, MimetypeMap.MIMETYPE_IMAGE_PNG, options);

        options.setCommandOptions("-resize 200%");
        long biggerSize = transform(MimetypeMap.MIMETYPE_IMAGE_JPEG, MimetypeMap.MIMETYPE_IMAGE_PNG, options);

        assertTrue("The commandOption appears not to have been used as the file size " + biggerSize +
                " is not larger than the default size " + defaultSize, biggerSize > defaultSize);
    }

    // This fails with remote transformers
    // The thumbnails actually have parameters in real life
    public void ignoreTestPageSourceOptions() throws Exception
    {
        // Test empty source options
        ImageTransformationOptions options = new ImageTransformationOptions();
        this.transform(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_IMAGE_PNG, options);

        // Test first page
        options = new ImageTransformationOptions();
        List<TransformationSourceOptions> sourceOptionsList = new ArrayList<TransformationSourceOptions>();
        sourceOptionsList.add(PagedSourceOptions.getPage1Instance());
        options.setSourceOptionsList(sourceOptionsList);
        this.transform(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_IMAGE_PNG, options);
        
        // Test second page
        options = new ImageTransformationOptions();
        sourceOptionsList = new ArrayList<TransformationSourceOptions>();
        PagedSourceOptions sourceOptions = new PagedSourceOptions();
        sourceOptions.setStartPageNumber(2);
        sourceOptions.setEndPageNumber(2);
        sourceOptionsList.add(sourceOptions);
        options.setSourceOptionsList(sourceOptionsList);
        this.transform(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_IMAGE_PNG, options);
        
        // Test page range invalid for target type
        options = new ImageTransformationOptions();
        sourceOptionsList = new ArrayList<TransformationSourceOptions>();
        sourceOptions = new PagedSourceOptions();
        sourceOptions.setStartPageNumber(1);
        sourceOptions.setEndPageNumber(2);
        sourceOptionsList.add(sourceOptions);
        options.setSourceOptionsList(sourceOptionsList);
        try {
            this.transform(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_IMAGE_PNG, options);
            fail("An exception regarding an invalid page range should have been thrown");
        }
        catch (Exception e)
        {
            // failure expected
        }
        
        // Test page out of range
        options = new ImageTransformationOptions();
        sourceOptionsList = new ArrayList<TransformationSourceOptions>();
        sourceOptions = new PagedSourceOptions();
        sourceOptions.setStartPageNumber(3);
        sourceOptions.setEndPageNumber(3);
        sourceOptionsList.add(sourceOptions);
        options.setSourceOptionsList(sourceOptionsList);
        try {
            this.transform(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_IMAGE_PNG, options);
            fail("An exception regarding an invalid page range should have been thrown");
        }
        catch (Exception e)
        {
            // failure expected
        }
    }
    
    /**
     * Mock mimetype service which returns a limited set of mimetypes
     * as {@link AbstractContentTransformerTest#testAllConversions()} will
     * fail if delegates are not available on the test agent. 
     * 
     * @author rgauss
     */
    public class MockMimetypeService extends MimetypeMap
    {
        private List<String> testMimetypes;
        
        public void init()
        {
            super.init();
            testMimetypes = new ArrayList<String>(10);
            testMimetypes.add(MimetypeMap.MIMETYPE_IMAGE_GIF);
            testMimetypes.add(MimetypeMap.MIMETYPE_IMAGE_JPEG);
            testMimetypes.add(MimetypeMap.MIMETYPE_IMAGE_PNG);
            testMimetypes.add(MimetypeMap.MIMETYPE_IMAGE_TIFF);
            testMimetypes.add(MimetypeMap.MIMETYPE_APPLICATION_ILLUSTRATOR);
            testMimetypes.add(MimetypeMap.MIMETYPE_APPLICATION_EPS);
            testMimetypes.add(MimetypeMap.MIMETYPE_APPLICATION_PHOTOSHOP);
            testMimetypes.add(MimetypeMap.MIMETYPE_PDF);
        }

        @Override
        public List<String> getMimetypes()
        {
            return testMimetypes;
        }
        
    }
}
