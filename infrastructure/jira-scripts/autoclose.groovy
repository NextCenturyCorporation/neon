/**
 * automatically closes all subtasks if a parent issue is closed
 */
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.comments.CommentManager
import com.opensymphony.workflow.WorkflowContext
import com.atlassian.jira.config.SubTaskManager
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.jira.util.JiraUtils;

String currentUser = ((WorkflowContext) transientVars.get("context")).getCaller();
WorkflowTransitionUtil workflowTransitionUtil = ( WorkflowTransitionUtil ) JiraUtils.loadComponent( WorkflowTransitionUtilImpl.class );
 
SubTaskManager subTaskManager = ComponentManager.getInstance().getSubTaskManager();
Collection subTasks = issue.getSubTaskObjects()
if (subTaskManager.subTasksEnabled && !subTasks.empty) {
    subTasks.each {
        workflowTransitionUtil.setIssue(it);
        workflowTransitionUtil.setUsername(currentUser);
        // actionId 2 transition an open issue, 701 transitions a resolved issue
        workflowTransitionUtil.setAction (it.resolutionObject ? 701 : 2)    // close issue
 
        // Add a comment so people have a clue why the child has been closed 
        CommentManager commentManager = (CommentManager) ComponentManager.getComponentInstanceOfType(CommentManager.class);
        String comment = "Closing as a result of the Close action being applied to the parent.";
        commentManager.create(it, currentUser, comment, true);
 
        // validate and transition issue
        workflowTransitionUtil.validate();
        workflowTransitionUtil.progress();
    }
}
