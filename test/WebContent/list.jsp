<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${titre}</title>
</head>
<body>
    <h1>${titre}</h1>
    <p>Controller : ${controller}</p>
    <p>Methode : ${methode}</p>
    <p>HTTP : ${httpMethod}</p>

    <h2>Employes</h2>
    <ul>
        <%
            List<String> employees = (List<String>) request.getAttribute("employees");
            if (employees != null) {
                for (String employee : employees) {
        %>
                    <li><%= employee %></li>
        <%
                }
            }
        %>
    </ul>
</body>
</html>