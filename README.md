########################################

Author: Abhash Jain (ajain28)

File: README for HW1 CSC591- DIC

#######################################

To run the above code you have to first run the bootstrap file on every console.

Steps to run the code :

1. Clone the repo in your local System.

	$ git clone https://github.ncsu.edu/ajain28/CSC591-HW1.git

2. Go to the Projet directory(CSC591-HW1) which you after clone. 
	
	$ cd CSC591-HW1

3. There are serveral file and 1 jarFile folder, which contains all the neceasiry jar file.

4. Now, run the bootstrap script on the console.

	$ . bootstrap

5. To build the class file, use build.sh. It will try to install jdk version. If prompted enter your credential to install the package.

	$ . build.sh

6. As mentioned in the HW, by default if no port number is mentioned it will listen on 6000 port. If you are not running zookeeper on 6000 then please provide appropriate port number.

7. To run the watcher, you can use watcher to launch with appropriate parameter.
	
	format for watcher : watcher IP[:port] N

	$  watcher 152.46.18.226 20

8. If you want to run player on the new console then you have execute bootstrap program first to set the path. if you stay on the same console then there is no need to run bootstrap program. To run the bootstrap you have to Navigate to project directory on the new console.

	format for player : player IP:[port] <player name> [count] [u_delay in seconds] [score]

	e.g: 
	1. for Interactive mode: 

	$ player 152.46.18.226 Abhash 
	
	2. For automated mode:
	
	$player 152.46.18.226:6000 Abhash 10 5 100

 NOTE: All the exection needs to be run from the Project  directory which in this case is CSC591-HW1.
