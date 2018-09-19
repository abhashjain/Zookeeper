#!/bin/bash
sudo apt-get install openjdk-8-jdk-headless
touch a.class
rm -rf *.class
export PATH=$PATH:$PWD/$(dirname "$0")
javac -cp ".:./jarfile/slf4j-api-1.7.25.jar:./jarfile/slf4j-simple-1.7.25.jar:./jarfile/zookeeper-3.4.12.jar" player.java
javac  -cp ".:./jarfile/slf4j-api-1.7.25.jar:./jarfile/slf4j-simple-1.7.25.jar:./jarfile/zookeeper-3.4.12.jar" viewer.java
chmod 777 player
chmod 777 watcher

