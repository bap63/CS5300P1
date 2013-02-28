CS5300 Project 1a - Submitted Feb 28 2013
by
Chantelle Farmer (csf25), Ben Perry (bap63) & Matthew Green (mcg67)
--------------------------------------------------------------------------------------------------------------

Running The Project
-------------------
You can run our assignment by deploying the compiled WAR file to a tomcat server.
Using eclipse, you can run our project by creating a new dynamic webproject called CS5300P1, then merging this 
entire directory with the blank propject. Run using a tomcat 7 webserver.


Overall Structure
-----------------
-The session ID is generated from a timestamp added with the client's IP address
-Cookies contain this session ID so the server can access an existing session
-Sessions are timed out after 10 minutes of inactivity.
-Every 30 minutes, a cleanup daemon removes expired entries from the session table.


Cookie Format (More Description Needed Here)
-------------
The format of our cookie is as such and is delimited by the '#' sign:
cookieData = this.getSessionID() + "#" + this.getVersionNumber()+ "#" + locations[0];


Session Table Design (More Description Needed Here)
--------------------
The format of our session table:
{data, versionNumber.toString(), expires.toString(), String.valueOf(expires.getTime())};


Description Of The Files
------------------------
We have three files that are used in this Dynamic Web Project. The first is our
Controller which is called Controller.java, the second our custom session object class 
called Session.java and finally, the daemon function which is in a class called Cleanup.java

Controller.java
~~
The controller in this assignment takes on the function of both the View and the Controller (from MVC architectue).
This class is what receives GET and POST requests from a client, creates the session object(our storage mechanism), 
as well as handles the modifcations and issue of our custom cookies 'CS5300PROJ1SESSION'. This class is also 
resposible for the generation of HTML aka the form and for invoking the daemon cleanup process for the hashmap called Cleanup.


Session.java
~~
Session defines our object whereby client sessions are stored. This class creates our session table as well as 
session objects so that when a user issues a GET request to our site, the controller creates a new cookie that is passed 
back and forth between client and server and from where the 'message' is stored and then taken and added to the ConcurrentHashMap in 
this class. The Session class is the backbone of our system as it handles all major operations relating to the table but for 
cleanup which is defined below.


Cleanup.java
~~
This file essentially extends the timer class in Java and sets up a service which checks our ConcurrentHashMap, 
reads through it and compares the time NOW to the timestamp in the table and if the difference is greater 
than 600seconds, the entry is removed and space in the table is freed. This process is evoked in the constructor of the 
Controller.
