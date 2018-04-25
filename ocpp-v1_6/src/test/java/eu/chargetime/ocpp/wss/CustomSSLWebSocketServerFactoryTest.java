package eu.chargetime.ocpp.wss;

import org.java_websocket.SSLSocketChannel2;
import org.junit.Test;

import javax.net.ssl.*;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link CustomSSLWebSocketServerFactory}
 */
public class CustomSSLWebSocketServerFactoryTest {

    @Test
    public void wrapChannel() throws IOException {
        SSLEngine sslEngine = mock(SSLEngine.class);
        SSLSession sslSession = mock(SSLSession.class);
        SSLContextSpi sslContextSpi = new TestSSLContextSpi(sslEngine);
        SSLContext sslContext = new TestSSLContext(sslContextSpi);
        SocketChannel channel = mock(SocketChannel.class);
        SelectionKey key = mock(SelectionKey.class);
        List<String> ciphers = Arrays.asList("cipher2", "cipher3", "cipher4");

        when(sslEngine.getEnabledCipherSuites()).thenReturn(new String[]{"cipher1", "cipher2", "cipher3"});
        when(sslEngine.getHandshakeStatus()).thenReturn(SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING);
        when(sslEngine.getSession()).thenReturn(sslSession);

        CustomSSLWebSocketServerFactory factory = new CustomSSLWebSocketServerFactory(sslContext, ciphers);
        ByteChannel byteChannel = factory.wrapChannel(channel, key);

        assertThat(byteChannel, instanceOf(SSLSocketChannel2.class));

        verify(sslEngine).setEnabledCipherSuites(new String[]{"cipher2", "cipher3"});
        verify(sslEngine).setUseClientMode(false);
    }

    static class TestSSLContext extends SSLContext {
        protected TestSSLContext(SSLContextSpi sslContextSpi) {
            super(sslContextSpi, null, null);
        }
    }

    static class TestSSLContextSpi extends SSLContextSpi {

        private SSLEngine sslEngine;

        public TestSSLContextSpi(SSLEngine sslEngine) {
            this.sslEngine = sslEngine;
        }

        @Override
        protected void engineInit(KeyManager[] keyManagers, TrustManager[] trustManagers, SecureRandom secureRandom) throws KeyManagementException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected SSLSocketFactory engineGetSocketFactory() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected SSLServerSocketFactory engineGetServerSocketFactory() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected SSLEngine engineCreateSSLEngine() {
            return sslEngine;
        }

        @Override
        protected SSLEngine engineCreateSSLEngine(String s, int i) {
            return sslEngine;
        }

        @Override
        protected SSLSessionContext engineGetServerSessionContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected SSLSessionContext engineGetClientSessionContext() {
            throw new UnsupportedOperationException();
        }
    }
}
