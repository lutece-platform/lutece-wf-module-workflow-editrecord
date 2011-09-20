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

import fr.paris.lutece.plugins.directory.business.EntryTypeDownloadUrl;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecord;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecordValue;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordValueService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.JSONUtils;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.constants.Markers;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.portal.web.xpages.XPageApplication;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


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
    private EditRecordValueService _editRecordValueService = EditRecordValueService.getService(  );

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
     * Do remove asynchronous uploaded file
     * @param request the HttpServletRequest
     * @return a json corresponding to the result of the action
     */
    public String doRemoveAsynchronousUploadedFile( HttpServletRequest request )
    {
        String strJson = StringUtils.EMPTY;
        String strIdHistory = request.getParameter( EditRecordConstants.PARAMETER_ID_HISTORY );
        String strIdTask = request.getParameter( EditRecordConstants.PARAMETER_ID_TASK );
        String strIdEntry = request.getParameter( EditRecordConstants.PARAMETER_ID_ENTRY );

        if ( StringUtils.isNotBlank( strIdHistory ) && StringUtils.isNumeric( strIdHistory ) &&
                StringUtils.isNotBlank( strIdTask ) && StringUtils.isNumeric( strIdTask ) &&
                StringUtils.isNotBlank( strIdEntry ) && StringUtils.isNumeric( strIdEntry ) )
        {
            int nIdHistory = Integer.parseInt( strIdHistory );
            int nIdTask = Integer.parseInt( strIdTask );
            int nIdEntry = Integer.parseInt( strIdEntry );

            EditRecord editRecord = _editRecordService.find( nIdHistory, nIdTask );
            IEntry entry = _editRecordService.getEntry( nIdEntry );

            if ( ( editRecord != null ) && !editRecord.isComplete(  ) && ( entry != null ) &&
                    ( editRecord.getListEditRecordValues(  ) != null ) &&
                    !editRecord.getListEditRecordValues(  ).isEmpty(  ) )
            {
                JSONObject json = new JSONObject(  );
                json.element( JSONUtils.JSON_KEY_ID_ENTRY, strIdEntry );

                try
                {
                    _editRecordService.doRemoveFile( editRecord, entry );
                    json.element( JSONUtils.JSON_KEY_SUCCESS, JSONUtils.JSON_KEY_SUCCESS );
                    json.accumulateAll( JSONUtils.getUploadedFileJSON( null ) );
                }
                catch ( HttpAccessException e )
                {
                    AppLogService.error( e );
                    json.element( JSONUtils.JSON_KEY_ERROR,
                        I18nService.getLocalizedString( EditRecordConstants.MESSAGE_ERROR_UPLOAD, request.getLocale(  ) ) );
                    json.accumulateAll( JSONUtils.getUploadedFileJSON( _editRecordService.getFileName( editRecord,
                                nIdEntry ) ) );
                }

                strJson = json.toString(  );
            }
        }

        return strJson;
    }

    /**
     * Get the uploaded files
     * @param request the HttpServletRequest
     * @return the json
     */
    public String getUploadedFiles( HttpServletRequest request )
    {
        String strJson = StringUtils.EMPTY;
        String strIdHistory = request.getParameter( EditRecordConstants.PARAMETER_ID_HISTORY );
        String strIdTask = request.getParameter( EditRecordConstants.PARAMETER_ID_TASK );
        String strIdEntry = request.getParameter( EditRecordConstants.PARAMETER_ID_ENTRY );

        if ( StringUtils.isNotBlank( strIdHistory ) && StringUtils.isNumeric( strIdHistory ) &&
                StringUtils.isNotBlank( strIdTask ) && StringUtils.isNumeric( strIdTask ) &&
                StringUtils.isNotBlank( strIdEntry ) && StringUtils.isNumeric( strIdEntry ) )
        {
            int nIdHistory = Integer.parseInt( strIdHistory );
            int nIdTask = Integer.parseInt( strIdTask );
            int nIdEntry = Integer.parseInt( strIdEntry );
            EditRecord editRecord = _editRecordService.find( nIdHistory, nIdTask );
            IEntry entry = _editRecordService.getEntry( nIdEntry );

            if ( ( editRecord != null ) && !editRecord.isComplete(  ) && ( entry != null ) &&
                    ( editRecord.getListEditRecordValues(  ) != null ) &&
                    !editRecord.getListEditRecordValues(  ).isEmpty(  ) )
            {
                String strFileName = StringUtils.EMPTY;

                for ( EditRecordValue editRecordValue : editRecord.getListEditRecordValues(  ) )
                {
                    if ( editRecordValue.getIdEntry(  ) == nIdEntry )
                    {
                        strFileName = editRecordValue.getFileName(  );

                        break;
                    }
                }

                JSONObject json = JSONUtils.getUploadedFileJSON( strFileName );

                // add id entry
                json.element( JSONUtils.JSON_KEY_ID_ENTRY, nIdEntry );

                strJson = json.toString(  );
            }
        }

        return strJson;
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
        Map<String, List<RecordField>> mapRecordFields = _editRecordService.getMapIdEntryListRecordField( listEntries,
                editRecord.getIdHistory(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( EditRecordConstants.MARK_EDIT_RECORD, editRecord );
        model.put( EditRecordConstants.MARK_LIST_ENTRIES, listEntries );
        model.put( EditRecordConstants.MARK_MAP_ID_ENTRY_LIST_RECORD_FIELD, mapRecordFields );
        model.put( EditRecordConstants.MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( EditRecordConstants.MARK_LOCALE, request.getLocale(  ) );
        model.put( EditRecordConstants.MARK_URL_RETURN, request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN ) );
        model.put( EditRecordConstants.MARK_SIGNATURE, request.getParameter( EditRecordConstants.PARAMETER_SIGNATURE ) );
        model.put( EditRecordConstants.MARK_TIMESTAMP, request.getParameter( EditRecordConstants.PARAMETER_TIMESTAMP ) );
        model.put( EditRecordConstants.MARK_ENTRY_TYPE_DOWNLOAD_URL, _editRecordService.getEntryTypeDownloadUrl(  ) );
        model.put( Markers.BASE_URL, AppPathService.getBaseUrl( request ) );

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
        String strSave = request.getParameter( EditRecordConstants.PARAMETER_SAVE );

        if ( StringUtils.isNotBlank( strSave ) )
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
        else
        {
            String strFileAction = getUploadAction( request );

            if ( StringUtils.isNotBlank( strFileAction ) )
            {
                if ( strFileAction.startsWith( EditRecordConstants.UPLOAD_SUBMIT_PREFIX ) )
                {
                    doUploadFileAction( request, strFileAction, editRecord );
                }
                else if ( strFileAction.startsWith( EditRecordConstants.UPLOAD_DELETE_PREFIX ) )
                {
                    doDeleteFileAction( request, strFileAction, editRecord );
                }
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

    /**
     * Checks the request parameters to see if an upload submit has been
     * called.
     *
     * @param request the HTTP request
     * @return the name of the upload action, if any. Null otherwise.
     */
    private static String getUploadAction( HttpServletRequest request )
    {
        Enumeration enumParamNames = request.getParameterNames(  );

        while ( enumParamNames.hasMoreElements(  ) )
        {
            String strParamName = (String) enumParamNames.nextElement(  );

            if ( strParamName.startsWith( EditRecordConstants.UPLOAD_SUBMIT_PREFIX ) ||
                    strParamName.startsWith( EditRecordConstants.UPLOAD_DELETE_PREFIX ) )
            {
                return strParamName;
            }
        }

        return null;
    }

    /**
     * Performs an upload action.
     * @param request the HTTP request
     * @param strFileAction the name of the action
     * @param editRecord the Edit Record
     * @throws SiteMessageException Site Message exception if there is an HTTP issue
     */
    private void doUploadFileAction( HttpServletRequest request, String strFileAction, EditRecord editRecord )
        throws SiteMessageException
    {
        // Get the id entry
        String strIdEntry = strFileAction.substring( EditRecordConstants.UPLOAD_SUBMIT_PREFIX.length(  ) );

        // A file was submitted
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        FileItem fileItem = multipartRequest.getFile( strIdEntry );

        if ( ( fileItem != null ) && StringUtils.isNotBlank( strIdEntry ) && StringUtils.isNumeric( strIdEntry ) )
        {
            int nIdEntry = Integer.parseInt( strIdEntry );
            IEntry entry = _editRecordService.getEntry( nIdEntry );

            if ( ( editRecord != null ) && !editRecord.isComplete(  ) && ( entry != null ) &&
                    entry instanceof EntryTypeDownloadUrl && ( entry.getFields(  ) != null ) &&
                    !entry.getFields(  ).isEmpty(  ) )
            {
                String strBlobStore = _editRecordService.getBlobStoreName( entry );
                String strWSRestUrl = _editRecordService.getWSRestUrl( entry );

                try
                {
                    // Remove file of the record field first
                    _editRecordService.doRemoveFile( editRecord, entry );

                    // Store the uploaded file in the blobstore webapp
                    String strBlobKey = _editRecordService.doUploadFile( strWSRestUrl, fileItem, strBlobStore );
                    String strDownloadFileUrl = _editRecordService.getFileUrl( strWSRestUrl, strBlobStore, strBlobKey );

                    // Update the record field
                    _editRecordService.doEditRecordFieldDownloadUrl( editRecord.getIdHistory(  ), nIdEntry,
                        strDownloadFileUrl );

                    // Update the edit record value
                    for ( EditRecordValue editRecordValue : editRecord.getListEditRecordValues(  ) )
                    {
                        if ( editRecordValue.getIdEntry(  ) == nIdEntry )
                        {
                            editRecordValue.setFileName( _editRecordValueService.getFileName( editRecordValue ) );
                        }
                    }
                }
                catch ( HttpAccessException e )
                {
                    _editRecordService.setSiteMessage( request, EditRecordConstants.MESSAGE_ERROR_UPLOAD,
                        SiteMessage.TYPE_STOP, null );
                }
            }
        }
    }

    /**
     * Do delete a file
     * @param request the HttpServletRequest
     * @param strFileAction the name of the action
     * @param editRecord the edit record
     * @throws SiteMessageException Site Message exception if there is an issue
     */
    private void doDeleteFileAction( HttpServletRequest request, String strFileAction, EditRecord editRecord )
        throws SiteMessageException
    {
        // Get the id entry
        String strIdEntry = strFileAction.substring( EditRecordConstants.UPLOAD_DELETE_PREFIX.length(  ) );

        if ( StringUtils.isNotBlank( strIdEntry ) && StringUtils.isNumeric( strIdEntry ) )
        {
            int nIdEntry = Integer.parseInt( strIdEntry );
            IEntry entry = _editRecordService.getEntry( nIdEntry );

            if ( ( editRecord != null ) && !editRecord.isComplete(  ) && ( entry != null ) &&
                    entry instanceof EntryTypeDownloadUrl && ( entry.getFields(  ) != null ) &&
                    !entry.getFields(  ).isEmpty(  ) )
            {
                try
                {
                    // Remove file of the record field first
                    _editRecordService.doRemoveFile( editRecord, entry );
                }
                catch ( HttpAccessException e )
                {
                    _editRecordService.setSiteMessage( request, EditRecordConstants.MESSAGE_ERROR_REMOVING_FILE,
                        SiteMessage.TYPE_STOP, null );
                }
            }
        }
    }
}
