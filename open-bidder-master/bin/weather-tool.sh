#!/bin/bash
#
# Runs the Weather Interceptor sample's WeatherTool.

set -e

if [[ ! $# == 1 ]]; then
  echo "$0 'command [arguments...]'"
  exit 1
fi

if [[ ! -d open-bidder-weather ]]; then
  echo "Expecting to be run in open-bidder top level directory"
  exit 1
fi

ARGS="$OPENBIDDER_ARGS $@"

mvn -q -pl open-bidder-weather exec:java \
  -Djava.util.logging.config.file=`pwd`/open-bidder-weather/src/test/resources/logging.properties \
  -Dexec.mainClass=com.google.openbidder.weather.tool.WeatherTool \
  -Dexec.classpathScope=test \
  -Dexec.args="${ARGS[@]}"
