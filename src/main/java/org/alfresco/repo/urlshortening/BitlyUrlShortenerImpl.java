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
package org.alfresco.repo.urlshortening;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.urlshortening.UrlShortener;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @deprecated as it is no longer used in the core repository code.
 */
@Deprecated
public class BitlyUrlShortenerImpl implements UrlShortener
{
    private static final Log log = LogFactory.getLog(BitlyUrlShortenerImpl.class);

    private int urlLength = 20;
    private String username;
    private String apiKey = "R_ca15c6c89e9b25ccd170bafd209a0d4f";
    private HttpClient httpClient;

    public BitlyUrlShortenerImpl()
    {
        httpClient = new HttpClient();
        httpClient.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("api-ssl.bitly.com", 443, Protocol.getProtocol("https"));
        httpClient.setHostConfiguration(hostConfiguration);
    }

    @Override
    public String shortenUrl(String longUrl)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Shortening URL: " + longUrl);
        }
        String shortUrl = longUrl;
        if (longUrl.length() > urlLength)
        {
            GetMethod getMethod = new GetMethod();
            getMethod.setPath("/v3/shorten");

            List<NameValuePair> args = new ArrayList<NameValuePair>();
            args.add(new NameValuePair("login", username));
            args.add(new NameValuePair("apiKey", apiKey));
            args.add(new NameValuePair("longUrl", longUrl));
            args.add(new NameValuePair("format", "txt"));
            getMethod.setQueryString(args.toArray(new NameValuePair[args.size()]));

            try
            {
                int resultCode = httpClient.executeMethod(getMethod);
                if (resultCode == 200)
                {
                    shortUrl = getMethod.getResponseBodyAsString();
                }
                else
                {
                    log.warn("Failed to shorten URL " + longUrl + "  - response code == " + resultCode);
                    log.warn(getMethod.getResponseBodyAsString());
                }
            }
            catch (Exception ex)
            {
                log.error("Failed to shorten URL " + longUrl, ex);
            }
            if (log.isDebugEnabled())
            {
                log.debug("URL " + longUrl + " has been shortened to " + shortUrl);
            }
        }
        return shortUrl.trim();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public int getUrlLength()
    {
        return urlLength;
    }
    
    /**
     * @param urlLength the urlLength to set
     */
    public void setUrlLength(int urlLength)
    {
        this.urlLength = urlLength;
    }
    
    /**
     * @param username the username to set
     */
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }
}