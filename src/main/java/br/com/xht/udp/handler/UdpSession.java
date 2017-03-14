package br.com.xht.udp.handler;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class UdpSession {

	private HashMap<Integer, IUdpTalk> udpTalkers = new HashMap<Integer, IUdpTalk>();
	private int sessionId;
	private ExecutorService pool = Executors.newFixedThreadPool(50);
	private Logger logger;
	private long timeLong = 0L;

	public UdpSession(int sessionId) {
		this.sessionId = sessionId;
		this.logger = LoggerFactory.getLogger("session-" + sessionId);
		this.timeLong = System.currentTimeMillis();
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
            talkerList.forEach(talkerEntry -> {
            	Integer personaId = talkerEntry.getKey();
            	IUdpTalk talker = talkerEntry.getValue();
            	
            	if (personaId.longValue() != senderPersonaId) {
            		logger.info("[Freeroam] Sending to " + personaId);
            		talker.sendFrom(udpTalk, dataPacket);
            		logger.info("[Freeroam] Sent to " + personaId);
				}
			});
			
			logger.info("[Freeroam] Done sending from persona " + senderPersonaId);
		});
	}
	
	public long getTimeDiff() {
		return System.currentTimeMillis() - timeLong;
	}
}
