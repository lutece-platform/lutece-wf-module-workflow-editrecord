/*
 * Copyright (c) 2002-2015, Mairie de Paris
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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * EditRecordDAO
 *
 */
public class EditRecordDAO implements IEditRecordDAO
{
    private static final String SQL_QUERY_SELECT = " SELECT id_history, id_task, message, is_complete " +
        " FROM task_edit_record WHERE id_history = ? AND id_task = ? ";
    private static final String SQL_QUERY_SELECT_BY_ID_TASK = " SELECT id_history, id_task, message, is_complete " +
        " FROM task_edit_record WHERE id_task = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO task_edit_record ( id_history, id_task, message, is_complete ) " +
        " VALUES ( ?,?,?,? ) ";
    private static final String SQL_QUERY_DELETE_BY_ID_HISTORY = " DELETE FROM task_edit_record WHERE id_history = ? AND id_task = ? ";
    private static final String SQL_QUERY_DELETE_BY_TASK = " DELETE FROM task_edit_record WHERE id_task = ? ";
    private static final String SQL_QUERY_UPDATE = " UPDATE task_edit_record SET message = ?, is_complete = ? WHERE id_history = ? AND id_task = ? ";

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void insert( EditRecord editRecord, Plugin plugin )
    {
        int nIndex = 1;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        daoUtil.setInt( nIndex++, editRecord.getIdHistory(  ) );
        daoUtil.setInt( nIndex++, editRecord.getIdTask(  ) );
        daoUtil.setString( nIndex++, editRecord.getMessage(  ) );
        daoUtil.setBoolean( nIndex++, editRecord.isComplete(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store( EditRecord editRecord, Plugin plugin )
    {
        int nIndex = 1;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );

        daoUtil.setString( nIndex++, editRecord.getMessage(  ) );
        daoUtil.setBoolean( nIndex++, editRecord.isComplete(  ) );

        daoUtil.setInt( nIndex++, editRecord.getIdHistory(  ) );
        daoUtil.setInt( nIndex++, editRecord.getIdTask(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EditRecord load( int nIdHistory, int nIdTask, Plugin plugin )
    {
        EditRecord editRecord = null;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        int nIndex = 1;
        daoUtil.setInt( nIndex++, nIdHistory );
        daoUtil.setInt( nIndex++, nIdTask );

        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            nIndex = 1;

            editRecord = new EditRecord(  );
            editRecord.setIdHistory( daoUtil.getInt( nIndex++ ) );
            editRecord.setIdTask( daoUtil.getInt( nIndex++ ) );
            editRecord.setMessage( daoUtil.getString( nIndex++ ) );
            editRecord.setIsComplete( daoUtil.getBoolean( nIndex++ ) );
        }

        daoUtil.free(  );

        return editRecord;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EditRecord> loadByIdTask( int nIdTask, Plugin plugin )
    {
        List<EditRecord> listEditRecords = new ArrayList<EditRecord>(  );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_ID_TASK, plugin );
        daoUtil.setInt( 1, nIdTask );

        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            int nIndex = 1;

            EditRecord editRecord = new EditRecord(  );
            editRecord.setIdHistory( daoUtil.getInt( nIndex++ ) );
            editRecord.setIdTask( daoUtil.getInt( nIndex++ ) );
            editRecord.setMessage( daoUtil.getString( nIndex++ ) );
            editRecord.setIsComplete( daoUtil.getBoolean( nIndex++ ) );
            listEditRecords.add( editRecord );
        }

        daoUtil.free(  );

        return listEditRecords;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteByIdHistory( int nIdHistory, int nIdTask, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_BY_ID_HISTORY, plugin );
        int nIndex = 1;
        daoUtil.setInt( nIndex++, nIdHistory );
        daoUtil.setInt( nIndex++, nIdTask );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteByIdTask( int nIdTask, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_BY_TASK, plugin );
        daoUtil.setInt( 1, nIdTask );
        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }
}
