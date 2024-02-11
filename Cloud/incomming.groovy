import java.sql.Timestamp
import java.util.Calendar
import java.text.SimpleDateFormat
def setNewParentField(){
   // This will set the new Parent field in Jira Cloud
    def remoteEpic = replica.customFields."Epic Link"?.value?.urn
    def localIssue = nodeHelper.getLocalIssueFromRemoteUrn(remoteEpic)?.key
    def url = "/rest/api/3/issue/${issue.key}/"
    def dataKey = null
    // We'll check if the remote side has a Epic Link, if they removed their epic link it will also be removed on the local side.
    // If we want to keep the epic link even when removed on the remote site remove the null check and the dataKey var.
    if(remoteEpic != null) dataKey = "\"${localIssue}\""

    def data = "{\"fields\": {\"parent\": {\"key\" : ${dataKey}}}}"
    
    httpClient.put(url,data)
}

// subTasks
// parent Id is not synced over on first sync from onPrem
// parent ID comes only over from the second sync
//debug.error(replica.parentId.toString())
if(firstSync && replica.parentId){
    issue.projectKey   = "DEMO" 
    issue.typeName     = "sub-task" //Make sure to use the right subtask type here.
    issue.summary      = replica.summary
	def localParent = nodeHelper.getLocalIssueFromRemoteId(replica.parentId.toLong())
	if(localParent){
        issue.parentId = localParent.id
	} else {
       throw new com.exalate.api.exception.IssueTrackerException("Subtask cannot be created: parent issue with remote id " + replica.parentId + " was not found. Please make sure the parent issue is synchronized before resolving this error" )
    }
    return
}
// if issue does not exsists and it already has an "Epic Link" it will be set on the first sync
if(firstSync){
   issue.projectKey   = "DEMO" 
   // Set type name from source issue, if not found set a default
   issue.typeName     = nodeHelper.getIssueType(replica.type?.name, issue.projectKey)?.name ?: "Task"
   issue.summary      = replica.summary
   store(issue) 
   setNewParentField()
}


// If the issue is already synced over and you add the Epic Link then it will be set or if you change the Epic link
if (!firstSync){
    setNewParentField()
}
issue.summary      = replica.summary
issue.description  = replica.description
issue.comments     = commentHelper.mergeComments(issue, replica)
issue.labels       = replica.labels

def linkMap = [
    "Fix":"Works"
]
replica.issueLinks.each{ l ->
    l.linkTypeName = linkMap[l.linkTypeName] ?: l.linkTypeName
}
issue.issueLinks = replica.issueLinks

// The filesize we send over is in bytes this converts the size to gb
def bytesToGb(filesize){
    if (filesize == 0) return 0
    double gb = filesize / (1024 * 1024 * 1024) 
    return Math.round(gb * 1000000) / 1000000d
}
// Create an empty list where we store the attachments in that are smaller then 300 mb/attachment
def tmpAtt = []
replica.attachments.each { 
    attachment ->
    // Check if the file is less than 300 Mb 
    if (bytesToGb(attachment.filesize) < 0.3){
        tmpAtt += attachment
    } 
}
// Note this will set the attachments that are only coming over from the remote site
issue.attachments = tmpAtt


def multiColor = replica.customFields."Multi color"?.value.each{
    c -> 
    issue.labels += nodeHelper.getLabel(c?.value.toString())
}


issue.customFields."Multi picklist".value = replica.customFields."Multi color"?.value



// Tempo
// To map the user to the right one, set default user when not found.
def defaultUser = "christophe.debeule@exalate.com"
def wLogUserMap = [
        "user1":"user2"
]

// This will add one or more days to your given time
Date addOneDayToTimestamp(Timestamp timestamp, Integer daysToAdd) {
    Calendar calendar = Calendar.getInstance()
    calendar.setTimeInMillis(timestamp.getTime())
    // Add day(s) to your given time
    calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
    
    return calendar.getTime()
}

issue.workLogs = workLogHelper.mergeWorkLogs(issue, replica, {w ->
    w.author = nodeHelper.getUserByEmail(wLogUserMap[w.author?.email] ?: defaultUser)
    w.startDate = addOneDayToTimestamp(w.startDate, 1) // Only add positive numbers 
})
