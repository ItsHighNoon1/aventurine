package us.itshighnoon.aventurine.render.mem;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;

import us.itshighnoon.aventurine.util.Logger;
import us.itshighnoon.aventurine.util.ResourceManager;

public class Font {
  private int textureId;
  private float lineHeight;
  private float base;
  private int scaleW;
  private int scaleH;
  private Map<Integer, Glyph> glyphs;

  public Font(String fntFile) {
    Path fontPath = Path.of(fntFile);
    glyphs = new HashMap<Integer, Glyph>();
    Logger.log("0067 Loading font " + fontPath.toString(), Logger.Severity.INFO);
    try {
      DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fontPath.toString())));
      int headerInfo = dis.readInt();
      if ((headerInfo & 0xFF) != 3) {
        Logger.log("0063 Font file " + fontPath.toString() + " not version 3", Logger.Severity.WARN);
      }
      while (dis.available() > 0) {
        int blockType = dis.readByte();
        int blockSize = Integer.reverseBytes(dis.readInt());
        switch (blockType) {
        case 1:
          // Info that we can safely ignore
          dis.skipBytes(blockSize);
          break;
        case 2:
          // Common
          int lineHeightInt = Short.toUnsignedInt(Short.reverseBytes(dis.readShort()));
          int baseInt = Short.toUnsignedInt(Short.reverseBytes(dis.readShort()));
          scaleW = Short.toUnsignedInt(Short.reverseBytes(dis.readShort()));
          scaleH = Short.toUnsignedInt(Short.reverseBytes(dis.readShort()));
          this.lineHeight = (float) lineHeightInt / scaleH;
          this.base = (float) baseInt / scaleH;
          dis.skipBytes(7);
          break;
        case 3:
          // Pages, tells us where the texture is
          byte[] blockBytes = new byte[blockSize];
          dis.read(blockBytes);
          int stringStart = 0;
          String texturePath = null;
          for (int stringEnd = 0; stringEnd < blockBytes.length; stringEnd++) {
            if (blockBytes[stringEnd] == 0) {
              if (texturePath != null) {
                Logger.log("0064 Font file " + fontPath.toString() + " contains multiple pages", Logger.Severity.WARN);
              }
              texturePath = new String(blockBytes, stringStart, stringEnd - stringStart);
              stringStart = stringEnd + 1;
            }
          }
          if (texturePath == null) {
            Logger.log("0065 Font file " + fontPath.toString() + " has no pages", Logger.Severity.ERROR);
            textureId = 0;
          } else {
            textureId = ResourceManager.loadTexture(fontPath.getParent().resolve(texturePath).toString());
          }
          break;
        case 4:
          // Glyph data
          int nChars = blockSize / 20;
          for (int charIdx = 0; charIdx < nChars; charIdx++) {
            int charId = Integer.reverseBytes(dis.readInt());
            int charX = Short.toUnsignedInt(Short.reverseBytes(dis.readShort()));
            int charY = Short.toUnsignedInt(Short.reverseBytes(dis.readShort()));
            int charW = Short.toUnsignedInt(Short.reverseBytes(dis.readShort()));
            int charH = Short.toUnsignedInt(Short.reverseBytes(dis.readShort()));
            int charXOff = Short.reverseBytes(dis.readShort());
            int charYOff = Short.reverseBytes(dis.readShort());
            int charXAdv = Short.reverseBytes(dis.readShort());
            dis.skipBytes(2);
            Glyph glyph = new Glyph((float) charX / scaleW, (float) (scaleH - charY - charH) / scaleH, (float) charW / scaleW,
                (float) charH / scaleH, (float) charXAdv / scaleW, (float) charXOff / scaleW,
                (float) charYOff / scaleH);
            glyphs.put(charId, glyph);
          }
          break;
        case 5:
          // Kerning pairs, which I may implement later
          dis.skipBytes(blockSize);
          break;
        default:
          Logger.log("0066 Font file " + fontPath.toString() + " contains unknown block " + blockType,
              Logger.Severity.WARN);
        }
      }
      dis.close();
    } catch (IOException e) {
      Logger.log("0062 Failed to load font " + fontPath.toString(), Logger.Severity.ERROR);
    }
  }

  public void cleanup() {
    ResourceManager.unloadTexture(textureId);
  }

  public int getTexture() {
    return textureId;
  }

  public Glyph getGlyph(int charCode) {
    return glyphs.get(charCode);
  }

  public float getLineHeight() {
    return lineHeight;
  }

  public float getBase() {
    return base;
  }

  public static class Glyph {
    private Vector2f texLocation;
    private Vector2f texSize;
    private float xAdvance;
    private float xOffset;
    private float yOffset;

    public Glyph(float x, float y, float texWidth, float texHeight, float xAdvance, float xOffset, float yOffset) {
      super();
      this.texLocation = new Vector2f(x, y);
      this.texSize = new Vector2f(texWidth, texHeight);
      this.xAdvance = xAdvance;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
    }

    public Vector2f getTexLocation() {
      return texLocation;
    }

    public Vector2f getTexSize() {
      return texSize;
    }

    public float getxAdvance() {
      return xAdvance;
    }

    public float getxOffset() {
      return xOffset;
    }

    public float getyOffset() {
      return yOffset;
    }
  }
}
