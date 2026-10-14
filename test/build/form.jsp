<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Formulaire Sprint 7</title>
</head>
<body>
    <h1>Enregistrement</h1>
    <form action="save" method="post">
        <p>Nom    : <input type="text"   name="nom"    required/></p>
        <p>Age    : <input type="number" name="age"    required/></p>
        <p>Email  : <input type="email"  name="email"  required/></p>
        <p><button type="submit">Enregistrer</button></p>
    </form>
</body>
</html>