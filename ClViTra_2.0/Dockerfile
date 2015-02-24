FROM ubuntu:14.04
MAINTAINER Aarij Siddiqui <siddiqui ÄT dbis.rwth-aachen.de>
# Let the container know that there is no tty
ENV DEBIAN_FRONTEND noninteractive

# Update base image
RUN sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list
RUN apt-get update -y
#RUN apt-get upgrade -y

# Install build tools
RUN apt-get install -y \
                     openjdk-7-jdk \
                     wget

# Set jdk7 as the default JDK
RUN ln -fs /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java /etc/alternatives/java && \
	wget http://layers.dbis.rwth-aachen.de/jenkins/job/Cloud-Video-Transcoder/lastSuccessfulBuild/artifact/ClViTra_2.0/target/clvitra.war -O /usr/clvitra.war

WORKDIR /home/docker/

# Install Tomcat Server & deploy the service
CMD mkdir /home/docker/tomcat && \
	mkdir /home/docker/tempdircopy && \
	wget http://mirror.netcologne.de/apache.org/tomcat/tomcat-8/v8.0.18/bin/apache-tomcat-8.0.18.tar.gz -O  /home/docker/tempdircopy/apache-tomcat-8.0.18.tar.gz && \
	tar -zxvf /home/docker/tempdircopy/apache-tomcat-8.0.18.tar.gz -C /home/docker/tempdircopy && \
		# Copy the tomcat to the right location
    cp -r /home/docker/tempdircopy/apache-tomcat-8.0.18/* /home/docker/tomcat && \
	cp -r /usr/clvitra.war /home/docker/tomcat/webapps && \
		# Start Tomcat Server
	sh /home/docker/tomcat/bin/catalina.sh run && \

EXPOSE 8080

