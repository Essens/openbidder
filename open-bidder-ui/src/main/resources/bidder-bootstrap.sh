#!/usr/bin/env bash
# Copyright 2012 Google Inc. All Rights Reserved.
#
# Bidder Google Compute Engine bootstrap script.
#
# This script is run on bidder instances and requires the various
# instance configuration created by the UI.

echo;
echo "============================================================"
echo "Bidder bootstrapping..."
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
    if [[ -f /etc/centos-release ]]; then
      rpm -qa | grep -qw epel-release || yum install -y epel-release
    else
      rpm -qa | grep -qw epel-release \
        || yum install -y https://dl.fedoraproject.org/pub/epel/7/x86_64/e/epel-release-7-5.noarch.rpm
    fi
    rpm -qa | grep -qw puppetlabs-release \
      || yum install -y https://yum.puppetlabs.com/el/7/products/x86_64/puppetlabs-release-7-11.noarch.rpm \
      && yum install -y puppet
  fi

  CHKCONFIG='chkconfig'

elif [[ -f /etc/debian_version ]]; then

  if [[ `lsb_release -si` == Ubuntu ]]; then
    apt-get -q update || true
    if ! type sysv-rc-conf > /dev/null 2>&1; then apt-get -yq install sysv-rc-conf; fi
    if ! type puppet       > /dev/null 2>&1; then apt-get -yq install puppet;       fi
    CHKCONFIG='sysv_chkconfig'
  else
    apt-get -q update || true
    if ! type chkconfig > /dev/null 2>&1; then apt-get -yq install chkconfig; fi
    if ! type puppet    > /dev/null 2>&1; then apt-get -yq install puppet;    fi
    CHKCONFIG='chkconfig -c'
  fi

elif [[ -f /etc/SuSE-release ]]; then

  zypper update || true
  if ! type puppet > /dev/null 2>&1; then zypper -nq install puppet; fi
  CHKCONFIG='chkconfig -c'

else
  echo "Unsupported system image!"
  exit 1
fi

# -----------------------------------------------------------------------
#
# Utilities

sysv_chkconfig () {
  [[ `sysv-rc-conf --list $1 | wc -l` -eq 0 ]] && return 1 || return 0
}

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

addopt () {
  value=$($1 $2)
  if [[ -z $value ]]; then
    echo ""
  else
    echo ""--$2"="$value" "
  fi
}

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

echo;
echo "Configuring environment"
echo "------------------------------------------------------------"
echo;

OB_HOME=/usr/local/open-bidder

user_dist_uri=$(read_attr_opt 'user_dist_uri')
version=$(      read_attr_req 'project_version')
jvm_options=$(  read_attr_req 'jvm_parameters')

if [[ $user_dist_uri == */ ]]; then
  user_dist_uri=${user_dist_uri:0:-1}
fi

main_options=\
$(addopt read_attr_req 'platform')\
$(addopt read_attr_req 'listen_port')\
$(addopt read_attr_req 'admin_port')\
$(addopt read_attr_req 'api_project_id')\
$(addopt read_attr_req 'api_project_number')\
$(addopt read_attr_opt 'load_balancer_host')\
$(addopt read_attr_opt 'impression_url')\
$(addopt read_attr_opt 'click_url')\
$(addopt read_attr_opt 'bid_interceptors')\
$(addopt read_attr_opt 'impression_interceptors')\
$(addopt read_attr_opt 'click_interceptors')\
$(addopt read_attr_opt 'doubleclick_encryption_key')\
$(addopt read_attr_opt 'doubleclick_integrity_key')\
$(addopt read_attr_opt 'doubleclick_match_interceptors')\
$(addopt read_attr_opt 'doubleclick_match_nid')\
$(addopt read_attr_opt 'doubleclick_match_url')\

main_options="$main_options $(read_attr_req 'main_parameters')"

# Directories

TMP_DIR=$(mktemp -d)
echo "Using temp directory: $TMP_DIR";
create_dir $OB_HOME

echo;
echo "Fetching Open Bidder distribution"
echo "------------------------------------------------------------"
echo;

dist_bin=open-bidder-binary-$version-bin.tar.gz
if [[ -f /var/run/$dist_bin ]]; then
  prevmd5=`md5sum -b /var/run/$dist_bin | cut -c -32`
else
  prevmd5=none
fi
if [[ 1 == $(gs_copy /var/run $dist_bin $user_dist_uri $prevmd5 ; echo $?) ]]; then
  echo "Installing distribution tarball: $dist_bin"
  rm -rf $OB_HOME/*
  tar xvfz /var/run/$dist_bin --no-same-owner -C $OB_HOME
else
  echo "Distribution tarball not updated"
fi

echo;
echo "Applying Puppet modules"
echo "------------------------------------------------------------"
echo;

echo "Bidder Puppet parameters"
echo "jvm_options: $jvm_options"
echo "main_options: $main_options"
puppet apply --verbose --color=false --modulepath=$OB_HOME/modules <<EOF
class { 'openbidder::bidder': \
  jvm_options => '$jvm_options', \
  main_options => '$main_options', \
  bidder_home => '$OB_HOME', \
}
EOF

echo;
echo "Finishing configuration"
echo "------------------------------------------------------------"
echo;

echo "Removing temporary directory: $TMP_DIR"
rm -rf $TMP_DIR

if [[ -f $OB_HOME/bin/ext-startup ]]; then
    echo;
    echo "Starting Extended bootstrapping script..."
    $OB_HOME/bin/ext-startup
fi

echo;
echo "Starting bidder"
echo "------------------------------------------------------------"
echo;

service open-bidder start

echo;
echo "============================================================"
echo "Bidder bootstrapped successfully!"
echo "============================================================"
echo;
