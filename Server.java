import java.net.Socket;
import java.util.HashMap;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
/**
 *
 * @author SIAKI
 */
public class Server {
    
    public static HashMap<String, clientThread> users = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        
        ServerSocket sersock;
        int portNumber = 3000;
        if(args.length > 0) portNumber = Integer.valueOf(args[0]);
        
        sersock = new ServerSocket(portNumber);
        System.out.println("Server Running on port number " + portNumber);
        
        serverCloseDown s = new serverCloseDown(users, sersock);
        s.start();
 
        String receiveMessage, sendMessage = "";
        while(true) {
            try{
                Socket sock = sersock.accept();
                //DataInputStream inputstream = new DataInputStream(sock.getInputStream());
                DataInputStream inputstream = new DataInputStream(sock.getInputStream());
                PrintStream outputstream = new PrintStream(sock.getOutputStream());
                if((receiveMessage = inputstream.readLine()) != null){
                    if(receiveMessage.contains( "create")) {
                        String name = receiveMessage.split(" ")[1];
                        users.put(name, new clientThread(name, sock, users));
                        users.get(name).start();
                        
                        sendMessage = "Welcome " + name + "!";
                    }
                }
                outputstream.println(sendMessage);
                outputstream.flush();
            } catch(IOException e){
                break;
            }
            
        }
    }
    
    public static class serverCloseDown extends Thread{
        ServerSocket serverSock;
        HashMap<String, clientThread> allUsers = new HashMap<>();
        
        public serverCloseDown(HashMap<String, clientThread> users, ServerSocket s){
            this.allUsers = users;
            this.serverSock = s;
        }
        
        @Override
        public void run(){
            String recieveLine;
            BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                try{
                    recieveLine = keyRead.readLine();
                    if(recieveLine.startsWith("quit")) break;
                } catch(IOException e){
                    
                }
            }
            
            for(String key : allUsers.keySet()){
                allUsers.get(key).ostream.println("Emergency Server ShutDown! Please try after some time.");
                allUsers.get(key).ostream.close();
                try {
                    allUsers.get(key).istream.close();
                } catch (IOException ex) {
                    
                }
                
                try {
                    allUsers.get(key).clientSock.close();
                } catch (IOException ex) {
                    
                }
            }
            try {
                serverSock.close();
            } catch (IOException ex) {
                
            }
        }
    }
}

class clientThread extends Thread{
    String userName;
    Socket clientSock = null;
    PrintStream ostream = null;
    DataInputStream istream = null;
    HashMap<String, clientThread> allUsers;

    public clientThread(String name, Socket clientSocket, HashMap<String, clientThread> users) {
        this.allUsers = new HashMap<>();
        this.clientSock = clientSocket;
        this.allUsers = users;
        this.userName = name;
    }

    @Override
    public void run() {
        int FILE_SIZE = 6022386;
        HashMap<String, clientThread> users;
        users = this.allUsers;

        try {
            istream = new DataInputStream(clientSock.getInputStream());
            ostream = new PrintStream(clientSock.getOutputStream());
            System.out.println("Client " + userName + " connected");
            String name = istream.readLine().trim();
            //ostream.println("");
            synchronized(this){
                while (true) {
                    String line = istream.readLine();
					String[] lineArr = line.split(" ");
                    if(lineArr.length > 0){
                    
                    String operation = lineArr[0].toLowerCase();
                    Boolean isFile = (lineArr.length > 1 && lineArr[1].toLowerCase().equals("file"));
                    //System.out.println(line);
                    
                    if(isFile){
                        String specificUserName;
                        if(isInteger(lineArr[lineArr.length - 1])){
                            specificUserName = lineArr[lineArr.length - 2];
                            if(isInteger(lineArr[lineArr.length - 1])) FILE_SIZE = Integer.valueOf(lineArr[lineArr.length - 1]);
                        }else{
                            specificUserName = lineArr[lineArr.length - 1];
                        }
                        String fileName = lineArr[2];
                        fileName = fileName.replaceAll("^\"|\"$", "");
                        fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                        
                        // receive file
                        byte[] mybytearray  = new byte [FILE_SIZE];
                        istream.read(mybytearray,0,mybytearray.length);
                        
                        //Sending a file
                        switch (operation) {
                            case "broadcast":
                                broadcast(fileName, mybytearray);
                                break;
                            case "blockcast":
                                blockcast(specificUserName, fileName, mybytearray);
                                break;
                            case "unicast":
                                unicast(specificUserName, fileName, mybytearray);
                                break;
                            default:
                                break;
                        }
                        
                    }else{
                        //Regular Text operations
                        switch (operation) {
                            case "broadcast":
                                broadcast(line);
                                break;
                            case "blockcast":
                                blockcast(line);
                                break;
                            case "unicast":
                                unicast(line);
                                break;
                            default:
                                break;
                        }
                    }
                    
                    if (line.startsWith("quit")) {
                        break;
                    }
                }
                }
                ostream.println("See you later! " + name);
                System.out.println(userName + " left!");
            
                istream.close();
                ostream.close();
                clientSock.close();
            }
        } catch (IOException e) {
                    
        }
    }
    
    public void broadcast(String s){
        String message = s.replaceFirst("broadcast", "@" + this.userName + ":");
        
        allUsers.keySet().forEach((key) -> {
            if(key.equals(this.userName)){
                allUsers.get(key).ostream.println("Message Sent");
            }else{
                allUsers.get(key).ostream.println(message);
            }
        });
        System.out.println(this.userName + " braodcasted message");
    }
    
    public void broadcast(String fileName, byte[] byteArray){
        String message = "File " + fileName + " by " + this.userName;
        
        allUsers.keySet().forEach((key) -> {
            if(key.equals(this.userName)){
                allUsers.get(key).ostream.println("Message Sent");
            }else{
                allUsers.get(key).ostream.println(message);
                allUsers.get(key).ostream.write(byteArray,0,byteArray.length);
            }
        });
        System.out.println(this.userName + " braodcasted file " + fileName);
    }
    
    public void blockcast(String s){
        
        String blockcastUser = s.substring(s.lastIndexOf(" ") + 1);
        String message = s.replaceFirst("blockcast", "@" + userName + ":");
        message = message.substring(0, message.lastIndexOf(blockcastUser));
        
        for(String key : allUsers.keySet()){
            if(key.equals(blockcastUser)){
                // Do nothing for this user.
            }else if(key.equals(userName)){
                allUsers.get(key).ostream.println("Message Sent");
            }else{
                allUsers.get(key).ostream.println(message);
            }
        }
        System.out.println(userName + " blockcast message excluding [" + blockcastUser + "]");
    }
    
    public void blockcast(String blockcastUser, String fileName, byte[] byteArray){
        String message = "File " + fileName + " by " + this.userName;
        
        allUsers.keySet().forEach((key) -> {
            if(key.equals(blockcastUser)){
                // Do nothing for this user.
            }else if(key.equals(userName)){
                allUsers.get(key).ostream.println("Message Sent");
            }else{
                allUsers.get(key).ostream.println(message);
                allUsers.get(key).ostream.write(byteArray,0,byteArray.length);
            }
        });
        System.out.println(userName + " blockcast file " + fileName + " excluding [" + blockcastUser + "]");
    }
    
    public void unicast(String s){
        String unicastUser = s.substring(s.lastIndexOf(" ") + 1);
        String message = s.replaceFirst("unicast", "@" + userName + ":");
        message = message.substring(0, message.lastIndexOf(unicastUser));
        
        if(allUsers.containsKey(unicastUser)) allUsers.get(unicastUser).ostream.println(message);
        allUsers.get(userName).ostream.println("Message Sent");
        System.out.println(userName + " unicast message to [" + unicastUser + "]");
    }
    
    public void unicast(String unicastUser, String fileName, byte[] byteArray){
        String message = "File " + fileName + " by " + this.userName;
        
        if(allUsers.containsKey(unicastUser)){
            allUsers.get(unicastUser).ostream.println(message);
            allUsers.get(unicastUser).ostream.write(byteArray,0,byteArray.length);
        }
        allUsers.get(userName).ostream.println("Message Sent");
        System.out.println(userName + " unicast file " + fileName + " to [" + unicastUser + "]");
    }
    
    public boolean isInteger(String str) {
        int size = str.length();
        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return size > 0;
    }
}