#!/bin/bash
####################
# Author- Abhash Jain (ajain28)
# CSC591 - HW1 BUILD FILE
# 
####################
sudo apt-get install -y openjdk-8-jdk-headless 
touch a.class
rm -rf *.class
export PATH=$PWD/$(dirname -- "$0"):$PATH
javac -cp ".:./jarfile/slf4j-api-1.7.25.jar:./jarfile/slf4j-simple-1.7.25.jar:./jarfile/zookeeper-3.4.12.jar" ZPlayer.java
javac  -cp ".:./jarfile/slf4j-api-1.7.25.jar:./jarfile/slf4j-simple-1.7.25.jar:./jarfile/zookeeper-3.4.12.jar" viewer.java
chmod 777 player
chmod 777 watcher
chmod 777 bootstrap
