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

import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecord;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecordValue;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.TaskEditRecordConfig;
import fr.paris.lutece.plugins.workflow.modules.editrecord.service.IEditRecordService;
import fr.paris.lutece.plugins.workflow.modules.editrecord.util.constants.EditRecordConstants;
import fr.paris.lutece.plugins.workflow.web.task.AbstractTaskComponent;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.xml.XmlUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import javax.servlet.http.HttpServletRequest;


/**
 *
 * EditRecordTaskComponent
 *
 */
public class EditRecordTaskComponent extends AbstractTaskComponent
{
    // TEMPLATES
    private static final String TEMPLATE_TASK_EDIT_RECORD_CONFIG = "admin/plugins/workflow/modules/editrecord/task_edit_record_config.html";
    private static final String TEMPLATE_TASK_EDIT_RECORD_FORM = "admin/plugins/workflow/modules/editrecord/task_edit_record_form.html";
    private static final String TEMPLATE_TASK_EDIT_RECORD_INFORMATION = "admin/plugins/workflow/modules/editrecord/task_edit_record_information.html";

    // SERVICES
    @Inject
    private IEditRecordService _editRecordService;
    @Inject
    @Named( EditRecordConstants.BEAN_EDIT_RECORD_CONFIG_SERVICE )
    private ITaskConfigService _taskEditRecordConfigService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String doValidateTask( int nIdResource, String strResourceType, HttpServletRequest request, Locale locale,
        ITask task )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayConfigForm( HttpServletRequest request, Locale locale, ITask task )
    {
        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( EditRecordConstants.MARK_CONFIG, _taskEditRecordConfigService.findByPrimaryKey( task.getId(  ) ) );
        model.put( EditRecordConstants.MARK_LIST_STATES,
            _editRecordService.getListStates( task.getAction(  ).getId(  ) ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_EDIT_RECORD_CONFIG, locale, model );

        return template.getHtml(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayTaskForm( int nIdResource, String strResourceType, HttpServletRequest request,
        Locale locale, ITask task )
    {
        TaskEditRecordConfig config = _taskEditRecordConfigService.findByPrimaryKey( task.getId(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( EditRecordConstants.MARK_CONFIG, config );
        model.put( EditRecordConstants.MARK_LIST_ENTRIES,
            _editRecordService.getFormListEntries( nIdResource, task.getId(  ), request ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_EDIT_RECORD_FORM, locale, model );

        return template.getHtml(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayTaskInformation( int nIdHistory, HttpServletRequest request, Locale locale, ITask task )
    {
        EditRecord editRecord = _editRecordService.find( nIdHistory, task.getId(  ) );
        TaskEditRecordConfig config = _taskEditRecordConfigService.findByPrimaryKey( task.getId(  ) );

        Map<String, Object> model = new HashMap<String, Object>(  );
        model.put( EditRecordConstants.MARK_CONFIG, config );
        model.put( EditRecordConstants.MARK_EDIT_RECORD, editRecord );

        if ( editRecord != null )
        {
            model.put( EditRecordConstants.MARK_LIST_ENTRIES, _editRecordService.getInformationListEntries( nIdHistory ) );
        }

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_EDIT_RECORD_INFORMATION, locale, model );

        return template.getHtml(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTaskInformationXml( int nIdHistory, HttpServletRequest request, Locale locale, ITask task )
    {
        StringBuffer sbXml = new StringBuffer(  );
        EditRecord editRecord = _editRecordService.find( nIdHistory, task.getId(  ) );

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
}
