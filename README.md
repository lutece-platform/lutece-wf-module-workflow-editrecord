![](http://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=wf-module-workflow-editrecord-deploy)
# Module workflow editrecord

## Introduction

This module contains a workflow task to edit a record of a directory (from the `plugin-directory` ). The front-office user accesses a page to edit the record, thanks to a link sent by email.

## Usage

 **Configure the workflow task** 

The field `State after edition` corresponds to the target state after the front-office user has edited the record.

The field `Default message` corresponds to the message displayed in the task form when the back-office user executes the action. The user can modify this message in the form. It is then displayed in the front-office edition page.

 **Execute an action when the record is edited** 

After the submission of the edition by the front-office user, the process calls the first automatic workflow action whose initial state is the state defined in the configuration task.

Thus, to execute a workflow action after the front-office user has submitted the edition of the record:
 
* Create a state in the workflow. This state can be used only for this purpose.
* In the task configuration, use the created state in the field `State after edition` .
* Create an automatic action whose initial state is the created state and the final state is your wanted state.
* Add your wanted tasks in this action.


Here is an example:

States
 
*  `Title` : In progress
*  `Title` : Waiting for edition
*  `Title` : Record edited
Actions
 
*  `Title` : Ask for record edition. `States` : In progress -> Waiting for edition. `Automatic` : no.
*  `Title` : Edit record. `States` : Record edited -> In progress. `Automatic` : yes.
Tasks
 
*  `Type` : Directory record edition. `Action` : Ask for record edition. `Configuration` : `State after edition` = Record edited
*  `Type` : as you want. `Action` : Edit record.



[Maven documentation and reports](http://dev.lutece.paris.fr/plugins/module-workflow-editrecord/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*