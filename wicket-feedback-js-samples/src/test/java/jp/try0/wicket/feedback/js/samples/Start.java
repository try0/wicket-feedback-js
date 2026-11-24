package jp.try0.wicket.feedback.js.samples;

import org.eclipse.jetty.ee11.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;

/**
 * Starts the samples on http://localhost:8080. Run with the samples module as the working directory.
 *
 * @author try0
 *
 */
public class Start {

	public static void main(String[] args) throws Exception {
		System.setProperty("wicket.configuration", "development");

		Server server = new Server(Integer.getInteger("port", 8080));
		WebAppContext context = new WebAppContext();
		context.setServer(server);
		context.setContextPath("/");
		context.setWar("src/main/webapp");
		server.setHandler(context);
		server.start();
		server.join();
	}

}
