clean:
	mvn clean -U

build:
	mvn clean install

test:
	mvn clean test

run: build
	java -jar target/chat-1.0.0-jar-with-dependencies.jar