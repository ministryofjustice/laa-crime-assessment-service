FROM amazoncorretto:21
RUN mkdir -p /opt/crime-assessment-service/
WORKDIR /opt/crime-assessment-service/
COPY ./build/libs/crime-assessment-service.jar /opt/crime-assessment-service/app.jar
EXPOSE 8091
ENTRYPOINT ["java","-jar","app.jar"]