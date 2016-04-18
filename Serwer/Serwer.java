package Serwer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Serwer {

	public static void main(String[] args) {
	//TODO: Naprawić odsyłanie feedbacku, wprowadzić timeouty, zabezpieczyć przed dostępem do katalogu nadrzędnego	
		ServerSocket serv;

	    System.out.println("Uruchomiono serwer na porcie 80 i 22029");
	    try {
	      serv = new ServerSocket(8080);
	    } catch (Exception e) {
	      System.out.println("Błąd: " + e);
	      return;
	    }
	    new Thread(new Runnable(){

			@Override
			public void run() {
			    System.out.println("Czekam na połączenia Geo");

				ServerSocket serv = null;
			      try {
					serv = new ServerSocket(22029);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				while(true){
				  	  try {
						new Thread(new SerwerGeo(serv.accept())).start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				  	 
				}
				
			}
	    	
	    }).start();
	    
	    System.out.println("Czekam na połączenia Http");
		while(true){
		  	  try {
				new Thread(new SerwerHttp(serv.accept())).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	 
		}

	}

}
