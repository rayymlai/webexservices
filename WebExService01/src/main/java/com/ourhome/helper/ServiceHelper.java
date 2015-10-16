/**
 * purpose - read webex config from config file
 * 
 * Config file should contain at least the following data elements
 * - siteName
 * - hostAccount
 * - hostPassword
 * 
 * @author ray - Oct 16, 2015 7:34:32 AM
 * 
 */
package com.ourhome.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.owasp.esapi.ESAPI;

public class ServiceHelper {

	private static ServiceHelper singletonHelper = null;
	private String siteName = "@";
	private String hostAccount = "@";
	private String hostPassword = "@";
	private String siteURL = "@";
	public static final String FAIL = "-1";
	private final String CREATE_MEETING = "CreateMeeting";
	private final String EDIT_MEETING = "SetMeeting";
	private final String DELETE_MEETING = "DelMeeting";
	private final String LIST_MEETING = "LstsummaryMeeting";
	private final String listSeparator = ","; // for parsing invitee email
												// address list
	private final String xmlURL = "WBXService/XMLService"; // WebEx XML API URL
	private final String configFile = "config/webex.properties";
	private static final Logger logger = Logger.getLogger(ServiceHelper.class
			.getName());

	/**
	 * constructor
	 */
	public ServiceHelper() {
		Properties serviceHelper = new Properties();
		InputStream serviceConfigFile = null;
		try {
			serviceConfigFile = new FileInputStream(configFile);
			serviceHelper.load(serviceConfigFile);
			// sanitize siteName and hostAccount after loading from config file,
			// but not password
			siteName = sanitizeXSS(serviceHelper.getProperty("siteName"));
			hostAccount = sanitizeXSS(serviceHelper.getProperty("hostAccount"));
			hostPassword = serviceHelper.getProperty("hostPassword");
			siteURL = "https://" + siteName + ".webex.com/" + xmlURL;
		} catch (IOException ex) {
			ex.printStackTrace();
			logger.info(ex.toString());
		} finally {
			if (serviceHelper != null) {
				try {
					// if file is already open, we should close config file
					serviceConfigFile.close();
				} catch (IOException ex) {
					ex.printStackTrace();
					logger.info("Config file not closed. " + ex.toString());
				}
			}
		}
	}

	/**
	 * return current instance - singleton design pattern
	 * 
	 * @return
	 */
	public static ServiceHelper getInstance() {
		if (singletonHelper == null) {
			singletonHelper = new ServiceHelper();
		}
		return singletonHelper;
	}

	/**
	 * create XML header with host account credentials
	 * 
	 * @return
	 */
	public String secureWebExXMLHeader() {
		String xmlHeader = "<header>\r\n";
		xmlHeader += "<securityContext>\r\n";
		xmlHeader += "<webExID>" + getHostAccount() + "</webExID>\r\n";
		xmlHeader += "<password>" + getHostPassword() + "</password>\r\n";
		xmlHeader += "<siteName>" + getSiteName() + "</siteName>\r\n";
		xmlHeader += "</securityContext>\r\n";
		xmlHeader += "</header>\r\n";
		// for debugging
		if (getSiteName() == null || getSiteName() == " ") {
			logger.info("WebEx site name is empty. Please verify the config file contents.");
		}
		return xmlHeader;
	}

	/**
	 * create WebEx XML enevelope
	 * 
	 * @return
	 */
	public String webExXMLEnvelope() {
		String xmlEnvelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
		xmlEnvelope += "<serv:message xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
		xmlEnvelope += " xmlns:serv=\"http://www.webex.com/schemas/2002/06/service\"";
		xmlEnvelope += " xsi:schemaLocation=\"http://www.webex.com/schemas/2002/06/service\">\r\n";
		return xmlEnvelope;
	}

	/**
	 * create XML body for WebEx meeting request API
	 * 
	 * @param meetingStartDate
	 * @param meetingPassword
	 * @param meetingTitle
	 * @param invitees
	 * @return
	 */
	public String createWebExXMLMeetingRequest(String meetingStartDate,
			String meetingPassword, String meetingTitle, String invitees) {

		String meetingRequest = "<body>\r\n";
		meetingRequest += "<bodyContent xsi:type=\"java:com.webex.service.binding.meeting." + CREATE_MEETING + "\">";
		meetingRequest += "<accessControl>\r\n";
		meetingRequest += "<meetingPassword>" + meetingPassword
				+ "</meetingPassword>\r\n";
		meetingRequest += "</accessControl>\r\n";

		meetingRequest += "<metaData>\r\n";
		meetingRequest += "<confName>" + meetingTitle + "</confName>\r\n";
		meetingRequest += "</metaData>\r\n";

		// add invitees. this example one invitee. we need to change to a list
		meetingRequest += "<participants>\r\n";
		meetingRequest += "<attendees>\r\n";

		// create list of attendee email addresses
		if (invitees == "") {
			// no ops, skip creating the email XML segment
		} else if (invitees == null) {
			invitees = "";
			logger.fine("Email address is null while creating meeting. Please verify the caller.");
		} else {
			// assumption: invitees is a string of email addresses separated by
			// ','
			List<String> inviteeList = Arrays.asList(invitees
					.split(listSeparator));

			for (int i = 0; i < inviteeList.size(); i++) {
				if (this.isValidEmailAddress(inviteeList.get(i))) {
					meetingRequest += "<attendee>\r\n";
					meetingRequest += "<person>\r\n";
					meetingRequest += "<name>" + inviteeList.get(i)
							+ "</name>\r\n";
					meetingRequest += "<email>" + inviteeList.get(i)
							+ "</email>\r\n";
					meetingRequest += "</person>\r\n";
					meetingRequest += "</attendee>\r\n";
				} else {
					logger.info("Email address is invalid. Please verify "
							+ inviteeList.get(i));
				}
			}
		}

		meetingRequest += "</attendees>\r\n";
		meetingRequest += "</participants>\r\n";

		meetingRequest += "<schedule>\r\n";
		meetingRequest += "<startDate>" + meetingStartDate
				+ "</startDate> \r\n";
		meetingRequest += "</schedule>\r\n";

		// send email reminder
		meetingRequest += "<remind>\r\n";
		meetingRequest += "<enableReminder>\r\n";
		meetingRequest += "<emails>\r\n";
		// meetingRequest += "<email>" + invitees + "</email>\r\n";

		// create email reminder based on invitee list
		if (invitees == "") {
			// no ops, skip creating the email XML segment
		} else if (invitees == null) {
			invitees = "";
			logger.fine("Email address is null while creating meeting. Please verify the caller.");
		} else {
			List<String> inviteeList = Arrays.asList(invitees
					.split(listSeparator));
			for (int i = 0; i < inviteeList.size(); i++) {
				if (this.isValidEmailAddress(inviteeList.get(i))) {
					meetingRequest += "<email>" + inviteeList.get(i)
							+ "</email>\r\n";
				} else {
					logger.info("Email address is invalid. Please verify "
							+ inviteeList.get(i));
				}
			}
		}

		meetingRequest += "</emails>\r\n";
		meetingRequest += "</enableReminder>\r\n";
		meetingRequest += "</remind>\r\n";

		meetingRequest += "<attendeeOptions>\r\n";
		meetingRequest += "<emailInvitations>" + "TRUE"
				+ "</emailInvitations>\r\n";
		meetingRequest += "</attendeeOptions>\r\n";

		meetingRequest += "</bodyContent>\r\n";
		meetingRequest += "</body>\r\n";
		meetingRequest += "</serv:message>\r\n";
		return meetingRequest;
	}

	/**
	 * edit meeting
	 * 
	 * @param meetingStartDate
	 * @param meetingPassword
	 * @param meetingTitle
	 * @param invitees
	 * @return
	 */
	public String editWebExXMLMeetingRequest(String meetingKey,
			String meetingStartDate, String meetingPassword,
			String meetingTitle, String invitees) {

		String meetingRequest = "<body>\r\n";
		meetingRequest += "<bodyContent xsi:type=\"java:com.webex.service.binding.meeting."
				+ EDIT_MEETING + "\">";
		meetingRequest += "<accessControl>\r\n";
		meetingRequest += "<meetingPassword>" + meetingPassword
				+ "</meetingPassword>\r\n";
		meetingRequest += "</accessControl>\r\n";

		meetingRequest += "<metaData>\r\n";
		meetingRequest += "<confName>" + meetingTitle + "</confName>\r\n";
		meetingRequest += "</metaData>\r\n";

		// add invitees. this example one invitee. we need to change to a list
		meetingRequest += "<participants>\r\n";
		meetingRequest += "<attendees>\r\n";

		// create list of attendee email addresses
		if (invitees == "") {
			// no ops, skip creating the email XML segment
		} else if (invitees == null) {
			invitees = "";
			logger.fine("Email address is null while editing meeting. Please verify the caller.");
		} else {
			// assumption: invitees is a string of email addresses separated by
			// ','
			List<String> inviteeList = Arrays.asList(invitees
					.split(listSeparator));

			for (int i = 0; i < inviteeList.size(); i++) {
				if (this.isValidEmailAddress(inviteeList.get(i))) {
					meetingRequest += "<attendee>\r\n";
					meetingRequest += "<person>\r\n";
					meetingRequest += "<name>" + inviteeList.get(i)
							+ "</name>\r\n";
					meetingRequest += "<email>" + inviteeList.get(i)
							+ "</email>\r\n";
					meetingRequest += "</person>\r\n";
					meetingRequest += "</attendee>\r\n";
				} else {
					// System.out.println("Email address is invalid. Please verify "
					// + inviteeList.get(i));
					logger.info("Email address is invalid. Please verify "
							+ inviteeList.get(i));
				}
			}
		}

		meetingRequest += "</attendees>\r\n";
		meetingRequest += "</participants>\r\n";

		meetingRequest += "<schedule>\r\n";
		meetingRequest += "<startDate>" + meetingStartDate
				+ "</startDate> \r\n";
		meetingRequest += "</schedule>\r\n";

		// send email reminder
		meetingRequest += "<remind>\r\n";
		meetingRequest += "<enableReminder>\r\n";
		meetingRequest += "<emails>\r\n";
		// meetingRequest += "<email>" + invitees + "</email>\r\n";

		// create email reminder based on invitee list
		if (invitees == "") {
			// no ops, skip creating the email XML segment
		} else if (invitees == null) {
			invitees = "";
			logger.fine("Email address is null while editing meeting. Please verify the caller.");
		} else {
			List<String> inviteeList = Arrays.asList(invitees
					.split(listSeparator));
			for (int i = 0; i < inviteeList.size(); i++) {
				if (this.isValidEmailAddress(inviteeList.get(i))) {
					meetingRequest += "<email>" + inviteeList.get(i)
							+ "</email>\r\n";
				} else {
					logger.info("Email address is invalid. Please verify "
							+ inviteeList.get(i));
				}
			}
		}

		meetingRequest += "</emails>\r\n";
		meetingRequest += "</enableReminder>\r\n";
		meetingRequest += "</remind>\r\n";

		meetingRequest += "<attendeeOptions>\r\n";
		meetingRequest += "<emailInvitations>" + "TRUE"
				+ "</emailInvitations>\r\n";
		meetingRequest += "</attendeeOptions>\r\n";
		meetingRequest += "<meetingkey>" + meetingKey + "</meetingkey>\r\n";

		meetingRequest += "</bodyContent>\r\n";
		meetingRequest += "</body>\r\n";
		meetingRequest += "</serv:message>\r\n";
		return meetingRequest;
	}

	/**
	 * delete webex meeting
	 * 
	 * @param meetingKey
	 * @return
	 */
	public String deleteWebExXMLMeetingRequest(String meetingKey) {

		String meetingRequest = "<body>\r\n";
		meetingRequest += "<bodyContent xsi:type=\"java:com.webex.service.binding.meeting."
				+ DELETE_MEETING + "\">\r\n";
		meetingRequest += "<meetingKey>" + meetingKey + "</meetingKey>\r\n";
		meetingRequest += "</bodyContent>\r\n";
		meetingRequest += "</body>\r\n";
		meetingRequest += "</serv:message>\r\n";
		return meetingRequest;
	}

	/**
	 * list meeting
	 * 
	 * @return
	 */
	public String listWebExXMLMeetingRequest(int meetingListSize) {
		int maxMeetingListSize = 100; // this implementation allows max 100 in
										// the list

		// list meeting options from webex xml api
		String startFrom = "1";
		String maximumNum = "";

		if (meetingListSize <= maxMeetingListSize) {
			maximumNum = Integer.toString(meetingListSize);
		} else if (meetingListSize > maxMeetingListSize) {
			maximumNum = Integer.toString(maxMeetingListSize);
		}

		String listMethod = "OR";
		String orderBy1 = "HOSTWEBEXID";
		String orderAd1 = "ASC";
		String orderBy2 = "CONFNAME";
		String orderAd2 = "ASC";
		String orderBy3 = "STARTTIME";
		String orderAd3 = "ASC";

		String meetingRequest = "<body>\r\n";
		meetingRequest += "<bodyContent xsi:type=\"java:com.webex.service.binding.meeting."
				+ LIST_MEETING + "\">";
		// list meeting by host account
		meetingRequest += "<listControl>\r\n";
		meetingRequest += "<startFrom>" + startFrom + "</startFrom>\r\n";
		meetingRequest += "<maximumNum>" + maximumNum + "</maximumNum>\r\n";
		meetingRequest += "<listMethod>" + listMethod + "</listMethod>\r\n";
		meetingRequest += "</listControl>\r\n";

		meetingRequest += "<order>\r\n";
		meetingRequest += "<orderBy>" + orderBy1 + "</orderBy>\r\n";
		meetingRequest += "<orderAD>" + orderAd1 + "</orderAD>\r\n";
		meetingRequest += "<orderBy>" + orderBy2 + "</orderBy>\r\n";
		meetingRequest += "<orderAD>" + orderAd2 + "</orderAD>\r\n";
		meetingRequest += "<orderBy>" + orderBy3 + "</orderBy>\r\n";
		meetingRequest += "<orderAD>" + orderAd3 + "</orderAD>\r\n";
		meetingRequest += "</order>\r\n";

		meetingRequest += "</bodyContent>\r\n";
		meetingRequest += "</body>\r\n";
		meetingRequest += "</serv:message>\r\n";
		return meetingRequest;
	}

	/**
	 * check if WebEx XML call is successful
	 * 
	 * @param webexResponseXML
	 * @return
	 */
	public boolean isXMLCallSuccessful(String webexResponseXML) {
		try {
			String responseXML = URLDecoder.decode(webexResponseXML, "UTF-8");

			// if successful
			if (responseXML.indexOf("SUCCESS") > 0) {
				return true;
			}
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			logger.info("Host Meeting URL not encoded successfully.  Please check if URL is malformed. "
					+ ex.toString());
		}
		return false;
	}

	/**
	 * If successful WebEx XML api call, retrieve meeting key
	 * 
	 * @param webexResponseXML
	 * @return
	 */
	public String getMeetingKey(String webexResponseXML) {
		int spotX, spotY;
		String meetingKey = FAIL;

		try {
			String responseXML = URLDecoder.decode(webexResponseXML, "UTF-8");

			// if successful
			if (responseXML.indexOf("SUCCESS") > 0) {
				// parse meetingkey
				spotX = responseXML.indexOf("<meet:meetingkey>");
				spotY = responseXML.indexOf("</meet:meetingkey>");
				meetingKey = responseXML.substring(
						spotX + "<meet:meetingkey>".length(), spotY);
				return meetingKey;

			}
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			logger.info("Host Meeting URL not encoded successfully.  Please check if URL is malformed. "
					+ ex.toString());
		}
		return meetingKey;
	}

	/**
	 * If successful WebEx XML api call, retrieve host meeting URL
	 * 
	 * @param webexResponseXML
	 * @return
	 */
	public String getHostMeetingURL(String webexResponseXML) {
		int spotX, spotY;
		String hostMeetingURL = FAIL;

		try {
			String responseXML = URLDecoder.decode(webexResponseXML, "UTF-8");

			// if successful
			if (responseXML.indexOf("SUCCESS") > 0) {

				// parse hostMeetingURL for the host account
				spotX = responseXML.indexOf("<serv:host>");
				spotY = responseXML.indexOf("</serv:host>");
				hostMeetingURL = responseXML.substring(
						spotX + "<serv:host>".length(), spotY);
				return hostMeetingURL;
			}
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			logger.info("Host Meeting URL not encoded successfully.  Please check if URL is malformed. "
					+ ex.toString());
		}
		return hostMeetingURL;
	}

	/**
	 * If successful WebEx XML api call, retrieve attendee meeting URL
	 * 
	 * @param webexResponseXML
	 * @return
	 */
	public String getAttendeeMeetingURL(String webexResponseXML) {
		int spotX, spotY;
		String attendeeMeetingURL = FAIL;

		try {
			String responseXML = URLDecoder.decode(webexResponseXML, "UTF-8");

			// if successful
			if (responseXML.indexOf("SUCCESS") > 0) {

				// parse attendeeMeetingURL for the attendees
				spotX = responseXML.indexOf("<serv:attendee>");
				spotY = responseXML.indexOf("</serv:attendee>");
				attendeeMeetingURL = responseXML.substring(spotX
						+ "<serv:attendee>".length(), spotY);
				return attendeeMeetingURL;
			}
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			logger.info("Meeting URL not encoded successfully.  Please check if URL is malformed. "
					+ ex.toString());
		}
		return attendeeMeetingURL;
	}

	/**
	 * sanitize XSS on a string
	 * 
	 * @param target
	 * @return
	 */

	public String sanitizeXSS(String target) {
		if (target == null) {
			return null;
		}
		target = ESAPI.encoder().canonicalize(target);
		target = target.replaceAll("\0", "");
		target = Jsoup.clean(target, Whitelist.none());
		return target;
	}

	/**
	 * check email address is valid
	 * 
	 * @param emailAddress
	 * @return
	 */
	public boolean isValidEmailAddress(String emailAddress) {

		boolean result = false;

		try {
			InternetAddress emailAddr = new InternetAddress(emailAddress);
			emailAddr.validate();

			// None of these regex patterns can catch most of the invalid email addresses
			//String ePattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
			//String ePattern =
			//   "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
			//
			// this regex pattern seems to be a little better than the above
			String ePattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
			
			Pattern p = java.util.regex.Pattern.compile(ePattern);
			Matcher m = p.matcher(emailAddress);
			return m.matches();
		} catch (AddressException ex) {
			logger.info("Email address '" + emailAddress
					+ "' is invalid. Please verify again.");
			result = false;
		}
		return result;
	}

	/**
	 * check if input date is valid
	 * 
	 * @param inputDate
	 * @param dateFormat
	 * @return
	 */
	public boolean isValidDate(String inputDate, String dateFormat) {

		if (inputDate == null) {
			return false;
		}
		SimpleDateFormat targetDateFormat = new SimpleDateFormat(dateFormat);
		targetDateFormat.setLenient(false);

		try {
			targetDateFormat.parse(inputDate);
		} catch (ParseException ex) {
			logger.info("Input date format is invalid. Please verify again. Details: "
					+ ex.toString());
			return false;
		}
		return true;
	}

	/**
	 * check password policy - must not be null, length > 4, length <= 256
	 * 
	 * @param password
	 * @param length
	 * @return
	 */
	public boolean isValidPassword(String password, int minLength, int maxLength) {
		try {
			if (password.length() == 0 || password.length() <= minLength
					|| password == null || password == "") {
				return false;
			} else {
				if (password.length() <= maxLength) {
					return true;
				} else {
					return false;
				}
			}
		} catch (NullPointerException ex) {
			return false;
		}
	}

	/**
	 * @return the siteName
	 */
	public String getSiteName() {
		return siteName;
	}

	/**
	 * @param siteName
	 *            the siteName to set
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	/**
	 * @return the hostAccount
	 */
	public String getHostAccount() {
		return hostAccount;
	}

	/**
	 * @param hostAccount
	 *            the hostAccount to set
	 */
	public void setHostAccount(String hostAccount) {
		this.hostAccount = hostAccount;
	}

	/**
	 * @return the hostPassword
	 */
	public String getHostPassword() {
		return hostPassword;
	}

	/**
	 * @param hostPassword
	 *            the hostPassword to set
	 */
	public void setHostPassword(String hostPassword) {
		this.hostPassword = hostPassword;
	}

	/**
	 * @return the siteURL
	 */
	public String getSiteURL() {
		return siteURL;
	}

	/**
	 * @param siteURL
	 *            the siteURL to set
	 */
	public void setSiteURL(String siteURL) {
		this.siteURL = siteURL;
	}

	/**
	 * @return the xmlURL
	 */
	public String getXmlURL() {
		return xmlURL;
	}
}
