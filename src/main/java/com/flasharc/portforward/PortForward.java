package com.flasharc.portforward;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortForward {
	private static Logger LOGGER = Logger.getLogger("PortForward");

	private final int myDestinationPort;
	private final String myDestinationHost;

	private final ServerSocket myServerSocket;

	public PortForward(int aLocalPort, String aDestinationHost, int aDestinationPort) throws IOException {
		myDestinationPort = aDestinationPort;
		myDestinationHost = aDestinationHost;

		myServerSocket = new ServerSocket(aLocalPort);
	}

	public synchronized void start() throws IOException {
		while (true) {
			Socket clientSocket = myServerSocket.accept();
			LOGGER.info("Inbound Request");
			RequestHandler handler = new RequestHandler(clientSocket);
			Thread newRequest = new Thread(handler);
			newRequest.start();
		}
	}

	private class RequestHandler implements Runnable {
		private final Socket clientSocket;

		private RequestHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		public void run() {
			LOGGER.info("Creating a new connection to destination");
			try (Socket newConnection = new Socket(myDestinationHost, myDestinationPort)) {
				Thread newInputThread = new Thread(new StreamCopier("input", clientSocket.getInputStream(),
						newConnection.getOutputStream()));
				Thread newOutputThread = new Thread(new StreamCopier("output", newConnection.getInputStream(),
						clientSocket.getOutputStream()));
				newInputThread.start();
				newOutputThread.start();
				LOGGER.info("Started the stream transfer threads. Waiting for them to complete.");
				newOutputThread.join();
				LOGGER.info("The output transfer has completed. Closing the connections");
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Exception while processing request", e);
			} finally {
				try {
					clientSocket.close();
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Exception closing the client socket", e);
				}
			}
		}
	}

	private class StreamCopier implements Runnable {
		private final InputStream myInputStream;
		private final OutputStream myOutputStream;
		private final String name;

		public StreamCopier(String name, InputStream anInputStream, OutputStream anOutputStream) {
			this.name = name;
			myInputStream = anInputStream;
			myOutputStream = anOutputStream;
		}

		public void run() {
			byte[] buffer = new byte[1024];
			try {
				int bytesRead;
				while ((bytesRead = myInputStream.read(buffer)) != -1) {
					LOGGER.info(name + ": Bytes Read: " + new String(buffer, 0, bytesRead));
					myOutputStream.write(buffer, 0, bytesRead);
					myOutputStream.flush();
					LOGGER.info(name + ": Flushed the contents to output");
				} 
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, name + ": Exception while transferring streams", e);
			}
		}
		
/*		public void run() {
			int d;
			try {
				while ((d = myInputStream.read()) != -1) {
					myOutputStream.write(d);
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, name + ": Exception while transferring streams", e);
			}
		}*/
	}
}
