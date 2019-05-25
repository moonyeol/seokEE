TCPServer.class: TCPServer.java DBCon.class docs.class JsonInterface
	javac -cp .:./lib/* TCPServer.java
DBCon.class: JsonInterface.class
	javac -cp .:./lib/komoran.jar:./lib/aho-corasick.jar:./lib/shineware-common-2.0.jar:./lib/shineware-ds-1.0.jar:./lib/mariadb-java-client-2.4.1.jar DBCon.java
docs.class:
	javac -cp .:./lib/* docs.java
JsonInterface:
	javac JsonInterface.java	
RSA1024Util.class:
	javac -cp .:./lib/* RSA1024Util.java
AES256Util.class:
	javac -cp .:./lib/* AES256Util.java
clean:
	rm -rf *.class
back:
	nohup java -cp .:./lib/* TCPServer &
run:
	java -cp .:./lib/* TCPServer
