# Copyright (c) 2012 Google.
#
# Class: openbidder::load_balancer
#
# This class sets a host up as an Open Bidder load balancer
#
# Parameters:
# $cpus:: Balancer's number of cpu cores
# $request_port:: Port to listen for requests (bids, impression/click).
# $stats_port:: Port that HA Proxy will expose a statistics page.
# $bidders:: An array of bidder hashes.  Each bidder hash must have the keys 'ip' and 'cpus'.
# $bidder_request_port:: Port that the bidders will listen for requests (bids, impression/click).
#
class openbidder::load_balancer(
    $request_port,
    $stats_port,
    $bidders,
    $bidder_request_port) {

  case $operatingsystem {
    centos, redhat: {
      $curl = '/bin/curl'
    }
    debian, ubuntu: {
      $curl = '/usr/bin/curl'
    }
    default: {
      fail("Unsupported system image!")
    }
  }

  exec { 'google-fluentd-installer':
    command => "$curl -f https://storage.googleapis.com/signals-agents/logging/google-fluentd-install.sh -o /var/run/google-fluentd-install.sh",
    creates => '/var/run/google-fluentd-install.sh',
  }

  exec { 'google-fluentd':
    command => '/bin/bash /var/run/google-fluentd-install.sh',
    creates => '/etc/google-fluentd/google-fluentd.conf',
    require => Exec['google-fluentd-installer'],
  }

  class { 'openbidder::load_balancer::haproxy':
    request_port => $request_port,
    number_of_processes => 1,
    stats_port => $stats_port,
    bidders => $bidders,
    bidder_request_port => $bidder_request_port,
    require => Exec['google-fluentd'],
  }
}
