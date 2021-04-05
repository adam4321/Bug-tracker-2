# Software Bug Tracker

This is an application for cataloging software bugs and assigning them
to one or more programmers to be fixed. To allow the users to understand
the backlog, the interface displays the percentage of open bugs for each
programmer and also for the overall bug list.

The application uses Handlebars to server-side render each
of its pages and then vanilla JavaScript on the client-side
to update pages. The server-side uses Java Spring Boot
and the database is MySql deployed locally and it is connectected to
through JDBC.

This implementation is an extension of the UI and a complete backend 
rewrite of a group project from Oregon State University CS-340 
by Adam Wright and Herman Cai.
