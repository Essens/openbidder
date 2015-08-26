#!/bin/bash

cd `dirname $0`/open-bidder-exchange-doubleclick/src/test/resources/adx-rtb-dictionaries

for f in $(ls); do
	gsutil cp gs://adx-rtb-dictionaries/$f .
done
