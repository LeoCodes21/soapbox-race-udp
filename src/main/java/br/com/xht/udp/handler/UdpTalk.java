package br.com.xht.udp.handler;

import java.util.Date;

public abstract class UdpTalk implements IUdpTalk, Comparable<UdpTalk> {

	private UdpWriter udpWriter;

	private IPacketProcessor packetProcessor;

	private IUdpHello udpHello;

	private long timeStart;

	private long ping = 0;
	
	private long lastPacketSent = 0;

	public UdpTalk(IUdpHello udpHello, IPacketProcessor packetProcessor, UdpWriter udpWriter) {
		this.timeStart = new Date().getTime();
		this.lastPacketSent = this.timeStart;
		this.udpHello = udpHello;
		this.packetProcessor = packetProcessor;
		this.udpWriter = udpWriter;
	}

	public void sendFrom(IUdpTalk udpTalk, byte[] dataPacket) {
		byte[] processed = packetProcessor.getProcessed(dataPacket, UdpSessions.get(getSessionId()).getTimeDiff());
		if (processed != null) {
			udpWriter.sendPacket(processed);
		}
	}

	protected abstract void parseSyncPacket() throws Exception;

	public IPacketProcessor getPacketProcessor() {
		return packetProcessor;
	}

	public int getSessionId() {
		return udpHello.getSessionId();
	}

	public byte getNumberOfClients() {
		return udpHello.getNumberOfClients();
	}

	public long getTimeStart() {
		return this.timeStart;
	}

	public long getDiffTime() {
		return new Date().getTime() - timeStart;
	}

	@Override
	public long getSleepTime() {
		return new Date().getTime() - this.lastPacketSent;
	}

	public void broadcast(byte[] dataPacket) {
		UdpSession udpSession = UdpSessions.get(getSessionId());
		udpSession.broadcast(this, dataPacket);
	}

	public long getPing() {
		return ping;
	}

	public byte[] getHelloPacket() {
		return udpHello.getHelloPacket();
	}

	@Override
	public IUdpHello getHello() {
		return udpHello;
	}

	@Override
	public void updateLastPacket() {
		this.lastPacketSent = new Date().getTime();
	}

	public int getPersonaId() {
		return udpHello.getPersonaId();
	}

	@Override
	public int compareTo(UdpTalk udpTalk) {
		if (ping < udpTalk.getPing()) {
			return -1;
		}
		if (ping > udpTalk.getPing()) {
			return 1;
		}
		return 0;
	}
}
