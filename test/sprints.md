## Spring 0 : 
- FrontControllerServlet (peut importe l'url en requette ca redirige dans )
        + proccessRequest(...)
        + output(url)

- on mets dans point jar io class io
- dans test on redirige tout vers frontcont... grace a .xml ou on y mets ce servlet,
## Spring 1 :
- `@Controller` : annotation de classe (RUNTIME) qui marque un controleur.
- `@GetMapping("/route")` : annotation de methode (RUNTIME) qui associe
  une route a une methode.
- `FrontControllerServlet.scanControllers()` : au demarrage du servlet
  (`init()`), parcourt tous les fichiers `.class` presents a la racine de
  `WEB-INF/classes` (= racine du classloader, puisqu'il n'y a pas de
  packages), charge chaque classe avec `Class.forName(...)`, et garde
  celles annotees `@Controller`.
- `output(...)` : affiche dans le navigateur, sous forme de liste HTML,
  chaque controleur trouve et ses methodes annotees `@GetMapping` avec
  leur route associee.

## Spring 2 : 
Obtenir l'url
URL : afficher le controller et les methode associé avec cette url

comment?

Créer annotation associé avec un méthode, avec un variable ...

Résultat attendu : 
- On doit lister tous les controllers, d'abord Url et puis nom
- Pour chaque controller lister il faut afficher aussi les méthode annoté avec @urlMapping

- Quand on ne reconnaît pas l'url, il faut faire un throw exception et proposer les url existant,

## Spring 3 :
Prise en charge des types de methode http (GET/POST)
- Creation d'une classe UrlMethode:
    - attributs:
        - url (String)
        - methode (String ou creation d'un Enum java) GET/POST
    - Override de la fonction equals pour des futurs comparaison
- Changement de la clé de l'ancien map par la nouvelle classe
- Mise à jour de la fonction initMapping
- il faut implémenter eqauls et hcode notre logique doit être de sorte que deux méthode soit le même si l'url et le methode

test : créer deux methode avec le même url sy methode
       à l'appel soit doit faire une exception

## Spring 3-Bis : 
- Incovation de la methode qui contient l'url par reflexion

## Spring 4 : 
Listner associé avec un evenement 

dans notre cas ici, on l'attache au démarrage du l'appWeb dans tomcat
et elle doit executer init() et faire le scan et le mappage dont il est censé faire


### Sprint 5-Support de vue ModelAndView
- Creation de la classe ModelAndView :
    - attributs : String view, Map<String, Object> data
    - fonction addAttribut(clé, object) pour ajouter des élements à la map
    - constructeur avec le parametre view
    - fonction setview
- Modification de ProcessRequest sur l'invocation:
    - exception si le type de retour n'est pas ModelAndView
    - caster le resultat de retour 
    - creation d'un fonction pour ajouter data du model dans le request SErvlet : addArgToRequest(req, map)
    - creation d'un dispatcher qui envoye vers la page correspondant au vu

## Sprint 5 bis
Integration de spring avec ces beans. Les controller sera gérer avec notre framework actuelle et Spring va manipuler (avec ces beans) le coté service et accés au base de données

Objectif actuelle obtenir : Avoir une instance de spring pour manipuler les beans dans le TEST
Instruction:
- Dans test 
    1 - Dans controller : on peut avoir une fonction list(ContexteSpring) 
            On maninuple quelques beans ici pour le test
- Dans framework : 
    1- Pendant le démarrage de l'application on doit pouvoir récuperer l'instance de spring qu'on va donner plus tard au fonction qui veut l'utiliser
    2 - Pendant l'appel du fonction list(ContexteSpring) : elle doit vérifier son paramètre, si on voit qu'elle
        demande en paramètre la contexteSpring on le lui donné

