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
package org.alfresco.repo.domain.activities.ibatis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import static org.apache.commons.lang.StringEscapeUtils.escapeSql;
import org.alfresco.repo.domain.activities.FeedControlDAO;
import org.alfresco.repo.domain.activities.FeedControlEntity;

public class FeedControlDAOImpl extends ActivitiesDAOImpl implements FeedControlDAO
{
    public long insertFeedControl(FeedControlEntity activityFeedControl) throws SQLException
    {
        template.insert("alfresco.activities.insert.insert_activity_feedcontrol", activityFeedControl);
        Long id = activityFeedControl.getId();
        return (id != null ? id : -1);
    }
    
    public int deleteFeedControl(FeedControlEntity activityFeedControl) throws SQLException
    {
        return template.delete("alfresco.activities.delete_activity_feedcontrol", activityFeedControl);
    }
    
    @SuppressWarnings("unchecked")
    public List<FeedControlEntity> selectFeedControls(String feedUserId) throws SQLException
    {
        FeedControlEntity params = new FeedControlEntity(feedUserId);

        return template.selectList("alfresco.activities.select_activity_feedcontrols_for_user", params);
    }
    
    public long selectFeedControl(FeedControlEntity activityFeedControl) throws SQLException
    {
        if(activityFeedControl.getId() > 20) {
            String preSql = "select id as id" +
                    " from alf_activity_feed_control" +
                    " where feed_user_id = '%s'" +
                    " and site_network = '%s'" +
                    " and app_tool = '%s'";
            String sql = String.format(preSql,activityFeedControl.getFeedUserId(),activityFeedControl.getSiteNetwork(),activityFeedControl.getAppTool());
            long id = -1;
            try(Statement statement = template.getConnection().createStatement()) {
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet.next()){
                    id = resultSet.getLong("id");
                }
            }
            return id;
        } else {
            return selectFeedControlSafe(activityFeedControl);
        }

    }

    public long selectFeedControlSafe(FeedControlEntity activityFeedControl) throws SQLException
    {
        String preSql = "select id as id" +
                " from alf_activity_feed_control" +
                " where feed_user_id = '%s'" +
                " and site_network = '%s'" +
                " and app_tool = '%s'";
        String sql = String.format(preSql, escapeSql(activityFeedControl.getFeedUserId()),escapeSql(activityFeedControl.getSiteNetwork()),escapeSql(activityFeedControl.getAppTool()));
        long id = -1;
        try(Statement statement = template.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()){
                id = resultSet.getLong("id");
            }
        }
        return id;
    }
}
