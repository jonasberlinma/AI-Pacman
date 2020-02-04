package edu.ucsb.cs56.projects.games.pacman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class GameServer implements GameInterface, Runnable {

	private Board board;
	private GameController bgc;
	private ObjectMapper objectMapper = null;
	private int port;

	public GameServer(Board board, GameController bgc, int port) {
		this.board = board;
		this.bgc = bgc;
		this.port = port;
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	@Override
	public Grid getGrid() {
		String json = null;
		Grid grid = null;
		try {
			json = objectMapper.writeValueAsString(board.getGrid());
			grid = objectMapper.readValue(json, Grid.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return grid;
	}

	@Override
	public PacPlayer getMsPacman() {
		String json = null;
		PacPlayer pacPlayer = null;
		try {
			json = objectMapper.writeValueAsString(board.getMsPacman());
			pacPlayer = objectMapper.readValue(json, PacPlayer.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pacPlayer;
	}

	@Override
	public PacPlayer getPacman() {

		String json = null;
		PacPlayer pacPlayer = null;
		try {
			json = objectMapper.writeValueAsString(board.getPacman());
			pacPlayer = objectMapper.readValue(json, PacPlayer.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pacPlayer;
	}

	@Override
	public Vector<Ghost> getGhosts() {
		String json = null;
		Vector<Ghost> ghosts = null;
		try {
			json = objectMapper.writeValueAsString(board.getGhosts());
			ghosts = objectMapper.readValue(json, new TypeReference<Vector<Ghost>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ghosts;
	}

	@Override
	public GameType getGameType() {
		String json = null;
		GameType gameType = null;
		try {
			json = objectMapper.writeValueAsString(board.getGameType());
			gameType = objectMapper.readValue(json, GameType.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return gameType;
	}

	@Override
	public int getNumPellet() {
		String json = this.getJSONFromInt(board.getNumPellet());
		int numPellets = this.getIntFromJSON(json);
		return numPellets;
	}

	@Override
	public int getScore() {
		String json = this.getJSONFromInt(board.getScore());
		int score = this.getIntFromJSON(json);
		return score;
	}

	@Override
	public void keyPressed(int key) {
		board.keyPressed(key);
	}

	@Override
	public void keyReleased(int key) {
		board.keyReleased(key);
	}

	@Override
	public int getAudioClipID() {
		String json = this.getJSONFromInt(board.getAudioClipID());
		int audioClipID = this.getIntFromJSON(json);
		return audioClipID;
	}

	@Override
	public int getNCompletedGames() {
		String json = this.getJSONFromInt(bgc.getNCompletedGames());
		int nCompletedGames = this.getIntFromJSON(json);
		return nCompletedGames;
	}

	@Override
	public int getNTrainedModels() {
		String json = this.getJSONFromInt(bgc.getNTrainedModels());
		int nTrainedModels = this.getIntFromJSON(json);
		return nTrainedModels;
	}

	// Helper functions
	private String getJSONFromInt(int value) {
		String ret = null;
		try {
			ret = objectMapper.writeValueAsString(new Integer(value));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ret;
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

	public void Start() {
		Thread gameServerThread = new Thread(this, "Game Server");
		gameServerThread.start();
	}

	@Override
	public void run() {
		// Open listen socket
		// -- Read command
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Waiting for connect on port: " + port);
			Socket clientSocket = serverSocket.accept();
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			String commandLine = null;
			while ((commandLine = in.readLine()) != null) {
				System.out.println("Server received: " + commandLine);
				String json = null;
				try {
					String[] command=commandLine.split("=");
					switch (command[0]) {
					case "grid":
						json = objectMapper.writeValueAsString(board.getGrid());
						break;
					case "msPacman":
						json = objectMapper.writeValueAsString(board.getMsPacman());
						break;
					case "pacman":
						json = objectMapper.writeValueAsString(board.getPacman());
						break;
					case "ghosts":
						json = objectMapper.writeValueAsString(board.getGhosts());
						break;
					case "gameType":
						json = objectMapper.writeValueAsString(board.getGameType());
						break;
					case "numPellets":
						json = this.getJSONFromInt(board.getNumPellet());
						break;
					case "score":
						json = this.getJSONFromInt(board.getScore());
						break;
					case "keyPressed":
						board.keyPressed(Integer.parseInt(command[1]));
						json = "";
						break;
					case "keyReleased":
						board.keyReleased(Integer.parseInt(command[1]));
						json = "";
						break;
					case "audioClipID":
						json = this.getJSONFromInt(board.getAudioClipID());
						break;
					case "nCompletedGames":
						json = this.getJSONFromInt(bgc.getNCompletedGames());
						break;
					case "nTrainedModels":
						json = this.getJSONFromInt(bgc.getNTrainedModels());
						break;
					default:
						System.exit(2);
					}
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				out.println(json);
				out.flush();
				System.out.println("Server sent: " + json);
			}
		} catch (IOException e1) {
			System.err.println("Unable to open socket on port: " + port);
			e1.printStackTrace();
			System.exit(1);
		} 
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Stopping");
		board.stop();
		try {
			board.join();
		} catch (InterruptedException e) {
		}
		bgc.stop();
		bgc.join();
		
		System.exit(0);
	}
}
