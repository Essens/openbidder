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

/* Services */

var openBidderServices = angular.module('openBidder.services', []);

/**
 * Provides some application functionality on top of the REST API for projects. This includes:
 * <ul>
 *   <li>event notifications for project data and default project changes;</li>
 *   <li>a central place to query the current state of project data; and</li>
 *   <li>facilities for setting default projects.</li>
 * </ul>
 */
openBidderServices.factory('Project', [ '$http', '$q', '$location', '$rootScope', 'User', 'Quota', 'Region', 'Network', 'Firewall', 'Zone', 'Instance', 'MachineType', 'Image', 'Report', 'Account', 'Remarketing', function($http, $q, $location, $rootScope, User, Quota, Region, Network, Firewall, Zone, Instance, MachineType, Image, Report, Account, Remarketing) {
  var projectConfigKeys = [
    'description',
    'oauth2ClientSecret',
    'oauth2ClientId'
  ];

  var projectDoubleClickKeys = [
    'adExchangeBuyerAccount',
    'doubleClickProjectResource',
    'cookieMatchNid',
    'cookieMatchUrl'
  ];

  var projectLoadBalancerKeys = [
    'loadBalancerOauth2Scopes'
  ];

  var projectBidderKeys = [
    'userDistUri',
    'vmParameters',
    'mainParameters',
    'bidderOauth2Scopes'
  ];

  var projectInterceptorKeys = [
    'bidInterceptors',
    'impressionInterceptors',
    'clickInterceptors',
    'matchInterceptors'
  ];

  var projectReportingKeys = ['doubleClickProjectResource'];

  var projectPreferredDealsKeys = [
    'doubleClickPreferredDealsBucket',
    'auctionType'
  ];

  var Project = function(data) {
    angular.extend(this, data);
  };

  var toProject = function(project) {
    return new Project(project);
  };

  var listDeferred = $q.defer();

  var notifyProjectChanges = function() {
    listDeferred.promise.then(function(projects) {
      $rootScope.$broadcast('projects', projects);
    });
  };

  var addProject = function(newProject) {
    listDeferred.promise.then(function(projects) {
      projects.push(newProject);
      _.each(projects, function(project) {
        project.defaultProject = project.resourceName == newProject.resourceName;
      });
      listDeferred = $q.defer();
      listDeferred.resolve(projects);
      notifyProjectChanges();
    });
    return newProject;
  };

  var updateProject = function(updatedProject, opt_fields) {
    listDeferred.promise.then(function(projects) {
      projects = _.map(projects, function(project) {
        if (project.resourceName == updatedProject.resourceName) {
          if (_.isArray(opt_fields)) {
            _.each(opt_fields, function(field) {
              project[field] = updatedProject[field];
            });
          } else {
            return updatedProject;
          }
        }
        return project;
      });
      listDeferred = $q.defer();
      listDeferred.resolve(projects);
      notifyProjectChanges();
    });
    return updatedProject;
  };

  var removeProject = function(removedProject) {
    listDeferred.promise.then(function(projects) {
      projects = _.filter(projects, function(project) {
        return project.resourceName != removedProject.resourceName;
      });
      listDeferred = $q.defer();
      listDeferred.resolve(projects);
      notifyProjectChanges();
    });
    return removedProject;
  };

  var changeDefault = function(defaultProject) {
    listDeferred.promise.then(function(projects) {
      _.each(projects, function(project) {
        project.defaultProject = project.resourceName == defaultProject.resourceName;
      });
      listDeferred = $q.defer();
      listDeferred.resolve(projects);
      notifyProjectChanges();
    });
    return defaultProject;
  };

  Project.reload = function() {
    var listPromise = $http.get('/projects').then(function(response) {
      return _.map(response.data, toProject);
    });
    listPromise.then(function(projects) {
      listDeferred.resolve(projects);
    }, function(error) {
      listDeferred.reject(error);
    });
    return listDeferred.promise;
  };

  Project.reload();

  Project.list = function() {
    return listDeferred.promise;
  };

  Project.get = function(projectId) {
    var getDeferred = $q.defer();
    listDeferred.promise.then(function(projects) {
      var project = _.find(projects, function(project) {
        return project.resourceName == projectId;
      });
      if (project) {
        getDeferred.resolve(project);
      } else {
        getDeferred.reject({status: HttpStatus.NOT_FOUND});
      }
    });
    return getDeferred.promise;
  };

  Project.prototype.create = function() {
    return $http.post('/projects', this).then(function(response) {
      var project = toProject(response.data);
      addProject(project);
      return project;
    });
  };

  Project.prototype.update = function() {
    return this.partialUpdate_();
  };

  Project.prototype.updateReporting = function() {
    return this.partialUpdate_(projectReportingKeys);
  };

  Project.prototype.updateProjectConfig = function() {
    return this.partialUpdate_(projectConfigKeys);
  };

  Project.prototype.updateDoubleClickConfig = function() {
    return this.partialUpdate_(projectDoubleClickKeys);
  };

  Project.prototype.updateLoadBalancerConfig = function() {
    return this.partialUpdate_(projectLoadBalancerKeys);
  };

  Project.prototype.updateBidderConfig = function() {
    return this.partialUpdate_(projectBidderKeys);
  };

  Project.prototype.updateInterceptorConfig = function() {
    return this.partialUpdate_(projectInterceptorKeys);
  };

  Project.prototype.updateDoubleClickPreferredDeals = function() {
    return this.partialUpdate_(projectPreferredDealsKeys);
  };

  Project.prototype.setAsDefault = function() {
    return $http.post('/projects/' + encodeUriSegment(this.resourceName) + '/defaultProject')
        .then(function(response) {
          return changeDefault((toProject(response.data)));
        });
  };

  Project.prototype.authorize = function() {
    if (this.canAuthorize()) {
      window.location.href = '/oauth2?projectId=' + encodeUriSegment(this.resourceName) +
        '&redirectTo=' + encodeUriSegment(absolutePath());
    }
  };

  Project.prototype.delete = function() {
    var self = this;
    return $http.delete('/projects/' + encodeUriSegment(this.resourceName)).then(function() {
      removeProject(self);
    });
  };

  Project.prototype.revokeAuthorization = function() {
    var self = this;
    return $http.delete('/projects/' + encodeUriSegment(this.resourceName) + '/authorized')
        .then(function(response) {
          self.authorized = false;
          return updateProject(toProject(response.data));
        });
  };

  Project.prototype.canAuthorize = function() {
    return this.apiProjectId && this.oauth2ClientId && this.oauth2ClientSecret;
  };

  Project.prototype.isOwner = function() {
    return this.projectRole == 'owner';
  };

  Project.prototype.isWritable = function() {
    return this.projectRole == 'owner' || this.projectRole == 'read_write';
  };

  Project.prototype.getUsers = function(opt_params) {
    return this.bind_(User.list, opt_params);
  };

  Project.prototype.getQuotas = function(opt_params) {
    return this.ifNetworkConfigured_(Quota.list, [], opt_params);
  };

  Project.prototype.getRegions = function(opt_params) {
    return this.ifNetworkConfigured_(Region.list, [], opt_params);
  };

  Project.prototype.getNetworks = function(opt_params) {
    return this.ifNetworkConfigured_(Network.list, [], opt_params);
  };

  Project.prototype.configureNetwork = function(opt_params) {
    if (this.networkConfigInProgress) {
      return $q.when();
    }
    var self = this;
    self.networkConfigInProgress = true;
    return this.bind_(Network.create, opt_params).then(function(network) {
      self.network = network.id;
      self.networkConfigInProgress = false;
      return network;
    }, function(error) {
      self.networkConfigInProgress = false;
      notifyProjectChanges();
      return $q.reject(error);
    });
  };

  Project.prototype.getFirewalls = function(opt_params) {
    return this.ifNetworkConfigured_(Firewall.list, [], opt_params);
  };

  Project.prototype.getZone = function(zoneId, opt_params) {
    return this.ifNetworkConfigured_(Zone.get, null, zoneId, opt_params);
  };

  Project.prototype.getZones = function(opt_params) {
    return this.ifNetworkConfigured_(Zone.list, [], opt_params);
  };

  Project.prototype.addUser = function(email, projectRole) {
    return this.bind_(User.create, email, projectRole);
  };

  Project.prototype.getReports = function(opt_params) {
    return this.bind_(Report.list, opt_params);
  };

  Project.prototype.uploadPreferredDeals = function(opt_params) {
    var uri = '/projects/' + encodeUriSegment(this.resourceName) + '/preferredDeals/upload';
    return $http.post(uri, {
      params: opt_params
    });
  }

  Project.prototype.getAccounts = function(opt_params) {
    return this.bind_(Account.list, opt_params);
  };

  Project.prototype.getRemarketingActions = function(opt_params) {
    return this.bind_(Remarketing.listActions, opt_params);
  };

  Project.prototype.addRemarketingAction = function(params) {
    return this.bind_(Remarketing.addAction, params);
  }

  Project.prototype.deleteRemarketingAction = function(actionId) {
    return this.bind_(Remarketing.deleteAction, actionId);
  }

  Project.prototype.updateRemarketingAction = function(params) {
    return this.bind_(Remarketing.updateAction, params);
  }

  Project.prototype.getReportLink = function(bucketName, reportName) {
    if (bucketName == '' || reportName == '') {
      return null;
    }
    return 'https://storage.cloud.google.com/' + encodeUriSegment(bucketName)
        + '/' + encodeUriSegment(reportName);
  };

  Project.prototype.getInstance = function(zoneId, instanceId, opt_params) {
    return this.ifNetworkConfigured_(Instance.get, null, zoneId, instanceId, opt_params);
  };

  Project.prototype.getInstances = function(zoneId, opt_params) {
    return this.ifNetworkConfigured_(Instance.list, [], zoneId, opt_params);
  };

  Project.prototype.createInstance = function(zoneId, opt_params) {
    return this.ifNetworkConfigured_(Instance.create, null, zoneId, opt_params);
  };

  Project.prototype.getMachineTypes = function(zoneId, opt_params) {
    return this.ifNetworkConfigured_(MachineType.list, [], zoneId, opt_params);
  };

  Project.prototype.getImages = function(opt_params) {
    return this.ifNetworkConfigured_(Image.list, [], opt_params);
  };

  Project.prototype.bindSubResources_ = function(subResources) {
    var self = this;
    _.each(subResources, function(resource) {
      resource.project = self;
    });
    return subResources;
  };

  Project.prototype.bindSubResource_ = function(subResource) {
    subResource.project = this;
    return subResource;
  };

  Project.prototype.bind_ = function(f) {
    var args = Array.prototype.slice.call(arguments, 1);
    args.unshift(this.resourceName);
    var self = this;
    return f.apply(null, args).then(function(response) {
      if (_.isArray(response)) {
        return self.bindSubResources_.call(self, response);
      } else if (_.isObject(response)) {
        return self.bindSubResource_.call(self, response);
      } else {
        return response;
      }
    });
  };

  Project.prototype.ifNetworkConfigured_ = function(f, opt_defaultValue) {
    if (this.network) {
      var args = Array.prototype.slice.call(arguments, 2);
      args.unshift(f);
      return this.bind_.apply(this, args);
    } else {
      var deferred = $q.defer();
      if (_.isUndefined(opt_defaultValue)) {
        deferred.reject();
      } else {
        deferred.resolve(opt_defaultValue);
      }
      return deferred.promise;
    }
  };

  Project.prototype.partialUpdate_ = function(opt_fields) {
    var request = opt_fields ? _.pick(this, opt_fields) : this;
    return $http.put('/projects/' + encodeUriSegment(this.resourceName), request)
      .then(function(response) {
          return updateProject(toProject(response.data));
      })
  };

  return Project;
}]);

/**
 * Create and list project networks.
 */
openBidderServices.factory('Network', ['$http', function($http) {
  var Network = function(data) {
    angular.extend(this, data);
  };

  var toNetwork = function(network) {
    return new Network(network);
  };

  Network.list = function(projectId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/networks', {
      params: opt_params
    }).then(function(response) {
      return _.map(response.data, toNetwork);
    });
  };

  Network.create = function(projectId, params) {
    return $http.post('/projects/' + encodeUriSegment(projectId) + '/networks', params
    ).then(function(response) {
      return toNetwork(response.data);
    });
  };

  return Network;
}]);

/**
 * List project firewalls.
 */
openBidderServices.factory('Firewall', ['$http', function($http) {
  var Firewall = function(data) {
    angular.extend(this, data);
  };

  var toFirewall = function(firewall) {
    return new Firewall(firewall);
  };

  Firewall.list = function(projectId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/firewalls', {
      params: opt_params
    }).then(function(response) {
      return _.map(response.data, toFirewall);
    });
  };

  return Firewall;
}]);

/**
 * A service to view and manage the users assigned to a project.
 */
openBidderServices.factory('User', ['$http', function($http) {
  var User = function(data) {
    angular.extend(this, data);
  };

  var toUser = function(user) {
    return new User(user);
  };

  User.list = function(projectId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/users', {
      params: opt_params
    }).then(function(response) {
      return _.map(response.data, toUser);
    });
  };

  User.create = function(projectId, email, projectRole) {
    return $http.post('/projects/' + encodeUriSegment(projectId) + '/users', {
      userEmail: email,
      projectRole: projectRole
    }).then(function(response) {
      return toUser(response.data);
    });
  };

  User.prototype.update = function() {
    return $http.put(this.id, this).then(function(response) {
      return toUser(response.data);
    });
  };

  User.prototype.remove = function() {
    return $http.delete(this.id);
  };

  return User;
}]);

/**
 * Compute Engine project quotas.
 */
openBidderServices.factory('Quota', ['$http', function($http) {
  var Quota = function(data) {
    angular.extend(this, data);
  };

  var toQuota = function(quota) {
    return new Quota(quota);
  };

  Quota.list = function(projectId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/quotas', {
      params: opt_params
    }).then(function(response) {
      return _.map(response.data, toQuota);
    });
  };

  return Quota;
}]);

/**
 * REST resource for Regions.
 */
openBidderServices.factory('Region', ['$http', function($http) {
  var Region = function(data) {
    angular.extend(this,data);
  }

  var toRegion = function(region) {
    return new Region(region);
  }

  Region.list = function(projectId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/regions', {
      params: opt_params
    }).then(function(response) {
      return _.map(response.data, toRegion);
    });
  }

  return Region;
}]);

/**
 * REST resource for Zones.
 */
openBidderServices.factory('Zone', ['$http', 'Instance', function($http, Instance) {
  var Zone = function(data) {
    angular.extend(this, data);
  };

  var toZone = function(zoneData) {
    var zone = new Zone(zoneData);
    if (_.isArray(zone.instances)) {
      zone.instances = _.map(zone.instances, function(instance) {
        return new Instance(instance);
      });
    }
    return zone;
  };

  Zone.get = function(projectId, zoneId, opt_params) {
    var uri = '/projects/' + encodeUriSegment(projectId) + '/zones/' + encodeUriSegment(zoneId);
    return $http.get(uri, {
      params: opt_params
    }).then(function(response) {
      return toZone(response.data);
    });
  };

  Zone.list = function(projectId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/zones', {
      params: opt_params || {}
    }).then(function(response) {
      return _.map(response.data, toZone);
    });
  };

  Zone.prototype.update = function() {
    return $http.put(this.id, {
      hostName: this.hostName
    });
  };

  Zone.prototype.registerZone = function(projectId, zoneId) {
    var uri = '/projects/' + encodeUriSegment(projectId) + '/zones/' + encodeUriSegment(zoneId) + '/register';
    return $http.post(uri, {
      id: this.id,
      hostName: this.hostName,
      maxBidRequestQps: this.maxBidRequestQps
    });
  };

  Zone.prototype.unregisterZone = function(projectId, zoneId) {
    var uri = '/projects/' + encodeUriSegment(projectId) +'/zones/' + encodeUriSegment(zoneId) + '/register';
    return $http.delete(uri).then(function(response) {
      return toZone(response.data);
    });
  };

  return Zone;
}]);

/**
 * Possible values of the instanceType field on an Instance.
 * @enum {string}
 */
var InstanceType = {
  BALANCER: 'balancer',
  BIDDER: 'bidder'
};

/**
 * Service for machine instances in a project.
 */
openBidderServices.factory('Instance', ['$http', function($http) {
  var Instance = function(data) {
    angular.extend(this, data);
  };

  var toInstance = function(instanceData) {
    return new Instance(instanceData);
  };

  var splitIfNotNull = function(value, sep, number) {
    return value ? value.split(sep)[number] : null;
  };

  Instance.get = function(projectId, zoneId, instanceId, opt_params) {
    var uri = '/projects/' + encodeUriSegment(projectId)
      + '/zones/' + encodeUriSegment(zoneId)
      + '/instances/' + encodeUriSegment(instanceId);
    return $http.get(uri, {
      params: opt_params
    }).then(function(response) {
      return toInstance(response.data);
    });
  };

  Instance.list = function(projectId, zoneId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/zones/' + encodeUriSegment(zoneId) + '/instances', {
      params: opt_params
    }).then(function(response) {
      return _.map(response.data, toInstance);
    });
  };

  Instance.create = function(projectId, zoneId, opt_params) {
    opt_params = opt_params || {};
    return $http.post('/projects/' + encodeUriSegment(projectId) + '/zones/' + encodeUriSegment(zoneId) + '/instances', {
      instanceType: opt_params.instanceType,
      machineType: opt_params.machineType,
      image: opt_params.image,
      zone: opt_params.zone,
      customBidderResource: opt_params.customBidderResource
    }).then(function(response) {
      return toInstance(response.data);
    });
  };

  Instance.prototype.delete = function(opt_params) {
    return $http.delete(this.id, {
      params: opt_params
    });
  };

  Instance.prototype.getStatsLink = function() {
    if (this.instanceType == InstanceType.BIDDER) {
      return 'http://' + this.externalIp + ':' + this.adminPort + '/admin';
    } else if (this.instanceType == InstanceType.BALANCER) {
      return 'http://' + this.externalIp + ':' + this.haProxyStatPort + '/stats';
    }
    return null;
  };

  Instance.prototype.getZoneName = function() {
    return splitIfNotNull(this.zone, '/', 4);
  };

  Instance.prototype.getMachineTypeName = function() {
    return splitIfNotNull(this.machineType, '/', 6);
  };

  Instance.prototype.getImageName = function() {
    return splitIfNotNull(this.image, '/', 4);
  };

  Instance.prototype.getNetworkName = function() {
    return splitIfNotNull(this.network, '/', 4);
  };

  return Instance;
}]);

/**
 * Service for machine types in a Google Compute Engine project.
 */
openBidderServices.factory('MachineType', ['$http', function($http) {
  var MachineType = function(data) {
    angular.extend(this, data);
  };

  var toMachineType = function(machineType) {
    return new MachineType(machineType);
  };

  MachineType.list = function(projectId, zoneId, opt_params) {
    var uri = '/projects/' + encodeUriSegment(projectId)
        + '/zones/' + encodeUriSegment(zoneId)
        + '/machinetypes';
    return $http.get(uri, {
      params: opt_params || {}
    }).then(function(response) {
      return _.map(response.data, toMachineType);
    });
  };

  return MachineType;
}]);

/**
 * Service for VM images in a Google Compute Engine project.
 */
openBidderServices.factory('Image', ['$http', '$q', function($http, $q) {
  var Image = function(data) {
    angular.extend(this,data);
  };

  var toImage = function(image) {
    return new Image(image);
  };

  Image.list = function(projectId, opt_params) {
    var defaultImages = $http.get('/projects/' + encodeUriSegment(projectId) + '/defaultimages', {
      params: opt_params || {}
    });
    var customImages = $http.get('/projects/' + encodeUriSegment(projectId) + '/customimages', {
      params: opt_params || {}
    });
    return $q.all([defaultImages, customImages]).then(function(response) {
      var data = response[0].data.concat(response[1].data);
      return _.map(data, toImage);
    });
  };

  return Image;
}]);

/**
 * Types of notices that can be placed in the NoticeQueue.
 * @enum {string}
 */
var NoticeType = {
  DANGER: 'danger',
  DEFAULT: 'default',
  ERROR: 'error',
  INFO: 'info',
  SUCCESS: 'success',
  WARNING: 'warning'
};

/**
 * A service to manage project reports.
 */
openBidderServices.factory('Report', ['$http', function($http) {
  var Report = function(data) {
    angular.extend(this, data);
  };

  var toReport = function(report) {
    return new Report(report);
  };

  Report.list = function(projectId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/reports', {
      params: opt_params
    }).then(function(response) {
      return _.map(response.data, toReport);
    });
  };

  return Report;
}]);

/**
 * Service for managing buyer accounts.
 */
openBidderServices.factory('Account', ['$http', function($http) {
  var Account = function(data) {
    angular.extend(this, data);
  };

  var toAccount = function(account) {
    return new Account(account);
  };

  Account.list = function(projectId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/accounts', {
      params: opt_params
    }).then(function(response) {
      return _.map(response.data, toAccount);
    });
  };

  return Account;
}]);

/**
 * A queue for notices to the user. It prevents duplicate notices from being added.
 */
openBidderServices.factory('NoticeQueue', function() {
  var notices = [];

  var noticeQueue = {};

  noticeQueue.getNotices = function() {
    return notices;
  };

  /**
   * Adds a notice with the given content and type to the queue, preventing
   * duplicates.
   */
  noticeQueue.addNotice = function(opt_options) {
    opt_options = opt_options || {};

    // Create the new notice.
    var newNotice = {
      type: opt_options.type || NoticeType.DEFAULT,
      content: opt_options.content,
      expand: !!opt_options.expand,
      extraContent: opt_options.extraContent
    };

    // Check if there is already an existing equivalent notice.
    var existingNotice = _.any(notices, function(notice) {
      return newNotice.type === notice.type
          && newNotice.content === notice.content;
    });

    // If not a duplicate, add the notice.
    if (!existingNotice) {
      notices.push(newNotice);
    }
  };

  noticeQueue.clearNotices = function() {
    notices = [];
  };

  noticeQueue.err = function(content, opt_stackTrace) {
    noticeQueue.addNotice({
      type: NoticeType.ERROR,
      content: content,
      extraContent: opt_stackTrace
    });
  };

  noticeQueue.success = function(content) {
    noticeQueue.addNotice({
      type: NoticeType.SUCCESS,
      content: content
    });
  };

  return noticeQueue;
});

/**
 * Generic handler for HTTP error codes. It posts notices describing the errors
 * received.
 */
openBidderServices.factory('HttpErrorHandler', ['NoticeQueue', function(NoticeQueue) {
   return {
     handle: function(data) {
       if (_.isUndefined(data.status)) {
         return;
       }

       // TODO(asavage): Link the appropriate action page in the notices.
       switch (data.status) {
         case HttpStatus.BAD_REQUEST:
           NoticeQueue.err('Missing fields or invalid request');
           break;
         case HttpStatus.UNAUTHORIZED:
           NoticeQueue.err('Invalid or missing OAuth 2 access token.');
           break;
         case HttpStatus.FORBIDDEN:
           NoticeQueue.err('Access denied. Are you logged in?');
           break;
         case HttpStatus.NOT_FOUND:
           NoticeQueue.err('Resource not found.');
           break;
         case HttpStatus.NOT_ACCEPTABLE:
           NoticeQueue.err('Invalid XSRF token. Refresh the page.');
           break;
         case HttpStatus.PRECONDITION_FAILED:
           NoticeQueue.err('Your Google API Project ID is incorrect or you do not have access to it');
           break;
         default:
           NoticeQueue.err('Unknown error.');
           break;
       }
     }
   };
}]);

/**
 * Server push notifications.
 */
openBidderServices.factory('Channel', ['$rootScope', '$http', '$routeParams', '$q', 'NoticeQueue', function($rootScope, $http, $routeParams, $q, NoticeQueue) {
  var socket;
  var tokenDeferred = null;

  var Channel = {};

  var onOpen = function() {
    tokenDeferred = null;
    $rootScope.$broadcast('channel:open');
  };

  var onMessage = function(message) {
    $rootScope.$broadcast('channel:message', angular.fromJson(message.data));
  };

  var onClose = function() {
    $rootScope.$broadcast('channel:close');
    connect();
  };

  var onError = function() {
    $rootScope.$broadcast('channel:error');
  };

  var connect = function() {
    if (tokenDeferred) {
      return; // already connecting
    }
    tokenDeferred = $q.defer();
    $http.get('/token/new', {
      params: {
        projectId: $routeParams.projectId
      }
    }).then(function(response ) {
      var token = response.data;
      var channel = new goog.appengine.Channel(token);
      socket = channel.open();
      socket.onopen = onOpen;
      socket.onmessage = onMessage;
      socket.onerror = onError;
      socket.onclose = onClose;
      tokenDeferred.resolve(token);
    }, function() {
      tokenDeferred.reject();
      $rootScope.$apply(function() {
        NoticeQueue.err('Unable to open notification channel. Please refresh.');
      });
    });
  };

  Channel.connect = function() {
    connect();
  };

  Channel.switchProject = function(projectId) {
    if (tokenDeferred) {
      tokenDeferred.promise.then(function(token) {
        $http.post('/token/project', {
          project: projectId,
          token: token
        });
      });
    }
  };

  return Channel;
}]);

/**
 * Service for altering page title.
 */
openBidderServices.factory('PageTitle', function() {
  var pageTitle;

  return {
    getTitle: function() {
      return pageTitle ? pageTitle + ' - Open Bidder' : 'Open Bidder';
    },
    setTitle: function(title) {
      pageTitle = title;
    }
  };
});

/**
 * $http interceptor for handling 500 error responses.
 */
openBidderServices.factory('errorHandlingInterceptor', ['$q', 'NoticeQueue', function($q, NoticeQueue) {
  return {
    response: function(response) {
      return response;
    },
    responseError: function(response) {
      if (response.status == HttpStatus.INTERNAL_SERVER_ERROR
          && response.data && response.data.messages) {
        _.each(response.data.messages, function(message) {
          if (message.messageType == 'ERROR') {
            NoticeQueue.err(message.message, message.stackTrace);
          }
        });
      }
      return $q.reject(response);
    }
  };
}]);

/**
 * A service to manage project remarketing.
 */
openBidderServices.factory('Remarketing', ['$http', function($http) {
  var Remarketing = function(data) {
    angular.extend(this, data);
  };

  var toRemarketing = function(remarketing) {
    return new Remarketing(remarketing);
  };

  Remarketing.listActions = function(projectId, opt_params) {
    return $http.get('/projects/' + encodeUriSegment(projectId) + '/remarketing/actions', {
      params: opt_params
    }).then(function(response) {
      return _.map(response.data, toRemarketing);
    });
  };

  Remarketing.addAction = function(projectId, params) {
    params = params || {};
    var uri = '/projects/' + encodeUriSegment(projectId) + '/remarketing/actions';
    return $http.post(uri, params).then(function(response) {
      return toRemarketing(response);
    });
  };

  Remarketing.deleteAction = function(projectId, actionId) {
    var uri = '/projects/' + encodeUriSegment(projectId) + '/remarketing/actions/' +
        encodeUriSegment(actionId);
    return $http.delete(uri).then(function(response) {
      return toRemarketing(response);
    });
  };

  Remarketing.updateAction = function(projectId, params) {
    params = params || {};
    var uri = '/projects/' + encodeUriSegment(projectId) + '/remarketing/actions/' +
        encodeUriSegment(params.actionId);
    return $http.put(uri, params).then(function(response) {
      return toRemarketing(response);
    });
  };

  return Remarketing;
}]);
