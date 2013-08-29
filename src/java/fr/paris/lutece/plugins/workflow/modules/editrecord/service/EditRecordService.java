/*
 * Copyright (c) 2002-2013, Mairie de Paris
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

import fr.paris.lutece.plugins.directory.business.EntryFilter;
import fr.paris.lutece.plugins.directory.business.EntryHome;
import fr.paris.lutece.plugins.directory.business.EntryType;
import fr.paris.lutece.plugins.directory.business.EntryTypeDownloadUrl;
import fr.paris.lutece.plugins.directory.business.EntryTypeHome;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.business.RecordFieldFilter;
import fr.paris.lutece.plugins.directory.business.RecordFieldHome;
import fr.paris.lutece.plugins.directory.business.RecordHome;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.service.upload.DirectoryAsynchronousUploadHandler;
import fr.paris.lutece.plugins.directory.utils.DirectoryErrorException;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecord;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecordValue;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.IEditRecordDAO;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.TaskEditRecordConfig;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.signrequest.EditRecordRequestAuthenticatorService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.plugins.workflowcore.business.action.Action;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceHistory;
import fr.paris.lutece.plugins.workflowcore.business.resource.ResourceWorkflow;
import fr.paris.lutece.plugins.workflowcore.business.state.State;
import fr.paris.lutece.plugins.workflowcore.business.state.StateFilter;
import fr.paris.lutece.plugins.workflowcore.service.action.IActionService;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceHistoryService;
import fr.paris.lutece.plugins.workflowcore.service.resource.IResourceWorkflowService;
import fr.paris.lutece.plugins.workflowcore.service.state.IStateService;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.plugins.workflowcore.service.task.ITaskService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.workflow.WorkflowService;
import fr.paris.lutece.util.ReferenceList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * EditRecordService
 * 
 */
public class EditRecordService implements IEditRecordService
{
    public static final String BEAN_SERVICE = "workflow-editrecord.editRecordService";

    // SERVICES
    @Inject
    private IEditRecordValueService _editRecordValueService;
    @Inject
    private ITaskService _taskService;
    @Inject
    private IStateService _stateService;
    @Inject
    private IResourceWorkflowService _resourceWorkflowService;
    @Inject
    private IResourceHistoryService _resourceHistoryService;
    @Inject
    @Named( EditRecordConstants.BEAN_EDIT_RECORD_CONFIG_SERVICE )
    private ITaskConfigService _taskEditRecordConfigService;
    @Inject
    private IActionService _actionService;

    // DAO
    @Inject
    private IEditRecordDAO _editRecordDAO;

    // SET

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSiteMessage( HttpServletRequest request, String strMessage, int nTypeMessage, String strUrlReturn )
            throws SiteMessageException
    {
        if ( StringUtils.isNotBlank( strUrlReturn ) )
        {
            SiteMessageService.setMessage( request, strMessage, nTypeMessage, strUrlReturn );
        }
        else
        {
            SiteMessageService.setMessage( request, strMessage, nTypeMessage );
        }
    }

    // CRUD

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( EditRecordPlugin.BEAN_TRANSACTION_MANAGER )
    public void create( EditRecord editRecord )
    {
        if ( editRecord != null )
        {
            _editRecordDAO.insert( editRecord, PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );

            for ( EditRecordValue editRecordValue : editRecord.getListEditRecordValues( ) )
            {
                editRecordValue.setIdHistory( editRecord.getIdHistory( ) );
                _editRecordValueService.create( editRecordValue );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( EditRecordPlugin.BEAN_TRANSACTION_MANAGER )
    public void update( EditRecord editRecord )
    {
        if ( editRecord != null )
        {
            _editRecordDAO.store( editRecord, PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );
            // Remove its edit record values first
            _editRecordValueService.remove( editRecord.getIdHistory( ) );

            for ( EditRecordValue editRecordValue : editRecord.getListEditRecordValues( ) )
            {
                editRecordValue.setIdHistory( editRecord.getIdHistory( ) );
                _editRecordValueService.create( editRecordValue );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EditRecord find( int nIdHistory, int nIdTask )
    {
        EditRecord editRecord = _editRecordDAO.load( nIdHistory, nIdTask,
                PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );

        if ( editRecord != null )
        {
            editRecord.setListEditRecordValues( _editRecordValueService.find( editRecord.getIdHistory( ) ) );
        }

        return editRecord;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EditRecord> findByIdTask( int nIdTask )
    {
        return _editRecordDAO.loadByIdTask( nIdTask, PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( EditRecordPlugin.BEAN_TRANSACTION_MANAGER )
    public void removeByIdHistory( int nIdHistory, int nIdTask )
    {
        EditRecord editRecord = find( nIdHistory, nIdTask );

        if ( editRecord != null )
        {
            _editRecordValueService.remove( editRecord.getIdHistory( ) );
            _editRecordDAO.deleteByIdHistory( nIdHistory, nIdTask,
                    PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( EditRecordPlugin.BEAN_TRANSACTION_MANAGER )
    public void removeByIdTask( int nIdTask )
    {
        for ( EditRecord editRecord : findByIdTask( nIdTask ) )
        {
            _editRecordValueService.remove( editRecord.getIdHistory( ) );
        }

        _editRecordDAO.deleteByIdTask( nIdTask, PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );
    }

    // GET

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceList getListStates( int nIdAction )
    {
        ReferenceList referenceListStates = new ReferenceList( );
        Action action = _actionService.findByPrimaryKey( nIdAction );

        if ( ( action != null ) && ( action.getWorkflow( ) != null ) )
        {
            StateFilter stateFilter = new StateFilter( );
            stateFilter.setIdWorkflow( action.getWorkflow( ).getId( ) );

            List<State> listStates = _stateService.getListStateByFilter( stateFilter );

            referenceListStates.addItem( DirectoryUtils.CONSTANT_ID_NULL, StringUtils.EMPTY );
            referenceListStates.addAll( ReferenceList.convert( listStates, EditRecordConstants.ID,
                    EditRecordConstants.NAME, true ) );
        }

        return referenceListStates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IEntry> getFormListEntries( int nIdRecord, int nIdTask, HttpServletRequest request )
    {
        AdminUser user = AdminUserService.getAdminUser( request );
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<IEntry> listEntries = new ArrayList<IEntry>( );

        Record record = RecordHome.findByPrimaryKey( nIdRecord, pluginDirectory );

        if ( record != null )
        {
            listEntries = DirectoryUtils.getFormEntries( record.getDirectory( ).getIdDirectory( ), pluginDirectory,
                    user );
        }

        return listEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IEntry> getInformationListEntries( int nIdHistory )
    {
        List<EditRecordValue> listEditRecordValues = _editRecordValueService.find( nIdHistory );
        List<IEntry> listEntries = new ArrayList<IEntry>( );
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        for ( EditRecordValue editRecordValue : listEditRecordValues )
        {
            IEntry entry = EntryHome.findByPrimaryKey( editRecordValue.getIdEntry( ), pluginDirectory );

            if ( entry != null )
            {
                listEntries.add( entry );
            }
        }

        return listEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IEntry> getListEntriesToEdit( HttpServletRequest request, List<EditRecordValue> listEditRecordValues )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<IEntry> listEntries = new ArrayList<IEntry>( );

        for ( EditRecordValue editRecordValue : listEditRecordValues )
        {
            IEntry entry = EntryHome.findByPrimaryKey( editRecordValue.getIdEntry( ), pluginDirectory );

            if ( entry.isRoleAssociated( ) )
            {
                entry.setFields( DirectoryUtils.getAuthorizedFieldsByRole( request, entry.getFields( ) ) );
            }

            listEntries.add( entry );
        }

        return listEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> getListIdEntriesToNotEdit( HttpServletRequest request, int nIdRecord,
            List<EditRecordValue> listEditRecordValues )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<Integer> listIdEntriesToNotEdit = new ArrayList<Integer>( );
        Record record = RecordHome.findByPrimaryKey( nIdRecord, pluginDirectory );

        if ( record != null )
        {
            // List all entries of the directory
            EntryFilter eFilter = new EntryFilter( );
            eFilter.setIdDirectory( record.getDirectory( ).getIdDirectory( ) );

            List<IEntry> listEntries = EntryHome.getEntryList( eFilter, pluginDirectory );

            // List entries to edit
            List<IEntry> listEntriesToEdit = getListEntriesToEdit( request, listEditRecordValues );

            // Extract the entries to not edit from the list of all entries
            for ( IEntry entry : listEntries )
            {
                boolean bIsEntryToEdit = false;

                for ( IEntry entryToEdit : listEntriesToEdit )
                {
                    if ( entry.getIdEntry( ) == entryToEdit.getIdEntry( ) )
                    {
                        bIsEntryToEdit = true;

                        break;
                    }
                }

                if ( !bIsEntryToEdit )
                {
                    listIdEntriesToNotEdit.add( entry.getIdEntry( ) );
                }
            }
        }

        return listIdEntriesToNotEdit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RecordField> getListRecordFieldsToNotEdit( HttpServletRequest request, int nIdRecord,
            List<EditRecordValue> listEditRecordValues )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<RecordField> listRecordFieldsToNotEdit = new ArrayList<RecordField>( );
        List<Integer> listIdEntriesToNotEdit = getListIdEntriesToNotEdit( request, nIdRecord, listEditRecordValues );

        if ( ( listIdEntriesToNotEdit != null ) && !listIdEntriesToNotEdit.isEmpty( ) )
        {
            // List record fields to not edit
            listRecordFieldsToNotEdit = RecordFieldHome.getRecordFieldSpecificList( listIdEntriesToNotEdit, nIdRecord,
                    pluginDirectory );
        }

        return listRecordFieldsToNotEdit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<RecordField>> getMapIdEntryListRecordField( List<IEntry> listEntries, int nIdHistory )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        Record record = getRecordFromIdHistory( nIdHistory );

        return DirectoryUtils.getMapIdEntryListRecordField( listEntries, record.getIdRecord( ), pluginDirectory );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IEntry getEntry( int nIdEntry )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        return EntryHome.findByPrimaryKey( nIdEntry, pluginDirectory );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntryType getEntryTypeDownloadUrl( )
    {
        EntryType entryTypeDownloadUrl = null;
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        for ( EntryType entryType : EntryTypeHome.getList( pluginDirectory ) )
        {
            if ( EntryTypeDownloadUrl.class.getName( ).equals( entryType.getClassName( ) ) )
            {
                entryTypeDownloadUrl = entryType;

                break;
            }
        }

        return entryTypeDownloadUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Record getRecordFromIdHistory( int nIdHistory )
    {
        Record record = null;
        ResourceHistory resourceHistory = _resourceHistoryService.findByPrimaryKey( nIdHistory );

        if ( ( resourceHistory != null ) && Record.WORKFLOW_RESOURCE_TYPE.equals( resourceHistory.getResourceType( ) ) )
        {
            Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

            // Record
            record = RecordHome.findByPrimaryKey( resourceHistory.getIdResource( ), pluginDirectory );
        }

        return record;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RecordField> getRecordFieldsList( int nIdHistory, int nIdEntry )
    {
        List<RecordField> listRecordFields = null;
        Record record = getRecordFromIdHistory( nIdHistory );

        if ( record != null )
        {
            Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
            RecordFieldFilter recordFieldFilter = new RecordFieldFilter( );
            recordFieldFilter.setIdDirectory( record.getDirectory( ).getIdDirectory( ) );
            recordFieldFilter.setIdEntry( nIdEntry );
            recordFieldFilter.setIdRecord( record.getIdRecord( ) );

            listRecordFields = RecordFieldHome.getRecordFieldList( recordFieldFilter, pluginDirectory );
        }

        return listRecordFields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecordField getRecordFieldDownloadUrl( int nIdHistory, int nIdEntry )
    {
        RecordField recordField = null;
        List<RecordField> listRecordField = getRecordFieldsList( nIdHistory, nIdEntry );

        if ( ( listRecordField != null ) && !listRecordField.isEmpty( ) )
        {
            recordField = listRecordField.get( 0 );
        }

        return recordField;
    }

    // DO

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doEditRecordData( HttpServletRequest request, EditRecord editRecord ) throws SiteMessageException
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        Record record = getRecordFromIdHistory( editRecord.getIdHistory( ) );

        if ( record != null )
        {
            String strUploadAction = DirectoryAsynchronousUploadHandler.getHandler( ).getUploadAction( request );
            List<IEntry> listEntriesToEdit = getListEntriesToEdit( request, editRecord.getListEditRecordValues( ) );
            List<RecordField> listRecordFields = getListRecordFieldsToNotEdit( request, record.getIdRecord( ),
                    editRecord.getListEditRecordValues( ) );

            for ( IEntry entry : listEntriesToEdit )
            {
                try
                {
                    DirectoryUtils.getDirectoryRecordFieldData( record, request, entry.getIdEntry( ), true,
                            listRecordFields, pluginDirectory, request.getLocale( ) );
                }
                catch ( DirectoryErrorException error )
                {
                    // Case if the user does not upload a file, then throw the error message
                    if ( StringUtils.isBlank( strUploadAction ) )
                    {
                        if ( error.isMandatoryError( ) )
                        {
                            Object[] tabRequiredFields = { error.getTitleField( ) };
                            SiteMessageService.setMessage( request, EditRecordConstants.MESSAGE_MANDATORY_FIELD,
                                    tabRequiredFields, SiteMessage.TYPE_STOP );
                        }
                        else
                        {
                            Object[] tabRequiredFields = { error.getTitleField( ), error.getErrorMessage( ) };
                            SiteMessageService.setMessage( request, EditRecordConstants.MESSAGE_DIRECTORY_ERROR,
                                    tabRequiredFields, SiteMessage.TYPE_STOP );
                        }
                    }
                }
            }

            record.setListRecordField( listRecordFields );

            // Special case for upload fields : if no action is specified, a submit
            // button associated with an upload might have been pressed :
            if ( StringUtils.isNotBlank( strUploadAction ) )
            {
                Map<String, List<RecordField>> mapListRecordFields = DirectoryUtils
                        .buildMapIdEntryListRecordField( record );

                // Upload the file
                try
                {
                    DirectoryAsynchronousUploadHandler.getHandler( ).doUploadAction( request, strUploadAction,
                            mapListRecordFields, record, pluginDirectory );
                }
                catch ( DirectoryErrorException error )
                {
                    if ( error.isMandatoryError( ) )
                    {
                        Object[] tabRequiredFields = { error.getTitleField( ) };
                        SiteMessageService.setMessage( request, EditRecordConstants.MESSAGE_MANDATORY_FIELD,
                                tabRequiredFields, SiteMessage.TYPE_STOP );
                    }
                    else
                    {
                        Object[] tabRequiredFields = { error.getTitleField( ), error.getErrorMessage( ) };
                        SiteMessageService.setMessage( request, EditRecordConstants.MESSAGE_DIRECTORY_ERROR,
                                tabRequiredFields, SiteMessage.TYPE_STOP );
                    }
                }

                // Put the map <idEntry, RecordFields> in the session
                request.getSession( ).setAttribute(
                        EditRecordConstants.SESSION_EDIT_RECORD_LIST_SUBMITTED_RECORD_FIELDS, mapListRecordFields );

                return false;
            }

            RecordHome.updateWidthRecordField( record, pluginDirectory );

            return true;
        }

        setSiteMessage( request, EditRecordConstants.MESSAGE_APP_ERROR, SiteMessage.TYPE_STOP,
                request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN ) );

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doChangeRecordState( EditRecord editRecord, Locale locale )
    {
        ITask task = _taskService.findByPrimaryKey( editRecord.getIdTask( ), locale );
        TaskEditRecordConfig config = _taskEditRecordConfigService.findByPrimaryKey( editRecord.getIdTask( ) );

        if ( ( task != null ) && ( config != null ) )
        {
            State state = _stateService.findByPrimaryKey( config.getIdStateAfterEdition( ) );
            Action action = _actionService.findByPrimaryKey( task.getAction( ).getId( ) );

            if ( ( state != null ) && ( action != null ) )
            {
                Record record = getRecordFromIdHistory( editRecord.getIdHistory( ) );

                // Update Resource
                ResourceWorkflow resourceWorkflow = _resourceWorkflowService.findByPrimaryKey( record.getIdRecord( ),
                        Record.WORKFLOW_RESOURCE_TYPE, action.getWorkflow( ).getId( ) );
                resourceWorkflow.setState( state );
                _resourceWorkflowService.update( resourceWorkflow );
                WorkflowService.getInstance( ).doProcessAutomaticReflexiveActions( record.getIdRecord( ),
                        Record.WORKFLOW_RESOURCE_TYPE, action.getStateAfter( ).getId( ),
                        resourceWorkflow.getExternalParentId( ), locale );
                // if new state have action automatic
                WorkflowService.getInstance( ).executeActionAutomatic( record.getIdRecord( ),
                        Record.WORKFLOW_RESOURCE_TYPE, action.getWorkflow( ).getId( ),
                        resourceWorkflow.getExternalParentId( ) );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doCompleteEditRecord( EditRecord editRecord )
    {
        editRecord.setIsComplete( true );
        update( editRecord );
    }

    // CHECK

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequestAuthenticated( HttpServletRequest request )
    {
        return EditRecordRequestAuthenticatorService.getRequestAuthenticator( ).isRequestAuthenticated( request );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRecordStateValid( EditRecord editRecord, Locale locale )
    {
        boolean bIsValid = false;

        ITask task = _taskService.findByPrimaryKey( editRecord.getIdTask( ), locale );
        TaskEditRecordConfig config = _taskEditRecordConfigService.findByPrimaryKey( editRecord.getIdTask( ) );

        if ( ( task != null ) && ( config != null ) )
        {
            Action action = _actionService.findByPrimaryKey( task.getAction( ).getId( ) );

            if ( ( action != null ) && ( action.getStateAfter( ) != null ) )
            {
                Record record = getRecordFromIdHistory( editRecord.getIdHistory( ) );

                // Update Resource
                ResourceWorkflow resourceWorkflow = _resourceWorkflowService.findByPrimaryKey( record.getIdRecord( ),
                        Record.WORKFLOW_RESOURCE_TYPE, action.getWorkflow( ).getId( ) );

                if ( ( resourceWorkflow != null ) && ( resourceWorkflow.getState( ) != null )
                        && ( resourceWorkflow.getState( ).getId( ) == action.getStateAfter( ).getId( ) ) )
                {
                    bIsValid = true;
                }
            }
        }

        return bIsValid;
    }
}
