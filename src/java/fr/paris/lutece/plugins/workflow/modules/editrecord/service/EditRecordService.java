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

import fr.paris.lutece.plugins.directory.business.EntryFilter;
import fr.paris.lutece.plugins.directory.business.EntryHome;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.PhysicalFile;
import fr.paris.lutece.plugins.directory.business.PhysicalFileHome;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.directory.business.RecordFieldFilter;
import fr.paris.lutece.plugins.directory.business.RecordFieldHome;
import fr.paris.lutece.plugins.directory.business.RecordHome;
import fr.paris.lutece.plugins.directory.service.DirectoryPlugin;
import fr.paris.lutece.plugins.directory.utils.DirectoryErrorException;
import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.workflow.business.ActionHome;
import fr.paris.lutece.plugins.workflow.business.ResourceHistory;
import fr.paris.lutece.plugins.workflow.business.ResourceHistoryHome;
import fr.paris.lutece.plugins.workflow.business.ResourceWorkflow;
import fr.paris.lutece.plugins.workflow.business.ResourceWorkflowHome;
import fr.paris.lutece.plugins.workflow.business.StateFilter;
import fr.paris.lutece.plugins.workflow.business.StateHome;
import fr.paris.lutece.plugins.workflow.business.task.ITask;
import fr.paris.lutece.plugins.workflow.business.task.TaskHome;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecord;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecordHome;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecordValue;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.TaskEditRecordConfig;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.signrequest.EditRecordRequestAuthenticatorService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.plugins.workflow.service.WorkflowPlugin;
import fr.paris.lutece.plugins.workflow.service.WorkflowService;
import fr.paris.lutece.plugins.workflow.utils.WorkflowUtils;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.workflow.Action;
import fr.paris.lutece.portal.business.workflow.State;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 *
 * EditRecordService
 *
 */
public final class EditRecordService
{
    private static final String BEAN_EDIT_RECORD_SERVICE = "workflow-editrecord.editRecordService";
    private EditRecordValueService _editRecordValueService;

    /**
     * Private constructor
     */
    private EditRecordService(  )
    {
    }

    /**
     * Get the instance of the service
     * @return the instance of the service
     */
    public static EditRecordService getService(  )
    {
        return (EditRecordService) SpringContextService.getPluginBean( EditRecordPlugin.PLUGIN_NAME,
            BEAN_EDIT_RECORD_SERVICE );
    }

    // SET

    /**
     * Set the site message
     * @param request the HTTP request
     * @param strMessage the message
     * @param nTypeMessage the message type
     * @param strUrlReturn the url return
     * @throws SiteMessageException the site message
     */
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

    /**
     * Set the edit record value service
     * @param editRecordValueService the edit record value service
     */
    public void setEditRecordValueService( EditRecordValueService editRecordValueService )
    {
        _editRecordValueService = editRecordValueService;
    }

    // CRUD

    /**
     * Create an edit record
     * @param editRecord the edit record
     * @return the newly created edit record primary key
     */
    public int create( EditRecord editRecord )
    {
        int nKey = DirectoryUtils.CONSTANT_ID_NULL;

        if ( editRecord != null )
        {
            nKey = EditRecordHome.create( editRecord );

            for ( EditRecordValue editRecordValue : editRecord.getListEditRecordValues(  ) )
            {
                _editRecordValueService.create( editRecordValue );
            }
        }

        return nKey;
    }

    /**
     * Update an edit record
     * @param editRecord the edit record
     */
    public void update( EditRecord editRecord )
    {
        if ( editRecord != null )
        {
            EditRecordHome.update( editRecord );
            // Remove its edit record values first
            _editRecordValueService.remove( editRecord.getIdEditRecord(  ) );

            for ( EditRecordValue editRecordValue : editRecord.getListEditRecordValues(  ) )
            {
                _editRecordValueService.create( editRecordValue );
            }
        }
    }

    /**
     * Find an edit record
     * @param nIdRecord the id record
     * @param nIdTask the id task
     * @return a edit record
     */
    public EditRecord find( int nIdRecord, int nIdTask )
    {
        EditRecord editRecord = EditRecordHome.find( nIdRecord, nIdTask );

        if ( editRecord != null )
        {
            editRecord.setListEditRecordValues( _editRecordValueService.find( editRecord.getIdEditRecord(  ) ) );
        }

        return editRecord;
    }

    /**
     * Remove an edit record
     * @param nIdRecord the id record
     * @param nIdTask the id task
     */
    public void removeByIdRecord( int nIdRecord, int nIdTask )
    {
        EditRecord editRecord = find( nIdRecord, nIdTask );
        _editRecordValueService.remove( editRecord.getIdEditRecord(  ) );
        EditRecordHome.removeByIdRecord( nIdRecord, nIdTask );
    }

    /**
     * Remove an edit record by id task
     * @param nIdTask the id task
     */
    public void removeByIdTask( int nIdTask )
    {
        for ( EditRecord editRecord : EditRecordHome.findByIdTask( nIdTask ) )
        {
            _editRecordValueService.remove( editRecord.getIdEditRecord(  ) );
        }

        EditRecordHome.removeByTask( nIdTask );
    }

    // GET

    /**
     * Get the list of states
     * @param nIdAction the id action
     * @return a ReferenceList
     */
    public ReferenceList getListStates( int nIdAction )
    {
        Plugin pluginWorkflow = PluginService.getPlugin( WorkflowPlugin.PLUGIN_NAME );
        ReferenceList referenceListStates = new ReferenceList(  );
        Action action = ActionHome.findByPrimaryKey( nIdAction, pluginWorkflow );

        if ( ( action != null ) && ( action.getWorkflow(  ) != null ) )
        {
            StateFilter stateFilter = new StateFilter(  );
            stateFilter.setIdWorkflow( action.getWorkflow(  ).getId(  ) );

            List<State> listStates = StateHome.getListStateByFilter( stateFilter, pluginWorkflow );

            referenceListStates.addItem( DirectoryUtils.CONSTANT_ID_NULL, StringUtils.EMPTY );
            referenceListStates.addAll( ReferenceList.convert( listStates, EditRecordConstants.ID,
                    EditRecordConstants.NAME, true ) );
        }

        return referenceListStates;
    }

    /**
     * Get the list of entries for the form
     * @param nIdRecord the id record
     * @param nIdTask the id task
     * @param request the HTTP request
     * @return a list of entries
     */
    public List<IEntry> getFormListEntries( int nIdRecord, int nIdTask, HttpServletRequest request )
    {
        AdminUser user = AdminUserService.getAdminUser( request );
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<IEntry> listEntries = new ArrayList<IEntry>(  );

        Record record = RecordHome.findByPrimaryKey( nIdRecord, pluginDirectory );

        if ( record != null )
        {
            listEntries = DirectoryUtils.getFormEntries( record.getDirectory(  ).getIdDirectory(  ), pluginDirectory,
                    user );
        }

        return listEntries;
    }

    /**
     * Get the list of entries for information
     * @param nIdEditRecord the id edit record
     * @return a list of entries
     */
    public List<IEntry> getInformationListEntries( int nIdEditRecord )
    {
        List<EditRecordValue> listEditRecordValues = EditRecordValueService.getService(  ).find( nIdEditRecord );
        List<IEntry> listEntries = new ArrayList<IEntry>(  );
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        for ( EditRecordValue editRecordValue : listEditRecordValues )
        {
            IEntry entry = EntryHome.findByPrimaryKey( editRecordValue.getIdEntry(  ), pluginDirectory );

            if ( entry != null )
            {
                listEntries.add( entry );
            }
        }

        return listEntries;
    }

    /**
     * Get the list of entries to edit
     * @param request the HTTP request
     * @param listEditRecordValues the list of edit record values
     * @return a list of entries
     */
    public List<IEntry> getListEntriesToEdit( HttpServletRequest request, List<EditRecordValue> listEditRecordValues )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<IEntry> listEntries = new ArrayList<IEntry>(  );

        for ( EditRecordValue editRecordValue : listEditRecordValues )
        {
            IEntry entry = EntryHome.findByPrimaryKey( editRecordValue.getIdEntry(  ), pluginDirectory );

            if ( entry.isRoleAssociated(  ) )
            {
                entry.setFields( DirectoryUtils.getAuthorizedFieldsByRole( request, entry.getFields(  ) ) );
            }

            listEntries.add( entry );
        }

        return listEntries;
    }

    /**
     * Get the list of entries to not edit
     * @param request the HTTP request
     * @param nIdRecord the id record
     * @param listEditRecordValues the list of edit record values
     * @return a list of entries
     */
    public List<IEntry> getListEntriesToNotEdit( HttpServletRequest request, int nIdRecord,
        List<EditRecordValue> listEditRecordValues )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        List<IEntry> listEntriesToNotEdit = new ArrayList<IEntry>(  );
        Record record = RecordHome.findByPrimaryKey( nIdRecord, pluginDirectory );

        if ( record != null )
        {
            // List all entries of the directory
            EntryFilter eFilter = new EntryFilter(  );
            eFilter.setIdDirectory( record.getDirectory(  ).getIdDirectory(  ) );

            List<IEntry> listEntries = EntryHome.getEntryList( eFilter, pluginDirectory );

            // List entries to edit
            List<IEntry> listEntriesToEdit = getListEntriesToEdit( request, listEditRecordValues );

            // Extract the entries to not edit from the list of all entries
            for ( IEntry entry : listEntries )
            {
                boolean bIsEntryToEdit = false;

                for ( IEntry entryToEdit : listEntriesToEdit )
                {
                    if ( entry.getIdEntry(  ) == entryToEdit.getIdEntry(  ) )
                    {
                        bIsEntryToEdit = true;

                        break;
                    }
                }

                if ( !bIsEntryToEdit )
                {
                    listEntriesToNotEdit.add( entry );
                }
            }
        }

        return listEntriesToNotEdit;
    }

    /**
     * Get the list of record fieds to not edit
     * @param request the HTTP request
     * @param nIdRecord the id record
     * @param listEditRecordValues the list of edit record values
     * @return a list of record fields
     */
    public List<RecordField> getListRecordFieldsToNotEdit( HttpServletRequest request, int nIdRecord,
        List<EditRecordValue> listEditRecordValues )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        // List record fields to not edit
        List<RecordField> listRecordFieldsToNotEdit = new ArrayList<RecordField>(  );

        // List all record fields of the record
        RecordFieldFilter filter = new RecordFieldFilter(  );
        filter.setIdRecord( nIdRecord );

        List<RecordField> listRecordField = RecordFieldHome.getRecordFieldList( filter, pluginDirectory );

        // List entries to not edit
        List<IEntry> listEntriesToNotEdit = getListEntriesToNotEdit( request, nIdRecord, listEditRecordValues );

        // Extract the record field to not edit from the list of all record fields
        for ( RecordField recordField : listRecordField )
        {
            for ( IEntry entry : listEntriesToNotEdit )
            {
                if ( recordField.getEntry(  ).getIdEntry(  ) == entry.getIdEntry(  ) )
                {
                    if ( recordField.getFile(  ) != null )
                    {
                        PhysicalFile physicalFile = PhysicalFileHome.findByPrimaryKey( recordField.getFile(  )
                                                                                                  .getPhysicalFile(  )
                                                                                                  .getIdPhysicalFile(  ),
                                pluginDirectory );
                        recordField.getFile(  ).setPhysicalFile( physicalFile );
                    }

                    listRecordFieldsToNotEdit.add( recordField );
                }
            }
        }

        return listRecordFieldsToNotEdit;
    }

    /**
     * Get the map id entry - list record fields
     * @param listEntries the list of entries to edit
     * @param nIdRecord the id record
     * @return a map of id entry - list record fields
     */
    public Map<String, List<RecordField>> getMapIdEntryListRecordField( List<IEntry> listEntries, int nIdRecord )
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );

        return DirectoryUtils.getMapIdEntryListRecordField( listEntries, nIdRecord, pluginDirectory );
    }

    // DO

    /**
     * Do edit the record data
     * @param request the HTTP request
     * @param nIdRecord the id record
     * @throws SiteMessageException site message if there is a problem
     */
    public void doEditRecordData( HttpServletRequest request, int nIdRecord )
        throws SiteMessageException
    {
        Plugin pluginDirectory = PluginService.getPlugin( DirectoryPlugin.PLUGIN_NAME );
        Record record = RecordHome.findByPrimaryKey( nIdRecord, pluginDirectory );

        if ( record != null )
        {
            List<EditRecordValue> listEditRecordValues = _editRecordValueService.find( nIdRecord );
            List<IEntry> listEntriesToEdit = getListEntriesToEdit( request, listEditRecordValues );

            List<RecordField> listRecordFields = getListRecordFieldsToNotEdit( request, nIdRecord, listEditRecordValues );

            try
            {
                for ( IEntry entry : listEntriesToEdit )
                {
                    DirectoryUtils.getDirectoryRecordFieldData( record, request, entry.getIdEntry(  ), true,
                        listRecordFields, pluginDirectory, request.getLocale(  ) );
                }

                record.setListRecordField( listRecordFields );
            }
            catch ( DirectoryErrorException error )
            {
                if ( error.isMandatoryError(  ) )
                {
                    Object[] tabRequiredFields = { error.getTitleField(  ) };
                    SiteMessageService.setMessage( request, EditRecordConstants.MESSAGE_MANDATORY_FIELD,
                        tabRequiredFields, SiteMessage.TYPE_STOP );
                }
                else
                {
                    Object[] tabRequiredFields = { error.getTitleField(  ), error.getErrorMessage(  ) };
                    SiteMessageService.setMessage( request, EditRecordConstants.MESSAGE_DIRECTORY_ERROR,
                        tabRequiredFields, SiteMessage.TYPE_STOP );
                }
            }

            RecordHome.updateWidthRecordField( record, pluginDirectory );
        }
        else
        {
            setSiteMessage( request, EditRecordConstants.MESSAGE_APP_ERROR, SiteMessage.TYPE_STOP,
                request.getParameter( EditRecordConstants.PARAMETER_URL_RETURN ) );
        }
    }

    /**
     * Do change the record state
     * @param nIdRecord the id record
     * @param nIdTask the id task
     * @param locale the locale
     */
    public void doChangeRecordState( int nIdRecord, int nIdTask, Locale locale )
    {
        Plugin pluginWorkflow = PluginService.getPlugin( WorkflowPlugin.PLUGIN_NAME );
        ITask task = TaskHome.findByPrimaryKey( nIdTask, pluginWorkflow, locale );
        TaskEditRecordConfig config = TaskEditRecordConfigService.getService(  ).findByPrimaryKey( nIdTask );

        if ( ( task != null ) && ( config != null ) )
        {
            State state = StateHome.findByPrimaryKey( config.getIdStateAfterEdition(  ), pluginWorkflow );
            Action action = ActionHome.findByPrimaryKey( task.getAction(  ).getId(  ), pluginWorkflow );

            if ( ( state != null ) && ( action != null ) )
            {
                // Create Resource History
                ResourceHistory resourceHistory = new ResourceHistory(  );
                resourceHistory.setIdResource( nIdRecord );
                resourceHistory.setResourceType( Record.WORKFLOW_RESOURCE_TYPE );
                resourceHistory.setAction( action );
                resourceHistory.setWorkFlow( action.getWorkflow(  ) );
                resourceHistory.setCreationDate( WorkflowUtils.getCurrentTimestamp(  ) );
                resourceHistory.setUserAccessCode( EditRecordConstants.USER_AUTO );
                ResourceHistoryHome.create( resourceHistory, pluginWorkflow );

                // Update Resource
                ResourceWorkflow resourceWorkflow = ResourceWorkflowHome.findByPrimaryKey( nIdRecord,
                        Record.WORKFLOW_RESOURCE_TYPE, action.getWorkflow(  ).getId(  ), pluginWorkflow );
                resourceWorkflow.setState( state );
                ResourceWorkflowHome.update( resourceWorkflow, pluginWorkflow );

                // if new state have action automatic
                WorkflowService.getInstance(  )
                               .executeActionAutomatic( nIdRecord, Record.WORKFLOW_RESOURCE_TYPE,
                    action.getWorkflow(  ).getId(  ), resourceWorkflow.getExternalParentId(  ) );

                // Remove EditRecord and EditRecordValues
                removeByIdRecord( nIdRecord, nIdTask );
            }
        }
    }

    // CHECK

    /**
     * Check if the request is authenticated or not
     * @param request the HTTP request
     * @return true if the requet is authenticated, false otherwise
     */
    public boolean isRequestAuthenticated( HttpServletRequest request )
    {
        return EditRecordRequestAuthenticatorService.getRequestAuthenticator(  ).isRequestAuthenticated( request );
    }
}
