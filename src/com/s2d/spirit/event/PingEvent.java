package com.s2d.spirit.event;

import com.s2d.spirit.Client;

public final class PingEvent extends SpiritEvent
{
  public PingEvent ( long timestamp, Client source )
  {
    super ( timestamp, source, Type.PING );
  }

  @Override
  public Client getSource ()
  {
    return Client.class.cast ( super.getSource () );
  }
  
  public Client getClient ()
  {
    return getSource ();
  }
}
