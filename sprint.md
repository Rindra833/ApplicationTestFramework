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

