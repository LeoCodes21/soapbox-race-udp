package br.com.xht.udp.handler;

public interface IUdpTalk {

	public void sendFrom(IUdpTalk udpTalk, byte[] dataPacket);

	public int getPersonaId();

	public byte[] getSyncPacket();

	public void broadcast(byte[] dataPacket);

	public int getSessionId();

	public long getDiffTime();
	
	public long getSleepTime();
	
	public byte[] getHelloPacket();
	
	public IUdpHello getHello();
	
	public void updateLastPacket();
}
