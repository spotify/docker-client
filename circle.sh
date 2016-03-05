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
    docker_opts='DOCKER_OPTS="$DOCKER_OPTS -D -H tcp://127.0.0.1:2375 -H unix:///var/run/docker.sock --registry-mirror=http://localhost:5000"'
    sudo sh -c "echo '$docker_opts' >> /etc/default/docker"

    cat /etc/default/docker

    # Fix CircleCI's docker cp according to https://discuss.circleci.com/t/unable-to-use-docker-cp-but-it-worked-2-days-ago/1137/8
    sudo curl -L -o /usr/bin/docker 'https://s3-external-1.amazonaws.com/circle-downloads/docker-1.8.2-circleci-cp-workaround'
    sudo chmod 0755 /usr/bin/docker

    ;;

  post_machine)
    # fix permissions on docker.log so it can be collected as an artifact
    sudo chown ubuntu:ubuntu /var/log/upstart/docker.log

    ;;

  dependencies)
    mvn clean install -Dmaven.javadoc.skip=true -DskipTests=true -B -V

    docker pull registry

    pip install --user codecov

    ;;

  test)
    set +x
    # print version info on the CI machine
    docker version
    # run a registry locally
    docker run -d -p 5000:5000 \
      -e STANDALONE=false \
      -e MIRROR_SOURCE=https://registry-1.docker.io \
      -e MIRROR_SOURCE_INDEX=https://index.docker.io \
      -e STORAGE_PATH=/registry \
      -e AWS_KEY=$AWS_KEY \
      -e AWS_SECRET=$AWS_SECRET \
      -e AWS_REGION=us-east-1 \
      -e AWS_BUCKET=spotify-docker-images \
      -e SETTINGS_FLAVOR=s3 \
      --name=registry \
      registry
    set -x

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
    docker logs registry &> $CIRCLE_ARTIFACTS/registry.log

    ;;

  collect_test_reports)
    cp */target/surefire-reports/*.xml $CIRCLE_TEST_REPORTS || true
    cp */target/failsafe-reports/*.xml $CIRCLE_TEST_REPORTS || true
    codecov

    ;;

esac
