- Utilisation de Git :
    - maka branch main (principale)
    - creer une branche autre (sprint 1 exemple), on clone ensuite.
    - Pull Request(jusqu'a une date donner)
    - merge request
    - effacer la branche apres

sprint 0 (creer un servlet):
    - FrontControllerServlet (peut importe l'url en requette ca redirige dans )
        + proccessRequest(...)
        + output(url)

    - on mets dans point jar io class io
    - dans test on redirige tout vers frontcont... grace a .xml ou on y mets ce servlet, 

sprint 1 :

    class controller(avec package au choix) :
    il faut que dans les test hoe misy @Controller
    - filter
    - listener

    mila fantantra ny controller rehetra
    comment :
        dans frontc..servl....jav :
            - variable (list controller) attribut
            - parcourrir tout les classes pour chaque path et test chaque annotation


    front... :
        lsit<String> listcont;
        init() {
            listcont = new ...;
            ...(parcourrir chaque path pour voir l'annotation Controller prends le full avec package du classe et test chaque classe pour voir si il y a son annotation)
        }

        processRequest () {
            boucle listcont
                et affiche.
        }

    
package annotations
   *creer classe Controller
avoir un code executer soit au demarrage de l'application(1) soit au premier appel du frontServlet(2)
   *utiliser Listener(1) ou non
   *mettre dans fonction init de frontServlet(2)
   *connaitre tous les controllers
   (2): *mettre un attribut dans FSC : list<String> controllers
   *init parcours les classes dans le classpath de l'app test si elles sont annotées @Controller
   si oui, ajouter le nom de la classe dans la liste des controllers
   *afficher la liste des controllers dans processRequest
   *dans web.xml de l'app de test, ajouter une variable dont la valeur est le package contenant les controllers et prendre ce package en parametre dans le framework 
   *creer une classe utilitaire contenant une ou des methodes pour recuperer le package et lister les classes presentes dans ce package pour ensuite verifier les annotations presentes et leur niveau pour avoir la liste des noms des controllers apres.

sprint 2 :
    - on a la liste des controller

sprint 3 :
    
sprint 3 (bis) :
    