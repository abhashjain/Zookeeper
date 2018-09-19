#Author: Abhash Jain (ajain28)
# CSC591- DIC Assignment 1

.PHONY: all bin clean
Viewer_file=./viewer.java
Player_file=./player.java

all:
	. build.sh
clean:
	rm -rf *.class
