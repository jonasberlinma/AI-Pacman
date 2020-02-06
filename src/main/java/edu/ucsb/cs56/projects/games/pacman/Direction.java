package edu.ucsb.cs56.projects.games.pacman;

public enum Direction {
	LEFT, RIGHT, UP, DOWN;
	public boolean isSame(Direction other) {
		return this == other;
	}

	public boolean isOpposite(Direction other) {
		return this == other.opposite();
	}

	public Direction opposite() {
		Direction ret = null;
		switch (this) {
		case LEFT:
			ret = RIGHT;
			break;
		case RIGHT:
			ret = LEFT;
			break;
		case UP:
			ret = DOWN;
			break;
		case DOWN:
			ret = UP;
			break;
		}
		return ret;
	}

	public static Direction parseDirection(String text) {
		Direction d = null;
		if (text != null) {
			switch (text) {
			case "LEFT":
			case "←":
			case "1":
				d = Direction.LEFT;
				break;
			case "RIGHT":
			case "→":
			case "2":
				d = Direction.RIGHT;
				break;
			case "DOWN":
			case "↓":
			case "3":
				d = Direction.DOWN;
				break;
			case "UP":
			case "↑":
			case "4":
				d = Direction.UP;
				break;
			case "0":
				break;
			default:
				System.err.println("Failed to parse direction " + text);
			}
		}
		return d;
	}
};

