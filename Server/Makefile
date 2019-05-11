TCPServer.class: TCPServer.java DBCon.class docs.class
	javac -cp .:json-simple-1.1.1.jar:mysql-connector-java-8.0.15.jar:./lib.*.jar TCPServer.java
DBCon.class:
	javac -cp .:json-simple-1.1.1.jar:mysql-connector-java-8.0.15.jar DBCon.java
docs.class:
	javac -cp .:./lib/* docs.java
clean:
	rm -rf *.class
run:
	java -cp .:json-simple-1.1.1.jar:mysql-connector-java-8.0.15.jar TCPServer

