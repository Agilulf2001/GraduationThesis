#set -v
cd ~/Dune/simulation/hard-origin
javac Utility/*.java
javac Common/*.java -cp .:/home/agi/Dune/simulation/log4j-1.2.17.jar
javac Sketch/*.java -cp .:/home/agi/Dune/simulation/log4j-1.2.17.jar
javac src/SimulationEx.java -cp .:/home/agi/Dune/simulation/argparse4j-0.8.1.jar:/home/agi/Dune/simulation/log4j-1.2.17.jar
echo Manifest-Version: 1.0 > manifest
echo Created-By: 11.0.7 \(Ubuntu\) >> manifest
echo Main-Class: src.SimulationEx >> manifest
echo Class-Path: argparse4j-0.8.1.jar log4j-1.2.17.jar >> manifest
find ./ -name "*.class" |xargs jar cvfm SimulationEx.jar manifest
find ./ -name "*.class" |xargs rm
rm manifest
mv SimulationEx.jar ../SimulationEx.jar

