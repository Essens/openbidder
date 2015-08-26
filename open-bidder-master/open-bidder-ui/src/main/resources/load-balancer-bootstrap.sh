#!/usr/bin/env bash
# Copyright 2012 Google Inc. All Rights Reserved.
#
# Load balancer Google Compute Engine bootstrap script.
#
# This script is run on load balancer instances and requires the various
# instance configuration created by the UI.

echo;
echo "============================================================"
echo "Load Balancer bootstrapping..."
echo "============================================================"
echo;

set -e
PATH=$PATH:/usr/local/bin:/sbin

echo;
echo "OS / Package Manager-specific bootstrapping"
echo "------------------------------------------------------------"
echo;

if [[ -f /etc/redhat-release ]]; then

  yum makecache
  if ! rpm -qa | grep epel-release > /dev/null 2>&1; then
    if [[ `cat /etc/centos-release | cut -d" " -f4 | cut -d "." -f1` == 7 ]] ||
       [[ `cat /etc/redhat-release | cut -d" " -f7 | cut -d "." -f1` == 7 ]]; then
      rpm -qa | grep -qw epel-release \
        || yum install -y https://dl.fedoraproject.org/pub/epel/7/x86_64/e/epel-release-7-2.noarch.rpm
      rpm -qa | grep -qw puppetlabs-release \
        || yum install -y https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-10.noarch.rpm \
        && yum install -y puppet
    else
      rpm -qa | grep -qw epel-release \
        || yum install -y https://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm
      rpm -qa | grep -qw puppetlabs-release \
        || yum install -y https://yum.puppetlabs.com/el/6.5/products/x86_64/puppetlabs-release-6-10.noarch.rpm \
        && yum install -y puppet
    fi
  fi

  CHKCONFIG='chkconfig'

elif [[ -f /etc/debian_version ]]; then

  if [[ `lsb_release -cs` == wheezy && ! -f /etc/apt/sources.list.d/backports.list ]]; then

    cat <<EOF > /etc/apt/sources.list.d/backports.list
# deb     http://gce_debian_mirror.storage.googleapis.com/ wheezy-backports main
# deb-src http://gce_debian_mirror.storage.googleapis.com/ wheezy-backports main
deb     http://http.debian.net/debian wheezy-backports main
deb-src http://http.debian.net/debian wheezy-backports main
EOF
    cat <<EOF > /etc/apt/preferences.d/updates.pref
Package: *
Pin: release a=wheezy/updates
Pin-Priority: 1000
EOF
    cat <<EOF > /etc/apt/preferences.d/stable.pref
Package: *
Pin: release a=stable
Pin-Priority: 995
EOF
    cat <<EOF > /etc/apt/preferences.d/backports.pref
Package: *
Pin: release a=backports
Pin-Priority: 750
EOF

  fi

  apt-get -q update || true
  if ! type chkconfig > /dev/null 2>&1; then apt-get -yq install chkconfig; fi
  if ! type puppet    > /dev/null 2>&1; then apt-get -yq install puppet;    fi
  # Workaround for http://bugs.debian.org/cgi-bin/bugreport.cgi?bug=620392
  if ! type /usr/share/sendmail/sendmail > /dev/null 2>&1; then apt-get -yq install sendmail-base; fi

  CHKCONFIG='chkconfig -c'

elif [[ -f /etc/SuSE-release ]]; then

  zypper update || true
  CHKCONFIG='chkconfig -c'

else
  echo "Unsupported system image!"
  exit 1
fi

echo;
echo "Linux kernel tuning"
echo "------------------------------------------------------------"
echo;

sysctl -w net.core.rmem_max=16777216
sysctl -w net.core.wmem_max=16777216
sysctl -w net.ipv4.tcp_rmem="4096 87380 16777216"
sysctl -w net.ipv4.tcp_wmem="4096 16384 16777216"
sysctl -w net.ipv4.ip_local_port_range="1024 65535"
sysctl -w net.ipv4.tcp_tw_recycle=1
sysctl -w net.ipv4.tcp_max_syn_backlog=10240
sysctl -w fs.file-max=100000
if [[ -f /proc/sys/net/netfilter/nf_conntrack_max ]]; then
  sysctl -e -w net.netfilter.nf_conntrack_max=262144
fi

if [[ 0 == $($CHKCONFIG iptables; echo $?) ]]; then
  service iptables stop
fi
if [[ 0 == $($CHKCONFIG ip6tables; echo $?) ]]; then
  service ip6tables stop
fi

# -----------------------------------------------------------------------
#
# Utilities

create_dir () {
  if [[ ! -d $1 ]]; then
    echo "Creating directory: $1";
    mkdir -p $1
  fi
}

META=http://metadata/0.1/meta-data
META_ATTRS=$META/attributes

read_attr_req () {
  result=$(curl -f $META_ATTRS/$1 2> /dev/null)
  if [[ ! $? == 0 ]]; then
    echo "ERROR: Expected $1 metadata key to exist." >&2
    exit 1
  fi
  echo $result
}

read_attr_opt () {
  result=$(curl -f $META_ATTRS/$1 2> /dev/null)
  if [[ ! $? == 0 ]]; then
    echo ""
  else
    echo $result
  fi
}

gs_copy () {
  destdir=$1
  filename=$2
  srcbucket=$3
  prevmd5=$4
  if [[ ! 0 == $(gsutil ls $srcbucket/$filename > /dev/null 2>&1 ; echo $?) ]]; then
    echo "ERROR: GCS resource not found or not accessible: $srcbucket/$filename" >&2
    exit 1
  elif [[ $prevmd5 == `gsutil stat $srcbucket/$filename | grep md5 | cut -f 4` ]]; then
    echo "GCS resource was not modified: $srcbucket/$filename" >&2
    return 0
  elif [[ 0 == $(gsutil cp $srcbucket/$filename $TMP_DIR/new-$filename ; echo $?) \
        && ( ! -f $destdir/$filename \
             || ! "$(md5sum $TMP_DIR/new-$filename)" == "$(md5sum $destdir/$filename)" ) ]]; then
    mv $TMP_DIR/new-$filename $destdir/$filename
    return 1
  else
    echo "ERROR: Cannot download GCS resource: $srcbucket/$filename"
    exit 1
  fi
}

echo;
echo "Configuring environment"
echo "------------------------------------------------------------"
echo;

BIDDER_TAG=bidder
OB_HOME=/usr/local/open-bidder

bidder_request_port=$(read_attr_req 'bidder_request_port')
haproxy_stat_port=$(  read_attr_req 'haproxy_stat_port')
request_port=$(       read_attr_req 'request_port')
user_dist_uri=$(      read_attr_opt 'user_dist_uri')
version=$(            read_attr_req 'project_version')

ZONE=`curl $META/zone 2> /dev/null`
ZONE=${ZONE##*/}
NETWORK=`curl $META/network 2> /dev/null`

if [[ $user_dist_uri == */ ]]; then
  user_dist_uri=${user_dist_uri:0:-1}
fi

# Directories

TMP_DIR=$(mktemp -d)
create_dir $OB_HOME
echo "Using temp directory: $TMP_DIR";
mkdir -p $OB_HOME/modules

echo;
echo "Fetching Open Bidder distribution"
echo "------------------------------------------------------------"
echo;

dist_bin=open-bidder-puppet-$version-balancer.tar.gz
if [[ -f /var/run/$dist_bin ]]; then
  prevmd5=`md5sum -b /var/run/$dist_bin | cut -c -32`
else
  prevmd5=none
fi
if [[ 1 == $(gs_copy /var/run $dist_bin $user_dist_uri $prevmd5 ; echo $?) ]]; then
  echo "Installing distribution tarball: $dist_bin"
  rm -rf $OB_HOME/modules/*
  tar xvfz /var/run/$dist_bin --no-same-owner --strip-components=1 -C $OB_HOME/modules
else
  echo "Distribution tarball not updated"
fi

echo;
echo "Analyzing zone configuration"
echo "------------------------------------------------------------"
echo;

gcloud compute instances list --zone $ZONE --format json > $TMP_DIR/instances.json
gcloud compute machine-types list --zone $ZONE --format json > $TMP_DIR/machine_types.json

cat <<EOF > $TMP_DIR/find_bidders.py
import json
import logging
import sys

logging.basicConfig(level=logging.INFO)

def denormalize_resource(zone):
  return zone.split("/")[-1]

def network_name(network_interface):
  for interface in network_interface:
    if "network" in interface:
      return interface["network"]

json_file = sys.argv[1]
target_zone = denormalize_resource(sys.argv[2])
instance_network = json.loads(sys.argv[3])
machine_types_file = sys.argv[4]
required_tags = set(sys.argv[5:])

machine_types_info = {}
with open(machine_types_file) as fd:
  machine_types = json.load(fd)
  for machine_type in machine_types:
    machine_types_info[machine_type["name"]] = machine_type

target_network = denormalize_resource(network_name(instance_network["networkInterface"]))

logging.info("target zone=%s, required tags=%s", target_zone, required_tags)

with open(json_file) as fd:
  instances = json.load(fd)
  bidders = []
  for instance in instances:
    if target_zone == denormalize_resource(instance["zone"]) and \
       target_network == denormalize_resource(network_name(instance["networkInterfaces"])) and \
        "tags" in instance and "items" in instance["tags"] and \
        set(instance["tags"]["items"]).issuperset(required_tags):
      machine_type = machine_types_info[denormalize_resource(instance["machineType"])]
      bidders.append({
        'ip': instance["networkInterfaces"][0]["networkIP"],
        'cpus': machine_type["guestCpus"]
      })
  bidders_json = json.dumps(bidders)
  print bidders_json.replace(':', '=>').replace('"', '\'')
EOF

python $TMP_DIR/find_bidders.py $TMP_DIR/instances.json \
       $ZONE $NETWORK $TMP_DIR/machine_types.json $BIDDER_TAG \
       | sort > $TMP_DIR/bidders

bidders=$(cat $TMP_DIR/bidders)

echo;
echo "Applying Puppet modules"
echo "------------------------------------------------------------"
echo;

echo "Puppet Puppet parameters:"
echo "bidders=$bidders"
echo "bidder_request_port=$bidder_request_port"
echo "request_port=$request_port"
echo "stats_port=$haproxy_stat_port"
puppet apply --verbose --color=false --modulepath=$OB_HOME/modules <<EOF
class { 'openbidder::load_balancer': \
  bidders => $bidders, \
  bidder_request_port => $bidder_request_port, \
  request_port => $request_port, \
  stats_port => $haproxy_stat_port, \
}
EOF

echo;
echo "Finishing configuration"
echo "------------------------------------------------------------"
echo;

cat <<EOF > $TMP_DIR/open-bidder
*/5 * * * * root bash /var/run/google.startup.script >> /var/log/open-bidder-refresh.log 2>&1
EOF

if ! diff $TMP_DIR/open-bidder /etc/cron.d/open-bidder > /dev/null; then
  mv $TMP_DIR/open-bidder /etc/cron.d/
fi

echo "Removing temporary directory: $TMP_DIR"
rm -rf $TMP_DIR

echo;
echo "============================================================"
echo "Load Balancer bootstrapped successfully!"
echo "============================================================"
echo;
