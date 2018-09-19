FROM openjdk:8
RUN mkdir -p /opt/public/js/compiled/
RUN mkdir -p /opt/public/css
COPY resources/public/index.html /opt/public/
COPY resources/public/js/compiled/lepus.js /opt/public/js/compiled/
COPY resources/public/css/style.css /opt/public/css/
COPY target/lepus.jar /opt/
WORKDIR /opt
ENTRYPOINT ["java", "-jar", "lepus.jar"]
