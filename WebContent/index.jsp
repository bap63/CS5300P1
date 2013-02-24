<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Ben's, Chantelle's &amp; Matt's Index Page</title>
</head>
<body>

	<!-- Start Form -->
	<form action="Controller" method="post">
	
		<input type="hidden" name="action" value="docomplete" />
		
		<input type="submit" name="command" value="Replace" />
		<input type="text" name="message" value="Please enter a value..." />
		<br /><br />
		<input type="submit" name="command" value="Refresh" />
		<br /><br />
		<input type="submit" name="command" value="Log Out!" />
		<br /><br />
	</form>
	<!-- End Form -->

<p>Session on <%= request.getLocalAddr().toString() %></p>
<p>Expires: </p>

</body>
</html>