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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides access to transformer properties which come from the Transformer sub system AND
 * the those that start with "content.transformer." in the parent context.<p>
 * 
 * By default a subsystem only provides properties defined within itself and only those
 * properties may be overridden by alfresco.global.properties. New properties may not be added.
 * As this class allows this to happen for the Transformers subsystem.
 * 
 * @author Alan Davis
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class TransformerProperties
{
    private static final String TRANSFORMERS_PROPERTIES = "alfresco/subsystems/Transformers/default/transformers.properties";

    private static Log logger = LogFactory.getLog(TransformerProperties.class);

    private static final String JOD_CONVERTER = ".JodConverter.";
    private static final String OPEN_OFFICE = ".OpenOffice.";

    private final ChildApplicationContextFactory subsystem;
    
    private final Properties globalProperties;
    
    TransformerProperties(ChildApplicationContextFactory subsystem, Properties globalProperties)
    {
        this.subsystem = subsystem;
        this.globalProperties = globalProperties;
    }

    public String getProperty(String name)
    {
        String value = subsystem.getProperty(name);
        if (value == null)
        {
            value = globalProperties.getProperty(name);

            if (value == null)
            {
                name = alias(name, JOD_CONVERTER, OPEN_OFFICE);
                value = globalProperties.getProperty(name);
            }
        }
        return value;
    }

    /**
     * Returns the default properties from the transformers.properties file. These may be overridden by customers in
     * other property files and JMX. 
     */
    public Properties getDefaultProperties()
    {
        Properties defaultProperties = new Properties();
        InputStream propertiesStream = getClass().getClassLoader().getResourceAsStream(TRANSFORMERS_PROPERTIES);
        if (propertiesStream != null)
        {
            try
            {
                defaultProperties.load(propertiesStream);
            }
            catch (IOException e)
            {
                logger.error("Could not read "+TRANSFORMERS_PROPERTIES+" so all properties will appear to be overridden by the customer", e);
            }
        }
        else
        {
            logger.error("Could not find "+TRANSFORMERS_PROPERTIES+" so all properties will appear to be overridden by the customer");
        }
        return defaultProperties;
    }

    public Set<String> getPropertyNames()
    {
        Set<String> propertyNames = new HashSet<String>(subsystem.getPropertyNames());
        for (String name: globalProperties.stringPropertyNames())
        {
            if (name.startsWith(TransformerConfig.PREFIX))
            {
                name = alias(name, OPEN_OFFICE, JOD_CONVERTER);
                if (!propertyNames.contains(name))
                {
                    propertyNames.add(name);
                }
            }
        }

        return propertyNames;
    }

    // When we moved the JodConverter into the Community edition (after 6.0.0-ea) we wanted to allow any Community
    // settings for content.transformer.OpenOffice (and related pipeline transformers specified) in
    // alfresco.global.properties to apply to the JodConverter (and related pipeline transformers), but where there
    // is jodConverter that value should be used.
    //     content.transformer.JodConverter.
    //     content.transformer.JodConverter.Html2Pdf.
    //     content.transformer.JodConverter.2Pdf.
    //     content.transformer.complex.JodConverter.Image.
    //     content.transformer.complex.JodConverter.PdfBox
    private String alias(String name, String from, String to)
    {
        int i = name.indexOf(from);
        if (i != -1)
        {
            name = name.substring(0, i) + to + name.substring(i+from.length());
        }
        return name;
    }

    public void setProperties(Map<String, String> map)
    {
        subsystem.setProperties(map);
    }

    public void removeProperties(Collection<String> propertyNames)
    {
        subsystem.removeProperties(propertyNames);
    }
}
