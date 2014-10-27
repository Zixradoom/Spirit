package com.s2d.spirit.tcp;

import java.net.InetAddress;

public interface TCPClientListener
{
  public void onClose ( TCPClient tcpClient );
  public void onException ( TCPClient tcpClient, Throwable t );
  public void onMessage ( TCPClient tcpClient, InetAddress remote, String message );
  public void onMessage ( TCPClient tcpClient, InetAddress remote, byte[] message );
}
