package Serwer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Serwer {

	public static void main(String[] args) {
		
		ServerSocket serv;

	    System.out.println("Uruchomiono serwer na porcie 80");
	    try {
	      serv = new ServerSocket(8080);
	    } catch (Exception e) {
	      System.out.println("Błąd: " + e);
	      return;
	    }
	    		  byte[] encoded;
	    		  String document = "<h2> Error - no file index.html </h2>";
				try {
					encoded = Files.readAllBytes(Paths.get("./index.html"));
		    	    document = new String(encoded,  Charset.defaultCharset());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    System.out.println("Czekam na połączenia");
		while(true){
		  	  try {
				new Thread(new SerwerHttp(serv.accept(), document)).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	 
		}

	}

}
