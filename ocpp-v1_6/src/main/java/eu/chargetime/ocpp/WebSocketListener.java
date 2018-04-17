package eu.chargetime.ocpp;
/*
    ChargeTime.eu - Java-OCA-OCPP
    
    MIT License

    Copyright (C) 2016-2018 Thomas Volden <tv@chargetime.eu>

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

import eu.chargetime.ocpp.model.SessionInformation;
import eu.chargetime.ocpp.wss.WssFactoryBuilder;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;

public class WebSocketListener implements Listener {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketListener.class);
    private final IServerSessionFactory sessionFactory;

    // In seconds
    private int pingInterval = 60;

    private WebSocketServer server;
    private WssFactoryBuilder wssFactoryBuilder;
    private HashMap<WebSocket, WebSocketReceiver> sockets;
    private volatile boolean closed = true;
    private boolean handleRequestAsync;

    public WebSocketListener(IServerSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.sockets = new HashMap<>();
    }

    @Override
    public void open(String hostname, int port, ListenerEvents handler) {
        Draft_6455 draft = new Draft_6455(Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList(new Protocol("ocpp1.6")));
        server = new WebSocketServer(new InetSocketAddress(hostname, port), Collections.<Draft>singletonList(draft)) {
            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {                

                WebSocketReceiver receiver = new WebSocketReceiver(
                        new WebSocketReceiverEvents() {
                            @Override
                            public boolean isClosed() {
                                return closed;
                            }

                            @Override
                            public void relay(String message) {
                               webSocket.send(message);
                            }
                        }
                );
                sockets.put(webSocket, receiver);
                SessionInformation information = new SessionInformation.Builder()
                        .Identifier(clientHandshake.getResourceDescriptor())
                        .InternetAddress(webSocket.getRemoteSocketAddress()).build();

                handler.newSession(sessionFactory.createSession(new JSONCommunicator(receiver)), information);
            }

            @Override
            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                sockets.get(webSocket).disconnect();
                sockets.remove(webSocket);
            }

            @Override
            public void onMessage(WebSocket webSocket, String s) {
                sockets.get(webSocket).relay(s);
            }

            @Override
            public void onError(WebSocket webSocket, Exception e) {

            }

            @Override
            public void onStart() {

            }
        };

        if(wssFactoryBuilder != null) {
            server.setWebSocketFactory(wssFactoryBuilder.build());
        }

        server.setConnectionLostTimeout(pingInterval);

        server.start();
        closed = false;
    }

    public void enableWSS(WssFactoryBuilder wssFactoryBuilder) {
        if(server != null) {
            throw new IllegalStateException("Cannot enable WSS on already running server");
        }

        this.wssFactoryBuilder = wssFactoryBuilder;
    }

    public void setPingInterval(int interval) {
        this.pingInterval = pingInterval;

        if(server != null) {
            server.setConnectionLostTimeout(interval);
        }
    }

    @Override
    public void close() {
        try {
            closed = true;
            for (WebSocket ws : sockets.keySet())
                ws.close();

            sockets.clear();

            server.stop(1);

        } catch (InterruptedException e) {
        	logger.error("Failed to close listener", e);
        } finally {
            server = null;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void setAsyncRequestHandler(boolean async) {
        this.handleRequestAsync = async;
    }
}
