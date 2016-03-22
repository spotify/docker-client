#!/bin/bash
set -e

show_help() {
  echo "Usage: $0 <command>"
  echo "Commands: install_docker, dump_docker_config"
}

if [[ -z $1 ]]; then
  show_help
  exit 1
fi

case "$1" in
  install_docker)

    if [[ -z $DOCKER_VERSION ]]; then
      echo "DOCKER_VERSION needs to be set as an environment variable"
      exit 1
    fi

    # TODO detect which docker version is already installed and skip
    # uninstall/reinstall if it matches $DOCKER_VERSION

    # stop docker service if running
    sudo stop docker || :

    sudo apt-get -qq update
    sudo apt-get -q -y purge docker-engine
    apt-cache policy docker-engine
    sudo apt-get -q -y install docker-engine=$DOCKER_VERSION* "linux-image-extra-$(uname -r)"

    if [[ "$DOCKER_VERSION" =~ ^1\.6\..* ]]; then
      # docker-engine 1.6.x packages don't seem to have the upstart job config,
      # so write it ourselves
      echo '
description "Docker daemon"
start on (local-filesystems and net-device-up IFACE!=lo)
stop on runlevel [!2345]
limit nofile 524288 1048576
limit nproc 524288 1048576
respawn
kill timeout 20
script
	DOCKER=/usr/bin/$UPSTART_JOB
	DOCKER_OPTS="-D=true -H=unix:///var/run/docker.sock -H=tcp://127.0.0.1:2375"
	exec "$DOCKER" -d $DOCKER_OPTS
end script
# Don'"'"'t emit "started" event until docker.sock is ready.
# See https://github.com/docker/docker/issues/6647
post-start script
	DOCKER_OPTS="-D=true -H=unix:///var/run/docker.sock -H=tcp://127.0.0.1:2375"
	while ! [ -e /var/run/docker.sock ]; do
		initctl status $UPSTART_JOB | grep -qE "(stop|respawn)/" && exit 1
		echo "Waiting for /var/run/docker.sock"
		sleep 0.1
	done
	echo "/var/run/docker.sock is up"
end script
      ' | sudo tee /etc/init/docker.conf
      sudo cat /etc/init/docker.conf
      sudo start docker
    else
      # set DOCKER_OPTS to make sure docker listens on the ports we intend
	    echo 'DOCKER_OPTS="-D=true -H=unix:///var/run/docker.sock -H=tcp://127.0.0.1:2375"' | sudo tee -a /etc/default/docker
      sudo restart docker
    fi

    ;;

  dump_docker_config)
    # output the upstart config and default config in case they are needed for
    # troubleshooting
    echo "Contents of /etc/init/docker.conf:"
    sudo cat /etc/init/docker.conf

    echo "Contents of /etc/default/docker"
    sudo cat /etc/default/docker || :

    echo "Contents of /var/log/upstart/docker.log"
    sudo cat /var/log/upstart/docker.log

    ;;

  *)
    echo "Unknown command $1"
    exit 2
esac
