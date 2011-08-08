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

import fr.paris.lutece.plugins.workflow.modules.editrecord.service.EditRecordPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;


/**
 *
 * TaskEditRecordConfigHome
 *
 */
public final class TaskEditRecordConfigHome
{
    private static final String BEAN_TASK_EDIT_RECORD_CONFIG_DAO = "workflow-editrecord.taskEditRecordConfigDAO";
    private static final Plugin _plugin = PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME );
    private static ITaskEditRecordConfigDAO _dao = (ITaskEditRecordConfigDAO) SpringContextService.getPluginBean( EditRecordPlugin.PLUGIN_NAME,
            BEAN_TASK_EDIT_RECORD_CONFIG_DAO );

    /**
     * Private constructor - this class need not be instantiated
     */
    private TaskEditRecordConfigHome(  )
    {
    }

    /**
     * Insert new configuration
     * @param config object configuration
     */
    public static void create( TaskEditRecordConfig config )
    {
        _dao.insert( config, _plugin );
    }

    /**
     * Update a configuration
     * @param config object configuration
     */
    public static void update( TaskEditRecordConfig config )
    {
        _dao.store( config, _plugin );
    }

    /**
     * Delete a configuration
     * @param nIdTask id task
     */
    public static void remove( int nIdTask )
    {
        _dao.delete( nIdTask, _plugin );
    }

    /**
     * Load a configuration
     * @param nIdTask id task
     * @return a configuration
     */
    public static TaskEditRecordConfig findByPrimaryKey( int nIdTask )
    {
        return _dao.load( nIdTask, _plugin );
    }

    /**
     * Load all configs
     * @return a list of configurations
     */
    public static List<TaskEditRecordConfig> findAll(  )
    {
        return _dao.loadAll( _plugin );
    }
}
