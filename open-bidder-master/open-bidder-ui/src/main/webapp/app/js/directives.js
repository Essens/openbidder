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

/* Directives */

var openBidderDirectives = angular.module('openBidder.directives', []);

/*
 * Instantiate a top-level Bootstrap style navbar with highlighting links and a project drop down.
 */
function NavBarController($scope, $routeParams, $location, $q, NoticeQueue, Project, Channel, UserEmail) {
  var updateProjectInfo = function() {
    Project.list().then(function(projectList) {
      $scope.projectsLoaded = true;
      var activeProject = _.find(projectList, function(project) {
        return project.resourceName == $routeParams.projectId;
      });
      $scope.activeProject = activeProject;
      $scope.activeProjectId = activeProject ? activeProject.resourceName : null;
      $scope.inactiveProjects = _.filter(projectList, function(project) {
        return $scope.activeProjectId != project.resourceName;
      });
    }, function(error) {
      $scope.projectsLoaded = true;
      $scope.activeProject = undefined;
      $scope.activeProjectId = undefined;
      $scope.inactiveProjects = undefined;
    });
  };

  updateProjectInfo();

  $scope.UserEmail = UserEmail;
  $scope.projectsLoaded = false;

  $scope.$on('projects', updateProjectInfo);

  $scope.$on('$routeChangeSuccess', function() {
    // update the full absolute path being shown (required for login/logout continue URLs)
    $scope.path = absolutePath();

    // also activate the relevant tab
    var parts = $location.path().split('/');
    $scope.activeTab = parts.length < 4 ? '' : parts[3];

    updateProjectInfo();

    if ($scope.activeProject) {
      Channel.switchProject($scope.activeProject.id);
    }
  });

  $scope.revokeTokens = function() {
    if ($scope.activeProject) {
      $scope.activeProject.revokeAuthorization().then(function() {
        NoticeQueue.success('OAuth tokens successfully revoked');
      });
    }
  };

  Channel.connect();
}
NavBarController.$inject = ['$scope', '$routeParams', '$location', '$q', 'NoticeQueue', 'Project', 'Channel', 'UserEmail'];

/**
 * Create the fixed top-level nav bar.
 */
openBidderDirectives.directive('navBar', function() {
  return {
    restrict: 'E',
    replace: true,
    scope: {},
    controller: NavBarController,
    templateUrl: '/app/templates/navbar.html'
  };
});

/**
 * Page heading directive in Bootstrap style.
 */
openBidderDirectives.directive('pageHeader', function() {
  return {
    restrict: 'E',
    replace: true,
    scope: {
      title: '@'
    },
    controller: ['$scope', 'PageTitle', function($scope, PageTitle) {
      $scope.$watch('title', function() {
        PageTitle.setTitle($scope.title);
      });
    }],
    template: '<div class="page-header"><h1>{{ title }}</h1></div>'
  };
});

/**
 * A controller that links notices in the NoticeQueue to the notice directive.
 * @constructor
 */
function NoticeController($scope, NoticeQueue) {
  $scope.NoticeQueue = NoticeQueue;

  $scope.$watch('NoticeQueue.getNotices()', function(newValue) {
    $scope.notices = newValue;
  });

  var clearNotices = function() {
    NoticeQueue.clearNotices();
  };

  // clear notices on page transitions
  $scope.$on('$routeChangeSuccess', clearNotices);
}
NoticeController.$inject = ['$scope', 'NoticeQueue'];

/**
 * A directive for displaying notices from the NoticeQueue service.
 */
openBidderDirectives.directive('notices', function() {
  return {
    restrict: 'E',
    replace: true,
    scope: true,
    templateUrl: '/app/templates/notices.html',
    controller: NoticeController
  };
});

/**
 * Defines the size of a custom popover that has a title and contains an image based on the
 * Bootstrap popover plugin.
 */
function WhatsThisController($scope, $attrs) {
  $scope.imageHeight = $attrs.height - 0; // coerce to a number
  $scope.imageWidth = $attrs.width - 0;
  $scope.popoverHeight = $scope.imageHeight + 52;
  $scope.popoverWidth = $scope.imageWidth + 28;
  $scope.title = $attrs.title;
  $scope.imageSrc = $attrs.src;
}
WhatsThisController.$inject = ['$scope', '$attrs'];

/**
 * Create a link that when clicked opens an image in a custom-sized popover.
 */
openBidderDirectives.directive('whatsThis', function() {
  return {
    restrict: 'E',
    replace: true,
    transclude: true,
    scope: {},
    controller: WhatsThisController,
    templateUrl: '/app/templates/popover.html',
    link: function($scope, element, attrs) {
      var anchor = $(element).children('a');
      var popover = $(element).children('div');
      var content = $(element).children('img');
      $(anchor).popover({
        title: $scope.title,
        placement: attrs.placement || 'bottom',
        trigger: attrs.trigger,
        template: popover,
        content: content,
        html: true
      });
      $(element).replaceWith(anchor);
    }
  };
});

function InputController($scope, $attrs) {
  $scope.modelName = _.last($attrs.ngModel.split('.'));
}
InputController.$inject = ['$scope', '$attrs'];

/**
 * Create a Bootstrap style text form field with a label.
 */
openBidderDirectives.directive('textInput', function() {
  return {
    restrict: 'E',
    replace: true,
    scope: {
      ngModel: '=',
      errors: '=',
      label: '@',
      readOnly: '=',
      ngDisabled: '='
    },
    transclude: true,
    controller: InputController,
    templateUrl: '/app/templates/textInput.html'
  };
});

/**
 * Create a Bootstrap style text area form field with a label.
 */
openBidderDirectives.directive('textArea', function() {
  return {
    restrict: 'E',
    replace: true,
    scope: {
      ngModel: '=',
      errors: '=',
      rows: '@',
      cols: '@',
      label: '@',
      asArray: '@'
    },
    transclude: true,
    controller: InputController,
    templateUrl: '/app/templates/textArea.html'
  };
});

/**
 * Bootstrap style form field help block.
 */
openBidderDirectives.directive('helpBlock', function() {
  return {
    restrict: 'E',
    replace: true,
    transclude: true,
    template: '<p class="help-block" ng-transclude></p>'
  };
});

/**
 * A directive that allows the user to select dates from a calendar.
 */
openBidderDirectives.directive('datepicker', function() {
  return {
    restrict: 'A',
    require: '?ngModel',
    link: function($scope, element, attrs, ngModelCtrl) {
      ngModelCtrl = ngModelCtrl || {$setViewValue: $.noop};

      element.datepicker({
        format: 'mm/dd/yyyy',
        autoclose: true
      }).on('changeDate', function() {
        $scope.$apply(function () {
          ngModelCtrl.$setViewValue(element.val());
        });
      });
    }
  };
});

/**
 * Project role selector.
 */
openBidderDirectives.directive('selectRole', function() {
  return {
    restrict: 'E',
    replace: true,
    scope: {},
    link: function($scope, element, attrs) {
      $scope.roleNames = _.object(_.map($scope.projectRoles, function(role) {
        return [role.name, role.value];
      }));
    },
    templateUrl: '/app/templates/selectRole.html'
  };
});

/**
 * Toggle buttons.
 */
openBidderDirectives.directive('radioButtons', ['$parse', function($parse) {
  var ITEMS_EXP = /^\s*(.*?)(?:\s*,\s*(.*?))?\s+for\s+([\$\w][\$\w\d]*)\s+in\s+(.*)$/;
  return {
    restrict: 'E',
    require: '?ngModel',
    replace: true,
    transclude: true,
    link: function($scope, element, attrs, ngModelCtrl) {
      var itemsExp = attrs.items || '';
      var match = itemsExp.match(ITEMS_EXP);
      if (!match) {
        throw Error('Expected: (_label_,)? _value_ for _item_ in _items_. Got: ' + itemsExp);
      }
      var displayFn = $parse(match[2] || match[1]);
      var valueFn = $parse(match[1]);
      var itemName = match[3];
      var itemsFn = $parse(match[4]);
      ngModelCtrl = ngModelCtrl || {$setViewValue: $.noop};

      var setState = function(value) {
        $(element).children().each(function() {
          $(this).toggleClass('active', $(this).attr('data-value') == value);
        });
        $scope.$apply(function() {
          ngModelCtrl.$setViewValue(value);
        });
      };

      var handleClick = function(e) {
        var button = $(e.target);

        // this isn't set yet so it's false if active, true if not.
        var active = !button.hasClass('active');
        if (active) {
          setState(button.attr('data-value'));
        }

        // no need to propagate this event
        return false;
      };

      var buttonTemplate = $('<button>')
          .attr('type', 'button')
          .addClass('btn')
          .addClass(attrs.btnClass);
      var render = function() {
        $(element).html('');
        _.forEach(itemsFn($scope), function(item) {
          var locals = {};
          locals[itemName] = item;
          var value = valueFn($scope, locals);
          buttonTemplate.clone()
              .toggleClass('active', ngModelCtrl.$modelValue == value)
              .attr('data-value', value)
              .text(displayFn($scope, locals))
              .on('click', handleClick)
              .appendTo(element);
        });
      };

      ngModelCtrl.$render = render;
      $scope.$watch(attrs.ngModel, render);
    },
    template: '<div ng-transclude></div>'
  };
}]);

/**
 * Text area component supporting ngModel of an array with one item per line.
 */
openBidderDirectives.directive('editList', function() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function($scope, element, attrs, ngModelCtrl) {
      var isArray = false;

      function toArray(text) {
        if (isArray) {
          return text ? text.trim().split(/\s+/) : [];
        } else {
          return text || '';
        }
      }

      function fromArray(array) {
        isArray = _.isArray(array);
        if (isArray) {
          return array ? joinWithSeparator(array, '\n') : '';
        } else {
          return array || '';
        }
      }

      ngModelCtrl.$parsers.push(toArray);
      ngModelCtrl.$formatters.push(fromArray);
    }
  };
});

function ProgressBarController($scope) {
  $scope.metricDisplayNames = {
    'CPUS': 'CPUs',
    'NETWORKS': 'Networks',
    'FIREWALLS': 'Firewalls'
  };
}
ProgressBarController.$inject = ['$scope'];

/**
 * Display a progress bar for the given quota.
 */
openBidderDirectives.directive('progressBar', function() {
  return {
    restrict: 'E',
    replace: true,
    scope: {
      quota: '='
    },
    controller: ProgressBarController,
    templateUrl: '/app/templates/progressBar.html'
  }
});

/**
 * Display a given list of instances.
 */
openBidderDirectives.directive('instanceTable', function() {
  return {
    restrict: 'E',
    replace: true,
    scope: {
      actionDescription: '@',
      project: '=',
      instances: '=',
      instanceType: '=',
      machineTypeModel: '=',
      machineTypes: '=',
      imageModel: '=',
      images: '=',
      zone: '='
    },
    controller: ['$scope', 'Instance', 'NoticeQueue', 'HttpErrorHandler', function($scope, Instance, NoticeQueue, HttpErrorHandler) {
      $scope.createInstance = function() {
        if ($scope.instanceType && $scope.machineTypeModel && $scope.imageModel) {
          $scope.project.createInstance($scope.zone.resourceName, {
            instanceType: $scope.instanceType,
            machineType: $scope.machineTypeModel,
            image: $scope.imageModel,
            zone: $scope.zone.id
          }).then(undefined, function(error) {
            if (error.status == HttpStatus.NOT_FOUND) {
              NoticeQueue.err("Error creating instances.");
            } else {
              HttpErrorHandler.handle(error);
            }
          });
        }
      };

      $scope.isBidder = function() {
        return $scope.instanceType == InstanceType.BIDDER;
      };

      $scope.isBalancer = function() {
        return $scope.instanceType == InstanceType.BALANCER;
      };

      $scope.zoneUnavailable = function() {
        if ($scope.zone != null && $scope.zone.scheduledOutages != null) {
          var now = new Date().getTime();
          return _.any($scope.zone.scheduledOutages, function(outage) {
            return outage.beginTime <= now && outage.endTime > now;
          });
        }
        return false;
      };

      $scope.getZoneName = function() {
        return $scope.zone ? $scope.zone.id.split('/')[4] : null;
      };

      var isSelected = function(instance) {
        return instance.selected;
      };

      var onInstances = function(f) {
        return f($scope.instances || [], isSelected);
      };

      var updateInstance = function(updatedInstance) {
        if (updatedInstance.instanceType != $scope.instanceType
            || updatedInstance.zone != $scope.zone.id) {
          return;
        }
        var replaced = false;
        if (!$scope.instances) {
          $scope.instances = [updatedInstance];
          return;
        }
        $scope.instances = _.map($scope.instances, function(instance) {
          if (instance.id == updatedInstance.id) {
            replaced = true;
            return updatedInstance;
          } else {
            return instance;
          }
        });
        if (!replaced) {
          $scope.instances.push(updatedInstance);
        }
      };

      $scope.anyInstancesSelected = function() {
        return onInstances(_.any);
      };

      $scope.allInstancesSelected = function() {
        return onInstances(_.all);
      };

      $scope.terminateSelectedInstances = function() {
        _.each(onInstances(_.filter), function(instance) {
          instance.delete({zone: $scope.zone.resourceName});
        });
      };

      $scope.toggleAllInstances = function() {
        _.each($scope.instances || [], function(instance) {
          instance.selected = $scope.allSelected;
        });
      };

      $scope.$watch('allInstancesSelected()', function(newValue) {
        $scope.allSelected = newValue;
      });

      $scope.$on('channel:message', function(e, message) {
        if (message.topic == 'INSTANCE') {
          $scope.$apply(function() {
            updateInstance(new Instance(message.message));
          });
        }
      });
    }],
    link: function($scope, element, attrs) {
      element.popover();
    },
    templateUrl: '/app/templates/instanceTable.html'
  };
});

/**
 * Display a given list of reports.
 */
openBidderDirectives.directive('reportTable', function() {
  return {
    restrict: 'E',
    replace: true,
    scope: {
      project: '=',
      reports: '='
    },
    templateUrl: '/app/templates/reportTable.html'
  };
});

/**
 * Trigger a Bootstrap popover.
 */
openBidderDirectives.directive('popover', function() {
  return {
    restrict: 'A',
    link: function($scope, element, attrs) {
      element.popover({
        trigger: attrs.popover
      });
    }
  };
});

/**
 * Warn of impending downtime.
 */
openBidderDirectives.directive('downtimeWarning', function() {
  return {
    restrict: 'E',
    scope: {
      zone: '='
    },
    template: '<i class="icon-warning-sign"></i>',
    link: function($scope, element, attrs) {
      var content;
      if ($scope.zone && $scope.zone.scheduledOutages && $scope.zone.scheduledOutages.length) {
        var now = new Date().getTime();
        var outage = $scope.zone.scheduledOutages[0];
        if (now >= outage.beginTime) {
          content = "This zone is currently under maintenance and scheduled downtime will end at "
                    + new Date(outage.endTime).toGMTString();
        } else {
          content = 'Scheduled downtime will begin at ' + new Date(outage.beginTime).toGMTString();
        }
      }

      $(element).popover({
        title: 'Scheduled Outage',
        content: content || 'Scheduled downtime will begin shortly',
        trigger: 'hover'
      });
    }
  };
});

/**
 * An icon to show if the zone is currently registered.
 */
openBidderDirectives.directive('registeredIcon', function() {
  return {
    restrict: 'E',
    replace: true,
    template: '<i class="icon-ok-sign"></i>',
    link: function($scope, element) {
      $(element).popover({
        content: 'This zone is currently registered with DoubleClick Ad Exchange',
        trigger: 'hover'
      });
    }
  }
});

/**
 * An icon to show if the zone is currently down.
 */
openBidderDirectives.directive('banIcon', function() {
  return {
    restrict: 'E',
    replace: true,
    template: '<i class="icon-ban-circle"></i>',
    link: function($scope, element) {
      $(element).popover({
        content: 'This zone is currently down',
        trigger: 'hover'
      });
    }
  }
});

/**
 * Auto select textarea text on click.
 */
openBidderDirectives.directive('selectOnClick', function() {
  return {
    restrict: 'A',
    link: function($scope, element, attrs) {
      element.click(function() {
        element.select();
      });
    }
  }
});
