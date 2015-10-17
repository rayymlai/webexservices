# WebEx REST API micro-service
Updated: Oct 16, 2015

This Maven project creates REST API Web services that can be deployed in a dockerized Tomcat Web container as a micro-service. It wraps public WebEx XML APIs, and exposes as JAX-RS REST API. It provides examples of:
* Creating RESTful Web services by wrapping public WebEx XML APIs
* Positive and negative test cases in JUnit test
* Use of ESAPI Maven plugin for cross-site scripting prevention
* Use of findbugs for static code analysis (code quality and seccurity vulnerabilities)

##Overview
This reference application contains:
* Creating RESTful Web services based on Java EE design patterns
* Unit tests with positive and negative test cases 
* Static code analysis using findBugs (code quality and security)
* Use jetty container for local testing - without the need to deploy manually to a remote app server

##Project Structure
/src/main/java/com.ourhome

* ConferencingServiceEndpoint.java - REST API contract for webex meetings. This end-point will act as a facade to ConferencingService.java backend service
* VerifyMe.java - simple validator (e.g. http://localhost:8080, or http://localhost:8080/conferencing/verifyMe)
   
/src/main/java/com.ourhome.helper
* ServiceHelper.java - various helper classes or utilities, e.g. read config file, validate email address, construct WebEx XML payload
  
/src//main/java/com.ourhome.model
* MeetingSchedule.java - data model for the meeting object used in REST API, aka Data Transfer Object pattern
  
/src//main/java/com.ourhome.services
* ConferencingService.java - backend service to create WebEx. This service tier implements business logic, and/or invokes the remote WebEx XML API
      
/config
* webex.properties - This file contains 3 data fields (siteName, hostAccount and hostPassword) that are essential to access WebEx

/resources
* ESAPI.properties - This file white-lists or black-lists the escape/special/control characters that may be used for cross-site scripting security attacks (used by OWASP ESAPI plugin)  
* validation.properties - This file defines the validation regex expression used for cross-site scripting check (used by OWASP ESAPI plugin).

/webapp/index.jsp
* This demo page will invoke VerifyMe.java to verify if this Web service is working


##Demo Guide
1. After editing webex.properties with valid values for WebEx access, run "mvn clean install" and "mvn jetty:run" to deploy the app
2. Open a new Web browser with URL http://localhost:8080/conferencing/verifyMe to test if REST API is working
3. Use 'curl' on your Linux/Mac (or Windows) host to test if your REST API is working, e.g.
e.g. 
```
curl -X POST -H "Content-type: application/json" -d '{"inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 00:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings
```

##REST API CONTRACTS

1. verifyMe - verify if war file is deployed correctly, and working fine
<hostname><port>/conferencing/verifyMe
GET

Example: 
```
http://localhost:8080/conferencing/verifyMe
```

2. createMeeting - create an online meeting (e.g. meeting center) by posting a JSON object with start date, title, meeting password
<hostname><port>/conferencing/meetings
POST

Example: 
```
%curl -X POST -H "Content-type: application/json" -d '{"inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 00:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings
Meeting created. Meeting details={"meetingHost":"test","inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 00:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789","meetingKey":"625566782","hostMeetingURL":"https://acstest-d.webex.com/acstest-d/j.php?MTID\u003dm6638e39dd6d2208ed201796830cb965e","attendeeMeetingURL":"https://acstest-d.webex.com/acstest-d/j.php?MTID\u003dmebed1dd43fdd54bebb57df007d674265"}
```

3. updateMeeting - update an existing WebEx meeting (with a known meeting key) by upserting a JSON object with any changes in start date, title, meeting password

<hostname><port>/conferencing/meetings/update/<meetingkey>
POST
Example: 
```
%curl -X POST -H "Content-type: application/json" -d '{"meetingKey":"625566782", "inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 01:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings/update/625566782 
```

4. DeleteMeeting - delete an existing WebEx meeting (with a known meeting key)

<hostname><port>/conferencing/meetings/update/<meetingkey>
DELETE

Example: 
```
%curl -X DELETE -H "Content-type: application/json" -d '{"meetingKey":"629600591", "inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 01:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings/update/629600591
```

5. listMeeting - list existing/future meetings (limit to max # specified in the API)

<hostname><port>/conferencing/meetings/list/<maxNumberOfMeetingListed>
GET

Example: 
```
%curl -X GET -H "Content-type: application/json" -d '{"meetingKey":"629600591", "inviteeList":"rayymlai@gmail.com","startDate":"10/13/2015 01:26:32","meetingTitle":"scrum meeting","meetingPassword":"123456789"}' http://localhost:8080/conferencing/meetings/list/20
```

##Implementation Guide
1. Pre-requisites
1.1 Maven project setup
* To create this Maven project from scratch, I use Maven archetype with Jersey simple webapp to generate the code template.
```
%mvn archetype:generate
```

* Add Maven dependencies: jetty (for easy deployment and testing), ESAPI plugin for XSS, etc.

```
	<build>
	...
		<plugins>
			<plugin>
				<groupId>org.eclipse.jetty</groupId>
				<artifactId>jetty-maven-plugin</artifactId>
				<version>9.2.11.v20150529</version>
			</plugin>
		</plugins>
	</build>
	...
	<dependencies>
                <!-- for JSON binding -->
		<dependency>
			<groupId>com.sun.jersey</groupId>
			<artifactId>jersey-bundle</artifactId>
			<version>${jersey.version}</version>
		</dependency>

		<dependency>
			<groupId>com.sun.jersey</groupId>
			<artifactId>jersey-server</artifactId>
			<version>${jersey.version}</version>
		</dependency>

		<dependency>
			<groupId>com.google.code.gson</groupId>
			<artifactId>gson</artifactId>
			<version>2.4</version>
		</dependency>

		<!-- Security XSS -->

		<dependency>
			<groupId>org.owasp.esapi</groupId>
			<artifactId>esapi</artifactId>
			<version>2.1.0</version>
		</dependency>

		<dependency>
			<groupId>org.jsoup</groupId>
			<artifactId>jsoup</artifactId>
			<version>1.8.3</version>
		</dependency>

		<dependency>
			<groupId>commons-validator</groupId>
			<artifactId>commons-validator</artifactId>
			<version>1.4.0</version>
		</dependency>
	</dependencies>
```

* Add ESAPI.properties and validation.properties under /resources for cross-site scripting 
The properties files are downloaded from OWASP official website.

* Once Maven project is created, generate eclipse setting file so that you can import the project into your eclipse IDE.
```
%mvn eclipse:clear
%mvn eclipse:eclipse
```

1.2 WebEx account credentials
You need to be an existing WebEx customer to get WebEx host account credentials (or sign up for trial). Edit webex.properties to specify your webex siteName (e.g. siteName is abc if your given site URL is abc.webex.com), host account (usually a username, not an email address) and password. This host account should have permission to create, edit or delete meetings.

2. A few useful commands:
To compile the codes:
```
%mvn clean install
```

To scan codes for security defects:
```
%mvn findbugs:findbugs
```

To review static code analysis results using UI:
```
%mvn findbugs:gui
```

To deploy locally for testing or for demo
```
%mvn jetty:run
```

##Design Guide
1.  JAX-RS Jersey framework to define REST API
2.  Separate UI web tier, service tier, data model from REST API
- use singleton for helper class (e.g. config file)
3.  Hide the complexity to create XML security header and payload in the helper class
4.  Incorporate negative test cases to verify invalid email addresses and invalid meeting date


##Constraints
1. WebEx XML API is slow due to XML marshalling/processing and also global routing based on the siteName
2. WebEx XML response is not synchronous or real time - usually <2 sec latency (1 to 2 seconds)


##Disclaimer
The domain knowledge of WebEx can be built up by reading the REST API documentation under https://developer.cisco.com/media/webex-xml-api/Chapter1IntroductiontoWebExXMLServices.html. You'll notice some compatibility issues between XML API version and also the backend WebEx (aka Train) versions.  There are some tricky implementation challenges learned while debugging, e.g. meetingkey is used in setMeeting() but meetingKey (not meetingkey) should be used in deleteMeeting(). 

The REST API implementation is based on a mix of Java EE design patterns, and has nothing related to any WebEx code base.
The deployment design to include ESAPI XSS check, valid email address check, API documentation generator is also not related
to any WebEx code base, or Cisco internal implementation of any WebEx-related Web services.

WebEx XML API sample codes using Java can be found under https://developer.cisco.com/site/webex-developer/develop-test/xml-api/sample-code/.
