
SYSC 3303 TEAM 15 Iteration 1
Members: Nwakpadolu Soluzochukwu John (100987902), Ali Faizan(100935765), Mohammed Omar Khan(100983417), Damian Porter(100938926), Griffin Barrett(100978435).

TFTP File Transfer System: Client, Error Simulation (Intermediate Host), and Server

In this iteration, the client, error simulator and server were developed to support steady-file transfer assuming the absence of errors

The Client provides a simple user interface that allows the user to input required values. It establishes the appropriate connection with the server and initiates the transfer. 
It does not terminate until the user indicates that no more files are to be transferred and it does not support concurrent file transfers.

The Error Simulation (port 23) communicates with the client and the server using DatagramSocket objects.
The ErrorSimulator uses a context-free language to read it's actions towards packets, the language is described as follows:
program packetType packetNumber [args]
Available 'programs' are :
drop [noArgs]
delay [timeInMillis]
duplicate [timeBetweenPacketsMillis numberOfDuplicates]
#lines that begin with '#' are comments and are ignored, there is no multi-line commenting, or beginning a comment mid-line
Currently the simulator reads from [project dir]/bin/tftp/IHErrorFile.txt, there is a symlink to this file in [project dir].


The Server(port 69) consists of multiple Java threads. Thus capable of supporting multiple concurrent read and write connections with different clients.
Once the server is started, it runs until it receives a shutdown command from the server operator.

The User Interface(S) allows the user to input commands and toggle modes for example "quiet" and "verbose" whereby in the verbose mode, the client, error simulator, and server output detailed information 
about packets received and sent, as well as any timeouts or retranmissions of packets. 
 



Setup Instructions

1.Import the project into Eclipse by going to import-> Existing Projects into Workspace
2.Select the project Team15_IT1
3.Run Server.java first by right clicking and selecting "Run as Java Application"
4.Run IntermediateHost.java second by right clicking and selecting "Run as Java Application"
5.Run Client.java third by right clicking and selecting "Run as Java Application"
6.You will see the results displayed on Client.java,IntermediateHost and Server


List of Commands: 
1.Reading a file: Type 'read (filename)'  Example: read s128.txt
2.Writing a file: Type 'write (filename)'  Example: write c128.txt
3.Toggle Test(verbose) mode: Type 'toggle test' 
4.Toggle Quiet mode: Type 'toggle quiet' 
5.For help: Type 'help'


 
Files:
 - Client.java
 - ClientUI.java
 - ClientController.java
 - PacketFactory.java
 - PacketType.java
 - FileType.java
 - Packet.java
 - DataPacket.java
 - AcknowledgementPacket.java
 - IDontCarePacket.java
 - ReadRequestPacket.java
 - WriteRequestPacket.java
 - FileFactory.java
 - IntermediateHost.java
 - Server.java
 - Receiver.java
 - Sender.java
 - AcknowledgementPacketTest.java
 - ServerTest.java
 - DataPacketTest.java
 - UML Diagrams for all the classes
 - UCM Diagram for the read file transfer and a write transfer, including the error simulator



Responsibilites:
 
Nwakpadolu Soluzochukwu John (100987902) wrote code for the Client and wrote the ReadMe text.
Griffin Barrett(100978435) wrote code for the Packet classes and utterly supervised the rest of the code.
Mohammed Omar Khan(100983417) wrote code for the File factory,the Intermediate Host and wrote the javadoc comments for all the classes.
Damian Porter(100938926) wrote code for runnables and drew the UCMs for the RRQ and WRQ
Ali Faizan(100935765) wrote code for the Server, drew the UMLs,wrote some of the test cases

N/B: After the lab on Monday, Griffin Barrett completely supervised and edited the code for the other classes while the rest of the group worked on the diagrams and other deliverables.
