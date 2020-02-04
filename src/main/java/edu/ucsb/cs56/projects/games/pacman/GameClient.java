package edu.ucsb.cs56.projects.games.pacman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GameClient implements GameInterface {
	private ObjectMapper objectMapper = null;
	private String host = "127.0.0.1";
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private boolean verbose = false;

	public GameClient(int port, boolean verbose) {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("Connected to: " + host);
		} catch (UnknownHostException e) {
			System.err.println("Can't connect to: " + host);
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Unable to open input or output stream to: " + host);
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
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

	@Override
	public int getNumPellet() {
		String json = getRemoteJSON("numPellet");
		return this.getIntFromJSON(json);
	}

	@Override
	public int getScore() {
		String json = getRemoteJSON("score");
		return this.getIntFromJSON(json);
	}

	@Override
	public void keyPressed(int key) {
		getRemoteJSON("keyPressed=" + key);
	}

	@Override
	public void keyReleased(int key) {
		getRemoteJSON("keyReleased=" + key);
	}

	@Override
	public int getAudioClipID() {
		String json = getRemoteJSON("audioClipID");
		return this.getIntFromJSON(json);
	}

	@Override
	public int getNCompletedGames() {
		String json = getRemoteJSON("nCompletedGames");
		return this.getIntFromJSON(json);
	}

	@Override
	public int getNTrainedModels() {
		String json = getRemoteJSON("nTrainedModels");
		return this.getIntFromJSON(json);
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
