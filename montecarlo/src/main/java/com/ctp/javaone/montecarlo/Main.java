package com.ctp.javaone.montecarlo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.jboss.weld.environment.se.ShutdownManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.events.ContainerInitialized;

@Singleton
public class Main {

	public static void main(String[] args) {
		
		final WeldContainer weld = new Weld().initialize();
		
		java.awt.EventQueue.invokeLater(new Runnable() {
                  public void run() {
                      weld.event().select(ContainerInitialized.class).fire(new ContainerInitialized());
                  }
                });

		List<Thread> threads = new ArrayList<Thread>();

		for(int i = 0; i < Generator.MAX_THREADS; i++) {
			final Worker worker = weld.instance().select(Worker.class).get();
			Thread thread = new Thread(worker);
			threads.add(thread);
			thread.start();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.exit(10000);
			}
		}
		
		ShutdownManager shutdownManager = weld.instance().select(ShutdownManager.class).get();
	    shutdownManager.shutdown();
	}

}
