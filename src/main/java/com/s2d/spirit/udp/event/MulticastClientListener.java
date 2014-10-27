package com.s2d.spirit.udp.event;

import java.net.InetAddress;

import com.s2d.spirit.udp.MulticastClient;

public interface MulticastClientListener
{
  public void onException ( MulticastClient client, Throwable t );
  public void onMessage ( MulticastClient multicastClient, InetAddress remote, String message );
  public void onMessage ( MulticastClient multicastClient, InetAddress remote, byte[] message );
  public void onClose ( MulticastClient multicastClient );
}
