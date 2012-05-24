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

import fr.paris.lutece.plugins.workflow.modules.editrecord.business.ITaskEditRecordConfigDAO;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.TaskEditRecordConfig;
import fr.paris.lutece.portal.service.plugin.PluginService;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;


/**
 *
 * TaskEditRecordConfigService
 *
 */
public class TaskEditRecordConfigService implements ITaskEditRecordConfigService
{
    public static final String BEAN_SERVICE = "workflow-editrecord.taskEditRecordConfigService";
    @Inject
    private ITaskEditRecordConfigDAO _taskEditRecordConfigDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( "workflow-editrecord.transactionManager" )
    public void create( TaskEditRecordConfig config )
    {
        if ( config != null )
        {
            _taskEditRecordConfigDAO.insert( config, PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( "workflow-editrecord.transactionManager" )
    public void update( TaskEditRecordConfig config )
    {
        if ( config != null )
        {
            _taskEditRecordConfigDAO.store( config, PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional( "workflow-editrecord.transactionManager" )
    public void remove( int nIdTask )
    {
        _taskEditRecordConfigDAO.delete( nIdTask, PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskEditRecordConfig findByPrimaryKey( int nIdTask )
    {
        return _taskEditRecordConfigDAO.load( nIdTask, PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaskEditRecordConfig> findAll(  )
    {
        return _taskEditRecordConfigDAO.loadAll( PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME ) );
    }
}
