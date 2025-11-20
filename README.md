docker run -d \
  --name my-tomcat \
  -p 8080:8080 \
  -v "$(pwd)/target/Cartapi.war":/usr/local/tomcat/webapps/Cartapi.war \
  tomcat:10.1-jdk17