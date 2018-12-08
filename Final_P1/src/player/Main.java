package player;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Main {

	public static void main(String[] args) throws IOException {
		Scanner in = new Scanner(System.in);
		System.out.print("Input the number of games to run: ");
		int gameCount = in.nextInt();
		in.close();
		for (int i = 0; i < gameCount; i++) {
			try {
				// Establish the 3 players
				Player p1 = new Player(1);
				Player p2 = new Player(2);
				Player p3 = new Player(3);

				// Get results from player1
				boolean[] p1Wins = new boolean[2];
				p1Wins[0] = p1.future1.get();
				p1Wins[1] = p1.future2.get();

				// Get results from player2
				boolean[] p2Wins = new boolean[2];
				p2Wins[0] = p2.future1.get();
				p2Wins[1] = p2.future2.get();

				// Get results from player3
				boolean[] p3Wins = new boolean[2];
				p3Wins[0] = p3.future1.get();
				p3Wins[1] = p3.future2.get();

				int p1Score = 0;
				int p2Score = 0;
				int p3Score = 0;

				if (p1Wins[0] && p1Wins[1]) {
					p1Score = 2;
				} else if (p1Wins[0] || p1Wins[1]) {
					p1Score = 1;
				}

				if (p2Wins[0] && p2Wins[1]) {
					p2Score = 2;
				} else if (p2Wins[0] || p2Wins[1]) {
					p2Score = 1;
				}

				if (p3Wins[0] && p3Wins[1]) {
					p3Score = 2;
				} else if (p3Wins[0] || p3Wins[1]) {
					p3Score = 1;
				}
				if (p1Score == 1 && p2Score == 1 && p3Score == 1) {
					p1Score = 0;
					p2Score = 0;
					p3Score = 0;
				}
				System.out
						.printf("Player 1 score - %d%nPlayer 2 score - %d%nPlayer 3 Score - %d%n",
								p1Score, p2Score, p3Score);
			} catch (IOException | InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
}
