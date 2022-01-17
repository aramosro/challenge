Account transfer challenge
==========================

A Gradle Spring boot REST application for create,get accounts and transfer money between accounts.

Upgrade to a production application
===================================

To convert this basic app to a "production" app, I consider to improve some actions nd add some new functionalities:

- use a real database(MySQL, PostgreSQL, Oracle,..).
- add a framework to database management as spring data, mybatis ... 
- add security authentication and authorization to app with spring security,apache siro ...
- implement Continuos Integration (Jenkins).
- run the application in Docker for secure and easy development and maintenance.
- use sonar to code analysis.
- use swagger for documenting the application.
