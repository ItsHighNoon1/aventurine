package us.itshighnoon.aventurine.ui;

public class GuiText {
  private String text;
  
  public GuiText() {
    
  }
  
  public void setText(String text) {
    if (this.text.equals(text)) {
      return;
    }
  }
}
