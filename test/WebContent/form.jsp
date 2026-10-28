<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Formulaire Sprint 8</title></head>
<body>
    <h1>Enregistrement avec fichier</h1>
    <!-- ATTENTION : enctype="multipart/form-data" obligatoire -->
    <form action="save" method="post" enctype="multipart/form-data">
        <p>Nom    : <input type="text"   name="nom"    /></p>
        <p>Age    : <input type="number" name="age"    /></p>
        <p>Email  : <input type="email"  name="email"  /></p>
        <p>Photo  : <input type="file"   name="photo"  /></p> <!-- "photo" sera le nom du MaFile -->
        <button type="submit">Enregistrer</button>
    </form>
</body>
</html>