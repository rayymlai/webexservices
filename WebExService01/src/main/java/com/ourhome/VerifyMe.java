package com.ourhome;

/**
 * @author ray
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
