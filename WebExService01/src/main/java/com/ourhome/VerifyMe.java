package com.ourhome;

/**
 * verifyMe - verify if web app is working
 * 
 * License: CC0 1.0 Universal
 * For more information, please see
 * <http://creativecommons.org/publicdomain/zero/1.0/>
 */
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/verifyMe")
public class VerifyMe {

	/**
	 * 
	 * @return
	 */
	@GET
	@Produces("text/plain")
	public String verifyMe() {
		return "WebEx Conferencing service is alive!";
	}
}
