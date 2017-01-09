CNT5106C: Computer Networks, Fall 2016

Internet chat application
by
UFID: 9951-5080
Name: Ananda Kishore Sirivella

To run the project:
	1. Compile :- 
		javac Server.java
		javac Client.java
	2. Running :-
		java Server <PORT_NUMBER or Default is 3000/>
		java Client <USER_NAME/> <PORT_NUMBER or Default is 3000>
	3. Place file in chatapp\ directory for ease of verification.

*Files will be saved for each client at location: chatapp\<USER_NAME/>.

Operations Commands for Server:
	1. Shut down the server:
		quit

Operations Commands for Client:
	1. Broadcast messages:
		broadcast <CONTENT/>
	2. Broadcast file:
		broadcast file <FilePath/>
	3. Unicast messages:
		unicast <CONTENT/> <RECIEVER_USER_NAME/>
	4. Unicast file:
		unicast file <FilePath/> <RECIEVER_USER_NAME/>
	5. Blockcast messages:
		blockcast <CONTENT/> <BLOCKED_USER_NAME/>
	6. Blockcast file:
		blockcast file <FilePath/> <BLOCKED_USER_NAME/>
	7. Shut down the chat window:
		quit

The submission folder has the following files:
	1. chatapp\Server.java - source for server.
	2. chatapp\Server.class - binary for server.
	3. chatapp\clientThread.class - binary for client thread at Server.
	4. chatapp\Server$serverCloseDown.class - binary for server shut Down operation.
	5. chatapp\Client.java - source for clients.
	6. chatapp\Client.class - binary for clients.
	7. chatapp\Client$ReadAndSend.class -  binary for clients to read the keyboard and send the information to server.
	8. chatapp\Client$RecieveAndPrint.class - binary for clients to recieve information from the server and print to chat screen.
	9. chatapp\test1.rar - test rar file for verifying file transfer functionality.
	10.chatapp\test1.txt - test text file for verifying file transfer functionality.
	11.chatapp\ReadMe.txt - text file with details about the Project.
	
Implementation information:
	Server:-
		Aceept new connections from clients and create a new thread to handle each client connection individually.
	clientThread:-
		Handles each operation of user. We need to keep listening on various client sockets parallely. Hence, we need to use
		threads.
	serverCloseDown:-
		Server side thread to handle keyboard input to server. As of now, it only handle shutting down of server.
		
	Client:-
		Establish a connection with Server and set send & recieve operation threads on running mode. We need to parallely recieve and send as well, hence we need threads.
	ReadAndSend:-
		A thread class to read user inpurt from keyboard, read files and send it to server.
	RecieveAndPrint:-
		A thread class to recieve data from server, download files and print to the chat screen.

References:
	- https://www.google.com/
	- http://stackoverflow.com/
	- https://docs.oracle.com/javase/7/docs/api/
	- https://docs.oracle.com/javase/tutorial/networking/sockets/