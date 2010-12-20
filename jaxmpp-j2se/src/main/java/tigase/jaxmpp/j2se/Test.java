package tigase.jaxmpp.j2se;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

public class Test {

	public static void main(String[] args) throws Exception {
		Logger logger = Logger.getLogger("tigase.jaxmpp");
		// create a ConsoleHandler
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);

		Jaxmpp jaxmpp = new Jaxmpp();
		// for BOSH connector
		jaxmpp.getProperties().setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL, "http://127.0.0.1:5280");
		// jaxmpp.getProperties().setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL,
		// "http://messenger.tigase.org:80/bosh");
		// for Socket connector
		jaxmpp.getProperties().setUserProperty(SocketConnector.SERVER_HOST, "tigase.tigase.org");
		// port value is not necessary. Default is 5222
		jaxmpp.getProperties().setUserProperty(SocketConnector.SERVER_PORT, 5222);

		// "bosh" and "socket" values available
		jaxmpp.getProperties().setUserProperty(Jaxmpp.CONNECTOR_TYPE, "bosh");

		jaxmpp.getProperties().setUserProperty(SessionObject.USER_JID, JID.jidInstance(args[0]));
		jaxmpp.getProperties().setUserProperty(SessionObject.PASSWORD, args[1]);

		System.out.println("// login");
		// not necessary. it allows to set own status on sending initial
		// presence
		jaxmpp.getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.BeforeInitialPresence,
				new Listener<PresenceEvent>() {

					@Override
					public void handleEvent(PresenceEvent be) {
						be.cancel();
						be.setPriority(-1);
						be.setStatus("jaxmpp2 based Bot!");
						be.setShow(Show.away);
					}
				});

		// listener of incoming messages
		jaxmpp.getModulesManager().getModule(MessageModule.class).addListener(MessageModule.MessageReceived,
				new Listener<MessageModule.MessageEvent>() {

					@Override
					public void handleEvent(MessageEvent be) {
						try {
							System.out.println("Received message: " + be.getMessage().getAsString());
						} catch (XMLException e) {
							e.printStackTrace();
						}
					}
				});

		final long t1 = System.currentTimeMillis();
		jaxmpp.login(true);
		System.out.println(" CONNECTED; secure=" + jaxmpp.isSecure());

		// ping example
		IQ pingIq = IQ.create();
		pingIq.setTo(JID.jidInstance("tigase.org"));
		pingIq.setType(StanzaType.get);
		pingIq.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));
		jaxmpp.send(pingIq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				System.out.println("Ping Error response " + error);
			}

			@Override
			public void onSuccess(Stanza responseStanza) throws XMLException {
				System.out.println("PONG");
			}

			@Override
			public void onTimeout() throws XMLException {
				System.out.println("No ping response");
			}
		});

		Thread.sleep(1000 * 120);

		// jaxmpp.getPresence().setPresence(null, "Bot changed status", 1);

		// jaxmpp.sendMessage(JID.jidInstance("bmalkow@malkowscy.net"), "Test",
		// "Wiadomosc ");

		// Thread.sleep(1000 * 15);

		System.out.println("// disconnect");
		jaxmpp.disconnect();
		final long t2 = System.currentTimeMillis();

		System.out.println(". " + (t2 - t1) + " ms");
	}
}
