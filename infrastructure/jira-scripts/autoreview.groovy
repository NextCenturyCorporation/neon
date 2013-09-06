/**
 * automatically transitions all subtasks to the code review state if the parent story is moved to the code review state
 */
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.comments.CommentManager
import com.opensymphony.workflow.WorkflowContext
import com.atlassian.jira.config.SubTaskManager
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.jira.util.JiraUtils;
new File("sampledir").mkdir() 
String currentUser = ((WorkflowContext) transientVars.get("context")).getCaller();
WorkflowTransitionUtil workflowTransitionUtil = ( WorkflowTransitionUtil ) JiraUtils.loadComponent( WorkflowTransitionUtilImpl.class );
 
SubTaskManager subTaskManager = ComponentManager.getInstance().getSubTaskManager();
Collection subTasks = issue.getSubTaskObjects()
if (subTaskManager.subTasksEnabled && !subTasks.empty) {
    subTasks.each {
	if ( it.editable && it.resolutionObject ) {
            workflowTransitionUtil.setIssue(it);
            workflowTransitionUtil.setUsername(currentUser);
            workflowTransitionUtil.setAction (711)    // code review action id
 
            // Add a comment indicating the automatic transition
            CommentManager commentManager = (CommentManager) ComponentManager.getComponentInstanceOfType(CommentManager.class);
            String comment = "Moving to Code Review as a result of the Code Review state being applied to the parent.";
            commentManager.create(it, currentUser, comment, true);
 
            // validate and transition issue
            workflowTransitionUtil.validate();
            workflowTransitionUtil.progress();
	}
    }
}
