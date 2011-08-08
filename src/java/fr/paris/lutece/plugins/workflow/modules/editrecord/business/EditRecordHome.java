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
 * EditRecordHome
 *
 */
public final class EditRecordHome
{
    private static final String BEAN_EDIT_RECORD_DAO = "workflow-editrecord.editRecordDAO";
    private static Plugin _plugin = PluginService.getPlugin( EditRecordPlugin.PLUGIN_NAME );
    private static IEditRecordDAO _dao = (IEditRecordDAO) SpringContextService.getPluginBean( EditRecordPlugin.PLUGIN_NAME,
            BEAN_EDIT_RECORD_DAO );

    /**
     * Private constructor - this class need not be instantiated
     */
    private EditRecordHome(  )
    {
    }

    /**
     * Creation of an instance of edit record
     * @param editRecord The instance of EditRecord
     * @return the newly create EditRecord primary key
     */
    public static int create( EditRecord editRecord )
    {
        return _dao.insert( editRecord, _plugin );
    }

    /**
     * Creation of an instance of edit record
     * @param editRecord The instance of EditRecord
     */
    public static void update( EditRecord editRecord )
    {
        _dao.store( editRecord, _plugin );
    }

    /**
     * Remove EditRecord by id record
     * @param nIdRecord the id record
     * @param nIdTask The task key
     */
    public static void removeByIdRecord( int nIdRecord, int nIdTask )
    {
        _dao.deleteByIdRecord( nIdRecord, nIdTask, _plugin );
    }

    /**
     * Remove EditRecord by task
     * @param nIdTask The task key
     */
    public static void removeByTask( int nIdTask )
    {
        _dao.deleteByTask( nIdTask, _plugin );
    }

    /**
     * Load the EditRecord Object
     * @param nIdRecord the history id
     * @param nIdTask the task id
     * @return the EditRecord Object
     */
    public static EditRecord find( int nIdRecord, int nIdTask )
    {
        return _dao.load( nIdRecord, nIdTask, _plugin );
    }

    /**
     * Load the EditRecord Object
     * @param nIdTask the task id
     * @return a list of editrecord
     */
    public static List<EditRecord> findByIdTask( int nIdTask )
    {
        return _dao.loadByIdTask( nIdTask, _plugin );
    }
}
