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

  $log_dir = '/var/log/haproxy'
  file { $log_dir:
    ensure => directory,
  }

  class { 'openbidder::load_balancer::haproxy':
    request_port => $request_port,
    number_of_processes => 1,
    stats_port => $stats_port,
    log_dir => $log_dir,
    bidders => $bidders,
    bidder_request_port => $bidder_request_port,

    require => File[$log_dir],
  }
}