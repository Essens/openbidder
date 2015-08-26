#!/bin/bash
#
# Wrapper script for maven-run-bidder.sh that configures it to run with a
# predefined module, main, bidder server properties, and interceptors.
INTERCEPTORS="\
--bid_interceptors=org.mobitrans.minimumInterceptor \
"

sh maven-run-bidder.sh -module mobitrans-bidder-server \
-main com.google.openbidder.sample.SamplesServer $INTERCEPTORS