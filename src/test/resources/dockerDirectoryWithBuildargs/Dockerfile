FROM busybox
ARG testargument
RUN if [ -z "$testargument" ]; then exit 1; else echo $testargument > /test.txt; fi;
CMD ["cat","/test.txt"]
