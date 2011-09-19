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
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecord;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordPlugin;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.JSONUtils;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.web.upload.IAsynchronousUploadHandler;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Handler for asynchronous uploads.
 * Files are stored using {@link SubForm#addFileItem(String, String, FileItem)}.
 * The <code>jessionid</code> parameter should be the <strong>REAL</strong> session id,
 * not the flash player one.
 * The uploaded files are deleted by SubForm when filling fields.
 *
 */
public class EditRecordAsynchronousUploadHandler implements IAsynchronousUploadHandler
{
    private EditRecordService _editRecordService;

    /**
     * Set the edit record service
     * @param editRecordService the edit record service
     */
    public void setEditRecordService( EditRecordService editRecordService )
    {
        _editRecordService = editRecordService;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInvoked( HttpServletRequest request )
    {
        return EditRecordPlugin.PLUGIN_NAME.equals( request.getParameter( XPageAppService.PARAM_XPAGE_APP ) );
    }

    /**
     * {@inheritDoc}
     */
    public void process( HttpServletRequest request, HttpServletResponse response, JSONObject mainObject,
        List<FileItem> listFileItems )
    {
        String strIdHistory = request.getParameter( EditRecordConstants.PARAMETER_ID_HISTORY );
        String strIdTask = request.getParameter( EditRecordConstants.PARAMETER_ID_TASK );
        String strIdEntry = request.getParameter( EditRecordConstants.PARAMETER_ID_ENTRY );
        String strSessionId = request.getParameter( EditRecordConstants.PARAMETER_JSESSION_ID );

        if ( StringUtils.isNotBlank( strSessionId ) )
        {
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
                        entry instanceof EntryTypeDownloadUrl && ( listFileItems != null ) &&
                        !listFileItems.isEmpty(  ) && ( entry.getFields(  ) != null ) &&
                        !entry.getFields(  ).isEmpty(  ) )
                {
                    // Upload file
                    FileItem fileItem = listFileItems.get( 0 );
                    String strBlobStore = _editRecordService.getBlobStoreName( entry );
                    String strWSRestUrl = _editRecordService.getWSRestUrl( entry );

                    try
                    {
                        // Remove file of the record field first
                        _editRecordService.doRemoveFile( editRecord, entry );

                        // Store the uploaded file in the blobstore webapp
                        String strBlobKey = _editRecordService.doUploadFile( strWSRestUrl, fileItem, strBlobStore );
                        String strDownloadFileUrl = _editRecordService.getFileUrl( strWSRestUrl, strBlobStore,
                                strBlobKey );

                        // Update the record field
                        _editRecordService.doEditRecordField( nIdHistory, nIdEntry, strDownloadFileUrl );

                        // Build JSON
                        JSONObject jsonListFileItems = JSONUtils.getUploadedFileJSON( fileItem.getName(  ) );
                        mainObject.accumulateAll( jsonListFileItems );

                        mainObject.element( JSONUtils.JSON_KEY_ID_ENTRY, nIdEntry );
                    }
                    catch ( HttpAccessException e )
                    {
                        mainObject.accumulate( JSONUtils.JSON_KEY_ERROR,
                            I18nService.getLocalizedString( EditRecordConstants.MESSAGE_ERROR_UPLOAD,
                                request.getLocale(  ) ) );
                    }
                }
                else
                {
                    mainObject.accumulate( JSONUtils.JSON_KEY_ERROR,
                        I18nService.getLocalizedString( EditRecordConstants.MESSAGE_ERROR_UPLOAD, request.getLocale(  ) ) );
                }
            }
            else
            {
                mainObject.accumulate( JSONUtils.JSON_KEY_ERROR,
                    I18nService.getLocalizedString( EditRecordConstants.MESSAGE_ERROR_UPLOAD, request.getLocale(  ) ) );
            }
        }
        else
        {
            AppLogService.error( EditRecordAsynchronousUploadHandler.class.getName(  ) + " : Session does not exists" );
            mainObject.accumulate( JSONUtils.JSON_KEY_ERROR,
                I18nService.getLocalizedString( EditRecordConstants.MESSAGE_ERROR_UPLOAD, request.getLocale(  ) ) );
        }
    }
}
