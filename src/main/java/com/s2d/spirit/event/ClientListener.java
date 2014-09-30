package com.s2d.spirit.event;

public interface ClientListener
{
	public void onClose ( CloseEvent closeEvent );
	public void onMessage ( MessageEvent messageEvent );
	public void onBinMessage ( BinMessageEvent binEvent );
	public void onPing ( PingEvent pingEvent );
	public void onPong ( PongEvent pongEvent );
	public void onTimeout ( TimeoutEvent timeoutEvent );
}
