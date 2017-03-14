package br.com.xht.udp.handler;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UdpSession {

	private HashMap<Integer, IUdpTalk> udpTalkers = new HashMap<Integer, IUdpTalk>();
	private int sessionId;
	private ExecutorService pool = Executors.newFixedThreadPool(50);
	private Logger logger;

	public UdpSession(int sessionId) {
		this.sessionId = sessionId;
		this.logger = LoggerFactory.getLogger("session-" + sessionId);
		
		new HelloWatchdog().start();
	}

	public int getSessionId() {
		return sessionId;
	}

	public void put(IUdpTalk udpTalk) {
		int personaId = udpTalk.getPersonaId();
		udpTalkers.put(personaId, udpTalk);
	}
	
	public boolean has(IUdpTalk udpTalk) {
		return udpTalkers.containsKey(udpTalk.getPersonaId());
	}

	public void broadcast(IUdpTalk udpTalk, byte[] dataPacket) {
		Set<Entry<Integer, IUdpTalk>> entrySet = udpTalkers.entrySet();
		long senderPersonaId = udpTalk.getPersonaId();
		
		pool.submit(() -> {
		    logger.info("[Freeroam] Sending from persona " + senderPersonaId);
            List<Entry<Integer, IUdpTalk>> talkerList = Lists.newArrayList(entrySet)
					.stream()
					.filter(e -> e.getKey() != senderPersonaId)
					.collect(Collectors.toList());
            talkerList.forEach(talker -> {
            	logger.info("[Freeroam] Talker: " + talker.getKey());
				
            	IUdpTalk talk = talker.getValue();
            	long sleepTime = talk.getSleepTime() * 80;
            	
            	if (sleepTime > 6500) {
            		sleepTime = 6500;
				} else if (sleepTime < 1500) {
            		sleepTime = 1500;
				}
            	
            	logger.info("[Freeroam] sleepTime = " + sleepTime);

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				talk.sendFrom(udpTalk, dataPacket);
				
				logger.info("[Freeroam] Done sending to " + talker.getKey());
			});

//			for (Entry<Integer, IUdpTalk> entry : entrySet) {
//				int key = entry.getKey();
//				
//				if (senderPersonaId != key) {
//					IUdpTalk talker = entry.getValue();
//                    logger.info("");
//				}
//			}
			
			logger.info("[Freeroam] Done sending from persona " + senderPersonaId);
		});
	}

	private class HelloWatchdog extends Thread {
		private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(500);
		
		@Override
		public void run() {
			scheduler.scheduleAtFixedRate(() -> {
				udpTalkers.values().forEach(talker -> {
					if (talker.getDiffTime() > 5000) {
						talker.sendFrom(talker, talker.getHello().getServerHelloMessage());
					}
				});
			}, 1L, 5L, TimeUnit.SECONDS);	
		}
	}
}
