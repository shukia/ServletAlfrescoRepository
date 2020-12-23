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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Test case for {@link OOXMLThumbnailContentTransformer} content transformer.
 * 
 * @author Nick Burch
 * @since 4.0.1
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class OOXMLThumbnailContentTransformerTest extends AbstractContentTransformerTest
{
    private ContentTransformer transformer;
    
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new OOXMLThumbnailContentTransformer();
        
        // Ugly cast just to set the MimetypeService
        ((ContentTransformerHelper)transformer).setMimetypeService(mimetypeService);
    }
    
    @Override
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testIsTransformable() throws Exception
    {
        // Does support Thumbnails
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, MimetypeMap.MIMETYPE_IMAGE_JPEG, new TransformationOptions()));
        
        // Unlike iWorks, it doesn't handle PDF previews
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, MimetypeMap.MIMETYPE_PDF, new TransformationOptions()));
    }
}
