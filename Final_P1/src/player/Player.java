package player;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Player {
	private static final int LAUNCHER_PORT = 1999;

	private static final int PLAYER1_PORT1 = 2000;
	private static final int PLAYER1_PORT2 = 2001;
	private static final int PLAYER1_OUTPORT = 2010;

	private static final int PLAYER2_PORT1 = 2002;
	private static final int PLAYER2_PORT2 = 2003;
	private static final int PLAYER2_OUTPORT = 2011;

	private static final int PLAYER3_PORT1 = 2004;
	private static final int PLAYER3_PORT2 = 2005;
	private static final int PLAYER3_OUTPORT = 2012;

	private static final String HOST = "localhost";

	public static void main(String[] args) throws NumberFormatException,
			UnknownHostException, IOException, InterruptedException,
			ExecutionException {
		new Player(Integer.parseInt(args[0]));
	}

	private Callable<Boolean> connection1;
	private Callable<Boolean> connection2;
	private Future<Boolean> future1;
	private Future<Boolean> future2;

	Player(int playerNum) throws UnknownHostException, IOException,
			InterruptedException, ExecutionException {
		DatagramSocket outputSocket = null;
		byte[] buf = new byte[2];
		if (playerNum == 1) {
			connection1 = new GameConnection(PLAYER1_PORT1, PLAYER2_PORT1);
			connection2 = new GameConnection(PLAYER1_PORT2, PLAYER3_PORT1);
			outputSocket = new DatagramSocket(PLAYER1_OUTPORT);
			buf[0] = 1;
		} else if (playerNum == 2) {
			connection1 = new GameConnection(PLAYER2_PORT1, PLAYER1_PORT1);
			connection2 = new GameConnection(PLAYER2_PORT2, PLAYER3_PORT2);
			outputSocket = new DatagramSocket(PLAYER2_OUTPORT);
			buf[0] = 2;
		} else if (playerNum == 3) {
			connection1 = new GameConnection(PLAYER3_PORT1, PLAYER1_PORT2);
			connection2 = new GameConnection(PLAYER3_PORT2, PLAYER2_PORT2);
			outputSocket = new DatagramSocket(PLAYER3_OUTPORT);
			buf[0] = 3;
		}

		ExecutorService executor = Executors.newSingleThreadExecutor();
		future1 = executor.submit(connection1);
		future2 = executor.submit(connection2);
		executor.shutdown();
		byte score = 0;
		if (future1.get()) {
			score++;
		}
		if (future2.get()) {
			score++;
		}
		buf[1] = score;
		DatagramPacket packet = new DatagramPacket(buf, buf.length,
				InetAddress.getByName(HOST), LAUNCHER_PORT);
		outputSocket.send(packet);
	}

	private class GameConnection extends DatagramSocket implements
			Callable<Boolean> {
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
				// The program only works if I print something right here?????
				System.out.print(" \b");
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
