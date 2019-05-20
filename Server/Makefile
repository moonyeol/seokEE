TCPServer.class: TCPServer.java DBCon.class docs.class RSA1024Util.class AES256Util.class
	javac -cp .:./lib/* TCPServer.java
DBCon.class:
	javac -cp .:./lib/*:/lib/komoran-2.4-e.jar DBCon.java
docs.class:
	javac -cp .:./lib/* docs.java
RSA1024Util.class:
	javac -cp .:./lib/* RSA1024Util.java
AES256Util.class:
	javac -cp .:./lib/* AES256Util.java
clean:
	rm -rf *.class
run:
	java -cp .:./lib/* TCPServer

