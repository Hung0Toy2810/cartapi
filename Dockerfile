# Dockerfile
FROM tomcat:10.1-jdk17

# Xóa app mặc định
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy file ROOT.war
COPY target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

# Expose port
EXPOSE 8080

# Tự động deploy
CMD ["catalina.sh", "run"]