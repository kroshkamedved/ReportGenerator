FROM openjdk:17-slim
LABEL authors="kroshkamedved"

WORKDIR /app

COPY ./target/ReportGenerator-0.0.1-SNAPSHOT.jar /app
ENV ALLOWED_ORIGIN="http://localhost"
EXPOSE 8080
CMD ["java", "-jar","ReportGenerator-0.0.1-SNAPSHOT.jar"]