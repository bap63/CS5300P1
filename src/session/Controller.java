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
	Session user = new Session();
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
	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		//Get action user is trying to execute
		String action = (String) request.getParameter("command");
		String localSessionID = "";

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			//Creates a new cookie
			user.getSession("Default Data",request.getRemoteAddr());
			localSessionID = user.getSessionID();
			String cookieData = localSessionID + "#" + user.getVersionNumber() 
			+ "#" + request.getLocalAddr().toString();
			Cookie cookie = new Cookie("CS5300PROJ1SESSION", cookieData);
			cookie.setMaxAge(615);
			response.addCookie(cookie);
			out.println("Welcome! A new session has been created.");
		} else {
			// Read existing cookie
			for (Cookie retrievedCookie : cookies) {
				String name = retrievedCookie.getName();
				String value = retrievedCookie.getValue();

				if (name.equals("CS5300PROJ1SESSION")) {
					out.println("Welcome back, Session ID is ");
					localSessionID = value.split("#")[0];
					out.println(localSessionID + "<br />");
					user.fetchSession(localSessionID);
					String data = user.readData();
					out.println("Data is :" + data);
				}
			}
		}
		if(action != null && action != ""){
			if(action.equals("Replace")){
				String message = request.getParameter("message");
				user.writeData(message);	
			}
			else if(action.equals("Logout")){
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
				out.println("Session ended.");
			}
			else{
				out.println("Invalid action");
			}
			
		}

		
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
