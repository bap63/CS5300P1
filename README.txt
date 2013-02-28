You can run our assignment by deploying the compiled WAR file to a tomcat server.
Using eclipse, you can run our project by creating a new dynamic webproject called CS5300P1, then merging this entire directory with the blank propject. Run using a tomcat 7 webserver.

Our project uses a JSP servlet to render HTML. The session table is built with the ConcurrentHashMap class found in java.util.

Details:

-The session ID is generated from a timestamp added with the client's IP address

-Cookies contain this session ID so the server can access an existing session

-Sessions are timed out after 10 minutes of inactivity.

-Every 30 minutes, a cleanup daemon removes expired entries from the session table.
