if(firstSync){
   issue.projectKey   = "DEMO" 
   // Set type name from source issue, if not found set a default
   issue.typeName     = nodeHelper.getIssueType(replica.type?.name, issue.projectKey)?.name ?: "Task"
}
issue.summary      = replica.summary
issue.description  = replica.description
issue.comments     = commentHelper.mergeComments(issue, replica)
issue.attachments  = attachmentHelper.mergeAttachments(issue, replica)
issue.labels       = replica.labels

def SetStatus(){
    def statusMappingTask = [
        "To Do":"Open",
        "In Progress":"In Progress",
        "Done":"Done"
    ]

    def statusMappingEpic = [
        "Open":"Backlog",
        "Work In Progress":"In Progress",
        "Finished":"Done"
    ]
    
    def statusMappingStory = [
        "Backlog":"Backlog",
        "Select For Dev":"Select For Development",
        "In Progress":"In Progress",
        "Done":"Done"
    ]
    
    def statusMappingBug = [
        "Open":"Open",
        "Waiting For Customer":"Waiting For Customer",
        "Exalated":"Exalated",
        "Review":"Review",
        "Waiting For Support":"Waiting For Support",
        "Awaiting Action":"Awaiting Action",
        "Awaiting Approval":"Awaiting Aprovel",
        "Closed":"Closed",
        "Resolved":"resolved"
    ]
    
    if (issue.type.name == "Task") { return statusMappingTask[replica.status.name] ?: "To Do"}
    if (issue.type.name == "Story") { return statusMappingStory[replica.status.name] ?: "Backlog"}
    if (issue.type.name == "Epic") { return statusMappingEpic[replica.status.name] ?: "Backlog"}
    if (issue.type.name == "Bug") { return statusMappingBug[replica.status.name] ?: "Open"}
    
}
if (!firstSync){
    issue.setStatus(SetStatus())
}


issue.customFields."Color".value = replica.customFields."Color".value.value
issue.customFields."Demo Date".value = replica.customFields."Demo Date".value
issue.customFields."JC_Status".value = replica.status.name

/*
User Synchronization (Assignee/Reporter)

Set a Reporter/Assignee from the source side, if the user can't be found set a default user
You can use this approach for custom fields of type User
def defaultUser = nodeHelper.getUserByEmail("default@idalko.com")
issue.reporter = nodeHelper.getUserByEmail(replica.reporter?.email) ?: defaultUser
issue.assignee = nodeHelper.getUserByEmail(replica.assignee?.email) ?: defaultUser
*/

/*
Comment Synchronization

Sync comments with the original author if the user exists in the local instance
Remove original Comments sync line if you are using this approach
issue.comments = commentHelper.mergeComments(issue, replica){ it.executor = nodeHelper.getUserByEmail(it.author?.email) }
*/

/*
Status Synchronization

Sync status according to the mapping [remote issue status: local issue status]
If statuses are the same on both sides don't include them in the mapping
def statusMapping = ["Open":"New", "To Do":"Backlog"]
def remoteStatusName = replica.status.name
issue.setStatus(statusMapping[remoteStatusName] ?: remoteStatusName)
*/

/*
Custom Fields

This line will sync Text, Option(s), Number, Date, Organization, and Labels CFs
For other types of CF check documentation
issue.customFields."CF Name".value = replica.customFields."CF Name".value
*/