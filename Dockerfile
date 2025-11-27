# Stage 1: Build WAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Chạy trên Tomcat 10.1.x (Jakarta EE 9+)
FROM tomcat:10.1-jre17-temurin-jammy

# Xóa webapps mặc định
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR của bạn vào (tên là ROOT.war → truy cập localhost:8080 luôn)
COPY --from=build /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]