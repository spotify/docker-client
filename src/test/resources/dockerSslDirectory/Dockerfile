FROM ubuntu:14.04

RUN apt-get update
RUN apt-get install -y apt-transport-https

RUN echo deb https://get.docker.io/ubuntu docker main > /etc/apt/sources.list.d/docker.list
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 36A1D7869245C8950F966E92D8576A8BA88D21E9
RUN apt-get update
RUN apt-get install -y --force-yes lxc-docker-1.6.2

ADD ca.pem /ca.pem
ADD server.pem /server.pem
ADD serverkey.pem /serverkey.pem

ENTRYPOINT ["docker", "-d"]
CMD ["-b", "none", "--tlsverify", "--tlscacert=ca.pem", "--tlscert=server.pem", "--tlskey=serverkey.pem", "-H=0.0.0.0:2376"]
