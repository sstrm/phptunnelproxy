/*
 * Created on 2004-11-11
 */
package cc.co.phptunnelproxy.ptplocal.experiment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author elgs
 */
public class PortMapTest {
	private static Selector selector = null;
	private static ByteBuffer buf = ByteBuffer.allocateDirect(1024 * 100);
	private static boolean dbg = false;

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException {
		selector = Selector.open();
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		ssChannel.configureBlocking(false);
		ssChannel.socket().bind(new InetSocketAddress(443));
		ssChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Listening on port 80.");
		while (selector.select() > 0) {
			for (Iterator it = selector.selectedKeys().iterator(); it.hasNext();) {
				// Get the selection key
				SelectionKey selKey = (SelectionKey) it.next();
				// Remove it from the list to indicate that it is being
				// processed
				it.remove();
				try {
					processSelectionKey(selKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void processSelectionKey(SelectionKey selKey)
			throws IOException {
		// Since the ready operations are cumulative,
		// need to check readiness for each operation
		if (selKey.isValid() && selKey.isAcceptable()) {
			// Get channel with connection request
			ServerSocketChannel ssChannel = (ServerSocketChannel) selKey
					.channel();
			SocketChannel sChannel = ssChannel.accept();
			sChannel.configureBlocking(false);
			// If serverSocketChannel is non-blocking, sChannel may
			// be null
			if (sChannel != null) {
				SelectionKey clientKey = sChannel.register(selector,
						SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				SocketChannel sServerChannel = SocketChannel.open();
				sServerChannel.configureBlocking(false);
				sServerChannel.connect(new InetSocketAddress("mail.google.com",
						443));
				// 172.16.31.130
				while (!sServerChannel.finishConnect()) {
					if (dbg)
						System.out.print(".");
				}
				if (dbg)
					System.out.println("Connected OK!");
				SelectionKey serverKey = sServerChannel.register(selector,
						sServerChannel.validOps());
				clientKey.attach(serverKey);
				serverKey.attach(clientKey);
			}
		}
		if (selKey.isValid() && selKey.isReadable()) {
			// Get channel with bytes to read
			SocketChannel sChannel = (SocketChannel) selKey.channel();
			SelectionKey palKey = (SelectionKey) selKey.attachment();
			if (palKey != null) {
				SocketChannel palChannel = (SocketChannel) palKey.channel();
				if (palKey.isValid() && selKey.isValid()) {
					// Clear the buffer and read bytes from socket
					buf.clear();
					int count = 0;
					try {
						while ((count = sChannel.read(buf)) > 0) {
							buf.flip(); // Make buffer readable
							while (buf.hasRemaining()) {
								palChannel.write(buf);
							}
							buf.clear();
						}
					} catch (IOException e) {
						sChannel.close();
						palChannel.close();
						if (dbg)
							System.out.println("Forced disconnected!");
					}
					if (count == -1) {
						sChannel.close();
						palChannel.close();
						if (dbg)
							System.out.println("Disconnected OK!");
					}
				} else {
					sChannel.close();
					palChannel.close();
					if (dbg)
						System.out.println("Not Connected!");
				}
			} else {
				sChannel.close();
				if (dbg)
					System.out.println("Timeout disconnected OK!");
			}
		}
	}
}