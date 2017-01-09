import java.io.File;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
//import java.net.*;
/**
 *
 * @author SIAKI
 */
public class Client{
      // The socket
    static Socket sock = null;
    static PrintStream pwrite = null;
    static InputStream istream = null;
    static OutputStream ostream = null;
    
    static BufferedReader keyRead = null;
    static DataInputStream recieveRead = null;
    static boolean closed = false;
    
    
    static int FILE_SIZE = 6022386;
    static String userName = "client" + (int)(Math.random() *100);
    
    static String home_dir = System.getProperty("user.dir");
    
    public static void main(String[] args) throws Exception{
        int portNumber = 3000;
        
        if(args.length > 0) userName = args[0];
        if(args.length > 1) portNumber = Integer.valueOf(args[1]);
        
        sock = new Socket("localhost", portNumber);

        ostream = sock.getOutputStream(); 
        pwrite = new PrintStream(ostream);
        pwrite.println("create " + userName);
        pwrite.flush();
        pwrite.println();
 
        istream = sock.getInputStream();
        recieveRead = new DataInputStream(istream);
        keyRead = new BufferedReader(new InputStreamReader(System.in));
        
        RecieveAndPrint print = new RecieveAndPrint();
        print.start();
        
        ReadAndSend read = new ReadAndSend();
        read.start();
    }
    
    static class ReadAndSend extends Thread{       
        @Override
        public void run(){
            String sendMessage;
            while(true){
                try{
                    sendMessage = keyRead.readLine();
                    String[] sendMessageArr = sendMessage.replaceAll(" +", " ").trim().split(" ");
                    //Sending a file
                    if(sendMessageArr.length > 1 && sendMessageArr[1].toLowerCase().equals("file")){
                        String filePathToSend = sendMessageArr[2].replaceAll("^\"|\"$", "");
                        File myFile = new File(filePathToSend);
                        if(myFile.exists() && !myFile.isDirectory()) { 
                            byte [] mybytearray  = new byte [(int)myFile.length()];
                            FileInputStream fistream = new FileInputStream(myFile);
                            BufferedInputStream bistream = new BufferedInputStream(fistream);
                            bistream.read(mybytearray,0,mybytearray.length);
                            //System.out.println("original size: " + myFile.length() + ";file size : " + mybytearray.length);
                        
                            pwrite.println(sendMessage.trim().replaceAll(" +", " ") + " " + mybytearray.length);
                            ostream.write(mybytearray,0,mybytearray.length);
                        }else{
                            System.out.println("File not found at " + filePathToSend);
                        }
                    }else{
                        pwrite.println(sendMessage);
                    }
                    pwrite.flush();
                    if(sendMessage.equals("quit")) break;
                } catch(IOException e){
                    
                }
            }
        }
    }
    
    static class RecieveAndPrint extends Thread{
        @Override
        public void run() {
            /*
            * Keep on reading from the socket till we receive "Bye" from the
            * server. Once we received that then we want to break.
            */
            String receiveMessage;
            while(true) {
                try{
                    receiveMessage = recieveRead.readLine();
                    if(receiveMessage.startsWith("quit")) break;
                    if(receiveMessage.split(" ")[0].toLowerCase().equals("file")){
                        // receive file
                        String fileName = receiveMessage.split(" ")[1];
                        byte [] mybytearray  = new byte [FILE_SIZE];
                        
                        
                        File dir = new File(home_dir + "\\" + userName);
                        if(!dir.exists()) dir.mkdir();
                        
                        try (FileOutputStream fostream = new FileOutputStream(home_dir + "\\" + userName + "\\" + fileName )) {
                            BufferedOutputStream bostream = new BufferedOutputStream(fostream);
                            int bytesRead = istream.read(mybytearray,0,mybytearray.length);
                            int current = bytesRead;
                            
                            bostream.write(mybytearray, 0 , current);
                            System.out.println(receiveMessage);
                            bostream.flush();
                        }
                    }else{
                        System.out.println(receiveMessage);
                    }
                } catch(IOException e) {
                    System.err.println("IOException:  " + e);
                } catch(NullPointerException n){
                    break;
                }
            }
        }
    }
}
