/*
 * The MIT License
 *
 * Copyright 2016 tomis007.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package gameboi;


import java.awt.image.BufferedImage;
import javax.swing.JFrame;

/**
 * GPU for gameboy
 * 
 * Updates the graphics according to CPU and memory
 * 
 * referenced:
 * http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-GPU-Timings
 * http://gbdev.gg8.se/files/docs/mirrors/pandocs.html
 * http://www.codeslinger.co.uk/pages/projects/gameboy/lcd.html
 *
 * @author tomis007
 */
public class GPU {
    private final BufferedImage screenDisplay;
    private final LcdScreen lcdscreen;
    private final GBMem memory;
    
    /**
     * keeps clock timing relative to cpu
     * 456 clock cycles to draw each scanline
     */
    private int modeClock;


    /**
     * GPU MODE
     * 2: Scanline (OAM) 80 cycles
     * 3: Scanline (VRAM) 172 cycles
     * 0: Horizontal Blank 204 cycles
     * 1: Vertical Blank 4560 cycles
     */ 
    private static final int OAM_MODE = 2;
    private static final int VRAM_MODE = 3;
    private static final int HORIZ_BLANK = 0;
    private static final int VERT_BLANK = 1;



    /*
     * lcdc register information
     *
     * Bit 7: LCD Display enable (0=Off, 1=On)
     * Bit 6: Window Tile Map Display Select (0=9800-9Bff, 1 = 9c00-9fff)
     * Bit 5: Window Display Enable (0=Off, 1=On)
     * Bit 4: BG & Window Tile Data Select (0=8800-97ff, 1=8000-8fff)
     * Bit 3: BG Tile Map Display Select (0=9800-9bff, 1=9c00-9fff)
     * Bit 2: OBJ (Sprite) Size (0=8x8, 1=8x16)
     * Bit 1: OBJ (Sprite) Display Enable (0=Off, 1=ON)
     * Bit 0: BG Display (FOR GBC) (0=Off, 1=ON)
     */

    private static final int LCDC_CONTROL = 0xff40;
    private static final int LCDC_DISPLAY_ENABLE = 7;
    private static final int WINDOW_DISPLAY_ENABLE = 5;
    private static final int SPRITE_ENABLE = 1;
    private static final int BACKGROUND_ENABLE = 0;

    private static final int LCDC_STAT = 0xff41;
    private static final int SC_Y = 0xff42;
    private static final int SC_X = 0xff43;
    private static final int W_Y = 0xff4a;
    private static final int W_X = 0xff4b;


    //sprite flags
    private static final int PALETTE_NUM = 4;
    private static final int HORIZ_FLIP = 5;
    private static final int VERT_FLIP = 6;
    private static final int PRIORITY = 7;
    private static final int SPRITE_HEIGHT = 2;


    /**
     * access to the cpu for requesting interrupts 
     */ 
    private final CPU cpu;
    

    
    public GPU(GBMem memory, CPU cpu) {
        this.memory = memory;
        this.cpu = cpu;
        modeClock = 456;
        
        JFrame f = new JFrame("GameBoi");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        screenDisplay = new BufferedImage(160, 144, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < screenDisplay.getWidth(); ++i) {
            for (int j = 0; j < screenDisplay.getHeight(); ++j) {
                screenDisplay.setRGB(i, j, 0xffffffff); // white
            }
        }

        lcdscreen = new LcdScreen(screenDisplay);
        memory.setScanLine(0);
        f.add(lcdscreen);
        f.pack();
        f.addKeyListener(new gameboyKeyListener(memory, cpu)); 
        f.setVisible(true);
    }
    
  
    
    /**
     * Updates the GPU graphics, draws each scanline
     * after appropriate clock cycles have occurred
     *
     * Referred heavily to:
     * http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-GPU-Timings
     * and http://www.codeslinger.co.uk/pages/projects/gameboy/lcd.html
     * for information on timing
     *
     *
     * @param cycles - clock cycles progressed since last update
     */ 
    public void updateGraphics(int cycles) {
        if (!lcdEnabled()) {
            setModeOne();
        }

        modeClock -= cycles;
        checkLCDInterrupts();

        //not time to update scanline yet
        if (modeClock > 0) {
            return;
        }

        // reset for next scanline cycle
        modeClock = lcdEnabled() ? 456 : 4560;
        int currentScanLine = memory.getScanLine();
        if (currentScanLine == 144) {
            //vertical blank interrupt
            cpu.requestInterrupt(0);
        }
        if (currentScanLine < 152) {
            if (currentScanLine < 144 && lcdEnabled()) {
                renderScan(currentScanLine);
            } else if (currentScanLine < 144 && !lcdEnabled()) {
                drawWhiteLine(currentScanLine);
            }
            memory.incScanLine();
        } else {
            memory.setScanLine(0);
        }
    }
    
    
    /**
     * Returns the status of the lcd as indicated by the 
     * lcdc flag in memory
     * 
     * @return true if bit 7 of lcdc is 1, false if 0
     */ 
    private boolean lcdEnabled() {
        return isSet(memory.readByte(LCDC_CONTROL), LCDC_DISPLAY_ENABLE);
    }


    /**
     * renderScan()
     * 
     * Renders current scanline for the gpu
     * repaints the lcdscreen as well
     */
    private void renderScan(int currentScanLine) {
        int lcdc = memory.readByte(LCDC_CONTROL);
        
        if (isSet(lcdc, BACKGROUND_ENABLE)) {
            drawBackground(currentScanLine);
        }

        if (isSet(lcdc, WINDOW_DISPLAY_ENABLE)) {
            drawWindow(currentScanLine);
        }

        if (isSet(lcdc, SPRITE_ENABLE)) {
            drawSprites(currentScanLine);
        }
        lcdscreen.repaint();
    }
    
    
    /**
     * Updates the lcd flag in memory at 0xff41
     * according to elapsed cycles. Basically a
     * state machine with 4 states:
     * hblank, vblank, oam, vram. As each state
     * changes, interrupts requested if enabled
     */
    private void checkLCDInterrupts() {
        int flags = memory.readByte(LCDC_STAT);
        int currentScanLine = memory.getScanLine();
        int currentMode = flags & 0x3;
        int nextMode;
        boolean requestInterrupt = false;


        //Set next mode and check for interrupts
        if (currentScanLine >= 144) {
            nextMode = VERT_BLANK;
            requestInterrupt = isSet(flags, 4) && (currentMode != VERT_BLANK);
        } else {
            if (modeClock >= 376) {
                nextMode = OAM_MODE;
                requestInterrupt = isSet(flags, 5) && (currentMode != OAM_MODE);
            } else if (modeClock >= 204) {
                nextMode = VRAM_MODE;
            } else {
                nextMode = HORIZ_BLANK;
                requestInterrupt = isSet(flags, 3) && (currentMode != HORIZ_BLANK);
            }
        }

        if (requestInterrupt) {
            cpu.requestInterrupt(1); //LCD Interrupt
        }

        //check for LY == LYCc
        if (memory.readByte(0xff45) == currentScanLine) {
            flags |= 1 << 2;
            if (isSet(flags, 6)) {
                cpu.requestInterrupt(1);
            }
        } else {
            flags &= ~(1 << 2);
        }
        
        flags = setMode(flags, nextMode);
        memory.writeByte(LCDC_STAT, flags);
    }
    
    
    /**
     * Sets the next mode in flag
     * (lowest two bits)
     */ 
    private int setMode(int flag, int nextMode) {
        flag &= 0xfc;
        switch(nextMode) {
            case 0: return flag;
            case 1: return flag | 0x1;
            case 2: return flag | 0x2;
            case 3: return flag | 0x3;
            default: return flag;
        }    
    }


    /**
     * sets the GPU Mode to mode one
     * this occurs when the LCD is disabled
     *
     */
    private void setModeOne() {
        //set mode one
        int lcdFlags = memory.readByte(LCDC_STAT);
        lcdFlags = setMode(lcdFlags, VERT_BLANK);
        memory.writeByte(LCDC_STAT, lcdFlags);
    }



    /**
     * draws the background on screen for
     * the scanline
     *
     *
     * @param scanLine to draw background for
     */
    private void drawBackground(int scanLine) {
        int lcdc = memory.readByte(LCDC_CONTROL);
        int tileMapAddress = isSet(lcdc, 3) ? 0x9c00 : 0x9800;
        int scY = memory.readByte(SC_Y);
        int scX = memory.readByte(SC_X);
        int tileDataAddress = isSet(lcdc, 4) ? 0x8000 : 0x9000;
        boolean signedIndex = !isSet(lcdc, 4);

        //draw tiles on current scanLine
        int yOffset = (((scY + scanLine) / 8) % 32) * 32;
        int tileLine = (scY + scanLine) % 8;

        for (int xTile = 0; xTile <= 20; ++xTile) {
            int xOffset = (xTile + (scX / 8)) % 32;
            byte tileIndex = (byte)memory.readByte(tileMapAddress + yOffset + xOffset);
            int tileAddress = signedIndex ? (tileIndex * 16) + tileDataAddress
                                          : (Byte.toUnsignedInt(tileIndex) * 16) + tileDataAddress;

            //calculate correct shift from scX
            int xShift = scX % 8;
            if (xTile == 0 && xShift != 0) {
                drawTile(tileAddress, tileLine, 0, scanLine, xShift, 7);
            } else if (xTile == 20 && xShift != 0) {
                drawTile(tileAddress, tileLine, 160 - xShift, scanLine, 0, xShift - 1);
            } else if (xTile != 20) {
                drawTile(tileAddress, tileLine, (xTile * 8) - xShift, scanLine, 0, 7);
            }
        }
    }


    /**
     *
     * draws a white background onto the lcd screen
     * 144 x 160
     */
    private void drawWhiteLine(int scanline) {
            for (int col = 0; col < 160; col++) {
                screenDisplay.setRGB(col, scanline, 0xffffffff);
            }
        lcdscreen.repaint();
    }



    /**
     *
     * draws the 8x8 tile located in memory
     * at address (starting byte). Places
     * top left corner of tile at xPos,yPos
     * on LCD screen
     *
     * NOTE: if window enabled, and overlaps the
     *       address for the background tile to be
     *       drawn at, the window takes priority and
     *       the background tile is not drawn
     *
     * @param tileAddress starting byte of tile data
     *        to draw
     * @param xPos of the upper left hand corner of the tile
     *             on the 160-144 LCD screen
     * @param yPos of the upper left hand corner of the tile
     *             on the 160-144 LCD screen
     * @param pixEnd the last pixel index in each line to draw (ie pixel 6 will draw
     *               the pixels from 0 - 6 and not 7)
     * @param pixStart the first pixel index in each line to start drawing from (ie pixel
     *                 six will start drawing at pixel 6) this is placed at xPos
     * @param line in the tile to draw (0 - 7)
     */
    private void drawTile(int tileAddress, int line, int xPos, int yPos, int pixStart, int pixEnd) {
        int paletteAddress = 0xff47;
        int pixByteA = memory.readByte(tileAddress + (2 * line));
        int pixByteB = memory.readByte(tileAddress + (2 * line) + 1);
        int wX = memory.readByte(W_X) - 7;
        int wY = memory.readByte(W_Y);
        boolean windowDrawn = isSet(memory.readByte(LCDC_CONTROL), WINDOW_DISPLAY_ENABLE);

        //draw each pixel in the line
        for (int pixel = pixStart; pixel <= pixEnd; ++pixel) {
            int colorNum = getPixelColorNum(pixByteA, pixByteB, pixel);
            int xCoord = (xPos + pixel - pixStart) % 160;

            if (wY <= yPos && xCoord >= wX && windowDrawn) {
                break; //window will be drawn at this position
            } else {
                screenDisplay.setRGB(xCoord, yPos, getColor(colorNum, paletteAddress));
            }
        }
    }


    /**
     * Draws one window tile line at specified position
     * Window doesn't wrap
     *
     * @param tileAddress of the tile
     * @param line of the tile to draw (0 - 7)
     * @param xPos on screen to draw top left of line
     * @param yPos on screen to draw top left of line
     */
    private void drawWindowTile(int tileAddress, int line, int xPos, int yPos) {
        int paletteAddress = 0xff47;

        int pixByteA = memory.readByte(tileAddress + (2 * line));
        int pixByteB = memory.readByte(tileAddress + (2 * line) + 1);

        //draw each pixel in the line
        for (int pixel = 0; pixel <= 7; ++pixel) {
            int colorNum = getPixelColorNum(pixByteA, pixByteB, pixel);
            int xCord = (xPos + pixel);
            if (xCord < 160 && yPos < 144 && xCord >= 0 && yPos >= 0) {
                screenDisplay.setRGB(xCord, yPos, getColor(colorNum, paletteAddress));
            }
        }
    }




    /**
     * draws the window onto the LCD display screen
     * as specified by wX and wY registers
     */
    private void drawWindow(int scanLine) {
        int lcdc = memory.readByte(LCDC_CONTROL);
        int wX = memory.readByte(W_X);
        int wY = memory.readByte(W_Y);
        int tileMapAddress = isSet(lcdc, 6) ? 0x9c00 : 0x9800;
        int tileDataAddress = isSet(lcdc, 4) ? 0x8000 : 0x9000;
        boolean signedIndex = !isSet(lcdc, 4);


        if (wY > 143 || wX > 166 || wY > scanLine) {
            return; //window not on screen or on this line
        }

        //draw tiles on current scanLine
        int yOffset = ((scanLine - wY) / 8) * 32;
        int tileLine = (scanLine - wY) % 8;

        for (int xTile = 0; xTile <= 20; ++xTile) {
            byte tileIndex = (byte)memory.readByte(tileMapAddress + yOffset + xTile);
            int tileAddress = signedIndex ? (tileIndex * 16) + tileDataAddress
                                          : (Byte.toUnsignedInt(tileIndex) * 16) + tileDataAddress;

            int xCoord = (wX - 7) + (xTile * 8);
            drawWindowTile(tileAddress, tileLine, xCoord, scanLine);
        }
    }

    /**
     *
     * draws the sprites onto the LCD screen
     *
     *
     *     todo SCANLINE LIMIT of 10 SPRITES
     */
    private void drawSprites(int scanLine) {
        int lcdc = memory.readByte(LCDC_CONTROL);
        int height = isSet(lcdc, SPRITE_HEIGHT) ? 16 : 8;

        for (int i = 0; i < 40; ++i){
            int offset = (39 - i) * 4;
            int yPos = memory.readByte(0xfe00 + offset);
            int xPos = memory.readByte(0xfe00 + offset + 1);
            int tileNum = memory.readByte(0xfe00 + offset + 2);
            int flags = memory.readByte(0xfe00 + offset + 3);

            if (height == 16) {
                tileNum &= 0xfe;
            }
            if (xPos < 168 && xPos > 0 && yPos < 160 && yPos > 0) {
                int address = (tileNum * 16) + 0x8000;
                drawSprite(xPos - 8, yPos - 16, address, flags, height, scanLine);
            }
        }
    }


    /**
     * draws the sprite at address
     * to the screen at xPos, yPos (top left corner
     * @param xPos of LCD screen to display top left
     * @param yPos of LCD screen to display top left
     * @param address of first byte of sprite data
     * @param height of the sprite (8 or 16)
     * @param flags associated with the sprite
     *
     * TODO scanline LIMIT
     */
    private void drawSprite(int xPos, int yPos, int address, int flags, int height, int scanLine) {
        boolean vertFlip = isSet(flags, VERT_FLIP);
        boolean horizFlip = isSet(flags, HORIZ_FLIP);
        boolean hasPriority = !isSet(flags, PRIORITY);
        int paletteAddress = isSet(flags, PALETTE_NUM) ? 0xff49 : 0xff48;

        for (int i = 0; i < height; ++i) {
            int pixDataA;
            int pixDataB;

            if (vertFlip) {
                int offset = (2 * (height - i)) - 2;
                pixDataA = memory.readByte(address + offset);
                pixDataB = memory.readByte(address + offset + 1);
            } else {
                int offset = (2 * i);
                pixDataA = memory.readByte(address + offset);
                pixDataB = memory.readByte(address + offset + 1);
            }
            if (yPos + i < 144 && yPos + i == scanLine) {
                drawSpriteLine(xPos, yPos + i, pixDataA, pixDataB,
                        horizFlip, hasPriority, paletteAddress);
            }
        }
    }


    /**
     * draws one line of a sprite
     *
     *
     * @param xPos left most corner of line to start (lcd screen) ( < 160)
     * @param yPos vertical position of line (lcd screen) ( < 144)
     * @param pixDataA of line
     * @param pixDataB of line
     * @param horizFlip flip horizontal (boolean)
     */
    private void drawSpriteLine(int xPos, int yPos, int pixDataA, int pixDataB,
                                boolean horizFlip, boolean hasPriority, int paletteAddress) {
        for (int pix = 0; pix < 8; ++pix) {
            int colorIndex = horizFlip ? 7 - pix : pix;

            int colorNum = getPixelColorNum(pixDataA, pixDataB, colorIndex);
            int color = getColor(colorNum, paletteAddress);
            if (xPos + pix < 160 && xPos + pix >= 0 && colorNum != 0 && yPos >= 0 && yPos < 144) {
                if (hasPriority) {
                    screenDisplay.setRGB(xPos + pix, yPos, color);
                } else {
                    if (getColor(0, 0xff47) == screenDisplay.getRGB(xPos + pix, yPos)) {
                        screenDisplay.setRGB(xPos + pix, yPos, color);
                    }
                }
            }
        }
    }


    /**
     * Returns the colorNumber specified by the two
     * bytes of pixel data of pixIndex pixel
     *
     *
     * NOTE: if pixel data is 0xf0 and 0xf0
     * pixel 7 will be [0,0] and pixel 0 will be [1,1]
     *
     *
     * @param pixDataA first byte of data
     * @param pixDataB second byte of data
     * @param pixIndex index of pixel to check (0 - 7)
     * @return colorNum (0 - 3) of appropriate pixel
     */
    private int getPixelColorNum(int pixDataA, int pixDataB, int pixIndex) {
        int colorNum = isSet(pixDataB, 7 - pixIndex) ? 1 : 0;
        colorNum = (colorNum << 1) | (isSet(pixDataA, 7 - pixIndex) ? 1 : 0);

        return colorNum;
    }




    /**
     * isSet
     * 
     * Tests the num if the bitNum bit is set
     * 
     * @param num number to test
     * @param bitNum bitnumber to test
     */ 
    private boolean isSet(int num, int bitNum) {
        return (((num >> bitNum) & 0x1) == 1);
    }


    /**
     * Translates the pixColor (0 - 3) to actual
     * color RGB
     *
     * @param pixNum from tile/sprite to translate
     * @param palAddress to interpret the pix number
     * @return a RGB value representing the actual color
     */
    private int getColor(int pixNum, int palAddress) {
        int palette = memory.readByte(palAddress);
        int colSelect;
        
        
        switch(pixNum) {
            case 0: colSelect = (palette & 0x3);
                    break;
            case 1: colSelect = (palette >> 2) & 0x3;
                    break;
            case 2: colSelect = (palette >> 4) & 0x3;
                    break;       
            case 3: colSelect = (palette >> 6) & 0x3;
                    break;
            default:
                    colSelect = 0;
                    break;
        }
        
        int color;
        switch (colSelect & 0x3) {
            case 0: color = 0xffffffff;
                    break;
            case 1: color = 0xffcccccc;
                    break;
            case 2: color = 0xff777777;
                    break;
            case 3: color = 0xff000000;
                    break;
            default: color = 0xffffffff;
        }
        return color;
    }
}
