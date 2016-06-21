package serverskiDeo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

public class ServerNit extends Thread {

	ServerSocket serverSoket;
	int port;
	static int brojac = 0;
	String ipAdresa;

	public ServerNit(int port,String ipadresa) {
		super();
		this.port = port;
		this.ipAdresa=ipadresa;
	}

	@Override
	public void run() {
		
		try {
			PortMapping p = new PortMapping(7879,ipAdresa,PortMapping.Protocol.TCP,ipAdresa);
			UpnpService upnS = new UpnpServiceImpl(new PortMappingListener(p));
			upnS.getControlPoint().search();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverSoket = new ServerSocket(port);
			Socket klijentSoket;
			
			while (true) {
				
				klijentSoket = serverSoket.accept();
				KlijentServerNit klijentServer = new KlijentServerNit(klijentSoket);
				klijentServer.start();
//				try {
//					new KlijentServerNit(serverSoket.accept()).start();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					upnS.shutdown();
//					e.printStackTrace();
//				} 
			}
			

		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}
