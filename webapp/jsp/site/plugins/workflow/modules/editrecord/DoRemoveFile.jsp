<%@ page errorPage="../../ErrorPagePortal.jsp" %>

<%@page import="fr.paris.lutece.plugins.workflow.modules.editrecord.web.EditRecordApp"%>
<jsp:useBean id="editRecord" scope="request" class="fr.paris.lutece.plugins.workflow.modules.editrecord.web.EditRecordApp" />

<%= editRecord.doRemoveAsynchronousUploadedFile( request ) %>