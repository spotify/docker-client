$Env:DOCKER_TLS_VERIFY = "1"
$Env:DOCKER_CERT_PATH = $(Split-Path $script:MyInvocation.MyCommand.Path)
$Env:DOCKER_HOST = "tcp://127.0.0.1:443"
#
# Bundle for user admin
# UCP Instance ID REDACTED-REDACTED-REDACTED
#
# This admin cert will also work directly against Swarm and the individual
# engine proxies for troubleshooting.  After sourcing this env file, use
# "docker info" to discover the location of Swarm managers and engines.
# and use the --host option to override $DOCKER_HOST
#
# Run this command from within this directory to configure your shell:
# Import-Module .\env.ps1
