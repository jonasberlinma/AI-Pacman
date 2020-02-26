package edu.ucsb.cs56.projects.games.pacman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GameClient {
	private ObjectMapper objectMapper = null;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private boolean verbose = false;

	public GameClient(String uihost, int uiport, boolean verbose) {
		this.verbose = verbose;
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			socket = new Socket(uihost, uiport);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("Connected to: " + uihost);
		} catch (UnknownHostException e) {
			System.err.println("Can't connect to: " + uihost);
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Unable to open input or output stream to: " + uihost);
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Get a local copy of the current grid
	 * 
	 * @return
	 */
	public Grid getGrid() {
		String json = null;
		Grid grid = null;
		try {
			json = getRemoteJSON("grid");
			grid = objectMapper.readValue(json, Grid.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return grid;
	}

	/**
	 * Get a local copy of MsPacman data
	 * 
	 * @return
	 */
	public PacPlayer getMsPacman() {
		String json = null;
		PacPlayer pacPlayer = null;
		try {
			json = getRemoteJSON("msPacman");
			pacPlayer = objectMapper.readValue(json, PacPlayer.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pacPlayer;
	}

	/**
	 * Get a local copy Pacman data
	 * 
	 * @return
	 */
	public PacPlayer getPacman() {
		String json = null;
		PacPlayer pacPlayer = null;
		try {
			json = getRemoteJSON("pacman");
			pacPlayer = objectMapper.readValue(json, PacPlayer.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pacPlayer;
	}

	/**
	 * Get a local copy of the current ghosts
	 * 
	 * @return
	 */
	public Vector<Ghost> getGhosts() {
		String json = null;
		Vector<Ghost> ghosts = null;
		try {
			json = getRemoteJSON("ghosts");
			ghosts = objectMapper.readValue(json, new TypeReference<Vector<Ghost>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ghosts;
	}

	/**
	 * Get the GameType of the current game
	 * 
	 * @return
	 */
	public GameType getGameType() {
		String json = getRemoteJSON("gameType");
		GameType gameType = null;
		if (json != null) {
			try {
				gameType = objectMapper.readValue(json, GameType.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return gameType;
	}

	/**
	 * Get number of pellets in current game
	 * 
	 * @return
	 */
	public int getNumPellet() {
		String json = getRemoteJSON("numPellet");
		return this.getIntFromJSON(json);
	}

	/**
	 * The the score of the current game
	 * 
	 * @return
	 */
	public int getScore() {
		String json = getRemoteJSON("score");
		return this.getIntFromJSON(json);
	}

	/**
	 * Send key press event to the remote game
	 * 
	 * @param key
	 */
	public void keyPressed(int key) {
		getRemoteJSON("keyPressed=" + key);
	}

	/**
	 * Send key release event to the remote game
	 * 
	 * @param key
	 */
	public void keyReleased(int key) {
		getRemoteJSON("keyReleased=" + key);
	}

	/**
	 * Get the sound clip the current game requested to be played
	 * 
	 * @return
	 */
	public int getAudioClipID() {
		String json = getRemoteJSON("audioClipID");
		return this.getIntFromJSON(json);
	}

	/**
	 * Get the number of games completed by the background players from the game
	 * controller
	 * 
	 * @return
	 */
	public int getNCompletedGames() {
		String json = getRemoteJSON("nCompletedGames");
		return this.getIntFromJSON(json);
	}

	/**
	 * Get the number of models that have been trained by the game controller
	 * 
	 * @return
	 */
	public int getNTrainedModels() {
		String json = getRemoteJSON("nTrainedModels");
		return this.getIntFromJSON(json);
	}

	/**
	 * In the current grid get the shortest path between two points
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public ArrayList<PathSection> getShortestPath(int x1, int y1, int x2, int y2) {
		String json = null;
		ArrayList<PathSection> path = null;
		try {
			json = getRemoteJSON("path&x1=" + x1 + "&y1=" + y1 + "&x2=" + x2 + "&y2=" + y2);
			if (json != null)
				path = objectMapper.readValue(json, new TypeReference<ArrayList<PathSection>>() {
				});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	/**
	 * From the current grid get the possible direction you can walk from the
	 * specified point without running into walls. (Only used for interactive board
	 * analysis)
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public ArrayList<Direction> getPossibleDirections(int x, int y) {
		String json = null;
		ArrayList<Direction> directions = null;
		try {
			json = getRemoteJSON("directions&x=" + x + "&y=" + y);
			directions = objectMapper.readValue(json, new TypeReference<ArrayList<Direction>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return directions;
	}

	/**
	 * Add a new ghost in the specified location. (Only used for interactive board
	 * analysis)
	 * 
	 * @param x
	 * @param y
	 */
	public void putGhost(int x, int y) {
		getRemoteJSON("ghost&x=" + x + "&y=" + y);
	}

	/**
	 * Clear the specified location of everything, ghosts, pellets, pills and
	 * fruits. (Only used for interactive board analysis)
	 * 
	 * @param x
	 * @param y
	 */
	public void clear(int x, int y) {
		getRemoteJSON("clear&x=" + x + "&y=" + y);
	}

	/**
	 * Analyze the surroundings of the specified point. Shortest paths to ghosts,
	 * pellets, pills, and fruit. (Only used in interactive board analysis)
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public LinkedHashMap<String, ArrayList<PathSection>> analyze(int x, int y) {
		LinkedHashMap<String, ArrayList<PathSection>> analysis = null;
		try {
			String json = getRemoteJSON("analyze&x=" + x + "&y=" + y);
			analysis = objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, ArrayList<PathSection>>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return analysis;
	}

	private int getIntFromJSON(String json) {
		Integer ret = 0;
		try {
			ret = objectMapper.readValue(json, Integer.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private String getRemoteJSON(String command) {
		out.println(command);
		out.flush();
		if (verbose) {
			System.out.println("Client sent: " + command);
		}
		String json = null;
		try {
			json = in.readLine();
			if (verbose) {
				System.out.println("Client received: " + json);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
}
