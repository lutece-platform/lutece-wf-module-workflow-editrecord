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
package fr.paris.lutece.plugins.workflow.modules.editrecord.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Transactional;

import fr.paris.lutece.plugins.directory.business.EntryType;
import fr.paris.lutece.plugins.directory.business.Field;
import fr.paris.lutece.plugins.directory.business.IEntry;
import fr.paris.lutece.plugins.directory.business.Record;
import fr.paris.lutece.plugins.directory.business.RecordField;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecord;
import fr.paris.lutece.plugins.workflow.modules.editrecord.business.EditRecordValue;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.util.ReferenceList;

/**
 *
 * IEditRecordService
 *
 */
public interface IEditRecordService
{
    // SET

    /**
     * Set the site message
     * 
     * @param request
     *            the HTTP request
     * @param strMessage
     *            the message
     * @param nTypeMessage
     *            the message type
     * @param strUrlReturn
     *            the url return
     * @throws SiteMessageException
     *             the site message
     */
    void setSiteMessage( HttpServletRequest request, String strMessage, int nTypeMessage, String strUrlReturn ) throws SiteMessageException;

    // CRUD

    /**
     * Create an edit record
     * 
     * @param editRecord
     *            the edit record
     */
    @Transactional( EditRecordPlugin.BEAN_TRANSACTION_MANAGER )
    void create( EditRecord editRecord );

    /**
     * Update an edit record
     * 
     * @param editRecord
     *            the edit record
     */
    @Transactional( EditRecordPlugin.BEAN_TRANSACTION_MANAGER )
    void update( EditRecord editRecord );

    /**
     * Find an edit record
     * 
     * @param nIdHistory
     *            the id history
     * @param nIdTask
     *            the id task
     * @return a edit record
     */
    EditRecord find( int nIdHistory, int nIdTask );

    /**
     * Find edit records by a given id task
     * 
     * @param nIdTask
     *            the id task
     * @return the list of edit records
     */
    List<EditRecord> findByIdTask( int nIdTask );

    /**
     * Remove an edit record
     * 
     * @param nIdHistory
     *            the id history
     * @param nIdTask
     *            the id task
     */
    @Transactional( EditRecordPlugin.BEAN_TRANSACTION_MANAGER )
    void removeByIdHistory( int nIdHistory, int nIdTask );

    /**
     * Remove an edit record by id task
     * 
     * @param nIdTask
     *            the id task
     */
    @Transactional( EditRecordPlugin.BEAN_TRANSACTION_MANAGER )
    void removeByIdTask( int nIdTask );

    // GET

    /**
     * Get the list of states
     * 
     * @param nIdAction
     *            the id action
     * @return a ReferenceList
     */
    ReferenceList getListStates( int nIdAction );

    /**
     * Get the list of entries for the form
     * 
     * @param nIdRecord
     *            the id record
     * @param nIdTask
     *            the id task
     * @param request
     *            the HTTP request
     * @return a list of entries
     */
    List<IEntry> getFormListEntries( int nIdRecord, int nIdTask, HttpServletRequest request );

    /**
     * Get the list of entries for information
     * 
     * @param nIdHistory
     *            the id edit record
     * @return a list of entries
     */
    List<IEntry> getInformationListEntries( int nIdHistory );

    /**
     * Get the list of entries to edit
     * 
     * @param request
     *            the HTTP request
     * @param listEditRecordValues
     *            the list of edit record values
     * @return a list of entries
     */
    List<IEntry> getListEntriesToEdit( HttpServletRequest request, List<EditRecordValue> listEditRecordValues );

    /**
     * Get the list of entries to not edit
     * 
     * @param request
     *            the HTTP request
     * @param nIdRecord
     *            the id record
     * @param listEditRecordValues
     *            the list of edit record values
     * @return a list of entries
     */
    List<Integer> getListIdEntriesToNotEdit( HttpServletRequest request, int nIdRecord, List<EditRecordValue> listEditRecordValues );

    /**
     * Get the list of record fieds to not edit
     * 
     * @param request
     *            the HTTP request
     * @param nIdRecord
     *            the id record
     * @param listEditRecordValues
     *            the list of edit record values
     * @param mapFieldEntry
     *            a map containing all fields associated to the list of entry
     * @return a list of record fields
     */
    List<RecordField> getListRecordFieldsToNotEdit( HttpServletRequest request, int nIdRecord, List<EditRecordValue> listEditRecordValues,
            Map<Integer, Field> mapFieldEntry );

    /**
     * Get the map id entry - list record fields
     * 
     * @param listEntries
     *            the list of entries to edit
     * @param nIdHistory
     *            the id history
     * @return a map of id entry - list record fields
     */
    Map<String, List<RecordField>> getMapIdEntryListRecordField( List<IEntry> listEntries, int nIdHistory );

    /**
     * Get the entry from a given id entry
     * 
     * @param nIdEntry
     *            the id entry
     * @return an {@link IEntry}
     */
    IEntry getEntry( int nIdEntry );

    /**
     * Get the entry type download url
     * 
     * @return the entry type downlaod url
     */
    EntryType getEntryTypeDownloadUrl( );

    /**
     * Get the record from a given id history
     * 
     * @param nIdHistory
     *            the id history
     * @return the record
     */
    Record getRecordFromIdHistory( int nIdHistory );

    /**
     * Get the list of record fields from a given id history and id entry
     * 
     * @param nIdHistory
     *            the id history
     * @param nIdEntry
     *            the id entry
     * @return the list of record fields
     */
    List<RecordField> getRecordFieldsList( int nIdHistory, int nIdEntry );

    /**
     * Get the record field associated to the entry type download url. <br />
     * There is currently only on record field per record for the entry type download url. So, this method will only fetch the first record field.
     * 
     * @param nIdHistory
     *            the id history
     * @param nIdEntry
     *            the id entry
     * @return the record field
     */
    RecordField getRecordFieldDownloadUrl( int nIdHistory, int nIdEntry );

    // DO

    /**
     * Do edit the record data
     * 
     * @param request
     *            the HTTP request
     * @param editRecord
     *            the edit record
     * @return true if the user the record must be updated, false otherwise
     * @throws SiteMessageException
     *             site message if there is a problem
     */
    boolean doEditRecordData( HttpServletRequest request, EditRecord editRecord ) throws SiteMessageException;

    /**
     * Do change the record state
     * 
     * @param editRecord
     *            edit record
     * @param locale
     *            the locale
     */
    void doChangeRecordState( EditRecord editRecord, Locale locale );

    /**
     * Do change the edit record to complete
     * 
     * @param editRecord
     *            the edit record
     */
    void doCompleteEditRecord( EditRecord editRecord );

    // CHECK

    /**
     * Check if the request is authenticated or not
     * 
     * @param request
     *            the HTTP request
     * @return true if the requet is authenticated, false otherwise
     */
    boolean isRequestAuthenticated( HttpServletRequest request );

    /**
     * Check if the record has the same state before executing the action
     * 
     * @param editRecord
     *            the edit record
     * @param locale
     *            the locale
     * @return true if the record has a valid state, false otherwise
     */
    boolean isRecordStateValid( EditRecord editRecord, Locale locale );
}
