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
package fr.paris.lutece.plugins.workflow.modules.editrecord.web;

import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.service.upload.DirectoryAsynchronousUploadHandler;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecord;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.IEditRecordService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.portal.web.xpages.XPageApplication;
import fr.paris.lutece.util.html.HtmlTemplate;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 *
 * EditRecordApp
 *
 */
public class EditRecordApp implements XPageApplication
{
    // TEMPLATES
    private static final String TEMPLATE_EDIT_RECORD = "skin/plugins/workflow/modules/editrecord/edit_record.html";

    // SERVICES
    private IEditRecordService _editRecordService = SpringContextService.getBean( EditRecordService.BEAN_SERVICE );

    /**
     * {@inheritDoc}
     */
    public XPage getPage( HttpServletRequest request, int nMode, Plugin plugin )
        throws UserNotSignedException, SiteMessageException
    {
        XPage page = null;

        if ( _editRecordService.isRequestAuthenticated( request ) )
        {
            String strIdHistory = request.getParameter( EditRecordConstants.PARAMETER_ID_HISTORY );
            String strIdTask = request.getParameter( EditRecordConstants.PARAMETER_ID_TASK );

            if ( StringUtils.isNotBlank( strIdHistory ) && StringUtils.isNumeric( strIdHistory ) &&
                    StringUtils.isNotBlank( strIdTask ) && StringUtils.isNumeric( strIdTask ) )
            {
                int nIdHistory = Integer.parseInt( strIdHistory );
                int nIdTask = Integer.parseInt( strIdTask );
                EditRecord editRecord = _editRecordService.find( nIdHistory, nIdTask );

                if ( ( editRecord != null ) && !editRecord.isComplete(  ) )
                {
                    if ( _editRecordService.isRecordStateValid( editRecord, request.getLocale(  ) ) )
                    {
                        doAction( request, editRecord );
                        page = getEditRecordPage( request, editRecord );
                    }
                    else
                    {
                        _editRecordService.setSiteMessage( request, Messages.USER_ACCESS_DENIED, SiteMessage.TYPE_STOP,
                            request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN ) );
                    }
                }
                else
                {
                    _editRecordService.setSiteMessage( request, EditRecordConstants.MESSAGE_RECORD_ALREADY_COMPLETED,
                        SiteMessage.TYPE_INFO, request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN ) );
                }
            }
            else
            {
                _editRecordService.setSiteMessage( request, Messages.MANDATORY_FIELDS, SiteMessage.TYPE_STOP,
                    request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN ) );
            }
        }
        else
        {
            _editRecordService.setSiteMessage( request, Messages.USER_ACCESS_DENIED, SiteMessage.TYPE_STOP,
                request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN ) );
        }

        return page;
    }

    /**
     * Get the edit record page
     * @param request the HTTP request
     * @param editRecord the edit record
     * @return a XPage
     * @throws SiteMessageException a site message if there is a problem
     */
    private XPage getEditRecordPage( HttpServletRequest request, EditRecord editRecord )
        throws SiteMessageException
    {
        XPage page = new XPage(  );

        List<IEntry> listEntries = _editRecordService.getListEntriesToEdit( request,
                editRecord.getListEditRecordValues(  ) );

        /**
         * Map of <idEntry, RecordFields>
         *         1) The user has uploaded/deleted a file
         *                 - The updated map is stored in the session
         *  2) The user has not uploaded/delete a file
         *          - The map is filled with the data from the database
         *          - The asynchronous uploaded files map is reinitialized
         */
        Map<String, List<RecordField>> mapRecordFields = null;

        // Get the map of <idEntry, RecordFields from session if it exists : 
        /** 1) Case when the user has uploaded a file, the the map is stored in the session */
        HttpSession session = request.getSession( false );

        if ( session != null )
        {
            mapRecordFields = (Map<String, List<RecordField>>) session.getAttribute( EditRecordConstants.SESSION_EDIT_RECORD_LIST_SUBMITTED_RECORD_FIELDS );
            // IMPORTANT : Remove the map from the session
            session.removeAttribute( EditRecordConstants.SESSION_EDIT_RECORD_LIST_SUBMITTED_RECORD_FIELDS );
        }

        // Get the map <idEntry, RecordFields> classically from the database
        /** 2) The user has not uploaded/delete a file */
        if ( mapRecordFields == null )
        {
            Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
            mapRecordFields = _editRecordService.getMapIdEntryListRecordField( listEntries, editRecord.getIdHistory(  ) );
            // Reinit the asynchronous uploaded file map
            DirectoryAsynchronousUploadHandler.getHandler(  ).reinitMap( request, mapRecordFields, pluginDirectory );
        }

        Record record = _editRecordService.getRecordFromIdHistory( editRecord.getIdHistory(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( EditRecordConstants.MARK_EDIT_RECORD, editRecord );
        model.put( EditRecordConstants.MARK_LIST_ENTRIES, listEntries );
        model.put( EditRecordConstants.MARK_MAP_ID_ENTRY_LIST_RECORD_FIELD, mapRecordFields );
        model.put( EditRecordConstants.MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( EditRecordConstants.MARK_LOCALE, request.getLocale(  ) );
        model.put( EditRecordConstants.MARK_URL_RETURN, request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN ) );
        model.put( EditRecordConstants.MARK_SIGNATURE, request.getParameter( EditRecordConstants.PARAMETER_SIGNATURE ) );
        model.put( EditRecordConstants.MARK_TIMESTAMP, request.getParameter( EditRecordConstants.PARAMETER_TIMESTAMP ) );

        if ( record != null )
        {
            model.put( EditRecordConstants.MARK_ID_DIRECTORY_RECORD, record.getIdRecord(  ) );
        }

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EDIT_RECORD, request.getLocale(  ), model );

        page.setTitle( I18nService.getLocalizedString( EditRecordConstants.PROPERTY_XPAGE_EDIT_RECORD_PAGETITLE,
                request.getLocale(  ) ) );
        page.setPathLabel( I18nService.getLocalizedString( EditRecordConstants.PROPERTY_XPAGE_EDIT_RECORD_PATHLABEL,
                request.getLocale(  ) ) );
        page.setContent( template.getHtml(  ) );

        return page;
    }

    /**
     * Do action
     * @param request HttpServletRequest
     * @param editRecord edit record
     * @throws SiteMessageException site message if edition is complete
     */
    private void doAction( HttpServletRequest request, EditRecord editRecord )
        throws SiteMessageException
    {
        String strAction = request.getParameter( EditRecordConstants.PARAMETER_ACTION );

        if ( StringUtils.isNotBlank( strAction ) )
        {
            if ( EditRecordConstants.ACTION_DO_MODIFY_RECORD.equals( strAction ) )
            {
                if ( doEditRecord( request, editRecord ) )
                {
                    // Back to home page
                    String strUrlReturn = request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN );
                    strUrlReturn = StringUtils.isNotBlank( strUrlReturn ) ? strUrlReturn
                                                                          : AppPathService.getBaseUrl( request );
                    _editRecordService.setSiteMessage( request, EditRecordConstants.MESSAGE_EDITION_COMPLETE,
                        SiteMessage.TYPE_INFO, strUrlReturn );
                }
            }
        }
    }

    /**
     * Do edit a record
     * @param request the HTTP request
     * @param editRecord the edit record
     * @return true if the record must be updated, false otherwise
     * @throws SiteMessageException a site message if there is a problem
     */
    private boolean doEditRecord( HttpServletRequest request, EditRecord editRecord )
        throws SiteMessageException
    {
        if ( _editRecordService.isRecordStateValid( editRecord, request.getLocale(  ) ) )
        {
            // Modify record data
            if ( _editRecordService.doEditRecordData( request, editRecord ) )
            {
                // Change record state
                _editRecordService.doChangeRecordState( editRecord, request.getLocale(  ) );
                // Change the status of the edit record to complete
                _editRecordService.doCompleteEditRecord( editRecord );

                return true;
            }

            return false;
        }
        else
        {
            _editRecordService.setSiteMessage( request, Messages.USER_ACCESS_DENIED, SiteMessage.TYPE_STOP,
                request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN ) );
        }

        return false;
    }
}
