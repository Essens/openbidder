#!/bin/bash
#
# Runs the bidder using our project specific settings.
# Should be run from the root of open-bidder.

set -e

helpAndEnd() {
  echo "$0 [-debug] [-module <path>] [-main <class>] [-log <path-to-logback.xml>] [main options]"
  echo "Example: $0 -debug -main com.mycompany.MyMain --bid_interceptors=MyTestInterceptor --listen_port=9876"
  echo "You must provide values at least for the following parameters:"
  echo "--api_project_id, --api_project_number, --service_account_id, --p12_file_path"
  echo "If the environment variable OPENBIDDER_ARGS exists, it will be used to contribute parameters."
  exit $1
}

MVN=mvn
MODULE=open-bidder-samples-binary
MAIN=com.google.openbidder.sample.SamplesServer
LOGCONF=`dirname $0`/logback.xml
ARGS="\
  --load_balancer_host=localhost\
  $OPENBIDDER_ARGS\
"

realpath() {
  [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}

while [[ $1 = -* ]]; do
  case $1 in
    -help|-h|-?) helpAndEnd 0 ;;
    -debug) MVN=mvnDebug ;;
    -main) shift; MAIN=$1 ;;
    -log) shift; LOGCONF=$1 ;;
    -module) shift; MODULE=$1 ;;
    *) ARGS+=" $1" ;;
  esac
  shift
done

$MVN -q -pl $MODULE exec:java \
  -Djetty.home=`pwd` \
  -Dlogback.configurationFile=`realpath $LOGCONF` \
  -Dexec.classpathScope="test" \
  -Dexec.mainClass=$MAIN \
  -Dexec.args="${ARGS[@]}"
