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

/**
 * Parent test for Tika powered transformer tests 
 * 
 * @author Nick Burch
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public abstract class TikaPoweredContentTransformerTest extends AbstractContentTransformerTest
{
   protected boolean isQuickPhraseExpected(String targetMimetype)
   {
       return (
             targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN) ||
             targetMimetype.equals(MimetypeMap.MIMETYPE_HTML) ||
             targetMimetype.equals(MimetypeMap.MIMETYPE_XML)
       );
   }
   protected boolean isQuickWordsExpected(String targetMimetype)
   {
       return (
             targetMimetype.startsWith(StringExtractingContentTransformer.PREFIX_TEXT) ||
             targetMimetype.equals(MimetypeMap.MIMETYPE_HTML) ||
             targetMimetype.equals(MimetypeMap.MIMETYPE_XML)
       );
   }
 
   /**
    * Tests for html vs xml vs plain text
    */
   protected void additionalContentCheck(String sourceMimetype, String targetMimetype, String contents) 
   {
      if(targetMimetype.equals(MimetypeMap.MIMETYPE_XML)) 
      {
         // Look for header and footer to confirm it was translated
         assertTrue(
               "XML header not found",
               contents.contains("<?xml version=")
         );
         assertTrue(
               "XHTML header not found",
               contents.contains("<html")
         );
         assertTrue(
               "XHTML footer not found",
               contents.contains("</html>")
         );
      }
      else if(targetMimetype.equals(MimetypeMap.MIMETYPE_HTML))
      {
         // Look for header and footer to confirm it was translated
         assertFalse(
               "XML header found but shouldn't be there for HTML",
               contents.contains("<?xml version=")
         );
         assertTrue(
               "HTML header not found",
               contents.contains("<html")
         );
         assertTrue(
               "HTML footer not found",
               contents.contains("</html>")
         );
         assertTrue(
               "Expanded HTML title not found",
               contents.contains("</title>")
         );
      }
      else if(targetMimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN))
      {
         // Ensure it really is plain text not xml/html
         assertFalse(
               "XML header found but shouldn't be there for Plain Text",
               contents.contains("<?xml version=")
         );
         assertFalse(
               "XHTML header found but shouldn't be there for Plain Text",
               contents.contains("<html")
         );
         assertFalse(
               "XHTML footer found but shouldn't be there for Plain Text",
               contents.contains("</html>")
         );
      }
   }
}
