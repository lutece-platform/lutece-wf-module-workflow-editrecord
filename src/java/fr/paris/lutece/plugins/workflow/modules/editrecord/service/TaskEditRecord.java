/*
 * Copyright (c) 2002-2012, Mairie de Paris
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

import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecord;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecordValue;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.TaskEditRecordConfig;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.state.IStateService;
import fr.paris.lutece.plugins.workflowcore.service.task.Task;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import javax.servlet.http.HttpServletRequest;


/**
 *
 * TaskEditRecord
 *
 */
public class TaskEditRecord extends Task
{
    @Inject
    private IEditRecordService _editRecordService;
    @Inject
    @Named( EditRecordConstants.BEAN_EDIT_RECORD_CONFIG_SERVICE )
    private ITaskConfigService _taskEditRecordConfigService;
    @Inject
    private IStateService _stateService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(  )
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processTask( int nIdResourceHistory, HttpServletRequest request, Locale locale )
    {
        String strMessage = request.getParameter( EditRecordConstants.PARAMETER_MESSAGE +
                EditRecordConstants.UNDERSCORE + getId(  ) );
        String[] listIdsEntry = request.getParameterValues( EditRecordConstants.PARAMETER_IDS_ENTRY +
                EditRecordConstants.UNDERSCORE + getId(  ) );

        boolean bCreate = false;
        List<EditRecordValue> listEditRecordValues = new ArrayList<EditRecordValue>(  );

        EditRecord editRecord = _editRecordService.find( nIdResourceHistory, getId(  ) );

        if ( editRecord == null )
        {
            editRecord = new EditRecord(  );
            editRecord.setIdHistory( nIdResourceHistory );
            editRecord.setIdTask( getId(  ) );
            bCreate = true;
        }

        if ( listIdsEntry != null )
        {
            for ( String strIdEntry : listIdsEntry )
            {
                if ( StringUtils.isNotBlank( strIdEntry ) && StringUtils.isNumeric( strIdEntry ) )
                {
                    int nIdEntry = Integer.parseInt( strIdEntry );
                    EditRecordValue editRecordValue = new EditRecordValue(  );
                    editRecordValue.setIdEntry( nIdEntry );

                    listEditRecordValues.add( editRecordValue );
                }
            }
        }

        editRecord.setMessage( StringUtils.isNotBlank( strMessage ) ? strMessage : StringUtils.EMPTY );
        editRecord.setListEditRecordValues( listEditRecordValues );
        editRecord.setIsComplete( false );

        if ( bCreate )
        {
            _editRecordService.create( editRecord );
        }
        else
        {
            _editRecordService.update( editRecord );
        }
    }

    // GET

    /**
         * {@inheritDoc}
         */
    @Override
    public Map<String, String> getTaskFormEntries( Locale locale )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle( Locale locale )
    {
        String strTitle = StringUtils.EMPTY;
        TaskEditRecordConfig config = _taskEditRecordConfigService.findByPrimaryKey( getId(  ) );

        if ( ( config != null ) && ( config.getIdStateAfterEdition(  ) != DirectoryUtils.CONSTANT_ID_NULL ) )
        {
            State state = _stateService.findByPrimaryKey( config.getIdStateAfterEdition(  ) );

            if ( state != null )
            {
                strTitle = state.getName(  );
            }
        }

        return strTitle;
    }

    // DO

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRemoveConfig(  )
    {
        _editRecordService.removeByIdTask( getId(  ) );
        _taskEditRecordConfigService.remove( getId(  ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doRemoveTaskInformation( int nIdHistory )
    {
        _editRecordService.removeByIdHistory( nIdHistory, getId(  ) );
    }
}
