FROM tomcat:10.1-jdk17-temurin

# Xóa app mặc định
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR (đổi tên thành ROOT.war để truy cập trực tiếp https://your-api.onrender.com/api/...)
COPY target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

# Tạo thư mục logs (Render đôi khi cần)
RUN mkdir -p /usr/local/tomcat/logs && chmod 777 /usr/local/tomcat/logs

# QUAN TRỌNG NHẤT: Fix cookie JSESSIONID cho Render + localhost HTTPS
COPY context.xml /usr/local/tomcat/conf/context.xml

EXPOSE 8080
CMD ["catalina.sh", "run"]