<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Resultat Sprint 7 BIS</title>
</head>
<body>
    <h1>Donnees recues</h1>

    <h2>Via l'objet Employee</h2>
    <ul>
        <li>Nom   : ${employee.nom}</li>
        <li>Age   : ${employee.age}</li>
        <li>Email : ${employee.email}</li>
    </ul>

    <h2>Rappel (attributs directs)</h2>
    <ul>
        <li>Nom   : ${nom}</li>
        <li>Age   : ${age}</li>
        <li>Email : ${email}</li>
    </ul>

    <p><a href="form">Retour au formulaire</a></p>
</body>
</html>