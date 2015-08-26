<%@ tag body-content="empty" description="Page footer" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:useBean id="appDeployer" scope="request" type="java.lang.String" />
<jsp:useBean id="appDeployTime" scope="request" type="java.lang.String" />
<jsp:useBean id="appVersion" scope="request" type="java.lang.String" />

<div class="footer">
  <spring:message code="app.version" arguments="${appVersion}" />
  <c:if test="${isAdmin}">
    | <spring:message code="app.deployString" arguments="${appDeployer},${appDeployTime}" />
  </c:if>
</div>
