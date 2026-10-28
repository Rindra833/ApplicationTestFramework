<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Resultat Sprint 8</title></head>
<body>
    <h1>Resultat de l'enregistrement</h1>

    <h2>Employé</h2>
    <ul>
        <li>Nom   : ${employee.nom}</li>
        <li>Age   : ${employee.age}</li>
        <li>Email : ${employee.email}</li>
    </ul>

    <h2>Fichier</h2>
    <p><b>${fichierMessage}</b></p>
    <c:if test="${not empty fichierNom}"> <!-- nécessite JSTL ou juste utiliser scriptlet -->
        <ul>
            <li>Nom : ${fichierNom}</li>
            <li>Taille : ${fichierTaille} bytes</li>
        </ul>
    </c:if>

    <p><a href="form">Retour</a></p>
</body>
</html>