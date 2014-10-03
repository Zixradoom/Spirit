package com.s2d.spirit.udp.event;

import com.s2d.spirit.udp.MulticastClient;

public interface MulticastClientListener
{

  public void onException ( MulticastClient client, Throwable t );

}
