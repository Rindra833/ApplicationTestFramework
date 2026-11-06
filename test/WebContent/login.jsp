<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Connexion</title></head>
<body>
    <h1>Connexion</h1>
    <!-- TRÈS IMPORTANT : method="post" et action="login" -->
    <form action="login" method="post">
    <p>Nom : <input type="text" name="username" required/></p>
    <p>Rôle : 
        <select name="role">
            <option value="user">Utilisateur</option>
            <option value="admin">Administrateur</option>
        </select>
    </p>
    <button type="submit">Connexion</button>
</form>
</body>
</html>