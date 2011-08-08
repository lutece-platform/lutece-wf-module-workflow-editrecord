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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * EditRecordValueDAO
 *
 */
public class EditRecordValueDAO implements IEditRecordValueDAO
{
    private static final String SQL_QUERY_SELECT = " SELECT id_edit_record, id_entry " +
        " FROM task_edit_record_value WHERE id_edit_record = ? ";
    private static final String SQL_QUERY_INSERT = " INSERT INTO task_edit_record_value (id_edit_record, id_entry ) " +
        " VALUES ( ?,? ) ";
    private static final String SQL_QUERY_DELETE = " DELETE FROM task_edit_record_value WHERE id_edit_record = ? ";

    /**
     * {@inheritDoc}
     */
    public synchronized void insert( EditRecordValue editRecordValue, Plugin plugin )
    {
        int nIndex = 1;

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        daoUtil.setInt( nIndex++, editRecordValue.getIdEditRecord(  ) );
        daoUtil.setInt( nIndex++, editRecordValue.getIdEntry(  ) );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    /**
     * {@inheritDoc}
     */
    public List<EditRecordValue> load( int nIdEditRecord, Plugin plugin )
    {
        List<EditRecordValue> listEditRecordValues = new ArrayList<EditRecordValue>(  );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nIdEditRecord );

        daoUtil.executeQuery(  );

        while ( daoUtil.next(  ) )
        {
            int nIndex = 1;

            EditRecordValue editRecordValue = new EditRecordValue(  );
            editRecordValue.setIdEditRecord( daoUtil.getInt( nIndex++ ) );
            editRecordValue.setIdEntry( daoUtil.getInt( nIndex++ ) );

            listEditRecordValues.add( editRecordValue );
        }

        daoUtil.free(  );

        return listEditRecordValues;
    }

    /**
     * {@inheritDoc}
     */
    public void delete( int nIdEditRecord, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nIdEditRecord );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }
}
