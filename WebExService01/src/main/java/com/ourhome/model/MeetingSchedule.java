package com.ourhome.model;

import com.google.gson.Gson;

/**
 * purpose - data model for online conferencing (e.g. meeting schedule)
 * 
 * @author ray - Oct 16, 2015 7:40:16 AM
 * 
 * License: CC0 1.0 Universal
 * For more information, please see
 * <http://creativecommons.org/publicdomain/zero/1.0/>
 * 
 */
public class MeetingSchedule {

	// minimal info to create a meeting
	private String meetingHost;
	private String inviteeList;
	private String startDate;
	private String meetingTitle;
	private String meetingPassword;
	
	// if successful createMeeting(), meetingKey and meetingURL should be created
	private String meetingKey;
	private String hostMeetingURL;
	private String attendeeMeetingURL;

	/**
	 * empty constructor
	 */
	public MeetingSchedule() {

	}

	/**
	 * constructor
	 * 
	 * create an instance of MeetingSchedule.
	 * please note that this is the minimum set of data to create a WebEx meeting
	 * and we don't pass hostPassword in this example (not a good practice to couple credentials)
	 * 
	 * @param meetingHost
	 * @param meetingTitle
	 * @param startDate
	 * @param inviteeList
	 */
	public MeetingSchedule(String meetingHost, String meetingTitle, String startDate,
			String inviteeList, String meetingPassword) {
		this.meetingHost = meetingHost;
		this.meetingTitle = meetingTitle;
		this.startDate = startDate;
		this.inviteeList = inviteeList;
		this.meetingPassword = meetingPassword;
	}
	
	/**
	 * @return the meetingHost
	 */
	public String getMeetingHost() {
		return meetingHost;
	}

	/**
	 * @param meetingHost
	 *            the meetingHost to set
	 */
	public void setMeetingHost(String meetingHost) {
		this.meetingHost = meetingHost;
	}

	/**
	 * @return the inviteeList
	 */
	public String getInviteeList() {
		return inviteeList;
	}

	/**
	 * @param inviteeList
	 *            the inviteeList to set
	 */
	public void setInviteeList(String inviteeList) {
		this.inviteeList = inviteeList;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the meetingTitle
	 */
	public String getMeetingTitle() {
		return meetingTitle;
	}

	/**
	 * @param meetingTitle
	 *            the meetingTitle to set
	 */
	public void setMeetingTitle(String meetingTitle) {
		this.meetingTitle = meetingTitle;
	}

	/**
	 * @return the meetingPassword
	 */
	public String getMeetingPassword() {
		return meetingPassword;
	}

	/**
	 * @param meetingPassword the meetingPassword to set
	 */
	public void setMeetingPassword(String meetingPassword) {
		this.meetingPassword = meetingPassword;
	}

	/**
	 * @return the meetingKey
	 */
	public String getMeetingKey() {
		return meetingKey;
	}

	/**
	 * @param meetingKey the meetingKey to set
	 */
	public void setMeetingKey(String meetingKey) {
		this.meetingKey = meetingKey;
	}

	/**
	 * @return the hostMeetingURL
	 */
	public String getHostMeetingURL() {
		return hostMeetingURL;
	}

	/**
	 * @param hostMeetingURL the hostMeetingURL to set
	 */
	public void setHostMeetingURL(String hostMeetingURL) {
		this.hostMeetingURL = hostMeetingURL;
	}

	/**
	 * @return the attendeeMeetingURL
	 */
	public String getAttendeeMeetingURL() {
		return attendeeMeetingURL;
	}

	/**
	 * @param attendeeMeetingURL the attendeeMeetingURL to set
	 */
	public void setAttendeeMeetingURL(String attendeeMeetingURL) {
		this.attendeeMeetingURL = attendeeMeetingURL;
	}

	/**
	 * convert this object to JSON
	 */
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
