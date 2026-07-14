<%@ page contentType="text/html;charset=UTF-8" %>
<%

    int[] test = (int[])request.getAttribute("int");
    String[] test1 = (String[])request.getAttribute("String");

%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <% for (int i = 0; i < test.length; i++) { %>
        <ul>
            <li><%= test[i]%></li>
            <li><%= test1[i]%></li>
        </ul>
    <% } %>
</body>
</html>