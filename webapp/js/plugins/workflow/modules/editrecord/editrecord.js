/**
 * Add the asynchronous upload field
 * @param nIdEntry the id entry
 * @param nIdHistory the id history
 * @param nIdTask the id task
 * @return false
 */
function addAsynchronousUploadField( nIdEntry, nIdHistory, nIdTask ) {
	var flashVersion = swfobject.getFlashPlayerVersion(  );
	/* Flash Player 9.0.24 or greater  - simple mode otherwise */
	if ( swfobject.hasFlashPlayerVersion( "9.0.24" ) ) {
		$( '#_editrecord_upload_submit_' + nIdEntry ).hide(  );
	    $( '#' + nIdEntry ).uploadify( {
	        'uploader' : 'js/plugins/workflow/modules/editrecord/uploadify/swf/uploadify.swf',
	        'script' : baseUrl + '/jsp/site/upload',
	        'cancelImg' : 'js/plugins/workflow/modules/editrecord/uploadify/cancel.png',
			'auto' : true,
			'buttonText' : 'Parcourir',
			'displayData' : 'percentage',
			
			// file types & size limit
//			'fileExt' : getFileExtValue( nIdEntry ),
//			'fileDesc': ( getFileExtValue( nIdEntry ) == null ? null : 'Fichiers ' + "(" + getFileExtValue( nIdEntry ) ) + ")",
			'sizeLimit' : getMaxLengthValue( nIdEntry ),
			
			// additional parameters
			'scriptData' : { 'jsessionid' : document.cookie.match(/JSESSIONID=([^;]+)/)[1], 'page':'workflow-editrecord', 'id_history' : nIdHistory, 'id_task' : nIdTask, 'id_entry' : nIdEntry},
			
			// event handlers
			'onComplete' : function( event, ID, fileObj, data ) {
				editrecordOnUploadComplete( event, ID, fileObj, data );
				$( '#' + nIdEntry ).uploadifySettings( 'hideButton', false );
			},
			'onError' : function( event, ID, fileObj,data ) {
				handleError( event,ID,fileObj,data,nIdEntry );
				$( '#' + nIdEntry ).uploadifySettings( 'hideButton', false );
			},
			'onCancel' : function( event, ID, fileObj, data ) {
				uploading--;
				$( '#' + nIdEntry ).uploadifySettings( 'hideButton', false );
			},
			'onSelect' : function( event,ID ) {
				if ( !editRecordStartUpload( event, ID, nIdEntry ) ) {
					return false;
				} else {
					$( '#' + nIdEntry ).uploadifySettings( 'hideButton' , true );
				}
			}
	    } );
	}
}

/**
 * Gets the max size value for the file
 * @param nIdEntry the file
 * @return the max size
 */
function getMaxLengthValue( nIdEntry ) {
	return getInputValue( '#_editrecord_upload_maxLength_' + nIdEntry );
}

/**
 * Removes a file
 * @param form the form
 * @param subform the subform
 * @param action the action button name
 */
function removeFile( nIdHistory, nIdTask, action ) {
	var nIdEntry = action.match( '_editrecord_upload_delete_(.*)' )[1];
	var jsonData = { "id_history" : nIdHistory, "id_task" : nIdTask, "id_entry" : nIdEntry };
	
	$.getJSON( baseUrl + '/jsp/site/plugins/workflow/modules/editrecord/DoRemoveFile.jsp', jsonData,
		function( json ) {
			editrecordDisplayUploadedFiles( json );
		}
	);
}

/**
 * Sets the files list
 * @param jsonData data
 */
function editrecordDisplayUploadedFiles( jsonData ) {
	// create the div
	var nIdEntry = jsonData.idEntry;
	
	if ( nIdEntry != null )	{
		/* error messages */
		if ( jsonData.errorMessages != undefined && jsonData.errorMessages != null ) {
			$( '#_editrecord_uploaded_files_errors_' + nIdEntry ).text( '' );
			var errorMessage = '';
			if ( $.isArray( jsonData.errorMessages ) ) {
				errorMessage +=  "<ul>";
				for ( var index = 0; index < jsonData.errorMessages.length; index++ ) {
					errorMessage += jsonData.errorMessages[index];
				}
				errorMessage += "</ul>";
			} else {
				// only one message...
				errorMessage += jsonData.errorMessages;
			}
			$( '#_editrecord_uploaded_files_errors_' + nIdEntry).html( errorMessage );
			$( '#_editrecord_uploaded_files_errors_' + nIdEntry).find( 'a' ).each( function(  ) {
				$( this ).attr( 'href', window.location.href + $( this ).attr( 'href' ) );
			} );
		}
		
		if ( jsonData.fileCount == 0 ) {
			// no file uploaded, hiding content
			$( '#_editrecord_uploaded_file_' + nIdEntry ).hide(  );
		} else {
			// show the hidden div (if not already)
			$( '#_editrecord_uploaded_file_' + nIdEntry ).show(  );

			var strContent = "<span class=\"form-label\">  \
								<label> Fichiers transmis : </label>  \
							</span>  \
							<span class=\"form-field-column\">&nbsp;"
								+ jsonData.uploadedFile + 
								"&nbsp;<input type=\"submit\" name=\"_editrecord_upload_delete_" + nIdEntry + "\" id=\"_editrecord_upload_delete_" + nIdEntry + "\" value=\"Supprimer\" />  \
							</span>";
			
			$( '#_editrecord_uploaded_file_' + nIdEntry ).html(
					strContent
			);
		}
	
	}
}

/**
 * Called when the upload if successfully completed
 * @param event event
 * @param ID id
 * @param fileObj fileObj
 * @param data data (json)
 */
function editrecordOnUploadComplete( event, ID, fileObj, data ) {
	uploading--;
	
	var nIdHistory = $( 'input[name="id_history"]' ).val(  );
	var nIdTask = $( 'input[name="id_task"]' ).val(  );
	
	var jsonData;
	try	{
		jsonData = $.parseJSON( data );
	} catch ( err )	{
		/* webapp conf problem : probably file upload limit */
		alert( 'Une erreur est survenue lors de l\'envoi du fichier.' );
		return;
	}
	
	if ( jsonData.error != null ) {
		alert( jsonData.error );
	}
	
	var jsonParameters = { "id_history" : nIdHistory, "id_task" : nIdTask, "id_entry" : jsonData.idEntry };
	
	$.getJSON( baseUrl + '/jsp/site/plugins/workflow/modules/editrecord/UploadedFiles.jsp', jsonParameters,
		function( json ) {
			editrecordDisplayUploadedFiles( json );
		}
	);
}

/**
 * Get the value of the input 
 * @param inputId the input id
 * @return the input value
 */
function getInputValue( inputId ) {
	var input = $( inputId )[0];
	if ( input != null ) {
		return input.value;
	}
	
	return null;
}

/**
 * Handles error
 * @param event event
 * @param ID id
 * @param fileObj  fileObj
 * @param data data
 * @param entryId entryId
 */
function handleError( event, ID, fileObj, data, nIdEntry ) {
	$( '#' + nIdEntry ).uploadifyCancel( ID );
	
	if ( data.type == "File Size" ) {
		var maxSize = data.info / 1024;
		var strMaxSize;
		
		if ( maxSize > 1024 ) {
			maxSize = Math.round( maxSize / 1024 * 100 ) / 100;
			strMaxSize = maxSize + "Mo";
		} else {
			strMaxSize = Math.round( maxSize * 100 ) / 100 + "ko";
		}
		alert( '"Le fichier est trop gros. La taille est limitée à ' + strMaxSize );
	}
	else
	{
		alert( 'Une erreur s\'est produite lors de l\'envoi du fichier : ' + data.info );
	}
}

/**
 * Start upload
 * @param event event
 * @param ID id
 * @param entryId the id entry
 * @return true
 */
function editRecordStartUpload( event, ID, entryId ) {
	uploading++;
	return true;
}

/**
 * Keep alive
 * @return void
 */
function keepAlive(  ) {
	if ( uploading > 0 ) {
		$.getJSON( baseUrl + 'jsp/site/plugins/workflow/modules/editrecord/KeepAlive.jsp' );
	}
	setTimeout( "keepAlive(  )", 240000 );
}

/** ------------------------------------------------------------------------------------------------------------ */

var uploading = 0;

$( document ).ready( function(  ) {
	var nIdHistory = $( 'input[name="id_history"]' ).val(  );
	var nIdTask = $( 'input[name="id_task"]' ).val(  );
	keepAlive(  );
	
	if ( baseUrl.match( '(.*/$)' ) ) {
		baseUrl = baseUrl.substring( 0, baseUrl.length - 1 );
	}
	// add asynchronous behaviour to inputs type=file
	$( 'input[type=file]' ).each( function( index ) {
		addAsynchronousUploadField( this.id, nIdHistory, nIdTask );
	} );

	//prevent user from quitting the page before his upload ended.
	$( 'input[type=submit]' ).each( function(  ) {
		$( this ).click( function( event ) {
			if ( uploading != 0 ) {
				event.preventDefault(  );
				alert( 'Merci de patienter pendant l\'envoi du fichier' );
			} else if ( this.name.match( '_editrecord_upload_delete_'  ) ) {
				event.preventDefault(  ); 
				removeFile( nIdHistory, nIdTask, this.name);
				return false;
			}
		} );
	} );
} );
