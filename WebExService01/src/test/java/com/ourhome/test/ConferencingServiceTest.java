package com.ourhome.test;

/**
 * unit test for major functions
 * - cover business services defined in ConferencingService.java
 * - cover positive and negative test cases
 * - cover integration test - combine create/edit/delete meeting sequence
 * 
 * 
 * Limitation
 * - does not cover REST API contract
 * - does not cover small load test
 * 
 * @author ray - Oct 16, 2015 7:40:42 AM
 * 
 * License: CC0 1.0 Universal
 * For more information, please see
 * <http://creativecommons.org/publicdomain/zero/1.0/>
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ourhome.helper.ServiceHelper;
import com.ourhome.model.MeetingSchedule;
import com.ourhome.services.ConferencingService;

public class ConferencingServiceTest {

	private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static Calendar calendar = Calendar.getInstance();
	private Date targetDate = calendar.getTime();

	private String meetingPassword = "123456789";
	private String meetingTitle = dateFormat.format(new Date())
			+ " scrum standup";
	private String invitees = "";

	private String[] validEmailAddresses = new String[] { "email@example.com" };

	private String[] invalidEmailAddresses = new String[] {
			// "plainaddress",
			"#@%^%#$@#$@#.com",
			"@example.com",
			"Joe Smith <email@example.com>",
			"email.example.com",
			"email@example@example.com",
			// ".email@example.com",
			"email.@example.com",
			// "email..email@example.com",
			"あいうえお@example.com",
			"email@example.com (Joe Smith)",
			// "email@example",
			// "email@-example.com",
			// "email@example.web",
			// "email@111.222.333.44444",
			"email@example..com", "Abc..123@example.com",
			"just\"not\"right@example.com" };

	private String[] validDates = new String[] { "1/1/2000", // leading 0s for
																// day and month
																// optional
			"01/1/2000", // leading 0 for month only optional
			"1/01/2000", // leading 0 for day only optional
			// "01/01/1800", // first accepted date
			// "12/31/2199", // last accepted date
			"01/31/2000", // January has 31 days
			"03/31/2000", // March has 31 days
			"05/31/2000", // May has 31 days
			"07/31/2000", // July has 31 days
			"08/31/2000", // August has 31 days
			"10/31/2000", // October has 31 days
			"12/31/2000", // December has 31 days
			"04/30/2000", // April has 30 days
			"06/30/2000", // June has 30 days
			"09/30/2000", // September has 30 days
			"11/30/2000", // November has 30 days
	};
	private String[] validHours = new String[] { "11:22:30", "01:02:03",
			"12:13:14", "23:59:59", "00:00:01" };
	private String[] invalidHours = new String[] { "24:24:24",
			// "01:02:03:04",
			"23,59,59", "24:01:02",
	// "00:00:00"
	};
	private String[] invalidDates = new String[] { "00/01/2000", // there is no
																	// 0-th day
			"00/01/2000", // there is no 0-th month
			// "12/31/0098", //out of lower boundary date
			// "01/01/2200", //out of high boundary date
			"01/32/2000", // January doesn't have 32 days
			"03/32/2000", // March doesn't have 32 days
			"05/32/2000", // May doesn't have 32 days
			"07/32/2000", // July doesn't have 32 days
			"08/32/2000", // August doesn't have 32 days
			"10/32/2000", // October doesn't have 32 days
			"12/32/2000", // December doesn't have 32 days
			"04/31/2000", // April doesn't have 31 days
			"06/31/2000", // June doesn't have 31 days
			"09/31/2000", // September doesn't have 31 days
			"11/31/2000", // November doesn't have 31 days
			// "02/001/2000", //SimpleDateFormat valid date (day with leading
			// 0s) even with lenient set to false
			// "1/0002/2000", //SimpleDateFormat valid date (month with leading
			// 0s) even with lenient set to false
			// "01/02/0003", //SimpleDateFormat valid date (year with leading
			// 0s) even with lenient set to false
			"01.01/2000", // . invalid separator between day and month
			"01/01.2000", // . invalid separator between month and year
			"01/01-2000", // / invalid separator between day and month
			"01-01/2000", // / invalid separator between month and year
			"01/01-2000", // _ invalid separator between day and month
			"01/01_2000", // _ invalid separator between month and year
			"01/01/2000/12345", // only whole string should be matched
			"13/01/2000", // month bigger than 13
	};

	@BeforeClass
	public static void setup() {
		// set calendar to 3 months later from current system date
		calendar.setTime(new Date());
		calendar.set(Calendar.DAY_OF_MONTH,
				calendar.get(Calendar.DAY_OF_MONTH) + 3);
	}

	@AfterClass
	public static void tearDown() {

	}

	@Test
	public void testCreateMeetingInvalidEmails() {
		String meetingKey;
		String result;

		String meetingStartDate = dateFormat.format(targetDate);
		MeetingSchedule meeting = null;
		for (String invalidEmail : invalidEmailAddresses) {
			// step 1 - check if invalid email
			Assert.assertFalse(
					"Verify invalid email address: " + invalidEmail,
					ServiceHelper.getInstance().isValidEmailAddress(
							invalidEmail));
			// step 2 - can create meeting even if invalid email address
			// (invitee not sent)
			meeting = new ConferencingService().createMeeting(meetingStartDate,
					meetingPassword, meetingTitle, invalidEmail);
			meetingKey = meeting.getMeetingKey();
			Assert.assertNotEquals(
					"If createMeeting() is successful, meetingKey should not be -1",
					meetingKey, "-1");

			// step 3 - delete meeting to clean up test data
			result = new ConferencingService().deleteMeeting(meetingKey);
			Assert.assertNotEquals(
					"If deleteMeeting() is successful, result should not be -1",
					result, "-1");
		}
	}

	@Test
	public void testCreateMeetingValidStartDate() {
		String meetingStartDate = "";
		MeetingSchedule meeting = null;
		String meetingKey;
		String result;

		// Step 1 - test valid dates
		for (String validDate : validDates) {
			meetingStartDate = validDate + " " + validHours[0];
			meeting = new ConferencingService().createMeeting(meetingStartDate,
					meetingPassword, meetingTitle, invitees);

			Assert.assertNotNull("Testing valid meeting start date: "
					+ meetingStartDate, meeting);

			// get meeting key
			meetingKey = meeting.getMeetingKey();
			Assert.assertNotEquals(
					"If createMeeting() is successful, meetingKey should not be -1. meetingobject="
							+ meeting.toString(), meetingKey, "-1");

			// delete meeting to clean up test data
			if (!meetingKey.equals("-1")) {
				result = new ConferencingService().deleteMeeting(meetingKey);
				Assert.assertNotEquals(
						"If deleteMeeting() is successful, result should not be -1",
						result, "-1");
			}
		}

		// Step 2 - test valid hours
		for (String validHour : validHours) {
			meetingStartDate = validDates[0] + " " + validHour;
			meeting = new ConferencingService().createMeeting(meetingStartDate,
					meetingPassword, meetingTitle, invitees);
			Assert.assertNotNull("Testing valid meeting start date: "
					+ meetingStartDate, meeting);

			// get meeting key
			meetingKey = meeting.getMeetingKey();
			Assert.assertNotEquals(
					"If createMeeting() is successful, meetingKey should not be -1",
					meetingKey, "-1");

			// delete meeting to clean up test data
			result = new ConferencingService().deleteMeeting(meetingKey);
			Assert.assertNotEquals(
					"If deleteMeeting() is successful, result should not be -1",
					result, "-1");
		}

		// Step 3 - test invalid dates
		for (String invalidDate : invalidDates) {
			meetingStartDate = invalidDate + " " + validHours[0];
			meeting = new ConferencingService().createMeeting(meetingStartDate,
					meetingPassword, meetingTitle, invitees);
			Assert.assertNull("Testing invalid meeting start date: "
					+ meetingStartDate, meeting);

			// invalid date should not create meeting successful, so this block
			// should not be executed
			if (meeting != null) {
				// get meeting key
				meetingKey = meeting.getMeetingKey();
				Assert.assertEquals(
						"If createMeeting() is successful, meetingKey should not be -1",
						meetingKey, "-1");

				// delete meeting to clean up test data
				if (!meetingKey.equals("-1")) {
					result = new ConferencingService()
							.deleteMeeting(meetingKey);
					Assert.assertNotEquals(
							"If deleteMeeting() is successful, result should not be -1",
							result, "-1");
				}
			} else {
				System.out
						.println("************ null meeting for invalid date case");
			}
		}

		// Step 4 - test invalid hours
		for (String invalidHour : invalidHours) {
			meetingStartDate = validDates[0] + " " + invalidHour;
			meeting = new ConferencingService().createMeeting(meetingStartDate,
					meetingPassword, meetingTitle, invitees);
			Assert.assertNull("Testing invalid meeting start date: "
					+ meetingStartDate, meeting);

			// invalid date should not create meeting successful, so this block
			// should not be executed
			if (meeting != null) {
				// get meeting key
				meetingKey = meeting.getMeetingKey();
				Assert.assertEquals(
						"If createMeeting() is successful, meetingKey should not be -1",
						meetingKey, "-1");

				// delete meeting to clean up test data
				if (!meetingKey.equals("-1")) {
					result = new ConferencingService()
							.deleteMeeting(meetingKey);
					Assert.assertNotEquals(
							"If deleteMeeting() is successful, result should not be -1",
							result, "-1");
				}
			} else {
				System.out
						.println("************ null meeting for invalid hour case");
			}
		}

	}

	@Test
	public void testCreateMeetingCoverage() {
		dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		targetDate = calendar.getTime();
		String meetingStartDate = dateFormat.format(targetDate);
		String meetingPassword = "123456789";
		meetingTitle = dateFormat.format(new Date()) + " scrum standup";
		MeetingSchedule meeting = null;

		// Case 1 - - happy path, all input parameters tested
		meeting = new ConferencingService().createMeeting(meetingStartDate,
				meetingPassword, meetingTitle, validEmailAddresses[0]);
		Assert.assertNotNull("Testing createMeeting(_, _, _, _)", meeting);

		// Case 2 - createMeeting(null, _, _, _)
		meeting = new ConferencingService().createMeeting(null,
				meetingPassword, meetingTitle, validEmailAddresses[0]);
		Assert.assertNull("Testing createMeeting(null, _, _, _)", meeting);

		// Case 3 - createMeeting( _, null, _, _)
		meeting = new ConferencingService().createMeeting(meetingStartDate,
				null, meetingTitle, validEmailAddresses[0]);
		Assert.assertNull("Testing createMeeting( _, null, _, _)", meeting);

		// Case 4 - createMeeting( _, _, null, _)
		meeting = new ConferencingService().createMeeting(meetingStartDate,
				meetingPassword, null, validEmailAddresses[0]);
		Assert.assertNotNull("Testing createMeeting( _, _, null, _)", meeting);

		// Case 5 - createMeeting( _, _, _, null)
		// this may throw null pointer exception
		meeting = new ConferencingService().createMeeting(meetingStartDate,
				meetingPassword, meetingTitle, null);
		Assert.assertNotNull("Testing createMeeting( _, _, _, null)", meeting);

	}

	@Test
	public void testEditMeetingCoverage() {
		String meetingStartDate = dateFormat.format(targetDate);
		MeetingSchedule meeting = null;

		// Case 1 - happy path, all input parameters tested
		meeting = new ConferencingService().createMeeting(meetingStartDate,
				meetingPassword, meetingTitle, validEmailAddresses[0]);
		String meetingKey = meeting.getMeetingKey();
		MeetingSchedule editMeeting = new ConferencingService().editMeeting(
				meetingKey, meeting);

		// Case 2 - editMeeting(null, _)
		editMeeting = new ConferencingService().editMeeting(null, meeting);
		Assert.assertEquals("Testing editMeeting(null, _)",
				editMeeting.getMeetingKey(), ServiceHelper.getInstance().FAIL);

		// Case 3 - editMeeting(_, null)
		editMeeting = new ConferencingService().editMeeting(meetingKey, null);
		Assert.assertNull("Testing editMeeting(_, null)", editMeeting);
	}

	@Test
	public void testDeleteMeetingCoverage() {
		String meetingStartDate = dateFormat.format(targetDate);
		MeetingSchedule meeting = null;

		// Case 1 - happy path, all input parameters tested
		meeting = new ConferencingService().createMeeting(meetingStartDate,
				meetingPassword, meetingTitle, validEmailAddresses[0]);
		String meetingKey = meeting.getMeetingKey();
		String status = new ConferencingService().deleteMeeting(meetingKey);
		Assert.assertNotEquals("Testing deleteMeeting(_)", status,
				ServiceHelper.getInstance().FAIL);

		// Case 2 - editMeeting(null)
		meeting = new ConferencingService().createMeeting(meetingStartDate,
				meetingPassword, meetingTitle, validEmailAddresses[0]);
		meetingKey = meeting.getMeetingKey();
		status = new ConferencingService().deleteMeeting(null);
		Assert.assertEquals("Testing deleteMeeting(null)", status,
				ServiceHelper.getInstance().FAIL);
	}

	@Test
	public void testCreateEditDeleteMeetingSequence() {
		// Step 1 - create meeting 3 months from now
		// meetingStartDate is set at setup()
		String meetingStartDate = dateFormat.format(targetDate);
		MeetingSchedule meeting = new ConferencingService().createMeeting(
				meetingStartDate, meetingPassword, meetingTitle, invitees);
		Assert.assertNotNull(
				"Successful createMeeting() should return an non-null or non-empty meeting object.",
				meeting);

		// Step 2 - edit meeting - 10 days later
		String meetingKey = meeting.getMeetingKey();
		dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		calendar.setTime(new Date());

		calendar.set(Calendar.DAY_OF_MONTH,
				calendar.get(Calendar.DAY_OF_MONTH) + 10);
		targetDate = calendar.getTime();
		String newMeetingStartDate = dateFormat.format(targetDate);

		meeting.setStartDate(newMeetingStartDate);
		MeetingSchedule modifiedMeeting = new ConferencingService()
				.editMeeting(meetingKey, meeting);
		String newMeetingKey = modifiedMeeting.getMeetingKey();
		Assert.assertEquals(
				"New meeting key from setMeeting() should be the same as Original meeting key",
				meetingKey, newMeetingKey);
		Assert.assertNotEquals(
				"New meeting date from setMeeting() should be Original meeting start date from startMeeting()",
				meetingStartDate, modifiedMeeting.getStartDate());

		// Step 3 - delete meeting
		meetingKey = modifiedMeeting.getMeetingKey();
		String status = new ConferencingService().deleteMeeting(newMeetingKey);
		Assert.assertNotNull(
				"deleteMeeting() should return a result that is non-null.",
				status);

	}

}
