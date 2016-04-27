package Serwer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SerwerGeo implements Runnable {
	
	private Socket remote;
	
	SerwerGeo(Socket remote){
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
	        
	        ArrayList<String> message = readMessage(in);
	        ArrayList<String> tmp = new ArrayList<String>();
	        tmp.add("var myCenter=new google.maps.LatLng(" +message.get(2).split(" ")[1] + "," +message.get(3).split(" ")[1] + ");");
	        writeTofile("coordinates.js", tmp, false);
	        
	        tmp.clear();
	        tmp.add("<br />");
	        tmp.add("<h2>" + dateFormat.format(date) + "</h2>");
	        
	        for(int i = 4; i <message.size(); ++i){
	        	tmp.add(message.get(i));
	        }
	        
	        writeTofile("logi.html", tmp, true);
	        
	        out.println("OK");
	        out.flush();
	        remote.close();
	        
	        } catch(Exception e){
	        	e.printStackTrace();
	        }
		
	}
	ArrayList<String> readMessage(BufferedReader buf){ // TODO: dołożyć timeout, ale po testach
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
	         System.out.println("zakończono odczyt wiadomości");
	 		return tmp;
	}
	
	void writeTofile(String filename, ArrayList<String> data, boolean append){
		try(FileWriter fw = new FileWriter(filename, append);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
			   for(String d : data){
				   out.println(d);
			   }
			} catch (IOException e) {
			   e.printStackTrace();
			}
	}

}
