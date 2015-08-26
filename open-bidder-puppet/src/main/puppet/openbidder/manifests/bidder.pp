# Copyright (c) 2012 Google.
#
# Class: openbidder:bidder
#
# This class sets a host up as an Open Bidder bidder.
#
class openbidder::bidder($jvm_options, $main_options, $bidder_home) {
  case $operatingsystem {
    centos, redhat: {
      $curl = '/bin/curl'
      case $operatingsystemmajrelease {
        7: {
          $jdk = 'java-1.8.0-openjdk-headless'
        }
        default: {
          $jdk = 'java-1.7.0-openjdk'
        }
      }
    }
    debian: {
      $curl = '/usr/bin/curl'
      $jdk = 'openjdk-7-jre-headless'
    }
    ubuntu: {
      $curl = '/usr/bin/curl'
      case $operatingsystemrelease {
        '15.04': {
          $jdk = 'openjdk-8-jre-headless'
        }
        default: {
          $jdk = 'openjdk-7-jre-headless'
        }
      }
    }
    opensuse: {
      $curl = '/usr/bin/curl'
      $jdk = 'java-1_8_0-openjdk'
    }
    sles: {
      $curl = '/usr/bin/curl'
      $jdk = 'java-1_7_0-openjdk'
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

  # Service control script.
  file { 'open-bidder-init':
    ensure => file,
    path => '/etc/init.d/open-bidder',
    source => 'puppet:///modules/openbidder/bidder/open-bidder',
    mode => 0755,
    owner => 'root',
    group => 'root',
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

  # Service.
  service {'open-bidder':
    enable => false,
    hasrestart => true,
    hasstatus => true,
    subscribe => [
      File['open-bidder-init'],
      File['open-bidder-default']
    ],
    require => [
      Package['jdk'],
      Exec['google-fluentd']
    ],
  }
}
