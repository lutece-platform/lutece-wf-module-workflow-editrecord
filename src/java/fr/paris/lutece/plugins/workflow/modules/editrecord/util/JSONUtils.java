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
package fr.paris.lutece.plugins.workflow.modules.editrecord.util;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;


/**
 * JSONUtils
 */
public final class JSONUtils
{
    //JSON KEY
    public static final String JSON_KEY_UPLOADED_FILE = "uploadedFile";
    public static final String JSON_KEY_ID_ENTRY = "idEntry";
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_ERROR_MESSAGES = "errorMessages";
    public static final String JSON_KEY_SUCCESS = "success";
    public static final String JSON_KEY_FILE_COUNT = "fileCount";

    /** private constructor */
    private JSONUtils(  )
    {
    }

    /**
     * Builds a json object for the file item list.
     * Key is {@link #JSON_UPLOADED_FILES}, value is the array of uploaded file.
     * @param strFileName the file name
     * @return the json
     */
    public static JSONObject getUploadedFileJSON( String strFileName )
    {
        JSONObject json = new JSONObject(  );

        if ( StringUtils.isNotBlank( strFileName ) )
        {
            json.accumulate( JSON_KEY_UPLOADED_FILE, strFileName );
            json.element( JSON_KEY_FILE_COUNT, 1 );
        }
        else
        {
            // no file
            json.element( JSON_KEY_FILE_COUNT, 0 );
        }

        return json;
    }
}
