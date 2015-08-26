# Copyright (c) 2012 Google.

# Class: openbidder::load_balancer::haproxy
#
# Installs and configures HaProxy for an Open Bidder load balancer
#
# Parameters:
# $request_port:: Port to listen for requests (bids, impression/click).
# $number_of_processes:: Number of HaProxy processes.
# $stats_port:: Port to run the HAproxy stats
# $chroot_dir:: Directory HAProxy chroot's into (optional)
# $log_level:: HAProxy log level (optional).
# $timeout_client_ms:: HAProxy client timeout (optional)
# $timeout_http_keep_alive_ms:: HAProxy HTTP keep alive timeout (optional)
# $maxconn_core:: HAProxy's global maximum connections per core (optional)
# $bidders:: List of bidders.
# $bidder_request_port:: Port that the bidders will listen for requests (bids, impression/click).
# $bidder_max_queued:: Maximum number of open connections to queue for a bidder [optional]
# $bidder_max_conn_core:: Number of connections per bidder core that
#            HA proxy backend will open.  More than these connections and HA Proxy will
#            queue connections.  The number of HA proxy backends is equal to the number
#            of cores on the load balancer instance.
# $bidder_timeout_connect_ms:: HAProxy connect timeout (optional)
# $bidder_timeout_queue_ms:: HAProxy queue timeout (optional)
# $bidder_timeout_server_ms:: HAProxy server timeout (optional)
# $impression_max_queued:: Idem to bidder_max_queued, for impression tracking.
# $impression_max_conn_core:: Idem to bidder_max_conn_core, for impression tracking.
# $impression_timeout_connect_ms:: HAProxy connect timeout (optional)
# $impression_timeout_queue_ms:: HAProxy queue timeout (optional)
# $impression_timeout_server_ms:: HAProxy server timeout (optional)
# $click_max_queued:: Idem to bidder_max_queued, for click tracking.
# $click_max_conn_core:: Idem to bidder_max_conn_core, for click tracking.
# $click_timeout_connect_ms:: HAProxy connect timeout (optional)
# $click_timeout_queue_ms:: HAProxy queue timeout (optional)
# $click_timeout_server_ms:: HAProxy server timeout (optional)
# $pixel_max_queued:: Idem to bidder_max_queued, for pixel tracking.
# $pixel_max_conn_core:: Idem to bidder_max_conn_core, for pixel tracking.
# $pixel_timeout_connect_ms:: HAProxy connect timeout (optional)
# $pixel_timeout_queue_ms:: HAProxy queue timeout (optional)
# $pixel_timeout_server_ms:: HAProxy server timeout (optional)
#
class openbidder::load_balancer::haproxy (
    $request_port,
    $number_of_processes,
    $stats_port,
    $chroot_dir = '/usr/share/haproxy',
    $log_level = 'info',
    $timeout_client_ms = 10000,
    $timeout_http_keep_alive_ms = 10000,
    $maxconn_core = 500,
    $bidders,
    $bidder_request_port,
    $bidder_max_queued = 1,
    $bidder_max_conn_core = 250,
    $bidder_timeout_connect_ms = 20,
    $bidder_timeout_queue_ms = 10,
    $bidder_timeout_server_ms = 100,
    $impression_max_queued = 1,
    $impression_max_conn_core = 10,
    $impression_timeout_connect_ms = 5000,
    $impression_timeout_queue_ms = 10,
    $impression_timeout_server_ms = 5000,
    $click_max_queued = 1,
    $click_max_conn_core = 5,
    $click_timeout_connect_ms = 5000,
    $click_timeout_queue_ms = 10,
    $click_timeout_server_ms = 5000,
    $pixel_max_queued = 1,
    $pixel_max_conn_core = 5,
    $pixel_timeout_connect_ms = 5000,
    $pixel_timeout_queue_ms = 10,
    $pixel_timeout_server_ms = 5000) {

  #
  # HA Proxy
  #
  package { 'haproxy':
    ensure => installed,
  }

  file { $chroot_dir:
    ensure => directory,
    mode => 700
  }

  file { 'haproxy.cfg':
    content => template('openbidder/load_balancer/haproxy.cfg.erb'),
    ensure => file,
    path => '/etc/haproxy/haproxy.cfg',
    mode => 0644,
    owner => 'root',
    group => 'root',
    require => Package['haproxy'],
  }

  file { 'haproxy':
    ensure => file,
    path => '/etc/default/haproxy',
    source => 'puppet:///modules/openbidder/load_balancer/haproxy',
    mode => 0644,
    owner => 'root',
    group => 'root',
    require => Package['haproxy'],
  }

  service { 'haproxy':
    enable => false,
    hasrestart => true,
    hasstatus => true,
    subscribe => [File['haproxy.cfg']],
    require => [
      File['haproxy'],
      File[$chroot_dir]
    ],
  }
}
