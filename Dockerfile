FROM openjdk:17
ARG JAR_FILE=/build/libs/server-fd79c83.jar
COPY ${JAR_FILE} /ecommerce.jar
ENTRYPOINT ["java","-jar","/ecommerce.jar"]