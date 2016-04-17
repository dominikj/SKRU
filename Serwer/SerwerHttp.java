package Serwer;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SerwerHttp extends Serwer implements Runnable {
	
		private Socket remote;
		private String documentHtml;
		
		SerwerHttp(Socket remote, String doc){
			this.documentHtml = doc;
			this.remote = remote;
		}
	@Override
	public void run() {
		
	        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	        Date date = new Date();
	        System.out.print("[" + dateFormat.format(date) + "]"); 
	        
	        System.out.println("Połączono z: " + remote.getInetAddress().toString());
	        try{
	        BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
	        PrintWriter out = new PrintWriter(remote.getOutputStream());
	        
	        ArrayList<String> header = readHeader(in);
	        
	        String[] tmp = header.get(0).split(" ");

	    	System.out.println(tmp[1]);
	    	
	    	Path path;
	    	
	    	
	    		if (tmp[1].equals("/")){
	    			 path = Paths.get("./index.html");
		    	}
	    		else
	    			 path = Paths.get("." + tmp[1]);
	    	
	    	String mimeType =  Files.probeContentType(path);
	    	
	    	try{
	    	 if (mimeType.split("/")[0].equals("text")){
	    	 sendPacket(mimeType, Files.readAllBytes(path), true, out); 
	    	}
	    	else
	    		 sendPacket(mimeType, Files.readAllBytes(path), false, out); 
	    	}
	    	catch (Exception e){
	    		e.printStackTrace();
	        	out.println("HTTP/1.0 404 Not Found");
	        	out.println("Connection: Closed");
	        	out.println("Content-Type: text/html");
		        out.println("Server: Kutas");
		        out.println("");
		        out.println("<h1>404 - Not Found</h1>");
		        out.flush();
	    	}
	    	finally{
	        remote.close();
	    	}
	        
	        }
	       catch (Exception e){
	    	   e.printStackTrace();
	       }
	    }
	
	ArrayList<String> readHeader(BufferedReader buf){
		ArrayList<String> tmp = new ArrayList<String>();
		 String str = ".";
	        while (!str.equals(""))
				try {
					System.out.println((str = buf.readLine()));
					tmp.add(str);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	         System.out.println("zakończono odczyt nagłówka");
	 		return tmp;
	}
	
	void sendPacket(String mimeType, byte[] data, boolean text, PrintWriter out){
			out.println("HTTP/1.0 200 OK");
	        out.println("Content-Type: " + mimeType );
	        out.println("Server: Kutas");
        	out.println("Content-Length: " + data.length);
	        out.println("");
	        
	        if(text){
	        	   out.println(new String(data, Charset.defaultCharset()));
	        }
	        else {
	        	   out.flush();
	        	System.out.println("ROZM: " + data.length);
	        	try {
	            	DataOutputStream dOut = new DataOutputStream(remote.getOutputStream());
					dOut.write(data);
		        	dOut.flush();
		        	dOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        
	        }
	        out.flush();
	}

	  }
		
	

