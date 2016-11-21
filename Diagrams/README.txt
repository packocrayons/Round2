SYSC 3303 TEAM 15 Iteration 2
Members: Nwakpadolu Soluzochukwu John (100987902), Ali Faizan(100935765), Mohammed Omar Khan(100983417), Damian Porter(100938926), Griffin Barrett(100978435).


TFTP File Transfer System: Client, Error Simulation (Intermediate Host), and Server

In this iteration, the client, error simulator and server were modified to handle error codes 1, 2 and 3 those being file not found, access violation and disk full respectivly.
Error code 6, file already exists, was not covered because in this system overwriting files is allowed.


System Description

The Client provides a simple user interface that allows the user to input required values. It establishes the appropriate connection with the server and initiates the transfer. 
It does not terminate until the user indicates that no more files are to be transferred and it does not support concurrent file transfers.

The Error Simulation (port 23) communicates with the client and the server using DatagramSocket objects. In this iteration, the error simulator just passed on packets (client to server, and server to client)

The Server(port 69) consists of multiple Java threads. Thus capable of supporting multiple concurrent read and write connections with different clients.
Once the server is started, it runs until it receives a shutdown command from the server operator.

The User Interface(S) allows the user to input commands and toggle modes for example "quiet" and "verbose" whereby in the verbose mode, the client, error simulator, and server output detailed information 
about packets received and sent, as well as any timeouts or retranmissions of packets. 


Setup Instructions

1.Import the project into Eclipse by going to import-> Existing Projects into Workspace
2.Select the folder Team15_IT2
3.Run Server.java first by right clicking and selecting "Run as Java Application"
4.Run IntermediateHost.java second by right clicking and selecting "Run as Java Application"
5.Run Client.java third by right clicking and selecting "Run as Java Application"
6.You will see the results displayed on Client ,IntermediateHost and Server consoles


List of Commands: 

1.Reading a file: Type 'read (filename)'  Example: read s128.txt
2.Writing a file: Type 'write (filename)'  Example: write c128.txt
3.Toggle Test(verbose) mode: Type 'toggle test' 
4.Toggle Quiet mode: Type 'toggle quiet' 
5.For help: Type 'help'


Files: Team15_IT2 >
	- README.txt
	- RRQ UCM Diagram.png
	- UML Diagram.png
	- WRQ UCM Diagram.png
	- RRQ Error Code 1.png
	- RRQ Error Code 2.png
	- RRQ Error Code 2b.png
	- RRQ Error Code 3.png
	- RRQ Error Code 6.png
	- WRQ Error Code 1.png
	- WRQ Error Code 2.png
	- WRQ Error Code 2b.png
	- WRQ Error Code 3.png
	- WRQ Error Code 6.png
	- Client Error.png

	client > //client testfiles
		- c0.txt
		- c1.txt
		- c128.txt
		- c128x512.txt
		- c512.txt
		- c513.txt
		- c1024.txt
		- Dog.jpg
		- HelloWorld.java
		- pdftest.pdf

	server > //server test files
		- s0.txt
		- s1.txt
		- s128.txt
		- s128x512.txt
		- s512.txt
		- s513.txt
		- s1024.txt
		- Dog.jpg
		- HelloWorld.java
		- pdftest.pdf
		- testgif.gif

	src >
		packets >
			- AcknowledgementPacket.java
			- DataPacket.java
			- ErrorPacket.java
			- ErrorType.java
			- IDontCarePacket.java
			- Packet.java
			- PacketFactory.java
			- PacketType.java
			- ReadRequestPacket.java
			- WriteRequestPacket.java

		tftp >
			- Client.java
			- ClientController.java
			- ClientErrorHandler.java
			- ClientUI.java
			- ErrorHandler.java
			- FileFactory.java
			- FileType.java
			- IntermidiateHost.java
			- OutputHandler.java
			- Receiver.java
			- Sender.java
			- Server.Java
			- ServerErrorHandler.java
			- ServerOutputHandler.java


Project Contributions Table
+-------------------+-------------------------------------------+------------------------------------------------+-------------+-------------+-------------+
|       Name        |                Iteration 1                |                  Iteration 2                   | Iteration 3 | Iteration 4 | Iteration 5 |
+-------------------+-------------------------------------------+------------------------------------------------+-------------+-------------+-------------+
| Damian Porter     | - Attempted some code                     | - Made the Timing Diagrams                     | NA          | NA          | NA          |
| 100938926         | - Made the UCMs                           | - Made the README File                         |             |             |             |
+-------------------+-------------------------------------------+------------------------------------------------+-------------+-------------+-------------+
| Griffin Barrett   | - Wrote code for the pacet classes        | - Wrote the code for the Error Packets         | NA          | NA          | NA          |
| 100978435         | - Wrote code for the sender/receiver      | - Put all the compenents together              |             |             |             |
|                   | - Helped write code for all other classes |                                                |             |             |             |
+-------------------+-------------------------------------------+------------------------------------------------+-------------+-------------+-------------+
| Omar Khan         | - Wrote code for filefactory and IM host  | - Updated code for FileFactory, Sender         | NA          | NA          | NA          |
| 100983417         | - Wrote all the coments                   |   and receiver for the error handling          |             |             |             |
+-------------------+-------------------------------------------+------------------------------------------------+-------------+-------------+-------------+
| Ali Faizan        | - Wrote code for server                   | - Worte code for ServerOutputHandler and       | NA          | NA          | NA          |
| 100935765         | - Made the UML class diagram              |   ServerErrorHandler                           |             |             |             |
|                   | - Made testcases                          | - Updated Server to handle errors              |             |             |             |
+-------------------+-------------------------------------------+------------------------------------------------+-------------+-------------+-------------+
| Nwakpadolu        | - Wrote code for the client               | - Wrote Code for ClientErrorHandler            | NA          | NA          | NA          |
| Soluzochukwu John | - Put together the README file            | - Updated Client and ClientUI to handle errors |             |             |             |
| 100987902         |                                           |                                                |             |             |             |
+-------------------+-------------------------------------------+------------------------------------------------+-------------+-------------+-------------+
