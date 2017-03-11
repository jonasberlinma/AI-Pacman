package edu.ucsb.cs56.projects.games.pacman;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Vector;

public class BackgroundGameController implements Runnable {

	private int nThreads = 0;
	private Thread controllerThread;
	private boolean doRun = false;
	private Vector<AIGame> gameList = new Vector<AIGame>();
	private String aiPlayerClassName = null;
	private int nCompletedGames = 0;

	BackgroundGameController(String aiPlayerClassName, int backgroundGameThreads) {
		this.nThreads = backgroundGameThreads;
		this.aiPlayerClassName = aiPlayerClassName;
		controllerThread = new Thread(this, "BackgroundGameController");
	}

	public void start() {
		doRun = true;
		controllerThread.start();
	}
	public void stop(){
		
	}
	public int getNCompletedGames(){
		return nCompletedGames;
	}
	@Override
	public void run() {
		while (doRun) {
			try {
				Thread.sleep(10);
				Iterator<AIGame> i = gameList.iterator();
				while (i.hasNext()) {
					AIGame aiGame = i.next();
					if (!aiGame.isRunning()) {
						aiGame.join();
						i.remove();
						nCompletedGames++;
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (gameList.size() < nThreads) {
				AIGame aiGame;
				try {
					aiGame = new AIGame(aiPlayerClassName, 0, true);
					gameList.addElement(aiGame);
					aiGame.start();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}
		}
	}

}
