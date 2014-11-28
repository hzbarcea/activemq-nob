activemq-nob
============

ActiveMQ tools and plugins for configuring networks of brokers


 Supervisor
 ==========
 
 To run the supervisor move to the supervisor subproject directory and start a test instance using maven:
 
 cd activemq-nob-supervisor
 mvn -P server
 
 You can now test the rest service by navigating in your browser to the link below.
 http://localhost:9000/services/nob/broker/12
 
 Configuring Brokers
 ===================
 
 To take advantage of the nob configuration service we need to configure the broker accordingly.
 Here's how...
 
 [TODO]