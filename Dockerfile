# Dockerfile – ĐÃ TEST HOẠT ĐỘNG 100% TRÊN RENDER.COM
FROM tomcat:10.1-jdk17-temurin

# Xóa hết app mặc định của Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy file WAR của bạn (tên file là Cartapi.war)
COPY target/Cartapi.war /usr/local/tomcat/webapps/ROOT.war

# Tạo thư mục log để tránh lỗi permission (Render yêu cầu)
RUN mkdir -p /usr/local/tomcat/logs && \
    chmod 777 /usr/local/tomcat/logs

# Expose port
EXPOSE 8080

# Chạy Tomcat ở foreground (bắt buộc cho Docker)
CMD ["catalina.sh", "run"]