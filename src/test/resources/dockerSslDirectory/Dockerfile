FROM docker:1.12.1-dind

ADD ca.pem /ca.pem
ADD server.pem /server.pem
ADD serverkey.pem /serverkey.pem

CMD ["-b", "none", "--tlsverify", "--tlscacert=ca.pem", "--tlscert=server.pem", "--tlskey=serverkey.pem", "-H=0.0.0.0:2376"]
