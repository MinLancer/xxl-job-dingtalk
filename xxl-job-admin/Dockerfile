FROM register.dpmall.com/java/java8:1.8.0_202
MAINTAINER xuxueli

ENV PARAMS=""

ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD /target/xxl-job-admin-2.5.0.jar /app.jar

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app.jar $PARAMS"]