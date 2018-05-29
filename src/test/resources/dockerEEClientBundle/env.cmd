@echo off
set DOCKER_TLS_VERIFY=1
set DOCKER_CERT_PATH=%~dp0
set DOCKER_HOST=tcp://127.0.0.1:443
REM
REM Bundle for user admin
REM UCP Instance ID REDACTED-REDACTED-REDACTED
REM
REM This admin cert will also work directly against Swarm and the individual
REM engine proxies for troubleshooting.  After sourcing this env file, use
REM "docker info" to discover the location of Swarm managers and engines.
REM and use the --host option to override $DOCKER_HOST
REM
REM Run this command from within this directory to configure your shell:
REM .\env.cmd
