/*
 * Copyright (c) 2002-2011, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.workflow.modules.editrecord.service.taskinfo;

import fr.paris.lutece.plugins.workflow.business.ResourceHistory;
import fr.paris.lutece.plugins.workflow.business.ResourceHistoryHome;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordPlugin;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.signrequest.EditRecordRequestAuthenticatorService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.plugins.workflow.service.WorkflowPlugin;
import fr.paris.lutece.plugins.workflow.service.taskinfo.AbstractTaskInfoProvider;
import fr.paris.lutece.plugins.workflow.service.taskinfo.ITaskInfoProvider;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;
import fr.paris.lutece.util.xml.XmlUtil;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 *
 * EditRecordTaskInfoProvider
 *
 */
public class EditRecordTaskInfoProvider extends AbstractTaskInfoProvider
{
    private static final String BEAN_EDIT_RECORD_TASK_INFO_PROVIDER = "workflow-editrecord.editRecordTaskInfoProvider";
    private static final String JSP_SITE_PORTAL = "jsp/site/Portal.jsp";

    /**
     * Get the instance of the provider
     * @return the instance of the provider
     */
    public static ITaskInfoProvider getProvider(  )
    {
        return (ITaskInfoProvider) SpringContextService.getPluginBean( EditRecordPlugin.PLUGIN_NAME,
            BEAN_EDIT_RECORD_TASK_INFO_PROVIDER );
    }

    /**
     * {@inheritDoc}
     */
    public String getPluginName(  )
    {
        return EditRecordPlugin.PLUGIN_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public String getTaskResourceInfo( int nIdHistory, int nIdTask, HttpServletRequest request )
    {
        String strInfo = StringUtils.EMPTY;
        Plugin pluginWorkflow = PluginService.getPlugin( WorkflowPlugin.PLUGIN_NAME );
        ResourceHistory resourceHistory = ResourceHistoryHome.findByPrimaryKey( nIdHistory, pluginWorkflow );

        if ( resourceHistory != null )
        {
            List<String> listElements = new ArrayList<String>(  );
            listElements.add( Integer.toString( resourceHistory.getIdResource(  ) ) );
            listElements.add( Integer.toString( nIdTask ) );

            String strTimestamp = Long.toString( new Date(  ).getTime(  ) );
            String strSignature = EditRecordRequestAuthenticatorService.getRequestAuthenticator(  )
                                                                       .buildSignature( listElements, strTimestamp );
            StringBuilder sbUrl = new StringBuilder( getBaseUrl( request ) );

            if ( !sbUrl.toString(  ).endsWith( EditRecordConstants.SLASH ) )
            {
                sbUrl.append( EditRecordConstants.SLASH );
            }

            UrlItem url = new UrlItem( sbUrl.toString(  ) + JSP_SITE_PORTAL );
            url.addParameter( XPageAppService.PARAM_XPAGE_APP, EditRecordPlugin.PLUGIN_NAME );
            url.addParameter( EditRecordConstants.PARAMETER_ID_RECORD, resourceHistory.getIdResource(  ) );
            url.addParameter( EditRecordConstants.PARAMETER_ID_TASK, nIdTask );
            url.addParameter( EditRecordConstants.PARAMETER_SIGNATURE, strSignature );
            url.addParameter( EditRecordConstants.PARAMETER_TIMESTAMP, strTimestamp );
            url.addParameter( EditRecordConstants.PARAMETER_URL_RETURN,
                AppPropertiesService.getProperty( EditRecordConstants.PROPERTY_URL_RETURN ) );

            StringBuffer sbInfo = new StringBuffer(  );

            Map<String, String> paramHref = new HashMap<String, String>(  );
            paramHref.put( EditRecordConstants.ATTRIBUTE_HREF, url.getUrl(  ) );

            XmlUtil.beginElement( sbInfo, EditRecordConstants.TAG_A, paramHref );
            sbInfo.append( I18nService.getLocalizedString( EditRecordConstants.MESSAGE_COMPLETE_RECORD,
                    request.getLocale(  ) ) );
            XmlUtil.endElement( sbInfo, EditRecordConstants.TAG_A );

            strInfo = sbInfo.toString(  );
        }

        return strInfo;
    }

    /**
     * Get the base url
     * @param request the HTTP request
     * @return the base url
     */
    private String getBaseUrl( HttpServletRequest request )
    {
        String strBaseUrl = StringUtils.EMPTY;

        if ( request != null )
        {
            strBaseUrl = AppPathService.getBaseUrl( request );
        }
        else
        {
            strBaseUrl = AppPropertiesService.getProperty( EditRecordConstants.PROPERTY_LUTECE_BASE_URL );

            if ( StringUtils.isBlank( strBaseUrl ) )
            {
                strBaseUrl = AppPropertiesService.getProperty( EditRecordConstants.PROPERTY_LUTECE_PROD_URL );
            }
        }

        return strBaseUrl;
    }
}
