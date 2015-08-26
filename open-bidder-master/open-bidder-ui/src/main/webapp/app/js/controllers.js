/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

/* Controllers */

var openBidderControllers = angular.module('openBidder.controllers', []);

/**
 * Landing page.
 * @constructor
 */
function IndexController() {
}

/**
 * Create a new project. Show help messages if the user currently has no
 * projects.
 * @constructor
 */
function NewProjectController($scope, $location, Project, HttpErrorHandler) {
  $scope.project = new Project();
  $scope.isNewProject = true;

  Project.list().then(function(projects) {
    $scope.showStartMessage = !projects.length;
  });

  $scope.createProject = function() {
    $scope.project.create().then(function(project) {
      $location.path('/project/' + encodeURIComponent(project.resourceName));
    }, function(response) {
      var data = response.data || {};
      $scope.errors = data.fieldMessages;
      HttpErrorHandler.handle(response);
    });
  };
}
NewProjectController.$inject = ['$scope', '$location', 'Project', 'HttpErrorHandler'];

/**
 * Delete an existing project.
 * @constructor
 */
function ProjectDeleteController($scope, $routeParams, Project, HttpErrorHandler, NoticeQueue) {

  $scope.projectCheckComplete = false;
  var updateProject = function() {
    Project.get($routeParams.projectId).then(function(project) {
      $scope.projectCheckComplete = true;
      $scope.project = project;
    }, function() {
      $scope.projectCheckComplete = true;
    });
  };

  updateProject();

  $scope.$on('projects', updateProject);

  $scope.deleteProject = function() {
    NoticeQueue.success("Deleting project");
    $scope.project.delete().then(function() {
      NoticeQueue.success("Project successfully deleted.");
    }, function(error) {
      if (error.status == HttpStatus.BAD_REQUEST) {
        NoticeQueue.err("Error deleting project. Please remove all running bidders "
          + "and load balancers and try again.");
      } else {
        HttpErrorHandler.handle(error);
      }
    });
  };
}
ProjectDeleteController.$inject = ['$scope', '$routeParams', 'Project', 'HttpErrorHandler', 'NoticeQueue'];

/**
 * Edit an existing project.
 * @constructor
 */
function ProjectDetailController($scope, $routeParams, Project, HttpErrorHandler, NoticeQueue) {
  var setProject = function(project) {
    $scope.project = cloneObject(project);
    project.getAccounts().then(function(accounts) {
      $scope.accounts = accounts;
    });
  };

  var updateProject = function(method, opt_successMessage) {
    return function() {
      if (!method || !_.isObject($scope.project) || !_.isFunction($scope.project[method])) {
        return;
      }
      $scope.errors = null;
      $scope.project[method]().then(function(project) {
        if (opt_successMessage) {
          NoticeQueue.success(opt_successMessage);
        }
      }, function(response) {
        var data = response.data || {};
        $scope.errors = data.fieldMessages;
        HttpErrorHandler.handle(response);
      });
    };
  };

  $scope.updateProjectConfig = updateProject('updateProjectConfig',
      'Project configuration successfully updated');
  $scope.updateDoubleClickConfig = updateProject('updateDoubleClickConfig',
      'DoubleClick configuration successfully updated');
  $scope.updateBidderConfig = updateProject('updateBidderConfig',
      'Bidder configuration successfully updated');
  $scope.updateLoadBalancerConfig = updateProject('updateLoadBalancerConfig',
      'Load balancer configuration successfully updated');
  $scope.updateInterceptorConfig = updateProject('updateInterceptorConfig',
      'Interceptor configuration successfully updated');

  $scope.routeParams = $routeParams;
  $scope.isNewProject = false;

  var updateActiveProject = function() {
    $scope.projectNotFound = false;
    Project.get($routeParams.projectId).then(setProject, function() {
      $scope.projectNotFound = true;
    });
  };

  $scope.revokeTokens = function() {
    $scope.project.revokeAuthorization().then(function(updatedProject) {
      $scope.project.authorized = false;
      NoticeQueue.success('OAuth tokens successfully revoked');
    }, HttpErrorHandler.handle);
  };

  var updateCookieMatch = function() {
    if (!$scope.project) {
      return;
    }
    if (!$scope.project.adExchangeBuyerAccount) {
      $scope.project.cookieMatchUrl = null;
      $scope.project.cookieMatchNid = null;
    } else {
      $scope.project.getAccounts().then(function(accounts) {
        var matchingAccount = _.find(accounts, function(account) {
          return account.id == $scope.project.adExchangeBuyerAccount;
        });
        $scope.accounts = accounts;
        $scope.project.cookieMatchUrl = matchingAccount && matchingAccount.cookieMatchUrl;
        $scope.project.cookieMatchNid = matchingAccount && matchingAccount.cookieMatchNid;
      });
    }
  };

  $scope.$watch('project.adExchangeBuyerAccount', updateCookieMatch);

  $scope.$on('$routeChangeSuccess', updateActiveProject);
}
ProjectDetailController.$inject = ['$scope', '$routeParams', 'Project', 'HttpErrorHandler', 'NoticeQueue'];

function NetworkController($scope, $routeParams, $http, Project, Network, Firewall, HttpErrorHandler, NoticeQueue) {
  $scope.routeParams = $routeParams;

  var updateActiveProject = function() {
    $scope.projectNotfound = false;
    $scope.whiteListedIpRanges = '';
    Project.get($routeParams.projectId).then(function(project) {
      $scope.project = project;
      $scope.whiteListedIpRanges = ($scope.project.whiteListedIpRanges || []).join('\n');
      project.getNetworks().then(function(networks) {
        $scope.networks = networks;
      });
      project.getFirewalls().then(function(firewalls) {
        $scope.firewalls = firewalls;
      });
    }, function(data) {
      $scope.projectNotFound = true;
      if (data.status != HttpStatus.NOT_FOUND) {
        HttpErrorHandler.handle(data);
      }
    });
  };

  updateActiveProject();

  var update = function(array, resource) {
    var replaced = false;
    var result = _.map(array, function(item) {
      if (item.id == resource.id) {
        replaced = true;
        return resource;
      }
      return item;
    });
    if (!replaced) {
      result.push(resource);
    }
    return result;
  };

  var remove = function(array, id) {
    return _.filter(array, function(item) {
      return item.id != id;
    });
  };

  var addNetwork = function(message) {
    var network = new Network(message);
    if ($scope.networks) {
      $scope.networks = update($scope.networks, network);
    }
  };

  var addFirewall = function(message) {
    var firewall = new Firewall(message);
    if ($scope.firewalls) {
      $scope.firewalls = update($scope.firewalls, firewall);
    }
  };

  var removeNetwork = function(networkId) {
    if ($scope.networks) {
      $scope.networks = remove($scope.networks, networkId);
    }
  };

  var removeFirewall = function(firewallId) {
    if ($scope.firewalls) {
      $scope.firewalls = remove($scope.firewalls, firewallId);
    }
  };

  $scope.$on('projects', updateActiveProject);

  $scope.$on('channel:message', function(unused_event, message) {
    $scope.$apply(function() {
      if (message.topic == 'NETWORK') {
        addNetwork(message.message);
      } else if (message.topic == 'FIREWALL') {
        addFirewall(message.message);
      } else if (message.topic == 'NETWORK_DELETE') {
        removeNetwork(message.message);
      } else if (message.topic == 'FIREWALL_DELETE') {
        removeFirewall(message.message);
      }
    });
  });

  $scope.getRemoteIp = function() {
    delete $http.defaults.headers.common['X-Requested-With'];
    $http.get('http://jsonip.com').success(function(response) {
      $scope.remoteIpAddress = response.ip;
    });
  };

  $scope.configureNetwork = function() {
    if ($scope.project && !$scope.project.networkConfigInProgress) {
      var ipRanges = _.filter(($scope.whiteListedIpRanges || '').split(/\s+/), function(item) {
        return item;
      });
      NoticeQueue.success("Configuring network and firewalls");
      $scope.project.configureNetwork({
            whiteListedIpRanges: ipRanges,
            loadBalancerRequestPort: $scope.project.loadBalancerRequestPort,
            loadBalancerStatPort: $scope.project.loadBalancerStatPort,
            bidderRequestPort: $scope.project.bidderRequestPort,
            bidderAdminPort: $scope.project.bidderAdminPort
          }).then(function() {
        NoticeQueue.clearNotices();
        NoticeQueue.success('Network successfully created');
      }, function(error) {
        NoticeQueue.clearNotices();
        if (error.status == HttpStatus.CONFLICT) {
          NoticeQueue.err('Google Compute Engine quota exceeded for networks. '
            + 'Please increase your quota or delete an existing network.');
        } else {
          HttpErrorHandler.handle(error);
        }
      });
    }
  };
}
NetworkController.$inject = [
  '$scope', '$routeParams', '$http', 'Project', 'Network', 'Firewall', 'HttpErrorHandler', 'NoticeQueue'
];

/**
 * Controller for the bidder management page. Gathers the quota and zone
 * information to be displayed.
 * @constructor
 */
function ZoneSummaryController($scope, $routeParams, Project, HttpErrorHandler, NoticeQueue) {
  $scope.routeParams = $routeParams;

  var ignoreUnauthorized = function(error) {
    if (error.status != HttpStatus.UNAUTHORIZED) {
      HttpErrorHandler.handle(error);
    }
  };

  var updateActiveProject = function() {
    $scope.project = null;
    $scope.quotas = [];
    $scope.regions = [];
    $scope.zones = [];
    $scope.projectNotFound = false;
    Project.get($routeParams.projectId).then(function(project) {
      $scope.project = project;
      project.getRegions().then(function(regions) {
        $scope.regions = regions;
      }, ignoreUnauthorized);
      project.getQuotas().then(function(quotas) {
        $scope.quotas = quotas;
      }, ignoreUnauthorized);
      project.getZones({summary: true}).then(function(zones) {
        $scope.zones = zones;
      }, ignoreUnauthorized);
    }, function() {
      $scope.projectNotFound = true;
    });
  };

  updateActiveProject();

  var ONE_WEEK = 7 * 24 * 3600 * 1000;

  $scope.hasDowntimeSoonOrInMaintenance = function(zone) {
    var now = new Date().getTime();
    return _.any(zone.scheduledOutages, function(outage) {
      return outage.beginTime >= now && outage.beginTime - now < ONE_WEEK
             || outage.beginTime <= now && outage.endTime > now;
    });
  };

  $scope.isDown = function(zone) {
    return zone.status == "DOWN";
  };

  $scope.$on('projects', updateActiveProject);
}
ZoneSummaryController.$inject = [
  '$scope', '$routeParams', 'Project', 'HttpErrorHandler', 'NoticeQueue'
];

/**
 * Controller for the zone instance summary page. Queries for the instance
 * information and provides functions for adding instances.
 * @constructor
 */
function ZoneDetailsController(
    $scope, $routeParams, Project, HttpErrorHandler, NoticeQueue) {
  $scope.InstanceType = InstanceType;
  $scope.routeParams = $routeParams;

  var ignoreUnauthorized = function(error) {
    if (error.status != HttpStatus.UNAUTHORIZED) {
      HttpErrorHandler.handle(error);
    }
  };

  var projectNotFound = function(error) {
    if (error.status == HttpStatus.NOT_FOUND) {
      $scope.projectNotFound = true;
    } else {
      ignoreUnauthorized(error);
    }
  };

  var zoneNotFound = function(error) {
    if (error.status == HttpStatus.NOT_FOUND) {
      $scope.zoneNotFound = true;
    } else {
      ignoreUnauthorized(error);
    }
  };

  var updateActiveProject = function() {
    $scope.projectNotFound = false;
    $scope.zoneNotFound = false;
    $scope.zoneFound = false;
    $scope.isZoneRegistered = false;
    $scope.balancers = null;
    $scope.bidders = null;
    $scope.zone = null;
    $scope.machineTypes = null;
    $scope.defaultBidderMachineType = null;
    $scope.defaultLoadBalancerMachineType = null;
    $scope.images = null;
    Project.get($routeParams.projectId).then(function(project) {
      $scope.project = project;
      project.getZone($routeParams.zoneId, {instances: true}).then(function(zone) {
        if (zone) {
          $scope.zoneFound = true;
          $scope.balancers = _.filter(zone.instances, function(instance) {
            return instance.instanceType == 'balancer';
          });
          $scope.bidders = _.filter(zone.instances, function(instance) {
            return instance.instanceType == 'bidder';
          });
        }
        $scope.zone = zone;
      }, zoneNotFound);
      project.getMachineTypes($routeParams.zoneId).then(function(machineTypes) {
        $scope.machineTypes = machineTypes;
      });
      project.getImages().then(function(images) {
        $scope.images = images;
      });
      if (project.bidderMachineTypes && _.has(project.bidderMachineTypes, $routeParams.zoneId)) {
        $scope.defaultBidderMachineType = project.bidderMachineTypes[$routeParams.zoneId];
      }
      if (project.loadBalancerMachineTypes
          && _.has(project.loadBalancerMachineTypes, $routeParams.zoneId)) {
        $scope.defaultLoadBalancerMachineType = project.loadBalancerMachineTypes[$routeParams.zoneId];
      }
    }, projectNotFound);
  };

  updateActiveProject();

  $scope.update = function() {
    if ($scope.zoneFound && !$scope.zone.isRegistered) {
      NoticeQueue.success('Updating zone host name');
      $scope.zone.update().then(function() {
        NoticeQueue.clearNotices();
        NoticeQueue.success('Zone host name has been updated. Please restart all bidders.');
      }, function(error) {
        if (error.status == 409) {
          NoticeQueue.err("The host name has already been used by another zone.");
        }
      });
    }
  };

  var registerErrorHandling = function(error) {
    if (error.status == HttpStatus.PRECONDITION_FAILED) {
      NoticeQueue.err("Empty Ad Exchange Buyer account id");
    } else if (error.status == HttpStatus.BAD_REQUEST) {
      NoticeQueue.err("Missing parameters for zone registration or unregistration");
    } else if (error.status == HttpStatus.NOT_FOUND) {
      NoticeQueue.err("Invalid Ad Exchange Buyer account id");
    } else if (error.status == HttpStatus.FORBIDDEN) {
      NoticeQueue.err("No write access to the Ad Exchange Buyer account "
          + "or Ad Exchange Buyer API access has not been activated.");
    } else {
      ignoreUnauthorized(error);
    }
  };

  $scope.registerZone = function() {
    if ($scope.zoneFound && !$scope.zone.isRegistered) {
      NoticeQueue.success("Registering the zone");
      $scope.zone.registerZone($routeParams.projectId, $routeParams.zoneId).then(function() {
        $scope.zone.isRegistered = true;
        NoticeQueue.clearNotices();
        NoticeQueue.success('Successfully registered the zone');
      }, registerErrorHandling);
    }
  };

  $scope.unregisterZone = function() {
    if ($scope.zoneFound && $scope.zone.isRegistered) {
      NoticeQueue.success("Unregistering the zone");
      $scope.zone.unregisterZone($routeParams.projectId, $routeParams.zoneId).then(function() {
        $scope.zone.isRegistered = false;
        $scope.zone.maxBidRequestQps = null;
        NoticeQueue.clearNotices();
        NoticeQueue.success('Successfully unregistered the zone');
      }, registerErrorHandling);
    }
  };

  $scope.$on('projects', updateActiveProject);
}
ZoneDetailsController.$inject = [
  '$scope', '$routeParams', 'Project', 'HttpErrorHandler', 'NoticeQueue'
];

/**
 * Controller for the bidder profile page.
 */
function BidderProfileController($scope, $routeParams, Project, MachineType, Image, NoticeQueue, HttpErrorHandler) {
  MachineType.list($routeParams.projectId, $routeParams.zoneId).then(function(machineTypes) {
    $scope.machineTypes = machineTypes;
  });
  Image.list($routeParams.projectId).then(function(images) {
    $scope.images = images;
  });
  $scope.zone = '/projects/' + encodeUriSegment($routeParams.projectId)
    + '/zones/' + encodeUriSegment($routeParams.zoneId);

  var projectNotFound = function(error) {
    if (error.status == HttpStatus.NOT_FOUND) {
      $scope.projectNotFound = true;
    } else {
      ignoreUnauthorized(error);
    }
  };

  $scope.createCustomBidder = function() {
    if ($scope.machineTypeModel && $scope.imageModel) {
      Project.get($routeParams.projectId).then(function(project) {
        $scope.project = project;
        $scope.project.createInstance($routeParams.zoneId, {
          instanceType: 'bidder',
          machineType: $scope.machineTypeModel,
          image: $scope.imageModel,
          zone: $scope.zone,
          customBidderResource: {mainParameters: $scope.mainParameters}
        });
      }, function() {
        $scope.projectNotFound = true;
      }).then(undefined, function(error) {
        if (error.status == HttpStatus.NOT_FOUND) {
          NoticeQueue.err("Error creating instances.");
        } else {
          HttpErrorHandler.handle(error);
        }
      });
    }
  };

  $scope.goBack = function() {
    window.history.back();
  }
}
BidderProfileController.$inject = ['$scope', '$routeParams', 'Project', 'MachineType', 'Image', 'NoticeQueue', 'HttpErrorHandler'];

/**
 * Controller for the instance details page.
 */
function InstanceDetailsController($scope, $routeParams, Project, HttpErrorHandler) {
  $scope.InstanceType = InstanceType;
  $scope.routeParams = $routeParams;

  var ignoreUnauthorized = function(data) {
    if (data.status != HttpStatus.UNAUTHORIZED) {
      HttpErrorHandler.handle(data);
    }
  };

  var updateActiveProject = function() {
    $scope.projectNotFound = false;
    $scope.instanceNotFound = false;
    Project.get($routeParams.projectId).then(function(project) {
      $scope.project = project;
      project.getInstance($routeParams.zoneId, $routeParams.instanceId).then(function(instance) {
        $scope.instance = instance;
      }, function(error) {
        if (error.status == HttpStatus.NOT_FOUND) {
          $scope.instanceNotFound = true;
        } else {
          ignoreUnauthorized(error);
        }
      });
    }, function(error) {
      if (error.status == HttpStatus.NOT_FOUND) {
        $scope.projectNotFound = true;
      } else {
        ignoreUnauthorized(error);
      }
    });
  };

  updateActiveProject();

  $scope.$on('projects', updateActiveProject);
}
InstanceDetailsController.$inject = ['$scope', '$routeParams', 'Project', 'HttpErrorHandler'];

/**
 * Controller for the report page.
 */
function ReportController($scope, $routeParams, Project, Report, HttpErrorHandler, NoticeQueue) {
  $scope.routeParams = $routeParams;
  $scope.isBucketSet = false;

  Project.get($routeParams.projectId).then(function(project) {
    $scope.project = project;
    updateIsBucketSet();
  }, HttpErrorHandler.handle);

  $scope.updateDoubleClickReportingBucket = function() {
    $scope.project.updateReporting().then(function() {
      NoticeQueue.success('Cloud storage bucket been updated.');
      updateIsBucketSet();
    }, function(response) {
      var data = response.data || {};
      $scope.errors = data.fieldMessages;
      HttpErrorHandler.handle(response);
    });
  };

  var updateIsBucketSet = function() {
    $scope.isBucketSet = $scope.project
      && $scope.project.doubleClickProjectResource 
      && $scope.project.doubleClickProjectResource.doubleClickReportingBucket != '';
  };

  // set a default date for datepicker.
  var setDefaultDate = function(opt_date) {
    opt_date = _.isUndefined(opt_date) ? new Date() : opt_date;
    $scope.reportDate = convertToFormattedDateStr(opt_date);
  };

  setDefaultDate();

  var ignoreUnauthorized = function(data) {
    if (data.status != HttpStatus.UNAUTHORIZED) {
      HttpErrorHandler.handle(data);
    }
  };

  $scope.getRtbReport = function() {
    Project.get($routeParams.projectId).then(function(project) {
      $scope.project = project;
      project.getReports({reportDate: $scope.reportDate}).then(function(reports) {
        $scope.perfReports = _.filter(reports, function(report) {
          return report.reportType == 'perfReport';
        });
        $scope.snippetReports = _.filter(reports, function(report) {
          return report.reportType == 'snippetReport';
        });
      }, ignoreUnauthorized);
    });
    $scope.showReportTable = true;
  };
}
ReportController.$inject = [
  '$scope', '$routeParams', 'Project', 'Report', 'HttpErrorHandler', 'NoticeQueue'];

/**
 * Controller for managing what users are in a project.
 * @constructor
 */
function UserController($scope, $routeParams, HttpErrorHandler, Project, NoticeQueue, UserEmail) {
  $scope.routeParams = $routeParams;
  Project.get($routeParams.projectId).then(function(project) {
    $scope.project = project;
  });

  $scope.projectRoles = [
    {name: 'owner', value: 'Owner'},
    {name: 'read_write', value: 'Read/Write'},
    {name: 'read', value: 'Read Only'}
  ];

  $scope.UserEmail = UserEmail;

  var loadUsers = function() {
    $scope.projectNotFound = false;
    Project.get($routeParams.projectId).then(function(project) {
      project.getUsers().then(function(users) {
        $scope.users = users;
      }, function() {
        $scope.projectNotFound = true;
      });
    });
  };

  loadUsers();

  $scope.getRoleName = function(roleName) {
    var role = _.find($scope.projectRoles, function(projectRole) {
      return projectRole.name == roleName;
    });
    return role ? role.value : 'Unknown';
  };

  $scope.addUser = function() {
    if (!$scope.newUserRole) {
      return;
    }
    var email = $scope.newUserEmail;
    var role = $scope.newUserRole;
    Project.get($routeParams.projectId).then(function(project) {
      project.addUser(email, role).then(function() {
        NoticeQueue.success("User " + $scope.newUserEmail + " successfully added to project");
        $scope.newUserEmail = '';
        loadUsers();
      }, HttpErrorHandler.handle);
    });
  };

  $scope.updateUser = function(user) {
    user.update().then(function() {
      NoticeQueue.success("User " + user.userEmail + " updated");
    }, HttpErrorHandler.handle);
  };

  $scope.removeUser = function(user) {
    user.remove($routeParams.projectId).then(function() {
      NoticeQueue.success("User " + user.userEmail + " successfully removed");
      loadUsers();
    }, HttpErrorHandler.handle);
  };
}
UserController.$inject = ['$scope', '$routeParams', 'HttpErrorHandler', 'Project', 'NoticeQueue', 'UserEmail'];

/**
 * Controller for the Audience page.
 * @constructor
 */
function AudienceController() {
}
AudienceController.$inject = [];

/**
 * Controller for the Flash Verification page.
 * @constructor
 */
function FlashVerificationController() {
}
FlashVerificationController.$inject = [];

/**
 * Controller for the Preferred Deals page.
 * @constructor
 */
function PreferredDealsController($scope, $routeParams, Project, HttpErrorHandler, NoticeQueue) {
  $scope.routeParams = $routeParams;
  $scope.isBucketSet = false;

  Project.get($routeParams.projectId).then(function(project) {
    $scope.project = project;
    updateIsBucketSet();
  }, HttpErrorHandler.handle);

  $scope.updateDoubleClickPreferredDeals = function() {
    $scope.project.updateDoubleClickPreferredDeals().then(function() {
      NoticeQueue.success('Preferred deals settings have been updated.');
      updateIsBucketSet();
    }, function(response) {
      var data = response.data || {};
      $scope.errors = data.fieldMessages;
      HttpErrorHandler.handle(response);
    });
  };

  var updateIsBucketSet = function() {
    $scope.isBucketSet = $scope.project && $scope.project.doubleClickPreferredDealsBucket != '';
  };

  $scope.uploadPreferredDeals = function() {
    NoticeQueue.success('Uploading preferred deals');
    Project.get($routeParams.projectId).then(function(project) {
      project.uploadPreferredDeals().then(function() {
        NoticeQueue.success('Preferred deals successfully being uploaded.');
      }, function(response) {
        HttpErrorHandler.handle(response);
      });
    });
  }
}
PreferredDealsController.$inject = ['$scope', '$routeParams', 'Project', 'HttpErrorHandler', 'NoticeQueue'];

/**
 * Responsible for setting the page title.
 */
function PageTitleController($scope, PageTitle) {
  $scope.PageTitle = PageTitle;
}
PageTitleController.$inject = ['$scope', 'PageTitle'];
openBidderControllers.controller('PageTitleController', PageTitleController);

/**
 * Controller for the Remarketing page.
 */
function RemarketingController($scope, $routeParams, Project, Remarketing, HttpErrorHandler, NoticeQueue) {
  $scope.routeParams = $routeParams;
  Project.get($routeParams.projectId).then(function(project) {
    $scope.project = project;
    project.getRemarketingActions().then(function(actions) {
      $scope.actions = actions;
    });
    project.getAccounts().then(function(accounts) {
      var matchingAccount = _.find(accounts, function(account) {
        return account.id == $scope.project.adExchangeBuyerAccount;
      });
      $scope.project.cookieMatchUrl = matchingAccount && matchingAccount.cookieMatchUrl;
      $scope.project.cookieMatchNid = matchingAccount && matchingAccount.cookieMatchNid;
    });
  });

  $scope.getState = function(isEnabled) {
    return isEnabled ? "Enabled" : "Disabled";
  }

  $scope.addRemarketingAction = function() {
    $scope.project.addRemarketingAction({
      actionId: $scope.actionId,
      isEnabled: !!$scope.isEnabled,
      description: $scope.description,
      maxCpm: $scope.maxCpm,
      clickThroughUrl: $scope.clickThroughUrl,
      creative: $scope.creative
    }).then(function() {
      NoticeQueue.success("Action successfully added");
    }, function(response) {
      var data = response.data || {};
      $scope.errors = data.fieldMessages;
      HttpErrorHandler.handle(response);
    });
  };

  $scope.deleteRemarketingAction = function() {
    _.each($scope.actions, function(action) {
      if (action.selected) {
        $scope.project.deleteRemarketingAction(action.actionId).then(function() {
          NoticeQueue.success("Action " + action.actionId + " successfully deleted");
        }, HttpErrorHandler.handle);
      }
    });
  };

  $scope.updateRemarketingAction = function(toState) {
    _.each($scope.actions, function(action) {
      if (action.selected) {
        $scope.project.updateRemarketingAction({
          actionId: action.actionId,
          isEnabled: toState,
          description: action.description,
          maxCpm: action.maxCpm,
          clickThroughUrl: action.clickThroughUrl,
          creative: action.creative
        }).then(function() {
          NoticeQueue.success("Action " + action.actionId + " successfully changed state to " +
              $scope.getState(toState).toLowerCase());
        }, HttpErrorHandler.handle);
      }
    });
  };

  var ignoreUnauthorized = function(data) {
    if (data.status != HttpStatus.UNAUTHORIZED) {
      HttpErrorHandler.handle(data);
    }
  };
}
RemarketingController.$inject = [ '$scope', '$routeParams', 'Project', 'Remarketing', 'HttpErrorHandler', 'NoticeQueue'];
