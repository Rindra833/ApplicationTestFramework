cd framework
rm -rf build dist
./build.sh

javap -p -classpath dist/sprint-mvc-framework.jar mg.rr.framework.RouteMapping | grep -i bind

cp dist/sprint-mvc-framework.jar \
   "/home/rindra/Documents/S5/Mr_Naina/Sprints/ApplicationTestFramework/test/WebContent/WEB-INF/lib/"

# Confirmer
javap -p -classpath "/home/rindra/Documents/S5/Mr_Naina/Sprints/sprint 5 bis/ApplicationTestFramework/test/WebContent/WEB-INF/lib/sprint-mvc-framework.jar" \
  mg.rr.framework.RouteMapping | grep -i bind

cd test
rm -rf build dist
./deploy.sh

# Arrêter Tomcat
/home/rindra/Documents/S5/apache-tomcat-11.0.23/bin/shutdown.sh
sleep 3

# Supprimer TOUT ce qui concerne sprint8
rm -rf /home/rindra/Documents/S5/apache-tomcat-11.0.23/webapps/sprint9
rm -f  /home/rindra/Documents/S5/apache-tomcat-11.0.23/webapps/sprint9.war

# Supprimer aussi le work dir (Tomcat compile les JSP ici, ça peut cacher des restes)
rm -rf /home/rindra/Documents/S5/apache-tomcat-11.0.23/work/Catalina/localhost/sprint9

# Copier le nouveau WAR
cp dist/sprint9.war /home/rindra/Documents/S5/apache-tomcat-11.0.23/webapps/

# Redémarrer
/home/rindra/Documents/S5/apache-tomcat-11.0.23/bin/startup.sh