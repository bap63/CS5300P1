package session;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Controller
 */
@WebServlet("/Controller")
public class Controller extends HttpServlet {
	// Create a session variable once!
	Session user = new Session();
	private static final long serialVersionUID = 1L;

	// Cleaner vars
	protected static Timer cleanerTimer = new Timer();
	protected static Cleanup cleaner = new Cleanup();
	
	// Constant values
	private static String cookieName = "CS5300PROJ1SESSION";

	
	private String message = "";
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Controller() {
		super();

		// Cleanup Daemon Initialization (Calls Cleanup using a timer object) 10mins 30mins
		//cleanerTimer.schedule(cleaner, 600000, 1800000);
		
		//Shorter Time For Testing 2secs 30secs
		cleanerTimer.schedule(cleaner, 2000, 30000);
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		// Get action user is trying to execute
		String action = (String) request.getParameter("command");
		String localSessionID = "";

		// Get any existing cookies for the site and look for ours
		Cookie[] cookies = request.getCookies();
		Cookie myCookie = null;
		if (cookies == null) {
			// set the default message
			message = "Welcome!";
			//out.println("Welcome! A new session has been created.");
			// Creates a new cookie
			user.getSession(message, request.getRemoteAddr());
			String[] locations = {request.getLocalAddr().toString()};
			String cookieData = user.createCookieData(locations);
			localSessionID = user.getSessionID();
			myCookie = new Cookie(cookieName, cookieData);
			// use the session expiration time for the cookie expiration
			myCookie.setMaxAge(user.getExpTime());
			response.addCookie(myCookie);
		} else {
			// Read existing cookies to look for ours
			for (Cookie retrievedCookie : cookies) {
				String name = retrievedCookie.getName();
				String value = retrievedCookie.getValue();
				// found our cookie; get the session ID from there
				if (name.equals(cookieName)) {
					myCookie = retrievedCookie;
					//out.println("Welcome back, Session ID is ");
					user.parseCookieData(value);
					localSessionID = user.getSessionID();
					//out.println(localSessionID + "<br />");
					user.fetchSession(localSessionID);
					// TODO: session version and exp must be incremented on every request.
					// this should happen in both the session data and the cookie
					
				}
			}
		}
		// if an action was passed in, execute it
		if (action != null && action != "") {
			// replace the current message stored in the session with the new one
			if (action.equals("Replace")) {
				message = request.getParameter("message");
				user.writeData(message);
			// log the user out by destroying the cookie; 
			// session data will be removed as part of the normal session cleanup process
			} else if (action.equals("Logout") && myCookie != null) {
				// Set Age To Zero & 'Add' The Cookie Back To The Client For It
				// To Expire Immediately
				myCookie.setMaxAge(0);
				response.addCookie(myCookie);
				message = "You have been logged out.";
				//out.println("Session ended.");
			// for reloads, retrieve the existing message from the session data store
			} else if (action.equals("Reload")) {
				message = user.readData();
			} else {
				//out.println("Invalid action");
			}

		}
		
		// HTML Form !!! REMEMBER TO CHANGE BACK TO POST !!!
		// Also need: the expiration time for the session, the network address and port of the server
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
		out.println("<html><head></head><body>");
		out.println("<h1>" + message + "</h1>");
		// only print the form if the user has not logged out
		if (action == null || ! action.equals("Logout")) {
			out.println("<form action=\"Controller\" method=\"get\">");
			out.println("<input type=\"submit\" name=\"command\" value=\"Replace\" />");
			out.println("<input type=\"text\" name=\"message\" />");
			out.println("<br /><br />");
			out.println("<input type=\"submit\" name=\"command\" value=\"Refresh\" />");
			out.println("<br /><br />");
			out.println("<input type=\"submit\" name=\"command\" value=\"Logout\" />");
			out.println("<br /><br />");
			out.println("</form>");
		}
		out.println("<p>Session on (Local): " + request.getLocalAddr().toString() + " | Port: "
		+ request.getLocalPort() +"</p>");
		out.println("<p>Session on (Remote): " + request.getRemoteAddr().toString() + " | Port: "
		+ request.getRemotePort() +"</p>");
		out.println("<p>Expires: " + user.getExpires() + "</p>");
		out.println("<p>Version: " + user.getVersionNumber() + "</p>");
		out.println("</body></html>");

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
