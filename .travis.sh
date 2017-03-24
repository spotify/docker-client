#!/bin/bash
set -e

if [[ -z $1 ]]; then
  "I need a command!"
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
    # Remove old docker files that might prevent the installation and starting of other versions
    sudo rm -fr /var/lib/docker || :

    if [[ "$RC" == "true" ]]; then
        dist_version="$(lsb_release --codename | cut -f2)"
        sudo sh -c "echo deb [arch=$(dpkg --print-architecture)] https://apt.dockerproject.org/repo ubuntu-${dist_version} testing >> /etc/apt/sources.list.d/docker.list"
    fi
    sudo apt-get -qq update
    sudo apt-get -q -y purge docker-engine
    apt-cache policy docker-engine
    sudo apt-get -q -y install docker-engine=$DOCKER_VERSION* "linux-image-extra-$(uname -r)"

    # set DOCKER_OPTS to make sure docker listens on the ports we intend
    echo 'DOCKER_OPTS="-D=true -H=unix:///var/run/docker.sock -H=tcp://127.0.0.1:2375"' | sudo tee -a /etc/default/docker

    if [[ "$DOCKER_VERSION" =~ ^1\.9\..* && ! $(mount | grep /dev/mqueue) ]]; then
      # docker-engine 1.9.x doesn't mount /dev/mqueue which is necessary to test `--ipc=host`
      sudo mkdir -p /dev/mqueue
      sudo mount -t mqueue none /dev/mqueue
    fi

    # restart the service for the /etc/default/docker change we made after
    # installing the package
    sudo restart docker

    if [[ "$ENABLE_SWARM" = "1"  ]]; then
        # initialize docker swarm to be able to run docker tests
        sudo docker swarm init --advertise-addr 127.0.0.1
    fi

    # Wait a minute so we can see more docker logs in case something goes wrong
    sleep 60

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
