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
package fr.paris.lutece.plugins.workflow.modules.editrecord.business;

import fr.paris.lutece.plugins.directory.utils.DirectoryUtils;
import fr.paris.lutece.plugins.workflow.business.StateHome;
import fr.paris.lutece.plugins.workflow.business.task.Task;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.TaskEditRecordConfigService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.plugins.workflow.service.WorkflowPlugin;
import fr.paris.lutece.portal.business.workflow.State;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.xml.XmlUtil;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 *
 * TaskEditRecord
 *
 */
public class TaskEditRecord extends Task
{
    // TEMPLATES
    private static final String TEMPLATE_TASK_EDIT_RECORD_CONFIG = "admin/plugins/workflow/modules/editrecord/task_edit_record_config.html";
    private static final String TEMPLATE_TASK_EDIT_RECORD_FORM = "admin/plugins/workflow/modules/editrecord/task_edit_record_form.html";
    private static final String TEMPLATE_TASK_EDIT_RECORD_INFORMATION = "admin/plugins/workflow/modules/editrecord/task_edit_record_information.html";

    /**
     * {@inheritDoc}
     */
    public void init(  )
    {
    }

    /**
     * {@inheritDoc}
     */
    public void processTask( int nIdResourceHistory, HttpServletRequest request, Plugin plugin, Locale locale )
    {
        String strMessage = request.getParameter( EditRecordConstants.PARAMETER_MESSAGE +
                EditRecordConstants.UNDERSCORE + getId(  ) );
        String[] listIdsEntry = request.getParameterValues( EditRecordConstants.PARAMETER_IDS_ENTRY +
                EditRecordConstants.UNDERSCORE + getId(  ) );

        boolean bCreate = false;
        List<EditRecordValue> listEditRecordValues = new ArrayList<EditRecordValue>(  );

        EditRecord editRecord = EditRecordService.getService(  ).find( nIdResourceHistory, getId(  ) );

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
            EditRecordService.getService(  ).create( editRecord );
        }
        else
        {
            EditRecordService.getService(  ).update( editRecord );
        }
    }

    // GET

    /**
     * {@inheritDoc}
     */
    public String getDisplayConfigForm( HttpServletRequest request, Plugin plugin, Locale locale )
    {
        TaskEditRecordConfigService configService = TaskEditRecordConfigService.getService(  );
        EditRecordService editRecordService = EditRecordService.getService(  );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( EditRecordConstants.MARK_CONFIG, configService.findByPrimaryKey( getId(  ) ) );
        model.put( EditRecordConstants.MARK_LIST_STATES, editRecordService.getListStates( getAction(  ).getId(  ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_EDIT_RECORD_CONFIG, locale, model );

        return template.getHtml(  );
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayTaskForm( int nIdResource, String strResourceType, HttpServletRequest request,
        Plugin plugin, Locale locale )
    {
        TaskEditRecordConfig config = TaskEditRecordConfigService.getService(  ).findByPrimaryKey( getId(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( EditRecordConstants.MARK_CONFIG, config );
        model.put( EditRecordConstants.MARK_LIST_ENTRIES,
            EditRecordService.getService(  ).getFormListEntries( nIdResource, getId(  ), request ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_EDIT_RECORD_FORM, locale, model );

        return template.getHtml(  );
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayTaskInformation( int nIdHistory, HttpServletRequest request, Plugin plugin, Locale locale )
    {
        EditRecord editRecord = EditRecordService.getService(  ).find( nIdHistory, getId(  ) );
        TaskEditRecordConfig config = TaskEditRecordConfigService.getService(  ).findByPrimaryKey( getId(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( EditRecordConstants.MARK_CONFIG, config );
        model.put( EditRecordConstants.MARK_EDIT_RECORD, editRecord );

        if ( editRecord != null )
        {
            model.put( EditRecordConstants.MARK_LIST_ENTRIES,
                EditRecordService.getService(  ).getInformationListEntries( nIdHistory ) );
        }

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_EDIT_RECORD_INFORMATION, locale, model );

        return template.getHtml(  );
    }

    /**
     * {@inheritDoc}
     */
    public ReferenceList getTaskFormEntries( Plugin plugin, Locale locale )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getTaskInformationXml( int nIdHistory, HttpServletRequest request, Plugin plugin, Locale locale )
    {
        StringBuffer sbXml = new StringBuffer(  );
        EditRecord editRecord = EditRecordService.getService(  ).find( nIdHistory, getId(  ) );

        if ( editRecord != null )
        {
            XmlUtil.beginElement( sbXml, EditRecordConstants.TAG_EDIT_RECORD );
            XmlUtil.addElement( sbXml, EditRecordConstants.TAG_MESSAGE, editRecord.getMessage(  ) );
            XmlUtil.beginElement( sbXml, EditRecordConstants.TAG_LIST_IDS_ENTRY );

            for ( EditRecordValue editRecordValue : editRecord.getListEditRecordValues(  ) )
            {
                XmlUtil.addElement( sbXml, EditRecordConstants.TAG_ID_ENTRY, editRecordValue.getIdEntry(  ) );
            }

            XmlUtil.endElement( sbXml, EditRecordConstants.TAG_LIST_IDS_ENTRY );
            XmlUtil.endElement( sbXml, EditRecordConstants.TAG_EDIT_RECORD );
        }
        else
        {
            XmlUtil.addEmptyElement( sbXml, EditRecordConstants.TAG_EDIT_RECORD, null );
        }

        return sbXml.toString(  );
    }

    /**
     * {@inheritDoc}
     */
    public String getTitle( Plugin plugin, Locale locale )
    {
        String strTitle = StringUtils.EMPTY;
        TaskEditRecordConfig config = TaskEditRecordConfigService.getService(  ).findByPrimaryKey( getId(  ) );

        if ( ( config != null ) && ( config.getIdStateAfterEdition(  ) != DirectoryUtils.CONSTANT_ID_NULL ) )
        {
            Plugin pluginWorkflow = PluginService.getPlugin( WorkflowPlugin.PLUGIN_NAME );
            State state = StateHome.findByPrimaryKey( config.getIdStateAfterEdition(  ), pluginWorkflow );

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
    public void doRemoveConfig( Plugin plugin )
    {
        EditRecordService.getService(  ).removeByIdTask( getId(  ) );
        TaskEditRecordConfigService.getService(  ).remove( getId(  ) );
    }

    /**
     * {@inheritDoc}
     */
    public void doRemoveTaskInformation( int nIdHistory, Plugin plugin )
    {
        EditRecordService.getService(  ).removeByIdHistory( nIdHistory, getId(  ) );
    }

    /**
     * {@inheritDoc}
     */
    public String doSaveConfig( HttpServletRequest request, Locale locale, Plugin plugin )
    {
        TaskEditRecordConfigService configService = TaskEditRecordConfigService.getService(  );
        String strError = null;
        String strIdStateAfterEdition = request.getParameter( EditRecordConstants.PARAMETER_ID_STATE );
        String strField = StringUtils.EMPTY;

        if ( StringUtils.isNotBlank( strIdStateAfterEdition ) && StringUtils.isNumeric( strIdStateAfterEdition ) )
        {
            int nIdStateAfterEdition = Integer.parseInt( strIdStateAfterEdition );
            boolean bCreate = false;

            TaskEditRecordConfig editRecordConfig = configService.findByPrimaryKey( getId(  ) );

            if ( editRecordConfig == null )
            {
                editRecordConfig = new TaskEditRecordConfig(  );
                editRecordConfig.setIdTask( getId(  ) );
                bCreate = true;
            }

            editRecordConfig.setIdStateAfterEdition( nIdStateAfterEdition );

            if ( bCreate )
            {
                configService.create( editRecordConfig );
            }
            else
            {
                configService.update( editRecordConfig );
            }
        }
        else
        {
            strField = I18nService.getLocalizedString( EditRecordConstants.PROPERTY_LABEL_STATE_AFTER_EDITION, locale );
        }

        if ( StringUtils.isNotBlank( strField ) )
        {
            Object[] tabRequiredFields = { strField };
            strError = AdminMessageService.getMessageUrl( request, EditRecordConstants.MESSAGE_MANDATORY_FIELD,
                    tabRequiredFields, AdminMessage.TYPE_STOP );
        }

        return strError;
    }

    /**
     * {@inheritDoc}
     */
    public String doValidateTask( int nIdResource, String strResourceType, HttpServletRequest request, Locale locale,
        Plugin plugin )
    {
        return null;
    }

    // CHECK

    /**
     * {@inheritDoc}
     */
    public boolean isConfigRequire(  )
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFormTaskRequire(  )
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTaskForActionAutomatic(  )
    {
        return false;
    }
}
