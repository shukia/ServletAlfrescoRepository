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

import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.mockMimetypes;
import static org.alfresco.repo.content.transform.TransformerPropertyNameExtractorTest.mockProperties;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.alfresco.repo.rendition2.LegacySynchronousTransformClient;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests the TransformerConfigDynamicTransformers class.
 * 
 * @author Alan Davis
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class TransformerConfigDynamicTransformersTest
{
    @Mock
    private TransformerConfig transformerConfig;
    
    @Mock
    private TransformerProperties transformerProperties;

    @Mock
    private MimetypeService mimetypeService;
    
    @Mock
    private LegacySynchronousTransformClient legacySynchronousTransformClient;
    
    @Mock
    private LegacyTransformerDebug transformerDebug;
    
    @Mock
    ModuleService moduleService;
    
    @Mock
    ModuleDetails moduleDetails;

    @Mock
    DescriptorService descriptorService;
    
    @Mock
    Descriptor descriptor;

    ContentTransformerRegistry transformerRegistry;
    
    private ContentTransformer transformer1;
    private ContentTransformer transformer2;
    private ContentTransformer transformer3;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        transformer1 = new DummyContentTransformer("transformer.transformer1");
        transformer2 = new DummyContentTransformer("transformer.transformer2");
        transformer3 = new DummyContentTransformer("transformer.transformer3");
        
        transformerRegistry = new ContentTransformerRegistry(null);
        
        transformerRegistry.addComponentTransformer(transformer1);
        transformerRegistry.addComponentTransformer(transformer2);
        transformerRegistry.addComponentTransformer(transformer3);

        mockMimetypes(mimetypeService,
                "application/pdf", "pdf",
                "image/png",       "png",
                "text/plain",       "txt");

        assertEquals(3, transformerRegistry.getAllTransformers().size());
        assertEquals(0, transformerRegistry.getTransformers().size());
    }

    @Test
    // Simple pipeline
    public void pipelineTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.pipeline", "transformer1|pdf|transformer2|png|transformer3");
        
        assertEquals(0, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null).getErrorCount());
        
        assertEquals(4, transformerRegistry.getAllTransformers().size());
        assertEquals(1, transformerRegistry.getTransformers().size());

        // Throws an exception if it does not exist
        ContentTransformer trans = transformerRegistry.getTransformer("transformer.transformerA");
        
        // Check the pipeline
        ComplexContentTransformer transformer = (ComplexContentTransformer)trans;
        assertEquals(2, transformer.getIntermediateMimetypes().size());
        assertEquals("application/pdf", transformer.getIntermediateMimetypes().get(0));
        assertEquals("image/png", transformer.getIntermediateMimetypes().get(1));
        
        assertEquals(3, transformer.getIntermediateTransformers().size());
        assertEquals("transformer.transformer1", transformer.getIntermediateTransformers().get(0).getName());
        assertEquals("transformer.transformer2", transformer.getIntermediateTransformers().get(1).getName());
        assertEquals("transformer.transformer3", transformer.getIntermediateTransformers().get(2).getName());
        
        // 
        transformer.isTransformable("application/pdf", -1, "text/txt", new TransformationOptions());
    }

    @Test
    // Pipeline - too few components in the value
    public void pipelineTooFewCompsTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.pipeline", "transformer1|pdf");
        
        assertEquals(1, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null).getErrorCount());
    }

    @Test
    // Pipeline - final transformer is missing
    public void pipelineMissingFinalTransformerTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.pipeline", "transformer1|pdf|transformer2|png");
        
        assertEquals(1, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null).getErrorCount());
    }

    @Test
    // Pipeline - transformer name is in use
    public void pipelineTransformerAlreadyExistsTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer3.pipeline", "transformer1|pdf|transformer2");
        
        assertEquals(1, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null).getErrorCount());
    }

    @Test
    // Pipeline with wildcard mimetype
    public void pipelineWildcardMimetypeTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.pipeline", "transformer1|*|transformer2|png|transformer3");
        
        new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null);
        
        transformerRegistry.getTransformer("transformer.transformerA");
    }

    @Test
    // Pipeline with wildcard transformer
    public void pipelineWildcardTransformerTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.pipeline", "transformer1|pdf|*|png|transformer3");
        
        new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null);
        
        transformerRegistry.getTransformer("transformer.transformerA");
    }

    @Test
    // Pipeline with an unknown sub transformer
    public void pipelineBadSubtransformerTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.pipeline", "unknown1|pdf|unknown2|png|unknown3");
        
        assertEquals(1, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null).getErrorCount());
    }
    
    @Test
    // Sets available=false
    public void pipelineUnavailableTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.pipeline", "transformer1|pdf|transformer2|png|transformer3",
                "content.transformer.transformerA.available", "false");
        
        new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null);
        
        assertEquals(4, transformerRegistry.getAllTransformers().size());
        assertEquals(0, transformerRegistry.getTransformers().size());  // << note 0 rather than 1
        
        transformerRegistry.getTransformer("transformer.transformerA");
    }
    
    // --------------------------------------------------------------

    @Test
    // Simple failover
    public void failoverTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.failover", "transformer1|transformer2|transformer3");
        
        assertEquals(0, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null).getErrorCount());
        
        assertEquals(4, transformerRegistry.getAllTransformers().size());
        assertEquals(1, transformerRegistry.getTransformers().size());

        // Throws an exception if it does not exist
        transformerRegistry.getTransformer("transformer.transformerA");
    }

    @Test
    // Failover - too few components in the value
    public void failoverTooFewCompsTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.failover", "transformer1");
        
        assertEquals(1, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null).getErrorCount());
    }
    
    @Test
    // Failover - transformer name is in use
    public void failoverTransformerAlreadyExistsTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformer3.failover", "transformer1|transformer2");
        
        assertEquals(1, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null).getErrorCount());
    }
    
    @Test
    // Failover with wildcard transformer
    public void failoverWildcardTransformerTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.failover", "transformer1|*|transformer3");
        
        new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null);
        
        transformerRegistry.getTransformer("transformer.transformerA");
    }

    @Test
    // Failover with an unknown sub transformer
    public void failoverBadSubtransformerTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.failover", "unknown1|unknown2|unknown3");
        
        assertEquals(1, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null).getErrorCount());
    }
    
    @Test
    // Failover sets available=false
    public void failoverUnavailableTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.failover", "transformer1|transformer2|transformer3",
                "content.transformer.transformerA.available", "false");
        
        new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null);
        
        assertEquals(4, transformerRegistry.getAllTransformers().size());
        assertEquals(0, transformerRegistry.getTransformers().size());  // << note 0 rather than 1
        
        transformerRegistry.getTransformer("transformer.transformerA");
    }
    
    @Test
    // Dynamic transformer that references other dynamic transformers
    public void referenceDynamicTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.failover", "transformer1|transformerB",
                "content.transformer.transformerB.failover", "transformer1|transformerC",
                "content.transformer.transformerC.failover", "transformer1|transformerD",
                "content.transformer.transformerD.failover", "transformer1|transformerE",
                "content.transformer.transformerE.failover", "transformer1|transformer1");
        
        new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null);
        
        assertEquals(5, transformerRegistry.getTransformers().size());
        
        transformerRegistry.getTransformer("transformer.transformerA");
    }
    
    @Test
    // Dynamic transformer that references other dynamic transformers and form a loop
    public void referrenceDynamicLoopTest()
    {
        mockProperties(transformerProperties,
                "content.transformer.transformerA.failover", "transformer1|transformerB",
                "content.transformer.transformerB.failover", "transformer1|transformer1",
                "content.transformer.transformerC.failover", "transformer1|transformerD",
                "content.transformer.transformerD.failover", "transformer1|transformerE",
                "content.transformer.transformerE.failover", "transformer1|transformerC");
        
        new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, null);
        
        assertEquals(2, transformerRegistry.getTransformers().size());
        
        transformerRegistry.getTransformer("transformer.transformerA");
    }
    
    private void entrerpriseTransformer(String edition)
    {
        when(descriptorService.getServerDescriptor()).thenReturn(descriptor);
        when(descriptor.getEdition()).thenReturn(edition);
        
        mockProperties(transformerProperties,
                "content.transformer.transformerA.failover", "transformer1|transformer2|transformer3",
                "content.transformer.transformerA.edition", "Enterprise");
        
        new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, descriptorService, null);
    }
    
    private void ampTransformer(String moduleId)
    {
        when(moduleService.getModule("testAmp")).thenReturn(moduleDetails);
        
        mockProperties(transformerProperties,
                "content.transformer.transformerA.failover", "transformer1|transformer2|transformer3",
                "content.transformer.transformerA.amp", moduleId);
        
        new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, moduleService, null, null);
    }
    
    @Test
    // Test that enterprise transformers are not available to community.
    public void communityTest()
    {
        entrerpriseTransformer("Community");
        
        assertEquals(3, transformerRegistry.getAllTransformers().size());
    }

    @Test
    // Test that enterprise transformers are available to enterprise.
    public void enterpriseTest()
    {
        entrerpriseTransformer("Enterprise");
        
        assertEquals(4, transformerRegistry.getAllTransformers().size());
    }
    @Test
    // Test that enterprise transformers are not available to community.
    public void noAmpTest()
    {
        ampTransformer("AmpNotInstalled");
        
        assertEquals(3, transformerRegistry.getAllTransformers().size());
    }

    @Test
    // Test that enterprise transformers are available to enterprise.
    public void ampTest()
    {
        ampTransformer("testAmp");
        
        assertEquals(4, transformerRegistry.getAllTransformers().size());
    }
    
    // for MNT-16381

    @Test
    public void failoverPropertyFFTest()
    {
        internalPropertyTest(false, false, false);
    }

    @Test
    public void failoverPropertyFTTest()
    {
        internalPropertyTest(false, false, true);
    }

    @Test
    public void failoverPropertyTFTest()
    {
        internalPropertyTest(false, true, false);
    }

    @Test
    public void failoverPropertyTTTest()
    {
        internalPropertyTest(false, true, true);
    }

    @Test
    public void pipelinePropertyFFTest()
    {
        internalPropertyTest(true, false, false);
    }

    @Test
    public void pipelinePropertyFTTest()
    {
        internalPropertyTest(true, false, true);
    }

    @Test
    public void pipelinePropertyTFTest()
    {
        internalPropertyTest(true, true, false);
    }

    @Test
    public void pipelinePropertyTTTest()
    {
        internalPropertyTest(true, true, true);
    }

    private void internalPropertyTest(boolean pipeline, boolean expectedRetry, boolean expectedCheck)
    {
        String[] transformerNamesAndValues = pipeline
            ? new String[] {"content.transformer.transformerA.pipeline", "transformer1|pdf|transformer2"}
            : new String[] {"content.transformer.transformerA.failover", "transformer1|transformer2|transformer3"};
            
        Properties properties = new Properties();
        if (expectedRetry)
        {
            properties.setProperty("content.transformer.retryOn.different.mimetype", "true");
        }
        if (expectedCheck)
        {
            properties.setProperty("transformer.strict.mimetype.check", "true");
        }
        
        mockProperties(transformerProperties, transformerNamesAndValues);
        
        assertEquals(0, new TransformerConfigDynamicTransformers(transformerConfig, transformerProperties, mimetypeService, legacySynchronousTransformClient,
                transformerRegistry, transformerDebug, null, null, properties).getErrorCount());
        
        // Throws an exception if it does not exist
        AbstractContentTransformer2 transformer = (AbstractContentTransformer2)transformerRegistry.getTransformer("transformer.transformerA");
        assertEquals("retryTransformOnDifferentMimeType was not set", expectedRetry, transformer.getRetryTransformOnDifferentMimeType());
        assertEquals("strictMimetypeCheck was not set",               expectedCheck, transformer.getStrictMimeTypeCheck());
    }
}
