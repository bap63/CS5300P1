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

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			// Creates a new cookie
			user.getSession("Default Data", request.getRemoteAddr());
			localSessionID = user.getSessionID();
			String cookieData = localSessionID + "#" + user.getVersionNumber()
					+ "#" + request.getLocalAddr().toString();
			Cookie cookie = new Cookie("CS5300PROJ1SESSION", cookieData);
			cookie.setMaxAge(615);
			response.addCookie(cookie);
			//out.println("Welcome! A new session has been created.");
			message = "Welcome! A new session has been created.";
		} else {
			// Read existing cookie
			for (Cookie retrievedCookie : cookies) {
				String name = retrievedCookie.getName();
				String value = retrievedCookie.getValue();

				if (name.equals("CS5300PROJ1SESSION")) {
					//out.println("Welcome back, Session ID is ");
					localSessionID = value.split("#")[0];
					//out.println(localSessionID + "<br />");
					user.fetchSession(localSessionID);
					//String data = user.readData();
					//out.println("Data is :" + data);
				}
			}
		}
		if (action != null && action != "") {
			if (action.equals("Replace")) {
				message = request.getParameter("message");
				user.writeData(message);
			} else if (action.equals("Logout")) {
				Cookie destroyCookie = null;
				if (cookies != null) {
					for (Cookie retrievedCookie : cookies) {
						Cookie tempCookie = retrievedCookie;
						if (tempCookie.getName().equals("CS5300PROJ1SESSION")) {
							destroyCookie = tempCookie;
						}
					}
				}
				// Set Age To Zero & 'Add' The Cookie Back To The Client For It
				// To Expire Immediately
				if (destroyCookie != null) {
					destroyCookie.setMaxAge(0);
					response.addCookie(destroyCookie);
				}
				//out.println("Session ended.");
			} else {
				//out.println("Invalid action");
			}

		}
		
		// HTML Form !!! REMEMBER TO CHANGE BACK TO POST !!!
		// Also need: the expiration time for the session, the network address and port of the server
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
		out.println("<html><head></head><body>");
		out.println("<h1>" + message + "</h1>");
		out.println("<form action=\"Controller\" method=\"get\">");
		// out.println("<input type=\"hidden\" name=\"action\" value=\"docomplete\" />");
		out.println("<input type=\"submit\" name=\"command\" value=\"Replace\" />");
		out.println("<input type=\"text\" name=\"message\" />");
		out.println("<br /><br />");
		out.println("<input type=\"submit\" name=\"command\" value=\"Refresh\" />");
		out.println("<br /><br />");
		out.println("<input type=\"submit\" name=\"command\" value=\"Logout\" />");
		out.println("<br /><br />");
		out.println("</form>");
		out.println("<p>Session on (Local): " + request.getLocalAddr().toString() + " | Port: "
		+ request.getLocalPort() +"</p>");
		out.println("<p>Session on (Remote): " + request.getRemoteAddr().toString() + " | Port: "
		+ request.getRemotePort() +"</p>");
		out.println("<p>Expires: " + user.getExpires() + "</p>");
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
