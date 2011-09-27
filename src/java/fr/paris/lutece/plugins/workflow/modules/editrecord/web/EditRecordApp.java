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
package fr.paris.lutece.plugins.workflow.modules.editrecord.web;

import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.service.upload.DirectoryAsynchronousUploadHandler;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecord;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
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
    private EditRecordService _editRecordService = EditRecordService.getService(  );

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
                    doAction( request, editRecord );
                    page = getEditRecordPage( request, editRecord );
                }
                else
                {
                    _editRecordService.setSiteMessage( request, EditRecordConstants.MESSAGE_NO_FIELD_TO_EDIT,
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

        // Remove asynchronous uploaded file from session
        HttpSession session = request.getSession(  );

        if ( session != null )
        {
            DirectoryAsynchronousUploadHandler.removeSessionFiles( session.getId(  ) );
        }

        List<IEntry> listEntries = _editRecordService.getListEntriesToEdit( request,
                editRecord.getListEditRecordValues(  ) );
        Map<String, List<RecordField>> mapRecordFields = _editRecordService.getMapIdEntryListRecordField( listEntries,
                editRecord.getIdHistory(  ) );
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
                doEditRecord( request, editRecord );

                // Back to home page
                String strUrlReturn = request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN );
                strUrlReturn = StringUtils.isNotBlank( strUrlReturn ) ? strUrlReturn
                                                                      : AppPathService.getBaseUrl( request );
                _editRecordService.setSiteMessage( request, EditRecordConstants.MESSAGE_EDITION_COMPLETE,
                    SiteMessage.TYPE_INFO, strUrlReturn );
            }
        }
    }

    /**
     * Do edit a record
     * @param request the HTTP request
     * @param editRecord the edit record
     * @throws SiteMessageException a site message if there is a problem
     */
    private void doEditRecord( HttpServletRequest request, EditRecord editRecord )
        throws SiteMessageException
    {
        // Modify record data
        _editRecordService.doEditRecordData( request, editRecord );
        // Change record state
        _editRecordService.doChangeRecordState( editRecord, request.getLocale(  ) );
        // Change the status of the edit record to complete
        _editRecordService.doCompleteEditRecord( editRecord );
    }
}
