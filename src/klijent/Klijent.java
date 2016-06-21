package klijent;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import serverskiDeo.DownloadingNit;
import serverskiDeo.ServerNit;

public class Klijent {

	static Socket soketZaCentralniServer = null;
	static BufferedReader ulazniTokOdCentralnogServera = null;
	static PrintStream izlazniTokKaCentralnomServeru = null;
	static BufferedReader ulazKonzola = null;
	public static LinkedList<String> listaFajlovaZaSeedovanje = new LinkedList<String>();

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		try {
			soketZaCentralniServer = new Socket("95.180.62.192", 9876);
			ulazKonzola = new BufferedReader(new InputStreamReader(System.in));
			String[] ipAdresa = InetAddress.getLocalHost().toString().split("/");
			ServerNit server = new ServerNit(7878,ipAdresa[1]);
			server.start();

			ulazniTokOdCentralnogServera = new BufferedReader(
					new InputStreamReader(soketZaCentralniServer.getInputStream()));
			izlazniTokKaCentralnomServeru = new PrintStream(soketZaCentralniServer.getOutputStream());

			System.out.println(ulazniTokOdCentralnogServera.readLine());

			String osnovniPodaci = "";
			String nasaTrenutnaIPAdresa = ((InetSocketAddress) soketZaCentralniServer.getLocalSocketAddress())
					.getHostString();
			File file = new File("src/klijent/adrese.txt");
			// System.out.println(file.getAbsolutePath());
			BufferedReader citac = new BufferedReader(new FileReader(file.getAbsolutePath())); //za proveru trenutne ip adrese u odnosu na proslu
			PrintWriter fajlUpisivanje = new PrintWriter(new FileWriter(file.getAbsolutePath()));

			if (file.length() == 0) { // prazan
				osnovniPodaci = "nov" + "#" + nasaTrenutnaIPAdresa;
				fajlUpisivanje.println(nasaTrenutnaIPAdresa);
				fajlUpisivanje.close();

			} else {
				String proslaAdresa = citac.readLine();
				osnovniPodaci = proslaAdresa + "#" + nasaTrenutnaIPAdresa;
				fajlUpisivanje.print(nasaTrenutnaIPAdresa);
				fajlUpisivanje.close();
			}

			izlazniTokKaCentralnomServeru.println(osnovniPodaci);

			citac.close();
			System.out.println(ulazniTokOdCentralnogServera.readLine()); // da
																			// li
																			// zelite
																			// da
																			// seedujete
			String daLiSeedujemo = ulazKonzola.readLine();
			while (true) {
				if (daLiSeedujemo.toUpperCase().equals("DA") || daLiSeedujemo.toUpperCase().equals("NE")) {
					izlazniTokKaCentralnomServeru.println(daLiSeedujemo);
					break;
				} else {
					System.out.println("Nepravilan unos: unesite DA ili NE!");
					daLiSeedujemo = ulazKonzola.readLine();
				}
			}

			if (daLiSeedujemo.toUpperCase().equals("DA")) {

				boolean daLiJeKraj = false;
				while (!daLiJeKraj) {
					System.out.println(ulazniTokOdCentralnogServera.readLine()); // unesite
																					// putanju
					while (true) {
						String putanja = ulazKonzola.readLine();
						listaFajlovaZaSeedovanje.add(putanja);
						File fajlZaSeed = new File(putanja);
						if (fajlZaSeed.exists()) {
							String nazivFajla = fajlZaSeed.getName();
							// System.out.println(nazivFajla);
							long velicinaFajla = fajlZaSeed.length();
							String checksum = "";
							try {
								checksum = MD5Checksum.getMD5Checksum(fajlZaSeed);

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							System.out.println("Da li zelite da seed-ujete jos neki fajl?");

							String nastavakSedovanja = ulazKonzola.readLine();
							while (true) {
								if (nastavakSedovanja.toUpperCase().equals("DA")
										|| nastavakSedovanja.toUpperCase().equals("NE")) {
									break;
								} else {
									System.out.println("Nepravilan unos: unesite DA ili NE!");
									nastavakSedovanja = ulazKonzola.readLine();
								}
							}
							if (nastavakSedovanja.toUpperCase().equals("NE")) {
								daLiJeKraj = true;
							}

							izlazniTokKaCentralnomServeru.println(
									nazivFajla + "#" + velicinaFajla + "#" + checksum + "#" + nastavakSedovanja);
							break;

						} else {
							System.out.println("Nepravilan unos, ponovo unesite putanju!");
						}
					}

				}

			}

			boolean kraj = false;
			while (!kraj) {
				System.out.println(ulazniTokOdCentralnogServera.readLine()); //unesite naziv fajla za download
				String unos = ulazKonzola.readLine();
				izlazniTokKaCentralnomServeru.println(unos);
				if (unos.equals("/quit")) {
					System.out.println(ulazniTokOdCentralnogServera.readLine());
					soketZaCentralniServer.close();
					citac.close();
					fajlUpisivanje.close();
					return;
				}
				String promenljiva = ulazniTokOdCentralnogServera.readLine();
				if(promenljiva.equals("NE")) {
					System.out.println("Trazeni fajl ne postoji!");
					continue;
				}

				System.out.println("Izaberite redni broj fajla koji zelite da download-ujete!");
				String[] odgovarajuciFajlovi = promenljiva.split("\\#");
				String nazivFajlaZaDownload = "";
				for (int i = 0; i < odgovarajuciFajlovi.length; i++) {
					String[] podaciOFajlu = odgovarajuciFajlovi[i].split("/");
					System.out.println(podaciOFajlu[0] + "	" + podaciOFajlu[1] + "	" + podaciOFajlu[2] + "	"
							+ podaciOFajlu[3] + "\n");
				}
				
				String klijentovIzbor = ulazKonzola.readLine();
				for (int i = 0; i < odgovarajuciFajlovi.length; i++) {
					String[] podaciOFajlu = odgovarajuciFajlovi[i].split("/");
					if(klijentovIzbor.equals(podaciOFajlu[0])) {
						nazivFajlaZaDownload = podaciOFajlu[1];
					}
				}

				izlazniTokKaCentralnomServeru.println(klijentovIzbor); // klijentov
																				// izbor
																				// r.br.
				String spisakSegmenataIAdresa = ulazniTokOdCentralnogServera.readLine();
				String[] ipAdresePoSegmentima = spisakSegmenataIAdresa.split("\\#");
				FileOutputStream output = new FileOutputStream("D://" + nazivFajlaZaDownload, true);
				//System.out.println(ipAdresePoSegmentima);
				
				int downloadovaniSegmenti = 0;
				for (int i = 0; i < ipAdresePoSegmentima.length;) {
					
					if((ipAdresePoSegmentima.length - downloadovaniSegmenti) % 4 == 0 || ipAdresePoSegmentima.length - downloadovaniSegmenti > 4) {
						
						String[] prvaNit = ipAdresePoSegmentima[i].split(":"); //brojseg ipa1/ipa2
						String[] drugaNit = ipAdresePoSegmentima[i + 1].split(":"); 
						String[] trecaNit = ipAdresePoSegmentima[i + 2].split(":"); 
						String[] cetvrtaNit = ipAdresePoSegmentima[i + 3].split(":");

						byte[] glavniBafer = new byte[4*3000];

						ExecutorService service = Executors.newSingleThreadExecutor();

						DownloadingNit nit1 = new DownloadingNit(Integer.parseInt(prvaNit[0]), prvaNit[1].split("/"), nazivFajlaZaDownload);
						DownloadingNit nit2 = new DownloadingNit(Integer.parseInt(drugaNit[0]), drugaNit[1].split("/"), nazivFajlaZaDownload);
						DownloadingNit nit3 = new DownloadingNit(Integer.parseInt(trecaNit[0]), trecaNit[1].split("/"), nazivFajlaZaDownload);
						DownloadingNit nit4 = new DownloadingNit(Integer.parseInt(cetvrtaNit[0]), cetvrtaNit[1].split("/"), nazivFajlaZaDownload);

						Future<?> f1 = service.submit(nit1);
						Future<?> f2 = service.submit(nit2);
						Future<?> f3 = service.submit(nit3);
						Future<?> f4 = service.submit(nit4);


						while(true) {
							int manje = -1;
							if(f1.isDone()) {
								//upisati na odgovarajuce mesto u glavnom baferu
								byte[] bafer = (byte[]) f1.get();
								for (int j = 0; j < bafer.length; j++) {
									glavniBafer[j] = bafer[j];
								}
								//System.out.println("Stigao paket 1");
							}
							
							if(f2.isDone()) {
								byte[] bafer = (byte[]) f2.get();
								for (int j = 0; j < bafer.length; j++) {
									glavniBafer[j+3000] = bafer[j];
								}
								//System.out.println("Stigao paket 2");
							}
							
							if(f3.isDone()) {
								byte[] bafer = (byte[]) f3.get();
								for (int j = 0; j < bafer.length; j++) {
									glavniBafer[j + 6000] = bafer[j];
								}
								//System.out.println("Stigao paket 3");
							}
							
							if(f4.isDone()) {
								byte[] bafer = (byte[]) f4.get();
								for (int j = 0; j < bafer.length; j++) {
									glavniBafer[j + 9000] = bafer[j];
								}
								//System.out.println("Stigao paket 4");
								if(bafer.length<3000){
								manje = bafer.length;	
								}
							}
							
							if(f1.isDone() && f2.isDone() && 
									f3.isDone() && f4.isDone()) {
								if(manje!=-1){
									byte[] noviBafer = new byte[9000+manje];
									for (int j = 0; j < noviBafer.length; j++) {
										noviBafer[j]=glavniBafer[j];
									}
									output.write(noviBafer);
								}else{
								output.write(glavniBafer);}
								System.out.println("Prebaceno na fajl");
								downloadovaniSegmenti+=4;
								i+=4;
								break;
							}
						}
						
					}
					
					if((ipAdresePoSegmentima.length - downloadovaniSegmenti) % 4 == 3) {
						
						String[] prvaNit = ipAdresePoSegmentima[i].split(":"); //brojseg ipa1/ipa2
						String[] drugaNit = ipAdresePoSegmentima[i + 1].split(":"); 
						String[] trecaNit = ipAdresePoSegmentima[i + 2].split(":"); 
						

						byte[] glavniBafer = new byte[3*3000];

						ExecutorService service = Executors.newSingleThreadExecutor();

						DownloadingNit nit1 = new DownloadingNit(Integer.parseInt(prvaNit[0]), prvaNit[1].split("/"), nazivFajlaZaDownload);
						DownloadingNit nit2 = new DownloadingNit(Integer.parseInt(drugaNit[0]), drugaNit[1].split("/"), nazivFajlaZaDownload);
						DownloadingNit nit3 = new DownloadingNit(Integer.parseInt(trecaNit[0]), trecaNit[1].split("/"), nazivFajlaZaDownload);

						Future<?> f1 = service.submit(nit1);
						Future<?> f2 = service.submit(nit2);
						Future<?> f3 = service.submit(nit3);


						while(true) {
							int manje = -1;
							if(f1.isDone()) {
								//upisati na odgovarajuce mesto u glavnom baferu
								byte[] bafer = (byte[]) f1.get();
								for (int j = 0; j < bafer.length; j++) {
									glavniBafer[j] = bafer[j];
								}
								//System.out.println("Stigao paket 1/1");
							}
							
							if(f2.isDone()) {
								byte[] bafer = (byte[]) f2.get();
								for (int j = 0; j < bafer.length; j++) {
									glavniBafer[j+3000] = bafer[j];
								}
								//System.out.println("Stigao paket 2/1");
							}
							
							if(f3.isDone()) {
								byte[] bafer = (byte[]) f3.get();
								for (int j = 0; j < bafer.length; j++) {
									glavniBafer[j + 6000] = bafer[j];
								}
								if(bafer.length<3000){
									manje=bafer.length;
								}
								//System.out.println("Stigao paket 3/1");
							}
							
							
							if(f1.isDone() && f2.isDone() && 
									f3.isDone()) {
								if(manje!=-1){
									byte[] noviBafer = new byte[6000+manje];
									for (int j = 0; j < noviBafer.length; j++) {
										noviBafer[j]=glavniBafer[j];
									}
									output.write(noviBafer);
								}else{
								output.write(glavniBafer);}
								System.out.println("Prebaceno na fajl 1");
								downloadovaniSegmenti+=3;
								i+=3;
								break;
							}
						}
						
					}
					
					if((ipAdresePoSegmentima.length - downloadovaniSegmenti) % 4 == 2) {
						
						String[] prvaNit = ipAdresePoSegmentima[i].split(":"); //brojseg ipa1/ipa2
						String[] drugaNit = ipAdresePoSegmentima[i + 1].split(":"); 
						

						byte[] glavniBafer = new byte[2*3000];

						ExecutorService service = Executors.newSingleThreadExecutor();

						DownloadingNit nit1 = new DownloadingNit(Integer.parseInt(prvaNit[0]), prvaNit[1].split("/"), nazivFajlaZaDownload);
						DownloadingNit nit2 = new DownloadingNit(Integer.parseInt(drugaNit[0]), drugaNit[1].split("/"), nazivFajlaZaDownload);
						

						Future<?> f1 = service.submit(nit1);
						Future<?> f2 = service.submit(nit2);


						while(true) {
							int manje=-1;
							if(f1.isDone()) {
								//upisati na odgovarajuce mesto u glavnom baferu
								byte[] bafer = (byte[]) f1.get();
								for (int j = 0; j < bafer.length; j++) {
									glavniBafer[j] = bafer[j];
								}//System.out.println("Stigao paket 1/2");
							}
							
							if(f2.isDone()) {
								byte[] bafer = (byte[]) f2.get();
								for (int j = 0; j < bafer.length; j++) {
									glavniBafer[j+3000] = bafer[j];
								}
								if(bafer.length<3000){
									manje=bafer.length;
								}
								//System.out.println("Stigao paket 2/2");
							}
							
							if(f1.isDone() && f2.isDone()) {
								if(manje!=-1){
									byte[] noviBafer = new byte[3000+manje];
									for (int j = 0; j < noviBafer.length; j++) {
										noviBafer[j]=glavniBafer[j];
									}
									output.write(noviBafer);
								}else{
								output.write(glavniBafer);}
								System.out.println("Prebaceno na fajl 2");
								downloadovaniSegmenti+=2;
								i+=2;
								break;
							}
						}
						
					}
					
					if((ipAdresePoSegmentima.length - downloadovaniSegmenti) % 4 == 1) {
						
						String[] prvaNit = ipAdresePoSegmentima[i].split(":"); //brojseg ipa1/ipa2
						
						ExecutorService service = Executors.newSingleThreadExecutor();

						DownloadingNit nit1 = new DownloadingNit(Integer.parseInt(prvaNit[0]), prvaNit[1].split("/"), nazivFajlaZaDownload);
						

						Future<?> f1 = service.submit(nit1);
						


						while(true) {
							
							if(f1.isDone()) {
								//upisati na odgovarajuce mesto u glavnom baferu
								byte[] bafer = (byte[]) f1.get();
								//System.out.println("Stigao paket 1/3");
								output.write(bafer);
								System.out.println("Prebaceno na fajl 3");
								downloadovaniSegmenti++;
								i++;
								break;
							}
							
						}
						
					}
					
					
					
				}
				
				izlazniTokKaCentralnomServeru.println("OK");
				listaFajlovaZaSeedovanje.add("D://"+nazivFajlaZaDownload);
				
				output.close();
				
				//javiti centralnom servisu da seedujemo jos jedan fajl

			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}

