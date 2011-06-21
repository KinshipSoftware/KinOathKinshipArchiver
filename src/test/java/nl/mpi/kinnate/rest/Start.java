package nl.mpi.arbil.wicket;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start {

    private final static Logger logger = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) throws Exception {
	Server server = new Server();
	SocketConnector connector = new SocketConnector();

	// Set some timeout options to make debugging easier.
	connector.setMaxIdleTime(1000 * 60 * 60);
	connector.setSoLingerTime(-1);
	connector.setPort(8081);
	server.setConnectors(new Connector[]{connector});

	WebAppContext bb = new WebAppContext();
	bb.setServer(server);
	bb.setContextPath("/");
	bb.setWar("src/main/webapp");

	// START JMX SERVER
	// MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	// MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
	// server.getContainer().addEventListener(mBeanContainer);
	// mBeanContainer.start();

	server.addHandler(bb);

	try {
	    System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
	    server.start();
	    System.in.read();
	    System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
	    // while (System.in.available() == 0) {
	    //   Thread.sleep(5000);
	    // }
	    server.stop();
	    server.join();
	} catch (Exception e) {
	    logger.error(null, e);
	    System.exit(100);
	}
    }
}
