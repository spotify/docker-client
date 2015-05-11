#!/bin/bash -ex

case "$1" in
  pre_machine)
    # ensure correct level of parallelism
    expected_nodes=2
    if [ "$CIRCLE_NODE_TOTAL" -ne "$expected_nodes" ]
    then
        echo "Parallelism is set to ${CIRCLE_NODE_TOTAL}x, but we need ${expected_nodes}x."
        exit 1
    fi

    # have docker bind to both localhost and unix socket
    docker_opts='DOCKER_OPTS="$DOCKER_OPTS -D -H tcp://127.0.0.1:2375 -H unix:///var/run/docker.sock"'
    sudo sh -c "echo '$docker_opts' >> /etc/default/docker"

    cat /etc/default/docker

    ;;

  dependencies)
    mvn clean install -Dmaven.javadoc.skip=true -DskipTests=true -B -V

    ;;

  test)
    # expected parallelism: 2x. needs to be set in the project settings via CircleCI's UI.
    case $CIRCLE_NODE_INDEX in
      0)
        # test with http
        export DOCKER_HOST=tcp://127.0.0.1:2375

        ;;

      1)
        # test with unix sockets
        export DOCKER_HOST=unix:///var/run/docker.sock

        ;;

    esac

    mvn test -B

    ;;

  post_test)
    cp target/surefire-reports/*.xml $CI_REPORTS

    ;;

esac
