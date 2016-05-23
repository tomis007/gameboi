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

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

/**
 * GPU for gameboy
 * 
 * Referred heavily to:
 * http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-GPU-Timings
 * 
 * @author tomis007
 */
public class GPU {
    private BufferedImage screenDisplay;
    private LcdScreen lcdscreen;
    private GBMem memory;
    
    /**
     * keeps clock timing relative to cpu 
     */
    private int modeClock;
    /**
     * contains current mode of the gpu
     * 4 values
     * 2: Scanline (OAM) 80 cycles
     * 3: Scanline (VRAM) 172 cycles
     * 0: Horizontal Blank 204 cycles
     * 1: Vertical Blank 4560 cycles
     */ 
    private int gpuMode;
    private int scanLine;
    
    
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
    private int lcdc;
    
    
    
    public GPU(GBMem memory) {
        this.memory = memory;
        
        JFrame f = new JFrame("GameBoi");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        screenDisplay = new BufferedImage(160, 144, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < screenDisplay.getWidth(); ++i) {
            for (int j = 0; j < screenDisplay.getHeight(); ++j) {
                screenDisplay.setRGB(i, j, 0xffff0000); // Red
            }
        }

        lcdscreen = new LcdScreen(screenDisplay);
        
        f.add(lcdscreen);
        f.pack();
        f.setVisible(true);
    }
    
    
    
    /**
     * Updates the GPU graphics
     * 
     * 
     * Referred heavily to:
     * http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-GPU-Timings
     * @param cycles - clock cycles progressed since last update
     */ 
    public void updateGraphics(int cycles) {
        scanLine = memory.getScanLine();
        modeClock += cycles;
        
        switch(gpuMode) {
            case 2:
                if (modeClock >= 80) {
                    modeClock = 0;
                    gpuMode = 3;
                }
                break;
            case 3:
                if (modeClock >= 172) {
                    modeClock = 0;
                    gpuMode = 0;
                    renderScan();
                }
                break;
            case 0:
                if (modeClock >= 204) {
                    modeClock = 0;
                    scanLine++;
                    if (scanLine >= 143) {
                        gpuMode = 1;
                        lcdscreen.repaint();
                    } else {
                        gpuMode = 2;
                    }
                }
                break;
            case 1:
                if (modeClock >= 456) {
                    modeClock = 0;
                    scanLine++;
                    if (scanLine > 153) {
                        gpuMode = 2;
                        scanLine = 0;
                    }
                }
                break;
            default:
                break;
        }
        memory.setScanLine(scanLine);
    }
    
    
    /**
     * renderScan()
     * 
     * Renders current scanline for the gpu
     * 
     * 
     * 
     */ 
    private void renderScan() {
        // get flags
        lcdc = memory.readByte(0xff40);
        
        if (isSet(lcdc, 0)) {
            drawTiles();
        }
    
        if (isSet(lcdc, 1)) {
            drawSprites();
        }
        
    }
    
    
    /**
     * drawTiles
     * 
     * Draw the tiles for the current scan line
     * 
     */ 
    private void drawTiles() {
        // get flags
        lcdc = memory.readByte(0xff40);
        //background coordinates
        int scX = memory.readByte(0xff43);
        int scY = memory.readByte(0xff42);
        //window coordinates
        int wY = memory.readByte(0xff4a);
        int wX = memory.readByte(0xff4b) - 7;

        // get first tile data address
        int tileDataAddress = (isSet(lcdc, 4)) ? 0x8000 : 0x8800;
        //test to see if going to draw window
        boolean drawWindow = (wY <= scanLine) && isSet(lcdc, 5);
        
        //get background tile address
        // NOTE??????
        int backgroundAddress;
        if (drawWindow) {
            backgroundAddress = isSet(lcdc, 6) ? 0x9c00 : 0x9800;
        } else {
            backgroundAddress = isSet(lcdc, 3) ? 0x9c00 : 0x9800;
        }
        
        int yPos = drawWindow ? scY + scanLine : scanLine - wY;
        int tileRow = (yPos / 8) * 32;
        
        for (int pix = 0; pix < 160; ++pix) {
            int xPos = (drawWindow && pix >= wX) ? pix - wX: pix + scX;
            int tileCol = xPos / 8;
            
            int tileAddress = backgroundAddress + tileRow + tileCol;
            
            int tileNum = memory.readByte(tileAddress);
            
            int tileLoc = tileDataAddress;
            
            tileLoc += isSet(lcdc, 4) ? tileNum * 16 : ((byte)tileNum + 128) * 16; 
            int line = yPos * 16;
            int pixDatA = memory.readByte(line + tileLoc);
            int pixDatB = memory.readByte(line + tileLoc + 1);
            
            int colorBit = 7 - (xPos % 8);
            int colorNum = (pixDatB >> colorBit) & 0x1;
            colorNum = (colorNum << 1) | ((pixDatA >> colorBit) & 0x1);
            
            screenDisplay.setRGB(pix, scanLine, getColor(colorNum, 0xff47));
        }
        
    }
    
    private void drawSprites() {
    
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
    
    private int getColor(int pixCode, int palAddress) {
        int palette = memory.readByte(palAddress);
        int colSelect;
        
        
        switch(pixCode) {
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





    

