<%@ tag description="Full error page with basic Bootstrap layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="title" required="true" type="java.lang.String" %>
<jsp:useBean id="isLoggedIn" scope="request" type="java.lang.Boolean" />
<jsp:useBean id="isAdmin" scope="request" type="java.lang.Boolean" />
<jsp:useBean id="userEmail" scope="request" class="java.lang.String" />

<!doctype html>
<html lang="en">
<head>
  <link rel="icon" type="/image/ico" href="/img/favicon.ico" />
  <meta charset="utf-8">
  <title>Error: <c:out value="${title}" /> - Open Bidder</title>
  <link rel="stylesheet" media="screen" href="/css/bootstrap.css">
  <link rel="stylesheet" media="screen" href="/css/open-bidder.css">
</head>
<body>

<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a class="brand" href="/">Open Bidder</a>
      <div>
        <ul class="nav pull-right">
          <c:if test="${isLoggedIn}">
            <li><p class="navbar-text"><c:out value="${userEmail}" /></p></li>
            <li><a href="/_ah/logout?continue=/">Logout</a></li>
          </c:if>
          <c:if test="${not isLoggedIn}">
            <li><a href="/_ah/login?continue=/">Login</a></li>
          </c:if>
        </ul>
      </div>
    </div>
  </div>
</div>

<jsp:doBody />

</body>
</html>
