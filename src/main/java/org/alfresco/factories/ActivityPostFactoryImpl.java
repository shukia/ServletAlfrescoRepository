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
package org.alfresco.factories;

import org.alfresco.repo.domain.activities.ActivityPostEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class ActivityPostFactoryImpl implements ActivityPostFactory{
    @Override
    public ActivityPostEntity getActivityPostEntity(HttpServletRequest request) {
        ActivityPostEntity newPost = new ActivityPostEntity();
        newPost.setActivityType(request.getParameter("activityType"));
        newPost.setPostDate(new Date(request.getParameter("postDate")));
        newPost.setUserId(request.getParameter("userId"));
        newPost.setSiteNetwork(request.getParameter("siteNetwork"));
        newPost.setAppTool(request.getParameter("appTool"));
        newPost.setLastModified(new Date());
        newPost.setTenantDomain(request.getParameter("tenantDomain"));
        newPost.setJobTaskNode(1);
        return newPost;
    }
}
