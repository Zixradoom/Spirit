package com.s2d.spirit.event;

import com.s2d.spirit.Client;

public final class PongEvent extends SpiritEvent
{
  public PongEvent ( long timestamp, Client source )
  {
    super ( timestamp, source, Type.PONG );
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
