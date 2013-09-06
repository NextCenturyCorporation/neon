/**
 * automatically starts the progress on a parent issue if a subtask is put in progress
 */
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.comments.CommentManager
import com.opensymphony.workflow.WorkflowContext
import com.atlassian.jira.config.SubTaskManager
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.issue.IssueFieldConstants;

String currentUser = ((WorkflowContext) transientVars.get("context")).getCaller();
WorkflowTransitionUtil workflowTransitionUtil = ( WorkflowTransitionUtil ) JiraUtils.loadComponent( WorkflowTransitionUtilImpl.class );
 
if (issue.isSubTask()) {
    def parent = issue.getParentObject()
    def statusId = parent.getStatusObject().getId()
    if ( statusId == IssueFieldConstants.OPEN_STATUS_ID.toString() || statusId == IssueFieldConstants.REOPENED_STATUS_ID.toString() ) {
      workflowTransitionUtil.setIssue(parent);
      workflowTransitionUtil.setUsername(currentUser);
      workflowTransitionUtil.setAction(4); // start progress

      CommentManager commentManager = (CommentManager) ComponentManager.getComponentInstanceOfType(CommentManager.class);
      String comment = "Starting progress as a result of a subtask being started.";
      commentManager.create(parent, currentUser, comment, true);
 
      // validate and transition issue
      workflowTransitionUtil.validate();
      workflowTransitionUtil.progress();
   }
}
