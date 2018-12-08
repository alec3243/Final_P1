package player;

public enum GameDecision {
	ROCK, PAPER, SCISSORS;

	public static GameDecision getRandom() {
		return values()[(int) (Math.random() * GameDecision.values().length)];
	}
}
