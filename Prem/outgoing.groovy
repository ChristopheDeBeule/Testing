replica.key            = issue.key
replica.type           = issue.type
replica.assignee       = issue.assignee
replica.reporter       = issue.reporter
replica.summary        = issue.summary
replica.description    = issue.description
replica.labels         = issue.labels
replica.comments       = issue.comments
replica.resolution     = issue.resolution
replica.status         = issue.status
replica.parentId       = issue.parentId

// Work logs
replica.workLogs = issue.workLogs
replica.remainingEstimate = issue.remainingEstimate

replica.priority       = issue.priority
replica.project        = issue.project

replica.customFields."Multi color" = issue.customFields."Multi color"

def bytesToGb(filesize){
    if (filesize == 0) return 0
    double gb = filesize / (1024 * 1024 * 1024) 
    return Math.round(gb * 1000000) / 1000000d
}

def tmpAtt = []

issue.attachments.each { 
    attachment ->
    if (bytesToGb(attachment.filesize) < 0.3){
        tmpAtt += attachment
    } 
}
//debug.error(tmpAtt.toString())
replica.attachments = tmpAtt

//IssueLinks.send()
replica.issueLinks = issue.issueLinks
if (issue.type?.name != "Epic"){
    replica.customFields."Epic Link" = issue.customFields."Epic Link"
}
// This finds any issue in the Epic -> cf[10100] = "Parent Link"
// /rest/api/2/search?jql=cf[10100]="DEMO-18"

//Comment these lines out if you are interested in sending the full list of versions and components of the source project. 
replica.project.versions = []
replica.project.components = []

