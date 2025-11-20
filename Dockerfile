FROM tomcat:10.1-jdk17-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

# WAR tự động chứa META-INF/context.xml rồi → không cần copy gì thêm
COPY target/Cartapi.war /usr/local/tomcat/webapps/ROOT.war

RUN mkdir -p /usr/local/tomcat/logs && chmod 777 /usr/local/tomcat/logs

EXPOSE 8080
CMD ["catalina.sh", "run"]