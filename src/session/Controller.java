package session;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.Cookie;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Controller
 */
@WebServlet("/Controller")
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Controller() {
		super();
		// TODO Auto-generated constructor stub
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

		// Get Client IP & Message From The Text Box & Create a session object
		String message = (String) request.getParameter("message");
		String action = (String) request.getParameter("command");
		// out.println(action);

		if (message == null) {
			message = "Welcome To Our Site ;)!";
		}

		Session user = new Session(message, request.getRemoteAddr());

		String localSessionID = user.getSessionID();
		localSessionID = localSessionID + "|" + user.getVersionNumber() + "|"
				+ request.getLocalAddr().toString();

		//out.println(localSessionID);

		// Cookie Time!
		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
			out.println("<html><head></head><body>");
			out.println("<h3>No Cookies Found :(</h3>");
			
			//out.println("No Cookies Found<br />");
			// Make A New Cookie
			Cookie cookie = new Cookie("CS5300PROJ1SESSION", localSessionID);
			cookie.setMaxAge(615);
			response.addCookie(cookie);
		} else {
			//This is just a test - not needed.
			for (Cookie retrievedCookie : cookies) {
				String name = retrievedCookie.getName();
				String value = retrievedCookie.getValue();

				if (name.equals("CS5300PROJ1SESSION")) {
					//out.println("<h1>" + name + " : " + value + "</h1><br />");
				}
			}
		}

		// Responding to button presses
		// String action = request.getParameter("command");
		//out.println(action);
		if (action != null) {
			if (action.equals("Replace")) {
				message = request.getParameter("message");
			} else if (action.equals("Logout")) {
				// Destroy cookie by setting age to zero and reading the cookie
				Cookie destroyCookie = null;
				if (cookies != null) {
					for (Cookie retrievedCookie : cookies) {
						Cookie tempCookie = retrievedCookie;
						if (tempCookie.getName().equals("CS5300PROJ1SESSION")) {
							destroyCookie = tempCookie;
						}
					}
				}
				//Set Age To Zero & 'Add' The Cookie Back To The Client For It To Expire Immediately
				if (destroyCookie != null) {
					destroyCookie.setMaxAge(0);
					response.addCookie(destroyCookie);
					
				}
				
				//Print Logout HTML
				// out.println("TESTS");
				response.setContentType("text/html");
				out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
				out.println("<html><head></head><body>");
				out.println("<h1>Thanks For Visiting!</h1>");
				out.println("<h3>Cookie Successfully Destroyed!</h3>");
				out.println("</body></html>");
				return;
			} else if (action.equals("Refresh")) {
				out.println("Refreshing The Page");
			} else {
				out.println("Oops! An Error Occurred In The Action Button Of The Form!");
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
