# WebEx REST API micro-service
This Maven project creates REST API Web service.

OVERVIEW
This reference application contains:
1. REST API to wrap the public Cisco WebEx XML API
2. Unit tests with positive and negative test cases 
3. Static code analysis using findBugs (code quality and security)
4. Use jetty container for local testing - without the need to deploy manually to a remote app server

PROJECT STRUCTURE
/src
   /main/java/com.ourhome
      ConferencingServiceEndpoint.java - REST API contract for webex meetings. This end-point will act as a facade to ConferencingService.java backend service
      VerifyMe.java - simple validator (e.g. http://localhost:8080, or http://localhost:8080/conferencing/verifyMe)
   /main/java/com.ourhome.helper
      ServiceHelper.java - various helper classes or utilities, e.g. read config file, validate email address, construct WebEx XML payload
  /main/java/com.ourhome.model
      MeetingSchedule.java - data model for the meeting object used in REST API, aka Data Transfer Object pattern
  /main/java/com.ourhome.services
      ConferencingService.java - backend service to create WebEx. This service tier implements business logic, and/or invokes the remote WebEx XML API
      
/config/webex.properties
  This file contains 3 data fields (siteName, hostAccount and hostPassword) that are essential to access WebEx
  
/webapp/index.jsp
  This demo page will invoke VerifyMe.java to verify if this Web service is working
  
DEMO GUIDE
1. After editing webex.properties with valid values for WebEx access, run "mvn clean install" and "mvn jetty:run" to deploy the app
2. Open a new Web browser with URL http://localhost:8080/conferencing/verifyMe to test if REST API is working
3. Use 'curl' on your Linux/Mac (or Windows) host to test if your REST API is working, e.g.
e.g. curl -X POST -H "Content-type: application/json" -d '{"inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 00:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings


REST API CONTRACTS

1. verifyMe - verify if war file is deployed correctly, and working fine
<hostname><port>/conferencing/verifyMe
GET

Example: http://localhost:8080/conferencing/verifyMe

2. createMeeting - create an online meeting (e.g. meeting center) by posting a JSON object with start date, title, meeting password
<hostname><port>/conferencing/meetings
POST

Example: curl -X POST -H "Content-type: application/json" -d '{"inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 00:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings
Meeting created. Meeting details={"meetingHost":"test","inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 00:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789","meetingKey":"625566782","hostMeetingURL":"https://acstest-d.webex.com/acstest-d/j.php?MTID\u003dm6638e39dd6d2208ed201796830cb965e","attendeeMeetingURL":"https://acstest-d.webex.com/acstest-d/j.php?MTID\u003dmebed1dd43fdd54bebb57df007d674265"}

3. updateMeeting - update an existing WebEx meeting (with a known meeting key) by upserting a JSON object with any changes in start date, title, meeting password

<hostname><port>/conferencing/meetings/update/<meetingkey>
POST
Example: curl -X POST -H "Content-type: application/json" -d '{"meetingKey":"625566782", "inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 01:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings/update/625566782 

4. DeleteMeeting - delete an existing WebEx meeting (with a known meeting key)

<hostname><port>/conferencing/meetings/update/<meetingkey>
DELETE

Example: curl -X DELETE -H "Content-type: application/json" -d '{"meetingKey":"629600591", "inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 01:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings/update/629600591

5. listMeeting - list existing/future meetings (limit to max # specified in the API)

<hostname><port>/conferencing/meetings/list/<maxNumberOfMeetingListed>
GET

Example: curl -X GET -H "Content-type: application/json" -d '{"meetingKey":"629600591", "inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 01:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings/list/20


IMPLEMENTATION GUIDE
1. Pre-requisites
edit webex.properties to specify your webex siteName (e.g. siteName is abc if your given site URL is abc.webex.com), host account (usually a username, not an email address) and password. This host account should have permission to create, edit or delete meetings.

2. A few useful commands:
To compile the codes:
%mvn clean install

To scan codes for security defects:
%mvn findbugs:findbugs

To review static code analysis results using UI:
%mvn findbugs:gui

To deploy locally for testing or for demo
%mvn jetty:run


DESIGN GUIDE
1.  JAX-RS Jersey framework to define REST API
2.  Separate UI web tier, service tier, data model from REST API
- use singleton for helper class (e.g. config file)
3.  Hide the complexity to create XML security header and payload in the helper class
4.  Incorporate negative test cases to verify invalid email addresses and invalid meeting date


CONSTRAINTS
1. WebEx XML API is slow due to XML marshalling/processing and also global routing based on the siteName
2. WebEx XML response is not synchronous or real time - usually <~2 sec latency
