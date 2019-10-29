This tools is highly inspired by WAD created by ADAM BEIN which only watches the changes in java files and deploys the war
on diifferent targets.

This tool does not deploys the the war to any location rather than creates the war in exploded form. You can run the project
from exploded war location.

# watch and build (WAB)

WAD watches changes in `src\main` folder, builds the project using the `pom.xml` 
WAD executes the below commands 
mvn compile
mvn war:exploded

# usage

1. clone the repo and execute the mvn package 
2. A wab.jar is created in the target folder
3. run the jar by using java utility

`[THIN_WAR]/java -jar wab.jar




