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
 * Referred heavily to:
 * http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-GPU-Timings
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


    //lcdc flag constants
    private static final int LCDC_DISPLAY_ENABLE = 7;
    private static final int WINDOW_DISPLAY_ENABLE = 5;
    private static final int SPRITE_ENABLE = 1;
    private static final int BACKGROUND_ENABLE = 0;

    /**
     * access to the cpu for requesting interrupts 
     */ 
    private final CPU cpu;
    
    /**
     * Bit 7: LCD Display enable (0=Off, 1=On)
     * Bit 6: Window Tile Map Display Select (0=9800-9Bff, 1 = 9c00-9fff)
     * Bit 5: Window Display Enable (0=Off, 1=On)
     * Bit 4: BG & Window Tile Data Select (0=8800-97ff, 1=8000-8fff)
     * Bit 3: BG Tile Map Display Select (0=9800-9bff, 1=9c00-9fff)
     * Bit 2: OBJ (Sprite) Size (0=8x8, 1=8x16)
     * Bit 1: OBJ (Sprite) Display Enable (0=Off, 1=ON)
     * Bit 0: BG Display (FOR GBC) (0=Off, 1=ON)
     */ 
    //private int lcdc;
    
    
    
    public GPU(GBMem memory, CPU cpu) {
        this.memory = memory;
        this.cpu = cpu;
        modeClock = 456;
        
        JFrame f = new JFrame("GameBoi");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        screenDisplay = new BufferedImage(160, 144, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < screenDisplay.getWidth(); ++i) {
            for (int j = 0; j < screenDisplay.getHeight(); ++j) {
                screenDisplay.setRGB(i, j, 0xff000000); // Black
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
     * Updates the GPU graphics
     * 
     * 
     * Referred heavily to:
     * http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-GPU-Timings
     * and http://www.codeslinger.co.uk/pages/projects/gameboy/lcd.html
     * @param cycles - clock cycles progressed since last update
     */ 
    public void updateGraphics(int cycles) {
        modeClock -= cycles;
        updateLCDMode();

        //not time to update scanline yet
        if (modeClock > 0) {
            return;
        }


        modeClock = 456; // reset for next scanline cycle
        int currentScanLine = memory.getScanLine();
        if (currentScanLine == 144) {
            cpu.requestInterrupt(0); //vertical blank interrupt
            //time to draw the current frame
            renderScan();
            lcdscreen.repaint();
        } else if (currentScanLine > 152) {
            memory.setScanLine(0);
//            modeClock = 4560; //account for vertical blank timing
        } else if (currentScanLine < 144) {
//            renderScanLineTiles();
        }

        if (currentScanLine <= 152) {
            memory.incScanLine();
        }
    }
    
    
    /**
     * Returns the status of the lcd as indicated by the 
     * lcdc flag in memory
     * 
     * @return true if bit 7 of lcdc is 1, false if 0
     */ 
    private boolean lcdEnabled() {
        return isSet(memory.readByte(0xff40), LCDC_DISPLAY_ENABLE);
    }
    
    /**
     * renderScan()
     * 
     * Renders current scanline for the gpu
     */
    private void renderScan() {
        // get flags
        int lcdc = memory.readByte(0xff40);
        
        if (isSet(lcdc, BACKGROUND_ENABLE)) {
            drawBackground();
        }

        if (isSet(lcdc, WINDOW_DISPLAY_ENABLE)) {
            drawWindow();
        }

        if (isSet(lcdc, SPRITE_ENABLE)) {
//            drawSprites();
        }
        
    }
    
    
    /**
     * Updates the lcd flag in memory at 0xff41
     * according to elapsed cycles
     * States change between
     * hblank, vblank, oam, vram
     *
     * requests interrupts as state changes
     */ 
    private void updateLCDMode() {
        if (!lcdEnabled()) {
            return;
        }

        int flags = memory.readByte(0xff41);
        int currentScanLine = memory.getScanLine();
        int currentMode = flags & 0x3;
        int nextMode;
        boolean requestInterrupt = false;


        //Set next mode and check for interrupts
        if (currentScanLine >= 144) {
            nextMode = VERT_BLANK;
            requestInterrupt = isSet(flags, 4) && (currentMode != VERT_BLANK);
        } else { //todo
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
        memory.writeByte(0xff41, flags);
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
     * Resets the state in 0xff41 to 1
     * Sets the modeClock back to 456, and the scanline itself
     * int memory to 0
     */ 
    private void resetToModeOne() {
        int flags = memory.readByte(0xff41);
        modeClock = 456;
        memory.setScanLine(0);
        //set state to 01
        flags &= 252;
        flags |= 0x1;
        memory.writeByte(0xff41, flags);
    }


    /**
     * Draws the background on the 'LCD'
     * screen
     *
     * Gameboy screen is 160x144
     * or 20 x 18 tiles (of 8x8). This draws
     * each of the tiles to create the background
     *
     *
     * todo yPosition offset (scY % 8 != 0)
     *
     */
    private void drawBackground() {
        int lcdc = memory.readByte(0xff40);
        int tileMapAddress = isSet(lcdc, 3) ? 0x9c00 : 0x9800;
        int scY = memory.readByte(0xff42);
        int scX = memory.readByte(0xff43);
        int tileDataAddress = isSet(lcdc, 4) ? 0x8000 : 0x9000;
        boolean signedIndex = !isSet(lcdc, 4);

        //draw all the tiles
        for (int yTile = 0; yTile < 18; yTile++) {
            if (scY % 8 != 0) {
                System.err.println("INVALID SCROLL Y WRAPPING");
            }
            int yOffset = (((scY + (yTile * 8)) / 8) % 32) * 32;

            for (int xTile = 0; xTile < 20; ++xTile) {
                int xOffset = (xTile + (scX / 8)) % 32;
                byte tileIndex = (byte)memory.readByte(tileMapAddress + yOffset + xOffset);

                int tileAddress = signedIndex ? (tileIndex * 16) + tileDataAddress
                                              : (Byte.toUnsignedInt(tileIndex) * 16) + tileDataAddress;

                if (xTile == 0) {
                    drawTile(tileAddress, xTile * 8, yTile * 8, scX % 8, 7);
                } else if (xTile == 19) {
                    drawTile(tileAddress, (xTile * 8) - (scX % 8), yTile * 8, 0, 7 - (scX % 8));
                } else {
                    drawTile(tileAddress, (xTile * 8) - (scX % 8), yTile * 8, 0, 7);
                }
            }
        }
    }



    /**
     *
     * draws the 8x8 tile located in memory
     * at address (starting byte). Places
     * top left corner of tile at xPos,yPos
     * on LCD screen
     *
     * @param tileAddress starting byte of tile data
     *        to draw
     * @param xPos of the upper left hand corner of the tile
     *             on the 160-144 LCD screen
     * @param yPos of the upper left hand corner of the tile
     *             on the 160-144 LCD screen
     */
    private void drawTile(int tileAddress, int xPos, int yPos, int pixStart, int pixEnd) {
        int paletteAddress = 0xff47;

        //draw each line of the tile
        for (int line = 0; line <= 7; ++line) {
            int pixByteA = memory.readByte(tileAddress + (2 * line));
            int pixByteB = memory.readByte(tileAddress + (2 * line) + 1);

            //draw each pixel in the line
            for (int pixel = pixStart; pixel <= pixEnd; ++pixel) {
                int colorNum = getPixelColorNum(pixByteA, pixByteB, pixel);
                int xCord = (xPos + pixel) % 160;
                int yCord = (yPos + line) % 144;
                screenDisplay.setRGB(xCord, yCord, getColor(colorNum, paletteAddress));
            }
        }
    }


    /**
     *
     * draws the window onto the LCD display screen
     * TODO Wrapping (wX % 8 != 0)
     */
    private void drawWindow() {
        int lcdc = memory.readByte(0xff40);
        int wX = memory.readByte(0xff4b);
        int wY = memory.readByte(0xff4a);

        if (wY > 143 || wX > 166) {
            return; //window not visible on screen
        }

        int tileMapAddress = isSet(lcdc, 6) ? 0x9c00 : 0x9800;
        int tileDataAddress = isSet(lcdc, 4) ? 0x8000 : 0x9000;
        boolean signedIndex = !isSet(lcdc, 4);


        //draw window Tiles that appear on screen
        for (int yTile = 0; yTile < 18 && ((yTile * 8) + wY) < 144; yTile++) {
            for (int xTile = 0; xTile < 20 && ((xTile * 8) + wX) < 166; xTile++) {
                byte tileIndex = (byte)memory.readByte(tileMapAddress + (yTile * 32) + xTile);
                int tileAddress = signedIndex ? (tileIndex * 16) + tileDataAddress
                        : (Byte.toUnsignedInt(tileIndex) * 16) + tileDataAddress;


                drawTile(tileAddress, (xTile * 8) + wX - 7, (yTile * 8) + wY, 0, 7);
            }
        }
    }



    /**
     *
     * draws the sprites onto the background
     *
     *
     * TODO!!!!!! priorities not correct etc...
     * TODO also doesnt work...
     */
    private void drawSprites() {
        int lcdc = memory.readByte(0xff40);
        int height = isSet(lcdc, 2) ? 16 : 8; //8x16 mode or 8x8 mode
        int currentScanLine = memory.getScanLine();

        //scan sprites and draw ones that
        //are on current scanline todo correct priorities, limit

        for (int i = 0; i < 40; ++i) {
            int yPos = memory.readByte((i * 4) + 0xfe00) - 16;
            int xPos = memory.readByte((i * 4) + 0xfe00 + 1) - 8;
            int patternNumber = memory.readByte((i * 4) + 0xfe00 + 2);
            int flags = memory.readByte((i * 4) + 0xfe00 + 3);

            //need to draw sprite
            if ((currentScanLine >= yPos) && (currentScanLine < (yPos + height))) {
                boolean flipHoriz = isSet(flags, 5);

                //todo flip Vertical
                boolean flipVert = isSet(flags, 6);

                //todo priority with background


                //read correct pixel data from memory for appropriate line of sprite
                int byteAddress = 0x8000 + (patternNumber * 16) + ((currentScanLine - yPos) * 2);

                int pixDataA = memory.readByte(byteAddress);
                int pixDataB = memory.readByte(byteAddress + 1);

                for (int pixel = 0; pixel < 8; pixel++) {
                    int colorNum;
                    if (flipHoriz) {
                        colorNum = getPixelColorNum(pixDataA, pixDataB, 7 - pixel);
                    } else {
                        colorNum = getPixelColorNum(pixDataA, pixDataB, pixel);
                    }

                    int paletteAddress = isSet(flags, 4) ? 0xff49 : 0xff48;

                    int pixColor = getColor(colorNum, paletteAddress);

                    if (pixColor != 0xffffffff) {
                        screenDisplay.setRGB(xPos + pixel, currentScanLine, pixColor);
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
     * color RGB (todo?)
     *
     * @param pixColor
     * @param palAddress
     * @return
     */
    private int getColor(int pixColor, int palAddress) {
        int palette = memory.readByte(palAddress);
        int colSelect;
        
        
        switch(pixColor) {
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





    

