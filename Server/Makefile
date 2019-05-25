TCPServer.class: TCPServer.java DBCon.class docs.class JsonInterface
	javac -cp .:./lib/* TCPServer.java
JsonInterface:
	javac JsonInterface.java
DBCon.class: JsonInterface
	javac -cp .:./lib/komoran.jar:./lib/aho-corasick.jar:./lib/shineware-common-2.0.jar:./lib/shineware-ds-1.0.jar:./lib/mariadb-java-client-2.4.1.jar DBCon.java
docs.class:
	javac -cp .:./lib/* docs.java
RSA1024Util.class:
	javac -cp .:./lib/* RSA1024Util.java
AES256Util.class:
	javac -cp .:./lib/* AES256Util.java
clean:
	rm -rf *.class
back:
	nohup java -cp .:./lib/* TCPServer &
network:
	sudo netstat -anp | grep 9000
run:
	java -cp .:./lib/* TCPServer
