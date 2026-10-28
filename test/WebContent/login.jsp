<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Connexion</title></head>
<body>
    <h1>Connexion</h1>
    <!-- TRÈS IMPORTANT : method="post" et action="login" -->
    <form action="login" method="post">
        <p>Nom d'utilisateur : <input type="text" name="username" required /></p>
        <button type="submit">Se connecter</button>
    </form>
</body>
</html>