#!/bin/bash
#
# Copies the bidder distribution files to the GCS bucket
# that you have configured in the UI (Cloud Storage URI)

set -e

helpAndEnd() {
  echo "$0 [-module <name>] [-puppet-module <name>] [-puppet-module-path <path>] <bucket-uri> <pom-version>"
  echo "If it doesn't exist, create a bucket with the given base URI as a prefix and"
  echo "populate it with the distribution files for the given version."
  echo "Example: $0 gs://my-test-bucket 0.6.0"
  exit $1  
}

if [[ "$1" == "--help" ]]; then
  helpAndEnd 0
fi

MODULE=open-bidder-samples-binary
PUPPET_MODULE_PATH=.
PUPPET_MODULE=open-bidder-puppet

while [[ $1 = -* ]]; do
  case $1 in
    -help|--help|-h|-?) helpAndEnd 0 ;;
    -module) shift; MODULE=$1 ;;
    -puppet-module) shift; PUPPET_MODULE=$1 ;;
    -puppet-module-path) shift; PUPPET_MODULE_PATH=$1 ;;
    *) helpAndEnd 1 ;;
  esac
  shift
done

if [[ $# != 2 ]]; then
  helpAndEnd 1
fi

BUCKET=$1
VERSION=$2

if [[ ! 0 == $(gsutil ls $BUCKET > /dev/null 2>&1 ; echo $?) ]]; then
  gsutil mb $BUCKET
  gsutil acl set public-read $BUCKET
fi

echo "Installing Open Bidder $VERSION in bucket $BUCKET"

gsutil cp -a public-read $MODULE/target/$MODULE-$VERSION-bin.tar.gz \
          $BUCKET/open-bidder-binary-$VERSION-bin.tar.gz
gsutil cp -a public-read $PUPPET_MODULE_PATH/$PUPPET_MODULE/target/$PUPPET_MODULE-$VERSION-balancer.tar.gz \
          $BUCKET/open-bidder-puppet-$VERSION-balancer.tar.gz

gsutil ls -l $BUCKET/open-bidder-*-$VERSION-*.tar.gz
