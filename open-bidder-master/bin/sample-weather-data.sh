#!/bin/bash
#
# Creates some test rules for the Weather Interceptor sample.

if [[ ! $# == 2 ]]; then
  echo "$0 <bucket> <adgroupId>"
  echo "Example: $0 gs://my-test-bucket 5432894551"
  exit 1
fi

bin/weather-tool.sh "addrules $1 $2   0 100 0 10 0 1 0.95   0 100 10 null 0 1 0.90"
