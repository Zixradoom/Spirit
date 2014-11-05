package com.s2d.spirit.event;

/**
 * Empty implementation of the {@link ClientListener}
 * @author Anthony J Simon
 *
 */
public class ClientAdapter implements ClientListener
{
  @Override
  public void onClose ( CloseEvent closeEvent ) {}

  @Override
  public void onMessage ( MessageEvent messageEvent ) {}

  @Override
  public void onBinMessage ( BinMessageEvent binEvent ) {}

  @Override
  public void onPing ( PingEvent pingEvent ) {}

  @Override
  public void onPong ( PongEvent pongEvent ) {}

  @Override
  public void onTimeout ( TimeoutEvent timeoutEvent ) {}
}
