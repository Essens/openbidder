<%@ page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ob" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:useBean id="isAdmin" scope="request" type="java.lang.Boolean" />
<jsp:useBean id="userEmail" scope="request" type="java.lang.String" />
<jsp:useBean id="xsrfToken" scope="request" type="java.lang.String" />

<!doctype html>
<html xmlns:ng="http://angularjs.org" lang="en" ng-app="openBidder" ng-controller="PageTitleController">
<head>
  <link rel="icon" type="/image/ico" href="/img/favicon.ico" />
  <meta charset="utf-8">
  <title ng-bind="PageTitle.getTitle()"></title>
  <link rel="stylesheet" media="screen" href="/app/css/app.css">
  <link rel="stylesheet" media="screen" href="/css/bootstrap.css">
  <link rel="stylesheet" media="screen" href="/css/open-bidder.css">
</head>
<body>
  <nav-bar></nav-bar>
  <notices></notices>
  <div class="container">
    <div class="content" ng-view>
    </div>
  </div>

  <ob:footer />

  <script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
  <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.3.0/angular.min.js"></script>
  <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.3.0/angular-route.min.js"></script>
  <script src="/js/underscore-min.js"></script>
  <script src="/js/bootstrap-datepicker.js"></script>
  <script type="text/javascript" src="/_ah/channel/jsapi"></script>
  <script src="/app/js/app.js"></script>
  <script src="/app/js/util.js"></script>
  <script src="/app/js/services.js"></script>
  <script src="/app/js/controllers.js"></script>
  <script src="/app/js/filters.js"></script>
  <script src="/app/js/directives.js"></script>
  <script src="/js/bootstrap.js"></script>
  <script type="text/javascript">
    angular.module('openBidder.constants', [])
        .value('UserEmail', '<c:out value="${userEmail}" />')
        .value('XsrfToken', '<c:out value="${xsrfToken}" />');
  </script>
</body>
</html>
