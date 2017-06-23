package com.flasharc.portforward;

import java.io.IOException;

import com.flasharc.portforward.utils.ArgsInterpreter;

public class PortForwardStarter {
	private static final String KEY_LOCAL_PORT = "localPort";
	private static final String KEY_REMOTE_HOST = "remoteHost";
	private static final String KEY_REMOTE_PORT = "remotePort";

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ArgsInterpreter argsInterpreter = new ArgsInterpreter(args);

		if (!(argsInterpreter.containsKey(KEY_REMOTE_HOST) && argsInterpreter.containsKey(KEY_REMOTE_PORT))) {
			System.out.println(
					"Not enough arguments.\nUsage: remoteHost=<hostName> remotePort=<port> localPort=<localPortToListen>");
			return;
		}

		int localPort = Integer.parseInt(argsInterpreter.getKeyValue(KEY_LOCAL_PORT, "5037"));
		String remoteHost = argsInterpreter.getKeyValue(KEY_REMOTE_HOST);
		int remotePort = Integer.parseInt(argsInterpreter.getKeyValue(KEY_REMOTE_PORT));

		PortForward forwarder = new PortForward(localPort, remoteHost, remotePort);
		forwarder.start();
	}
}
