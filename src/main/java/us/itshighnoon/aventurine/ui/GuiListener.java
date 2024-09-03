package us.itshighnoon.aventurine.ui;

public interface GuiListener {
  public void onHover(int x, int y);
  
  public void onUnhover(int x, int y);
  
  public void onClick(int mouseButton, int x, int y);
  
  public void onScroll(int scrollVal, int x, int y);
}
