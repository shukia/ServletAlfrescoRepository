/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
/*
 * Copyright (C) 2005 - 2020 Jesper Steen Møller
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * @deprecated OOTB extractors are being moved to T-Engines.
 *
 * Test for the MP3 metadata extraction from id3 tags.
 */
@Deprecated
public class MP3MetadataExtracterTest extends TikaAudioMetadataExtracterTest
{
    private MP3MetadataExtracter extracter;
 
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        extracter = (MP3MetadataExtracter)ctx.getBean("extracter.MP3");
        extracter.register();
    }

    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testSupports() throws Exception
    {
        for (String mimetype : MP3MetadataExtracter.SUPPORTED_MIMETYPES)
        {
            boolean supports = extracter.isSupported(mimetype);
            assertTrue("Mimetype should be supported: " + mimetype, supports);
        }
    }

    public void testMP3Extraction() throws Exception
    {
        testExtractFromMimetype(MimetypeMap.MIMETYPE_MP3);
    }
    @Override
    public void testOggExtraction() throws Exception {}
    @Override
    public void testFlacExtraction() throws Exception {}
    @Override
    public void testMP4AudioExtraction() throws Exception {}

    /**
     * We don't have quite the usual metadata. Tests the descriptions one.
     * Other tests in {@link #testFileSpecificMetadata(String, Map)}
     */
    protected void testCommonMetadata(String mimetype, Map<QName, Serializable> properties) 
    {
       // Title is as normal
       assertEquals(
             "Property " + ContentModel.PROP_TITLE + " not found for mimetype " + mimetype,
             QUICK_TITLE,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_TITLE)));
       // Has Author, not Creator, and is different
       assertEquals(
             "Property " + ContentModel.PROP_AUTHOR + " not found for mimetype " + mimetype,
             "Hauskaz",
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_AUTHOR)));
       
       // Description is a composite
       assertContains(
             "Property " + ContentModel.PROP_DESCRIPTION + " didn't contain " +  QUICK_TITLE + " for mimetype " + mimetype,
             QUICK_TITLE,
             DefaultTypeConverter.INSTANCE.convert(String.class, properties.get(ContentModel.PROP_DESCRIPTION)));
       // Check rest of it later
    }

   /** 
    * Tests for various MP3 specific bits of metadata 
    */
    public void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties) 
    {
       super.testFileSpecificMetadata(mimetype, properties);
    }
}
