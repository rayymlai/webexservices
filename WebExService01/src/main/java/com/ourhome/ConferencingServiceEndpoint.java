package com.ourhome;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ourhome.helper.ServiceHelper;
import com.ourhome.model.MeetingSchedule;
import com.ourhome.services.ConferencingService;

/**
 * REST API contract for backend services
 * 
 * @author ray - Oct 16, 2015 7:40:28 AM
 * 
 */
@Path("/meetings")
public class ConferencingServiceEndpoint {

	public ConferencingServiceEndpoint() {

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * create webex meeting
	 * 
	 * @param meetingStartDate
	 * @param meetingTitle
	 * @param meetingPassword
	 * @param invitees
	 * @return
	 */
	public Response createMeeting(MeetingSchedule meeting) {

		MeetingSchedule validMeetingSchedule = new ConferencingService()
				.createMeeting(meeting.getStartDate(),
						meeting.getMeetingPassword(),
						meeting.getMeetingTitle(), meeting.getInviteeList());

		if (validMeetingSchedule == null
				|| validMeetingSchedule.getMeetingKey().equals(
						ServiceHelper.getInstance().FAIL)) {
			return Response
					.status(400)
					.entity("Failure to create meeting. Please check if you have valid meeting password or valid email address format.")
					.build();
		}
		return Response
				.status(200)
				.entity("Meeting created. Meeting details="
						+ validMeetingSchedule.toString()).build();

	}

	@POST
	@Path("/simple/{meetingTitle}/{meetingPassword}/{invitees}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * create simple webex meeting
	 * use today's date and time, only provide meeting title, meeting password and invitees
	 * 
	 * @param meetingStartDate
	 * @param meetingTitle
	 * @param meetingPassword
	 * @param invitees
	 * @return
	 */
	public Response createSimpleMeeting(
			@PathParam("meetingTitle") String meetingTitle,
			@PathParam("meetingPassword") String meetingPassword,
			@PathParam("invitees") String invitees) {

		// set meetingStartDate to be 3 days later
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
		Date targetDate = calendar.getTime();
		String meetingStartDate = dateFormat.format(targetDate);

		MeetingSchedule meeting = new ConferencingService().createMeeting(
				meetingStartDate, meetingPassword, meetingTitle, invitees);
		if (meeting == null
				|| meeting.getMeetingKey().equals(
						ServiceHelper.getInstance().FAIL)) {
			return Response
					.status(400)
					.entity("Failure to create meeting. Please check if you have valid meeting password or valid email address format.")
					.build();
		} else {
			return Response
					.status(200)
					.entity("Meeting created. Meeting details="
							+ meeting.toString()).build();
		}

	}

	@POST
	@Path("/update/{meetingKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * edit meeting
	 * @param meetingKey
	 * @param meeting
	 * @return
	 */
	public Response editMeeting(@PathParam("meetingKey") String meetingKey,
			MeetingSchedule meeting) {

		MeetingSchedule validMeetingSchedule = new ConferencingService()
				.editMeeting(meetingKey, meeting);

		if (validMeetingSchedule.getMeetingKey().equals(
				ServiceHelper.getInstance().FAIL)) {
			return Response
					.status(400)
					.entity("Failure to edit meeting. Please check if you have valid meeting password or valid email address format.")
					.build();
		}
		return Response
				.status(200)
				.entity("Meeting edited. Meeting details="
						+ validMeetingSchedule.toString()).build();

	}

	@DELETE
	@Path("/update/{meetingKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * delete meeting
	 * 
	 * @param meetingKey
	 * @return
	 */
	public Response deleteMeeting(@PathParam("meetingKey") String meetingKey) {

		String status = new ConferencingService().deleteMeeting(meetingKey);

		if (status.equals(ServiceHelper.getInstance().FAIL)) {
			return Response
					.status(400)
					.entity("Failure to delete meeting. Please check if this meeting key is valid.")
					.build();
		}
		return Response.status(200)
				.entity("Meeting successfully deleted. Details=" + status)
				.build();

	}

	/**
	 * list meeting, max up to meetingListSize
	 * 
	 * @param meetingListSize
	 * @return
	 */
	@GET
	@Path("/list/{meetingListSize}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listMeeting(
			@PathParam("meetingListSize") int meetingListSize) {

		String status = new ConferencingService().listMeeting(meetingListSize);

		if (status.equals(ServiceHelper.getInstance().FAIL)) {
			return Response
					.status(400)
					.entity("Failure to list meeting. Please check with administrator if meetings exist.")
					.build();
		}
		return Response.status(200)
				.entity("Meeting successfully listed. Details=" + status)
				.build();

	}

	@GET
	@Path("/mocktest")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	/**
	 * smoke test - testing with some default test data to see if API works
	 * 
	 * @return
	 */
	public Response mockCreateMeeting() {

		// set meetingStartDate to be 3 days later
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.DAY_OF_MONTH,
				calendar.get(Calendar.DAY_OF_MONTH) + 3);
		Date targetDate = calendar.getTime();
		String meetingStartDate = dateFormat.format(targetDate);

		String meetingPassword = "123456789";
		String meetingTitle = "scrum standup";
		String invitees = "voice-noreply@gmail.com";

		MeetingSchedule meeting = new ConferencingService().createMeeting(
				meetingStartDate, meetingPassword, meetingTitle, invitees);
		if (meeting == null
				|| meeting.getMeetingKey().equals(
						ServiceHelper.getInstance().FAIL)) {
			return Response.status(400).entity("Failure to create meeting")
					.build();
		}
		return Response
				.status(200)
				.entity("Meeting created. Meeting details="
						+ meeting.toString()).build();
	}
}
