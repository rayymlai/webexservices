package com.ourhome.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import com.ourhome.helper.ServiceHelper;
import com.ourhome.model.MeetingSchedule;


/**
 * purpose - backend webex schedule meeting services
 * 
 * @author ray - Oct 16, 2015 7:39:27 AM
 * 
 * License: CC0 1.0 Universal
 * For more information, please see
 * <http://creativecommons.org/publicdomain/zero/1.0/>
 * 
 */
public class ConferencingService {

	private static final Logger logger = Logger
			.getLogger(ConferencingService.class.getName());

	/**
	 * constructor
	 */
	public ConferencingService() {

	}

	/**
	 * create a webex meeting
	 * 
	 * @param meetingStartDate
	 * @param meetingPassword
	 * @param meetingTitle
	 * @param invitees
	 * @return
	 */
	public MeetingSchedule createMeeting(String meetingStartDate,
			String meetingPassword, String meetingTitle, String invitees) {

			// local variables to parse XML response
			String meetingKey = null;
			;
			String hostMeetingURL = null;
			String attendeeMeetingURL = null;
			MeetingSchedule meeting = null;
			
			// if you dont have start date or password, you can't create a meeting. return null meeting
			if ( meetingStartDate == null || meetingPassword == null) {
				return meeting; // null
			}

			// input data validation before creating the meeting request
			boolean validDate = ServiceHelper.getInstance().isValidDate(
					meetingStartDate, "MM/dd/yyyy HH:mm:ss");
			boolean validPassword = ServiceHelper.getInstance()
					.isValidPassword(meetingPassword, 4, 256);

			// validate input parameters. if true, then invoke WebEx XML API to
			// create meeting
			if (validDate && validPassword) {
				meeting = new MeetingSchedule(ServiceHelper.getInstance()
						.getHostAccount(), ServiceHelper.getInstance()
						.sanitizeXSS(meetingTitle), meetingStartDate, invitees,
						meetingPassword);
				// you can still create a meeting even though no invitees or no meeting title
				if (invitees == null) {
					meeting.setInviteeList("");
				}
				if (meetingTitle == null) {
					meeting.setMeetingTitle("");
				}

				try {
					URL siteURLServer = new URL(ServiceHelper.getInstance()
							.getSiteURL());

					// URLConnection supports HTTPS protocol only with JDK 1.4+
					URLConnection urlConnectionXMLServer = siteURLServer
							.openConnection();
					urlConnectionXMLServer.setDoOutput(true);

					PrintWriter out = new PrintWriter(
							urlConnectionXMLServer.getOutputStream());
					String meetingRequest = ServiceHelper.getInstance()
							.webExXMLEnvelope()
							+ ServiceHelper.getInstance()
									.secureWebExXMLHeader()
							+ ServiceHelper.getInstance()
									.createWebExXMLMeetingRequest(
											meetingStartDate, meetingPassword,
											meetingTitle, invitees);
					out.println(meetingRequest);
					out.close();

					// System.out.println("XML Request POSTed to " +
					// ServiceHelper.getInstance().getSiteURL() + "\n");

					logger.fine("XML Request POSTed to "
							+ ServiceHelper.getInstance().getSiteURL() + "\n");
					logger.fine(meetingRequest + "\n");

					// Get response code from XML API (in future, should return
					// meeting
					// key)
					BufferedReader in = new BufferedReader(
							new InputStreamReader(
									urlConnectionXMLServer.getInputStream()));
					String line;
					String responseXML = "";
					while ((line = in.readLine()) != null) {
						responseXML += line;
					}
					in.close();

					meetingKey = ServiceHelper.getInstance().getMeetingKey(
							responseXML);
					hostMeetingURL = ServiceHelper.getInstance()
							.getHostMeetingURL(responseXML);
					attendeeMeetingURL = ServiceHelper.getInstance()
							.getAttendeeMeetingURL(responseXML);

					meeting.setMeetingKey(meetingKey);
					meeting.setHostMeetingURL(hostMeetingURL);
					meeting.setAttendeeMeetingURL(attendeeMeetingURL);

					logger.info("\nWebEx XML API Response - meetingKey=" + meeting.getMeetingKey() + "  createMeeting(): \n"
							+ responseXML);					
					return meeting;
				} catch (NullPointerException ex) {
					ex.printStackTrace();
					logger.info("Create meeting failed - null pointer exception. Please check if input parameters are valid. "
							+ ex.toString());
					return meeting; // return null
				} catch (Exception ex) {
					ex.printStackTrace();
					logger.info("Create meeting failed. " + ex.toString());
					return meeting; // return null
				}
			} else { // invalid date or invalid password
				if (!validDate) {
					logger.info("Cannot create a meeting due to invalid start date.  Date format should be MM/dd/yyyy HH:mm:ss.");
				}
				if (!validPassword) {
					logger.info("Cannot create a meeting due to invalid password. Meeting password policy requires minimum 4 characters.");
				}
			}

		return meeting; // default = unsuccessful, no meeting key
	}

	/**
	 * edit meeting (WebEx XML API: setMeeting)
	 * 
	 * @param meetingKey
	 * @param meeting
	 * @return
	 */ 
	public MeetingSchedule editMeeting(String meetingKey,
			MeetingSchedule meeting) {

		MeetingSchedule editMeeting = null;
		try {
			editMeeting = new MeetingSchedule(ServiceHelper.getInstance()
					.getHostAccount(), meeting.getMeetingTitle(),
					meeting.getStartDate(), meeting.getInviteeList(),
					meeting.getMeetingPassword());

			String hostMeetingURL = null;
			String attendeeMeetingURL = null;

			// input data validation before editing the meeting request
			boolean validDate = ServiceHelper.getInstance().isValidDate(
					meeting.getStartDate(), "MM/dd/yyyy HH:mm:ss");
			boolean validPassword = ServiceHelper.getInstance()
					.isValidPassword(meeting.getMeetingPassword(), 4, 256);

			// validate input parameters. if true, then invoke WebEx XML API to
			// create meeting
			if (validDate && validPassword) {
				editMeeting = new MeetingSchedule(ServiceHelper.getInstance()
						.getHostAccount(), ServiceHelper.getInstance()
						.sanitizeXSS(meeting.getMeetingTitle()),
						meeting.getStartDate(), meeting.getInviteeList(),
						meeting.getMeetingPassword());

				if (meeting.getInviteeList() == null) {
					meeting.setInviteeList("");
				}
				if (meeting.getMeetingTitle() == null) {
					meeting.setMeetingTitle("");
				}
				
				try {
					URL siteURLServer = new URL(ServiceHelper.getInstance()
							.getSiteURL());

					// URLConnection supports HTTPS protocol only with JDK 1.4+
					URLConnection urlConnectionXMLServer = siteURLServer
							.openConnection();
					urlConnectionXMLServer.setDoOutput(true);

					PrintWriter out = new PrintWriter(
							urlConnectionXMLServer.getOutputStream());
					String meetingRequest = ServiceHelper.getInstance()
							.webExXMLEnvelope()
							+ ServiceHelper.getInstance()
									.secureWebExXMLHeader()
							+ ServiceHelper.getInstance()
									.editWebExXMLMeetingRequest(meetingKey,
											editMeeting.getStartDate(),
											editMeeting.getMeetingPassword(),
											editMeeting.getMeetingTitle(),
											editMeeting.getInviteeList());
					out.println(meetingRequest);
					out.close();

					logger.fine("XML Request POSTed to "
							+ ServiceHelper.getInstance().getSiteURL() + "\n");
					logger.fine(meetingRequest + "\n");

					// Get response code from XML API (in future, should return
					// meeting
					// key)
					BufferedReader in = new BufferedReader(
							new InputStreamReader(
									urlConnectionXMLServer.getInputStream()));
					String line;
					String responseXML = "";
					while ((line = in.readLine()) != null) {
						responseXML += line;
					}
					in.close();

					// if WebEx XML API setMeeting() call fails, set meeting key
					// to FAIL
					if (!ServiceHelper.getInstance().isXMLCallSuccessful(
							responseXML)) {
						editMeeting
								.setMeetingKey(ServiceHelper.getInstance().FAIL);
					} else {
						hostMeetingURL = ServiceHelper.getInstance()
								.getHostMeetingURL(responseXML);
						attendeeMeetingURL = ServiceHelper.getInstance()
								.getAttendeeMeetingURL(responseXML);

						editMeeting.setHostMeetingURL(hostMeetingURL);
						editMeeting.setAttendeeMeetingURL(attendeeMeetingURL);
						// if successful, assign meeting key to indicate
						// completion
						editMeeting.setMeetingKey(meetingKey);

						logger.info("\nWebEx XML API Response - meetingKey=" + meeting.getMeetingKey() + "    edit or setMeeting(): \n"
								+ responseXML);
					}

					return editMeeting;
				} catch (NullPointerException ex) {
					ex.printStackTrace();
					logger.info("Edit meeting not successful - null pointer exception. Please check if input parameters are valid. "
							+ ex.toString());
					return meeting; // return null
				} catch (Exception ex) {
					ex.printStackTrace();
					logger.info("Edit meeting not successful. " + ex.toString());
					return meeting; // return null
				}
			} else {
				if (!validDate) {
					logger.info("Cannot edit a meeting due to invalid start date.  Date format should be MM/dd/yyyy HH:mm:ss.");
				}
				if (!validPassword) {
					logger.info("Cannot edit a meeting due to invalid password. Meeting password policy requires minimum 4 characters.");
				}
			}
			// if unsuccessful, assign meeting key to FAIL
			editMeeting.setMeetingKey(ServiceHelper.getInstance().FAIL);
		} catch (NullPointerException ex) {
			return editMeeting; // default = unsuccessful, no meeting key
		}
		return editMeeting; // default = unsuccessful, no meeting key
	}

	/**
	 * delete meeting
	 * 
	 * @param meetingKey
	 * @param meeting
	 * @return
	 */
	public String deleteMeeting(String meetingKey) {
		try {
			URL siteURLServer = new URL(ServiceHelper.getInstance()
					.getSiteURL());

			// URLConnection supports HTTPS protocol only with JDK 1.4+
			URLConnection urlConnectionXMLServer = siteURLServer
					.openConnection();
			urlConnectionXMLServer.setDoOutput(true);

			PrintWriter out = new PrintWriter(
					urlConnectionXMLServer.getOutputStream());
			String meetingRequest = ServiceHelper.getInstance()
					.webExXMLEnvelope()
					+ ServiceHelper.getInstance().secureWebExXMLHeader()
					+ ServiceHelper.getInstance().deleteWebExXMLMeetingRequest(
							meetingKey);
			out.println(meetingRequest);
			out.close();

			// System.out.println("XML Request POSTed to " +
			// ServiceHelper.getInstance().getSiteURL() + "\n");

			logger.fine("XML Request POSTed to "
					+ ServiceHelper.getInstance().getSiteURL() + "\n");
			logger.fine(meetingRequest + "\n");

			// Get response code from XML API (in future, should return
			// meeting
			// key)
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlConnectionXMLServer.getInputStream()));
			String line;
			String responseXML = "";
			while ((line = in.readLine()) != null) {
				responseXML += line;
			}
			in.close();

			if (ServiceHelper.getInstance().isXMLCallSuccessful(responseXML)) {
				logger.info("\nWebEx XML API Response - meetingKey=" + meetingKey +  "    deleteMeeting(): \n"
						+ responseXML);
				return responseXML;
			} else {
				logger.info("\nCannot delete this meeting.\n" + responseXML);
				return ServiceHelper.getInstance().FAIL;
			}
		} catch (NullPointerException ex) {
			return ServiceHelper.getInstance().FAIL; // default = unsuccessful,
														// no meeting key
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("Delete meeting failed. " + ex.toString());
			return ServiceHelper.getInstance().FAIL; // default = unsuccessful,
														// no meeting key
		}
	}

	/**
	 * list meetings
	 * 
	 * @param meetingListSize
	 * @return
	 */
	public String listMeeting(int meetingListSize) {
		try {
			URL siteURLServer = new URL(ServiceHelper.getInstance()
					.getSiteURL());

			// URLConnection supports HTTPS protocol only with JDK 1.4+
			URLConnection urlConnectionXMLServer = siteURLServer
					.openConnection();
			urlConnectionXMLServer.setDoOutput(true);

			PrintWriter out = new PrintWriter(
					urlConnectionXMLServer.getOutputStream());
			String meetingRequest = ServiceHelper.getInstance()
					.webExXMLEnvelope()
					+ ServiceHelper.getInstance().secureWebExXMLHeader()
					+ ServiceHelper.getInstance().listWebExXMLMeetingRequest(
							meetingListSize);
			out.println(meetingRequest);
			out.close();

			// System.out.println("XML Request POSTed to " +
			// ServiceHelper.getInstance().getSiteURL() + "\n");

			logger.fine("XML Request POSTed to "
					+ ServiceHelper.getInstance().getSiteURL() + "\n");
			logger.fine(meetingRequest + "\n");

			// Get response code from XML API (in future, should return
			// meeting
			// key)
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlConnectionXMLServer.getInputStream()));
			String line;
			String responseXML = "";
			while ((line = in.readLine()) != null) {
				responseXML += line;
			}
			in.close();

			if (ServiceHelper.getInstance().isXMLCallSuccessful(responseXML)) {
				logger.info("\nWebEx XML API Response - LstsummaryMeeting(): \n"
						+ responseXML);
				return responseXML;
			} else {
				logger.info("\nCannot list this meeting.\n" + responseXML);
				return ServiceHelper.getInstance().FAIL;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.info("List meeting not successful. " + ex.toString());
		}

		return ServiceHelper.getInstance().FAIL; // default = unsuccessful, no
													// meeting key
	}
}
