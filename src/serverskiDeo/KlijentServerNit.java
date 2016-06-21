package serverskiDeo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import klijent.Klijent;

public class KlijentServerNit extends Thread {
	
	Socket soketZaKOmunikaciju;
	BufferedReader ulazniLinijskiTok;
	PrintStream izlazniLinjskiTok;
	

	public KlijentServerNit(Socket soketZaKOmunikaciju) {
		super();
		this.soketZaKOmunikaciju = soketZaKOmunikaciju;
	}
	
	@Override
	public void run() {
		ServerNit.brojac++;
		
		
		try {
			ulazniLinijskiTok = new BufferedReader(new InputStreamReader(soketZaKOmunikaciju.getInputStream()));
			izlazniLinjskiTok = new PrintStream(soketZaKOmunikaciju.getOutputStream());
			
			if(ServerNit.brojac > 4) {
				izlazniLinjskiTok.println("NE");
				ServerNit.brojac--;
				return;
			}
			
			izlazniLinjskiTok.println("DA");
			String nazivFajla = ulazniLinijskiTok.readLine();
			System.out.println("Stigao naziv");
			int redniBrojSegmenta = Integer.parseInt(ulazniLinijskiTok.readLine());
			System.out.println("stigao br segmenta");
			
			String putanja = "";
			for (int i = 0; i < Klijent.listaFajlovaZaSeedovanje.size(); i++) { //pronalazenje fajla u listi fajlova koje seedujemo
				if(Klijent.listaFajlovaZaSeedovanje.get(i).endsWith(nazivFajla)) {
					putanja = Klijent.listaFajlovaZaSeedovanje.get(i);
					break;
				}
			}
			
			Path path = Paths.get(putanja); // kreiranje putanje
			System.out.println("nadjen faj na putanji "+putanja);
			byte[] data = Files.readAllBytes(path); // prevodjenje fajla u niz bajtova
			System.out.println("pretvoren niz bajtova velicine"+data.length);
			int velicinaZaSlanje = 0;
			if(redniBrojSegmenta*3000<data.length){
				velicinaZaSlanje=3000;
			}else{
				velicinaZaSlanje=data.length-((redniBrojSegmenta-1)*3000);
			}
			izlazniLinjskiTok.println(velicinaZaSlanje);
			System.out.println("poslata velicina");
			
		//	DataOutputStream dOut = new DataOutputStream(soketZaKOmunikaciju.getOutputStream());
		//	ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			OutputStream dOut = soketZaKOmunikaciju.getOutputStream();
			if (redniBrojSegmenta == 1 && data.length < 3000) {
				System.out.println("salje se prvi segment manji od nule");
				dOut.write(data, 0, data.length);
				//bOut.write(data,0,data.length);
				//bOut.writeTo(soketZaKOmunikaciju.getOutputStream());
				System.out.println("poslat");
			} else if(redniBrojSegmenta == 1) {
				System.out.println("salje se prvi segment");
				dOut.write(data, 0, 3000);
				//bOut.write(data,0,3000);
				//bOut.writeTo(soketZaKOmunikaciju.getOutputStream());
				System.out.println("poslat prvi");
			} else if((redniBrojSegmenta * 3000) > data.length) {
				System.out.println("salje se zadnji segment");
				dOut.write(data, (redniBrojSegmenta -1)*3000,  data.length-((redniBrojSegmenta-1)*3000));
				//bOut.write(data, (redniBrojSegmenta -1)*3000,  data.length-((redniBrojSegmenta-1)*3000));
				//bOut.writeTo(soketZaKOmunikaciju.getOutputStream());
				System.out.println("poslat");
			} else {
				System.out.println("salje se obican segment");
				dOut.write(data, (redniBrojSegmenta -1)*3000, 3000);
				//bOut.write(data, (redniBrojSegmenta -1)*3000, 3000);
				//bOut.writeTo(soketZaKOmunikaciju.getOutputStream());
				System.out.println("poslat");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		ServerNit.brojac--; //azurirati brojac
	}
	
}
