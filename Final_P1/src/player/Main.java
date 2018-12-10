package player;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {
	private static final int LAUNCHER_PORT = 1999;

	public static void main(String[] args) throws IOException {

		if (args.length == 0 || !args[0].matches("[0-9]*")) {
			System.err
					.println("Enter number of games to play as a command-line argument.");
			System.exit(1);
		}
		DatagramSocket processReceiver = new DatagramSocket(LAUNCHER_PORT);
		int numGames = Integer.parseInt(args[0]);
		for (int i = 1; i <= numGames; i++) {
			System.out.println("Game number " + i + "~~~~~");
			try {
				// Initialize environment data
				String javaHome = System.getProperty("java.home");
				String javaBin = javaHome + File.separator + "bin"
						+ File.separator + "java";
				String classpath = System.getProperty("java.class.path");
				String className = Player.class.getName();

				// Create a process for each player
				ProcessBuilder builder1 = new ProcessBuilder(javaBin, "-cp",
						classpath, className, "1");
				ProcessBuilder builder2 = new ProcessBuilder(javaBin, "-cp",
						classpath, className, "2");
				ProcessBuilder builder3 = new ProcessBuilder(javaBin, "-cp",
						classpath, className, "3");
				builder1.inheritIO().start();
				builder2.inheritIO().start();
				builder3.inheritIO().start();

				int p1Score = 0;
				int p2Score = 0;
				int p3Score = 0;
				final int PLAYERS = 3;
				byte[] buf;
				DatagramPacket data;
				for (int j = 0; j < PLAYERS; j++) {
					buf = new byte[2];
					data = new DatagramPacket(buf, buf.length);
					processReceiver.receive(data);
					if (buf[0] == 1) {
						p1Score = buf[1];
					} else if (buf[0] == 2) {
						p2Score = buf[1];
					} else if (buf[0] == 3) {
						p3Score = buf[1];
					}
				}

				if (p1Score == 1 && p2Score == 1 && p3Score == 1) {
					p1Score = 0;
					p2Score = 0;
					p3Score = 0;
				}
				System.out
						.printf("Player 1 score - %d%nPlayer 2 score - %d%nPlayer 3 Score - %d%n",
								p1Score, p2Score, p3Score);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		processReceiver.disconnect();
		processReceiver.close();
	}
}
