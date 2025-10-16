FROM amazoncorretto:21-alpine

RUN addgroup -S appgroup && adduser -u 1001 -S appuser -G appgroup

WORKDIR /opt/crime-assessment-service
COPY build/libs/crime-assessment-service.jar app.jar
RUN chown -R appuser:appgroup /opt/crime-assessment-service

USER 1001

EXPOSE 8091 8092
ENTRYPOINT ["java","-jar","app.jar"]