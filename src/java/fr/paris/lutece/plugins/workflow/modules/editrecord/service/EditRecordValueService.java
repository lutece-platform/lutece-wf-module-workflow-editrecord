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
package fr.paris.lutece.plugins.workflow.modules.editrecord.service;

import fr.paris.lutece.plugins.directory.business.EntryTypeDownloadUrl;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecordValue;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecordValueHome;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.httpaccess.HttpAccessException;

import org.apache.commons.lang.StringUtils;

import java.util.List;


/**
 *
 * EditRecordValueService
 *
 */
public final class EditRecordValueService
{
    private static final String BEAN_EDIT_RECORD_VALUE_SERVICE = "workflow-editrecord.editRecordValueService";
    private EditRecordService _editRecordService;

    /**
     * Private constructor
     */
    private EditRecordValueService(  )
    {
    }

    /**
     * Get the instance of the service
     * @return the instance of the service
     */
    public static EditRecordValueService getService(  )
    {
        return (EditRecordValueService) SpringContextService.getPluginBean( EditRecordPlugin.PLUGIN_NAME,
            BEAN_EDIT_RECORD_VALUE_SERVICE );
    }

    /**
     * Set the edit record service
     * @param editRecordService the edit record service
     */
    public void setEditRecordService( EditRecordService editRecordService )
    {
        _editRecordService = editRecordService;
    }

    /**
     * Create a new edit record value
     * @param editRecordValue the edit record value
     */
    public void create( EditRecordValue editRecordValue )
    {
        EditRecordValueHome.create( editRecordValue );
    }

    /**
     * Find edit record values from a given id history
     * @param nIdHistory the id history
     * @return a list of EditRecordValue
     */
    public List<EditRecordValue> find( int nIdHistory )
    {
        List<EditRecordValue> listEditRecordValues = EditRecordValueHome.find( nIdHistory );

        for ( EditRecordValue editRecordValue : listEditRecordValues )
        {
            editRecordValue.setFileName( getFileName( editRecordValue ) );
        }

        return listEditRecordValues;
    }

    /**
     * Remove EditRecordValue from a given id edit record
     * @param nIdHistory the id history
     */
    public void remove( int nIdHistory )
    {
        EditRecordValueHome.remove( nIdHistory );
    }

    /**
     * Get the file name
     * @param editRecordValue the edit record value
     * @return the file name
     */
    public String getFileName( EditRecordValue editRecordValue )
    {
        String strFileName = StringUtils.EMPTY;
        IEntry entry = _editRecordService.getEntry( editRecordValue.getIdEntry(  ) );

        if ( ( entry != null ) && entry instanceof EntryTypeDownloadUrl )
        {
            RecordField recordField = _editRecordService.getRecordField( editRecordValue.getIdHistory(  ),
                    editRecordValue.getIdEntry(  ) );

            if ( recordField != null )
            {
                String strUrl = entry.convertRecordFieldTitleToString( recordField, null, false );

                if ( StringUtils.isNotBlank( strUrl ) )
                {
                    try
                    {
                        strFileName = _editRecordService.getFileName( strUrl );
                    }
                    catch ( HttpAccessException e )
                    {
                        AppLogService.error( e );
                    }
                }
            }
        }

        return strFileName;
    }
}
