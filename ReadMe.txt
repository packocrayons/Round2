
SYSC 3303 TEAM 17 ITERATION 3
Members: Brydon Gibson (100975274), Lisa Martini(101057495), Griffin Barrett(100978435).

TFTP File Transfer System: Client, Error Simulation (Intermediate Host), and Server

In this iteration, the client, error simulator and server were developed to support steady-file transfer with file errors from IT2 and timeout/retransmit from IT3.

The Client provides a simple user interface that allows the user to input required values. It establishes the appropriate connection with the server and initiates the transfer. 
It does not terminate until the user indicates that no more files are to be transferred and it does not support concurrent file transfers.

The Error Simulation (port 23) communicates with the client and the server using DatagramSocket objects.
The ErrorSimulator uses a context-free language to read it's actions towards packets, the language is described as follows:
	program packetType packetNumber [args]
	Available 'programs' are :
	drop (packet) (packetNum) [noArgs]
	delay (packet) (packetNum) [timeInMillis]
	duplicate (packet) (packetNum) [numberOfDuplicates timeBetweenDuplicates]
	opcode (packet) (packetNum) [newOpcodeFirstByte newOpcodeSecondByte]
	mode (packet) (packetNum)
	#lines that begin with '#' are comments and are ignored, there is no multi-line commenting, or beginning a comment mid-line
Times can be replaced with conditions - conditions are met on specific packets, for example,
	delay ack 4 cond data 7
will delay acknowledgement packet 4 until the intermediate host sees data packet 7.
Available packets are :
	ack
	data
	readrequest
	writerequest
	error
Currently the simulator reads from [project dir]/intermediateHost/IHErrorFile.txt.
(note - for packets that do not have associated numbers - like an error packet or readrequest/writerequest, any valid number should be put in place as a placeholder)


The Server(port 69) consists of multiple Java threads. Thus capable of supporting multiple concurrent read and write connections with different clients.
Once the server is started, it runs until it receives a shutdown command from the server operator.

The User Interface(S) allows the user to input commands and toggle modes for example "quiet" and "verbose" whereby in the verbose mode, the client, error simulator, and server output detailed information 
about packets received and sent, as well as any timeouts or retranmissions of packets. 
 
Our choices for IT3 :
	Sender can timeout and retransmit , ignores duplicate ack (discard ack and wait for a next ack). 
	Receiver can timeout but doesn't retransmit ack . For duplicate data , receiver send the ack corresponding and discard the data packet.
	For both host (receiver and sender) after 4 timeout, the host is shutting down because it means en error packet has been sent but never received. 
	When an error happened the host sned the error packet and shuts down. If the error occured before any connection (file not found or access violation for client) the client isn't shutting down but no thread is created to deal with the transfer. 
	If RRQ and WRQ are lost, the client just retransmit the request. 
	For now on we don't really handle the case of duplicated or delayed request we will deal with it in next IT.

Setup Instructions

1.Import the project into Eclipse by going to import-> Existing Projects into Workspace
2.Select the project Team17_IT3
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
tftp package :
 - Client.java
 - ClientUI.java
 - ClientController.java
 - ClientErrorHandler.java
 - EffectType.java
 - ErrorHandler.java
 - FileFactory.java
 - FileType.java
 - IntermediateHost.java (error simulator that read the IHErrorFile and delay, loose or duplicate packet designated)
 - OutputHandler.java
 - PacketFX.java
 - Receiver.java (Receiver receives the data files, writes them, and sends an acknowledgement packet when its done writing)
 - Sender.java (Sender get the file, sends it to the port, receives the acknowledgement packet, and closes the file and socket )
 - SendReceiveInterface.java
 - Server.java
 - ServerErrorHandler.java
 - ServerOutputHandler.java


packets package :
 - Packet.java
 - DataPacket.java
 - AcknowledgementPacket.java
 - IDontCarePacket.java
 - ReadRequestPacket.java
 - WriteRequestPacket.java
 - ErrorType.java
 - ErrorPacket.java
 - PacketFactory.java
 - PacketType.java
 - ReadRequestPacket.java
 - WriteRequestPacket.java



server and client directory for text files.
Other files:
 - IHErrorFile.txt (contains information to simulate loss, delay or duplicate packet) 
 - UML Diagrams for all the classes (updated)
 - ReadMe.txt
 - Timing diagrams for timeout and retransmission (new)
 - Timing diagrams for file transfer errors , same as IT2
 - UCM Diagram for the read file transfer and a write transfer, including the error simulator haven't been updated since IT1.



Responsibilites for IT3:
 
Brydon Gibson (100975274) wrote the interpreter for the control language for the intermediate host and the beginning of the Sender to deal with timeout/retransmission.
Griffin Barrett(100978435) wrote code for Receiver to deal with timeout/retransmission and finished Sender code  plus Client and Server code.
Lisa Martini (101057495) drew the timing diagrams and UML CLass diagrams, wrote part of ReadMe, wrote code to deal with error loss or delay in sender and receiver + took care of verbose/quiet mode printing correctly.

