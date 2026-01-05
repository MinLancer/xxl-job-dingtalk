# base image
FROM alibabadragonwell/dragonwell:21.0.9.0.9.10-extended-ga-alinux

# maintainer
MAINTAINER xuxueli

# set params
ENV PARAMS=""

# set timezone
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# copy jar
ADD target/xxl-job-admin-3.3.2.jar /app.jar

# command
# log home: -e LOG_HOME=/data/applogs
# jvm options: -e JAVA_OPTS="-Xms128m -Xmx128m"
# app params: -e PARAMS="--server.port=8080"
ENTRYPOINT ["sh","-c","java ${LOG_HOME:+-DLOG_HOME=$LOG_HOME} -jar $JAVA_OPTS /app.jar $PARAMS"]
