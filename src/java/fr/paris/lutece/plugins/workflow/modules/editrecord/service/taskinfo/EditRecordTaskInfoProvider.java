/*
 * Copyright (c) 2002-2017, Mairie de Paris
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

import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordPlugin;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.signrequest.EditRecordRequestAuthenticatorService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.plugins.workflow.service.taskinfo.AbstractTaskInfoProvider;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * EditRecordTaskInfoProvider
 *
 */
public class EditRecordTaskInfoProvider extends AbstractTaskInfoProvider
{
    private static final String PROPERTY_BASE_URL_USE_PROPERTY = "workflow-editrecord.base_url.use_property";
    @Inject
    private IResourceHistoryService _resourceHistoryService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginName( )
    {
        return EditRecordPlugin.PLUGIN_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTaskResourceInfo( int nIdHistory, int nIdTask, HttpServletRequest request )
    {
        String strInfo = StringUtils.EMPTY;
        ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdHistory );

        if ( resourceHistory != null )
        {
            List<String> listElements = new ArrayList<String>( );
            listElements.add( Integer.toString( nIdHistory ) );
            listElements.add( Integer.toString( nIdTask ) );

            String strTimestamp = Long.toString( new Date( ).getTime( ) );
            String strSignature = EditRecordRequestAuthenticatorService.getRequestAuthenticator( ).buildSignature( listElements, strTimestamp );
            StringBuilder sbUrl = new StringBuilder( getBaseUrl( request ) );

            if ( !sbUrl.toString( ).endsWith( EditRecordConstants.SLASH ) )
            {
                sbUrl.append( EditRecordConstants.SLASH );
            }

            UrlItem url = new UrlItem( sbUrl.toString( ) + AppPathService.getPortalUrl( ) );
            url.addParameter( XPageAppService.PARAM_XPAGE_APP, EditRecordPlugin.PLUGIN_NAME );
            url.addParameter( EditRecordConstants.PARAMETER_ID_HISTORY, nIdHistory );
            url.addParameter( EditRecordConstants.PARAMETER_ID_TASK, nIdTask );
            url.addParameter( EditRecordConstants.PARAMETER_SIGNATURE, strSignature );
            url.addParameter( EditRecordConstants.PARAMETER_TIMESTAMP, strTimestamp );
            url.addParameter( EditRecordConstants.PARAMETER_URL_RETURN, AppPropertiesService.getProperty( EditRecordConstants.PROPERTY_URL_RETURN ) );

            strInfo = url.getUrl( );
        }

        return strInfo;
    }

    /**
     * Get the base url
     * 
     * @param request
     *            the HTTP request
     * @return the base url
     */
    private String getBaseUrl( HttpServletRequest request )
    {
        String strBaseUrl = StringUtils.EMPTY;

        if ( ( request != null ) && !isBaseUrlFetchedInProperty( ) )
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

    /**
     * Check if the base url must be fetched in the config.properties file or in the request
     * 
     * @return true if it must be fetched in the config.properties file
     */
    private boolean isBaseUrlFetchedInProperty( )
    {
        return Boolean.valueOf( AppPropertiesService.getProperty( PROPERTY_BASE_URL_USE_PROPERTY ) );
    }
}
