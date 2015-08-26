# Copyright (c) 2012 Google.
#
# Class: openbidder:bidder
#
# This class sets a host up as an Open Bidder bidder.

class openbidder::bidder($jvm_options, $main_options, $bidder_home) {

  $real_log_dir = '/var/log/open-bidder'

  case $operatingsystem {
    centos, redhat: {
      case $operatingsystemmajrelease {
        7: {
          $jdk = 'java-1.7.0-openjdk-headless'
        }
        default: {
          $jdk = 'java-1.7.0-openjdk'
        }
      }
    }
    debian: {
      $jdk = 'openjdk-7-jre-headless'
    }
    sles: {
      $jdk = 'java-1_7_0-ibm'
    }
    default: {
      fail("Unsupported system image!")
    }
  }

  # Java runtime.
  package { 'jdk':
    name => $jdk,
    ensure => installed,
  }

  # Startup environment.
  file { 'open-bidder-default':
    content => template('openbidder/bidder/open-bidder.erb'),
    ensure => file,
    path => '/etc/default/open-bidder',
    mode => 0644,
    owner => 'root',
    group => 'root',
  }

  # Ensure the log directory parameter is created, owned and symlinked to open bidder.
  file { "$real_log_dir":
    ensure => directory,
    owner => 'root',
    group => 'root',
    mode => '0755',
  }

  # Symlink to the log directory.
  file { 'open-bidder-logs':
    ensure => link,
    path => "$bidder_home/logs",
    target => "$real_log_dir",
    mode => '0755',
    force => true,
    require => File["$real_log_dir"],
  }

  # Service control script.
  file { 'open-bidder-init':
    ensure => file,
    path => '/etc/init.d/open-bidder',
    source => 'puppet:///modules/openbidder/bidder/open-bidder',
    mode => 0755,
    owner => 'root',
    group => 'root',
  }

  # Service.
  service {'open-bidder':
    enable => false,
    hasrestart => true,
    hasstatus => true,

    require => [Package['jdk']],

    subscribe => [File['open-bidder-init'],
                  File['open-bidder-default'],
                  File['open-bidder-logs']],
  }
}
