<%@ page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="ob" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<ob:errorPage title="Access Denied">
  <ob:content>
    <p>Username not found. You must be either an administrator of this app or added to an
      existing project. To make yourself an admin marked yourself as a developer or admin of this
      app in the <a href="http://appengine.google.com">App Engine Admin Console</a>.</p>
  </ob:content>

  <ob:footer />
</ob:errorPage>
