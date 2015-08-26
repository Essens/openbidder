'use strict';

// Declare app level module which depends on filters, and services
var openBidderApp = angular.module('openBidder', [
  'ngRoute',
  'openBidder.filters',
  'openBidder.services',
  'openBidder.directives',
  'openBidder.controllers',
  'openBidder.constants']);

openBidderApp.config(['$httpProvider', function($httpProvider) {
  $httpProvider.interceptors.push('errorHandlingInterceptor');
}]);

openBidderApp.config(['$routeProvider', function($routeProvider) {
  $routeProvider.when('/', {
    templateUrl: '/app/views/index.html',
    controller: IndexController
  });
  $routeProvider.when('/project', {
    templateUrl: '/app/views/newProject.html',
    controller: NewProjectController
  });
  $routeProvider.when('/project/:projectId', {
    templateUrl: '/app/views/existingProject.html',
    controller: ProjectDetailController
  });
  $routeProvider.when('/project/:projectId/delete', {
    templateUrl: '/app/views/deleteProject.html',
    controller: ProjectDeleteController
  });
  $routeProvider.when('/project/:projectId/network', {
    templateUrl: '/app/views/network.html',
    controller: NetworkController
  });
  $routeProvider.when('/project/:projectId/zone', {
    templateUrl: '/app/views/zoneSummary.html',
    controller: ZoneSummaryController
  });
  $routeProvider.when('/project/:projectId/zone/:zoneId', {
    templateUrl: '/app/views/zoneDetails.html',
    controller: ZoneDetailsController
  });
  $routeProvider.when('/project/:projectId/zone/:zoneId/instance/:instanceId', {
    templateUrl: '/app/views/instanceDetails.html',
    controller: InstanceDetailsController
  });
  $routeProvider.when('/project/:projectId/report', {
    templateUrl: '/app/views/reports.html',
    controller: ReportController
  });
  $routeProvider.when('/project/:projectId/user', {
    templateUrl: '/app/views/users.html',
    controller: UserController
  });
  $routeProvider.when('/project/:projectId/preferredDeals', {
    templateUrl: '/app/views/preferredDeals.html',
    controller: PreferredDealsController
  });
  $routeProvider.when('/project/:projectId/remarketing', {
    templateUrl: '/app/views/remarketing.html',
    controller: RemarketingController
  });
  $routeProvider.when('/project/:projectId/zone/:zoneId/bidderProfile', {
    templateUrl: '/app/views/bidderProfile.html',
    controller: BidderProfileController
  });
}]);
