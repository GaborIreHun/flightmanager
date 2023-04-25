FROM java:8
ADD target/flightmanager-0.0.1-SNAPSHOT.jar flightmanager-0.0.1-SNAPSHOT.jar
ENTRYPOINT [ "java","-jar","flightmanager-0.0.1-SNAPSHOT.jar" ]