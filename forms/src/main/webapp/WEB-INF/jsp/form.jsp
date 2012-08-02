<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="crosswalk.*" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="/static/css/reset.css" />
<link rel="stylesheet" type="text/css" href="/static/css/cdrui_styles.css" />

<link type="text/css" href="/cdradmin/css/jquery/ui/jquery-ui.css" rel="stylesheet" />

<script type="text/javascript" src="/cdradmin/js/jquery/jquery.min.js"></script> 
<script type="text/javascript" src="/cdradmin/js/jquery/ui/jquery-ui.min.js"></script> 

<script type="text/javascript">
	$(document).ready( function() {
		$(".datepicker").datepicker({dateFormat : 'yy-mm-dd', changeYear : true, changeMonth : true, yearRange : '-300:+02' }).val($.datepicker.formatDate('yy-mm-dd', new Date()));
		// $(".datepicker").datepicker('option', 'constrainInput', true);
		// $(".datepicker").datepicker('option', 'maxDate', '+0m +0w');
	});
</script>

<!--[if IE 8]>
	<link rel="stylesheet" type="text/css" href="/static/css/cdrui_styles_ie8.css" />
<![endif]-->
<meta name="description" content="Carolina Digital Repository Deposit Form" />
<meta name="keywords" content="Carolina Digital Repository, deposit" />
<meta name="robots" content="index, nofollow" />
<link rel="shortcut icon" href="/static/images/favicon.ico" type="image/x-icon" />
<title><c:out value="${form.title}"/></title>
</head>
<body>
<div id="pagewrap">
	<div id="pagewrap_inside">
		<div class="darkest shadowbottom" id="header">
			<div class="threecol dark shadowbottom">
				<div class="contentarea">
					<h1>Carolina Digital Repository</h1>
					<a href="/" id="titlelink"><img src="/static/images/carolinadigitalrepository.png"></a>
					
					
				</div>
			</div>
			<div class="fourcol darkest">
				<div class="contentarea">
				</div>
			</div>
		</div>
		<div id="content">
			<div class="contentarea">
<h2><c:out value="${form.title}"/></h2>
<p><c:out value="${form.description}"/></p>
<form:form modelAttribute="form" enctype="multipart/form-data">
	<c:forEach items="${form.elements}" varStatus="elementRow">
		<spring:bind path="form.elements[${elementRow.index}]" ignoreNestedPath="true">
			<% if(Paragraph.class.isInstance(status.getValue())) { 
					Paragraph p = (Paragraph)status.getValue(); 
					if(p.getHeading() != null) { %>
					<br/><h3><%= p.getHeading() %></h3>
					<% }
					if(p.getText() != null) { %>
					<p><%= ((Paragraph)status.getValue()).getText() %></p>
					<% } %>
			<% } else if(MetadataBlock.class.isInstance(status.getValue())) { 
					MetadataBlock mb = (MetadataBlock)status.getValue(); %>
					<br/><h3 style="clear: both;"><%= ((MetadataBlock)status.getValue()).getName() %></h3>
					<% if(mb.getDescription() != null) { %>
					<p><%= ((MetadataBlock)status.getValue()).getDescription() %></p>
					<% } %>
					<div style="margin: 0em 2em;">
					  <p>
					<c:forEach items="${form.elements[elementRow.index].ports}" var="port" varStatus="portRow">
						<spring:bind path="form.elements[${elementRow.index}].ports[${portRow.index}]" ignoreNestedPath="true">
							<div style="float: left; clear: both; width: 12em; height: 2em; text-align: right; vertical-align: baseline; margin-right: 1em;"><c:out value="${port.label}"/></div>
							<% if(status.getValue() instanceof DateInputField) { %>
							<form:input cssClass="datepicker" path="elements[${elementRow.index}].ports[${portRow.index}].enteredValue" title="${port.usage}" />
							<% } else if(status.getValue() instanceof TextInputField) { %>
							<form:input path="elements[${elementRow.index}].ports[${portRow.index}].enteredValue" title="${port.usage}" maxlength="${port.maxSize}" size="${port.preferredSize}" />
							<% } else { %>
							<form:input path="elements[${elementRow.index}].ports[${portRow.index}].enteredValue" title="${port.usage}" />
							<% } %>
							<c:if test="${port.required}"><span style="color:red">*</span></c:if>
							<form:errors cssStyle="color:red;" path="elements[${elementRow.index}].ports[${portRow.index}].enteredValue" /><br />
						</spring:bind>
					</c:forEach>
					  </p>
					</div>
			<% } %>
		</spring:bind>
	</c:forEach>
	<br/><h3>File for Deposit</h3>
	<div style="margin: 0em 2em;">
		<p><input name="file" type="file" /><spring:hasBindErrors name="form"><span style="color:red"><%= errors.getFieldError("file") == null ? "" : errors.getFieldError("file").getDefaultMessage() %></span></spring:hasBindErrors></p>
	</div>
	<div style="text-align: center; margin: 2em;">
		<input type="submit" value="submit deposit" />
	</div>
</form:form>
</div>
</div>
<div id="footer" class="darkest">
	<div class="threecol dark">
		<div class="contentarea">
			<p>
				<a href="/">Home</a>&nbsp;|&nbsp;
				<a href="search?types=Collection">Browse Collections</a>&nbsp;|&nbsp;
				<a href="external?page=about.about">About</a>&nbsp;|&nbsp;
				<a href="external?page=contact&amp;refer=https%3a%2f%2fcdr.lib.unc.edu%2f">Contact Us</a>&nbsp;|&nbsp;
				<a href="http://www.lib.unc.edu">Library Home</a>&nbsp;|&nbsp;
				<a href="http://www.lib.unc.edu/aoffice/policies/privacy_policy.html">Privacy Policy</a>
			</p>
		</div>
	</div>
	<div class="fourcol darkest">
		<div class="contentarea"><p class="left"><a href="http://www.unc.edu">UNC Home </a></p><a href="http://www.unc.edu"><img src="/static/images/uncwell.png" id="footer_uncwell"></a></div>
	</div>
</div>
</div>
</div>
</body>
</html>