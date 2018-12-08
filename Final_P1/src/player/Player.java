package player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Player {
	private static final int PLAYER1_PORT1 = 2000;
	private static final int PLAYER1_PORT2 = 2001;
	private static final int PLAYER2_PORT1 = 2002;
	private static final int PLAYER2_PORT2 = 2003;
	private static final int PLAYER3_PORT1 = 2004;
	private static final int PLAYER3_PORT2 = 2005;

	private Callable<Boolean> connection1;
	private Callable<Boolean> connection2;
	Future<Boolean> future1;
	Future<Boolean> future2;

	Player(int playerNum) throws UnknownHostException, IOException {
		// connection1 initiates, connection2 waits
		if (playerNum == 1) {
			connection1 = new GameConnection(PLAYER1_PORT1, PLAYER2_PORT1);
			connection2 = new GameConnection(PLAYER1_PORT2, PLAYER3_PORT1);
		} else if (playerNum == 2) {
			connection1 = new GameConnection(PLAYER2_PORT1, PLAYER1_PORT1);
			connection2 = new GameConnection(PLAYER2_PORT2, PLAYER3_PORT2);
		} else if (playerNum == 3) {
			connection1 = new GameConnection(PLAYER3_PORT1, PLAYER1_PORT2);
			connection2 = new GameConnection(PLAYER3_PORT2, PLAYER2_PORT2);
		}

		ExecutorService executor = Executors.newSingleThreadExecutor();
		future1 = executor.submit(connection1);
		future2 = executor.submit(connection2);
		executor.shutdown();
	}

	private class GameConnection extends DatagramSocket implements
			Callable<Boolean> {
		private static final String HOST = "localhost";
		private int targetPort;

		GameConnection(int sourcePort, int targetPort)
				throws UnknownHostException, IOException {
			super(sourcePort);
			this.targetPort = targetPort;
		}

		@Override
		public Boolean call() throws Exception {
			GameDecision localDecision = GameDecision.getRandom();
			GameDecision remoteDecision = null;
			try {
				remoteDecision = sendAndReceive(localDecision);
				disconnect();
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Return true if local wins
			if (localDecision == GameDecision.ROCK
					&& remoteDecision == GameDecision.SCISSORS
					|| localDecision == GameDecision.SCISSORS
					&& remoteDecision == GameDecision.PAPER
					|| localDecision == GameDecision.PAPER
					&& remoteDecision == GameDecision.ROCK) {
				return true;
			} else {
				// Return false if remote wins
				return false;
			}
		}

		private GameDecision sendAndReceive(GameDecision localDecision)
				throws IOException {
			// Put the localDecision ordinal value into the buffer
			byte[] buf = new byte[1];
			buf[0] = (byte) localDecision.ordinal();

			// Pack the buffer into UDP packet, send through socket
			DatagramPacket out = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(HOST), targetPort);
			send(out);

			// Receive the remote GameDecision
			buf = new byte[1];
			DatagramPacket response = new DatagramPacket(buf, buf.length);
			receive(response);

			return GameDecision.values()[buf[0]];
		}
	}

}
