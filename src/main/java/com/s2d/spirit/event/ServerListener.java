package com.s2d.spirit.event;

public interface ServerListener
{
	public void onConnect ( ConnectEvent connectEvent );
	public void onTimeout ( ServerTimeoutEvent serverTimeoutEvent );
	public void onClose ( ServerCloseEvent serverCloseEvent );
}
