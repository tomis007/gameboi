/*
 * The MIT License
 *
 * Copyright 2017 tomis007.
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
package main.java.gameboi.gpu;


import main.java.gameboi.cpu.CPU;
import main.java.gameboi.joypad.gameboyKeyListener;
import main.java.gameboi.memory.GBMem;

import java.awt.image.BufferedImage;
import javax.swing.*;
import java.nio.ByteBuffer;

/**
 * GPU for gameboy
 * 
 * Updates the graphics according to CPU and memory
 *
 * TODO: Still buggy, Dr.Mario doesn't work?
 * 
 * referenced:
 * http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-GPU-Timings
 * http://gbdev.gg8.se/files/docs/mirrors/pandocs.html
 * http://www.codeslinger.co.uk/pages/projects/gameboy/lcd.html
 *
 * @author tomis007
 */
public class GPU {
    private final GBMem memory;
    private ByteBuffer buffer;
    private final CPU cpu;
    /**
     * keeps clock timing relative to cpu
     * 456 clock cycles to draw each scanline
     */
    private int modeClock;
    private boolean prev_enabled;
    private boolean gbcMode;

    private int currentMode;

    private static int BYTE_SAVE_LENGTH = 3;

    /**
     * GPU MODE
     * 2: Scanline (OAM) 80 cycles
     * 3: Scanline (VRAM) 172 cycles
     * 0: Horizontal Blank 204 cycles
     * 1: Vertical Blank 4560 cycles
     */
    //TODO ENUM?
    private static final int OAM_MODE = 2;
    private static final int LCD_TRANS = 3;
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
    private static final int LYC = 0xff45;
    private static final int W_X = 0xff4b;


    //sprite flags
    private static final int PALETTE_NUM = 4;
    private static final int HORIZ_FLIP = 5;
    private static final int VERT_FLIP = 6;
    private static final int PRIORITY = 7;
    private static final int SPRITE_HEIGHT = 2;


    public GPU(GBMem memory, CPU cpu) {
        this.memory = memory;
        this.cpu = cpu;
        modeClock = 456;
        buffer = ByteBuffer.allocate(69120);
        prev_enabled = true;
        currentMode = OAM_MODE;
        this.memory.setScanLine(0);
        this.gbcMode = false;
    }

    /**
     * Updates the GPU graphics, draws each scanline
     * after appropriate clock cycles have occurred
     *
     *
     * NOTE: operation when lcd is disabled probably isn't accurate,o
     *       but the current behavior seems to work...
     *
     * Referred heavily to:
     * http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-GPU-Timings
     * for information on timing
     *
     *
     * @param cycles - clock cycles progressed since last update
     */ 
    public void updateGraphics(int cycles) {
        if (lcdEnabled() && prev_enabled) {
            // dispatch mode
            dispatchMode(cycles);
        } else if (lcdEnabled() && !prev_enabled) {
            //start in HBLAnk
            set_mode(HORIZ_BLANK, 0);
            dispatchMode(cycles);
            prev_enabled = true;
        } else {
            //disabled set to VBlank
            if (currentMode != VERT_BLANK) {
                set_mode(VERT_BLANK, 0);
                memory.setScanLine(0);
            }
            prev_enabled = false;
        }
    }


    /**
     * Save the state of the gpu to a byte array
     * @return byte save data
     */
    public byte[] saveState() {
        byte[] buf = new byte[BYTE_SAVE_LENGTH];
        buf[0] = (byte)currentMode;
        buf[1] = (byte)(modeClock & 0xff);
        buf[2] = (byte)((modeClock >> 8) & 0xff);

        return buf;
    }


    /**
     * Load gpu state from byte array
     *
     * @param buf from saveState
     */
    public void loadState(byte[] buf) {
        currentMode = Byte.toUnsignedInt(buf[0]);
        modeClock = Byte.toUnsignedInt(buf[1]);
        modeClock |= (Byte.toUnsignedInt(buf[2]) << 8);
        gbcMode = memory.isGBCRom();
    }


    public static int byteSaveLength() {
        return BYTE_SAVE_LENGTH;
    }

    /**
     * copies the screen data into buffer
     * maps as follows:
     *
     * 0xffffffff - 0
     * 0xffcccccc - 1
     * 0xff777777 - 2
     * 0xff000000 - 3
     *
     * only three different shades in gameboy
     *
     * @param buffer to write the data into
     */
    public void drawBuffer(ByteBuffer buffer) {
        buffer.position(0);
        this.buffer.position(0);
        for (int i = 0; i < 69120; ++i) {
            buffer.put(this.buffer.get(i));
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
        if (isSet(lcdc, BACKGROUND_ENABLE))
            drawBackground(currentScanLine);
        if (isSet(lcdc, WINDOW_DISPLAY_ENABLE))
            drawWindow(currentScanLine);
        if (isSet(lcdc, SPRITE_ENABLE))
            drawSprites(currentScanLine);
    }

    /**
     * Horizontal blank of the screen rendering process
     */
    private void horiz_blank() {
        if (modeClock < 204) {
            return;
        }
        //time to increment scanline
        memory.incScanLine();
        //check for LYC interrupt
        if (memory.readByte(LYC) == memory.getScanLine()) {
            memory.setLCDCoincidence(1);
            if ((memory.readByte(LCDC_STAT) & 0x40) != 0) {
                cpu.requestInterrupt(1);
            }
        } else {
            memory.setLCDCoincidence(0);
        }

        if (memory.getScanLine() < 144) {
            set_mode(OAM_MODE, modeClock % 204);
        } else {
            //vblank time
            cpu.requestInterrupt(0);
            set_mode(VERT_BLANK, modeClock % 204);
        }
    }

    /**
     * Vertical blank of the screen rendering process
     */
    private void v_blank() {
        if ((modeClock >= 456) || (memory.getScanLine() == 153 && modeClock >= 64)) {
            memory.incScanLine();
            if (memory.getScanLine() > 153) {
                memory.setScanLine(0);
                set_mode(OAM_MODE, 0);
            } else {
                set_mode(VERT_BLANK, modeClock % 456);
            }
        }
    }

    public void setGBCMode(boolean mode) {
        this.gbcMode = mode;
    }

    /**
     * Mode 2 of the drawing process,
     * reading from OAM memory
     */
    private void o_ram() {
        if (modeClock >= 80) {
            set_mode(LCD_TRANS, modeClock % 80);
        }
    }

    private void lcd_trans() {
        if (modeClock >= 174) {
            renderScan(memory.getScanLine());
            set_mode(HORIZ_BLANK, modeClock % 204);
        }
    }

    /**
     * Updates the current lcd mode
     * @param cycles that have progressed
     */
    private void dispatchMode(int cycles) {
        modeClock += cycles;
        switch (currentMode) {
            case HORIZ_BLANK:
                horiz_blank();
                return;
            case VERT_BLANK:
                v_blank();
                return;
            case OAM_MODE:
                o_ram();
                return;
            case LCD_TRANS:
                lcd_trans();
                return;
            default:
        }
    }
    
    /**
     * Updates the current lcd mode
     * @param mode next mode
     * @param cycles to reset to
     */
    private void set_mode(int mode, int cycles) {
        boolean req_int = false;
        modeClock = cycles;
        currentMode = mode;
        int flag = memory.readByte(LCDC_STAT);
        flag &= 0xfc;

        switch (mode) {
            case HORIZ_BLANK:
                flag |= 0x0;
                if ((flag & 0x8) != 0) req_int = true;
                break;
            case VERT_BLANK:
                flag |= 0x1;
                if ((flag & 0x10) != 0) req_int = true;
                break;
            case OAM_MODE:
                flag |= 0x2;
                if ((flag & 0x20) != 0) req_int = true;
                break;
            case LCD_TRANS:
                flag |= 0x3;
                break;
            default:
                break;
        }
        memory.writeByte(LCDC_STAT, flag);
        if (req_int) {
            cpu.requestInterrupt(1);
        }
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


        // for GBC TODO
        // Look up additional info in VRAM bank 1 (same index #)
        // This gives palette num, bank num, horiz/vert/priority
        // index should be the same
        // Calculate the address, vram bank, and draw the actual background tile
        // need to add (vram bank, palette address to drawTile)
        // TODO HORIZ/VERT FLIP/ PRIORITY

        //draw tiles on current scanLine
        int yOffset = (((scY + scanLine) / 8) % 32) * 32;
        int tileLine = (scY + scanLine) % 8;

        for (int xTile = 0; xTile <= 20; ++xTile) {
            int xOffset = (xTile + (scX / 8)) % 32;
            byte tileIndex = (byte)memory.readVram(tileMapAddress + yOffset + xOffset, 0);
            int bgTileInfo;
            if (gbcMode)
                bgTileInfo = memory.readVram(tileMapAddress + yOffset + xOffset, 1);
            else
                bgTileInfo = 0;
            int tileAddress = signedIndex ? (tileIndex * 16) + tileDataAddress
                                          : (Byte.toUnsignedInt(tileIndex) * 16) + tileDataAddress;


            //calculate correct shift from scX
            int xShift = scX % 8;
            if (scanLine == 0) {
                if (tileAddress == 0x9000) {
                    tileAddress = 0x9010;
                }
            }
            if (xTile == 0 && xShift != 0) {
                drawTile(tileAddress, tileLine, 0, scanLine, xShift, 7, bgTileInfo);
            } else if (xTile == 20 && xShift != 0) {
                drawTile(tileAddress, tileLine, 160 - xShift, scanLine, 0, xShift - 1, bgTileInfo);
            } else if (xTile != 20) {
                drawTile(tileAddress, tileLine, (xTile * 8) - xShift, scanLine, 0, 7, bgTileInfo);
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
    private void drawTile(int tileAddress, int line, int xPos, int yPos, int pixStart, int pixEnd, int bgTileInfo) {
        int paletteAddress = 0xff47;
        int wX = memory.readByte(W_X) - 7;
        int wY = memory.readByte(W_Y);
        boolean windowDrawn = isSet(memory.readByte(LCDC_CONTROL), WINDOW_DISPLAY_ENABLE);
        int pixByteA = memory.readByte(tileAddress + (2 * line));
        int pixByteB = memory.readByte(tileAddress + (2 * line) + 1);

        if (gbcMode) {
            //set correct GBC tile info
            //vertical flip
            if (isSet(bgTileInfo, 6)) {
                line = 7 - line;
            }
            paletteAddress = bgTileInfo & 0x7;
            int bankNum = isSet(bgTileInfo, 3) ? 1 : 0;
            pixByteA = memory.readVram(tileAddress + (2 * line), bankNum);
            pixByteB = memory.readVram(tileAddress + (2 * line) + 1, bankNum);
        }

        //draw each pixel in the line
        for (int pixel = pixStart; pixel <= pixEnd; ++pixel) {
            int pix = pixel;
            // Horizontal flip
            if (gbcMode && isSet(bgTileInfo, 5)) {
                pix = 7 - pix;
            }
            int colorNum = getPixelColorNum(pixByteA, pixByteB, pix);
            int xCoord = (xPos + pix - pixStart) % 160;

            if (wY <= yPos && xCoord >= wX && windowDrawn) {
                break; //window will be drawn at this position
            } else {
                drawToBuffer(xCoord, yPos, getColor(colorNum, paletteAddress, true));
            }
        }
    }


    /**
     *
     * draws a pixel of color to the buffer
     *
     * @param col position
     * @param row position
     * @param color of pixel to draw
     */
    private void drawToBuffer(int col, int row, int color) {
        int index = (3 * col) + (row * 160 * 3);
        buffer.put(index, (byte)((color & 0xff0000) >> 16));
        buffer.put(index + 1, (byte)((color & 0xff00) >> 8));
        buffer.put(index + 2, (byte)(color & 0xff));
    }


    /**
     * translates buffer position to RGB color
     *
     * @param col index
     * @param row index
     * @return RGB value of the color
     */
    private int bufferToColor(int col, int row) {
        int color = 0;
        int index = (3 * col) + (row * 160 * 3);

        color |= Byte.toUnsignedInt(buffer.get(index)) << 16;
        color |= Byte.toUnsignedInt(buffer.get(index + 1)) << 8;
        color |= Byte.toUnsignedInt(buffer.get(index + 2));
        return color;
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
    private void drawWindowTile(int tileAddress, int line, int xPos, int yPos, int bgTileInfo) {
        int paletteAddress = 0xff47;

        int pixByteA = memory.readByte(tileAddress + (2 * line));
        int pixByteB = memory.readByte(tileAddress + (2 * line) + 1);

        if (gbcMode) {
            //set correct GBC tile info
            paletteAddress = bgTileInfo & 0x7;
            int bankNum = isSet(bgTileInfo, 3) ? 1 : 0;
            pixByteA = memory.readVram(tileAddress + (2 * line), bankNum);
            pixByteB = memory.readVram(tileAddress + (2 * line) + 1, bankNum);
        }

        //draw each pixel in the line
        for (int pixel = 0; pixel <= 7; ++pixel) {
            int colorNum = getPixelColorNum(pixByteA, pixByteB, pixel);
            int xCoord = (xPos + pixel);
            if (xCoord < 160 && yPos < 144 && xCoord >= 0 && yPos >= 0) {
                //TODO is this bg palette?
                drawToBuffer(xCoord, yPos, getColor(colorNum, paletteAddress, true));
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
        int bgTileInfo;


        if (wY > 143 || wX > 166 || wY > scanLine) {
            return; //window not on screen or on this line
        }
        //draw tiles on current scanLine
        int yOffset = ((scanLine - wY) / 8) * 32;
        int tileLine = (scanLine - wY) % 8;

        for (int xTile = 0; xTile <= 20; ++xTile) {
            byte tileIndex = (byte)memory.readVram(tileMapAddress + yOffset + xTile, 0);
            if (gbcMode)
                bgTileInfo = memory.readVram(tileMapAddress + yOffset + xTile, 1);
            else
                bgTileInfo = 0;

            int tileAddress = signedIndex ? (tileIndex * 16) + tileDataAddress
                                          : (Byte.toUnsignedInt(tileIndex) * 16) + tileDataAddress;

            int xCoord = (wX - 7) + (xTile * 8);
            drawWindowTile(tileAddress, tileLine, xCoord, scanLine, bgTileInfo);
        }
    }

    /**
     *
     * draws the sprites onto the LCD screen
     *
     *
     *     todo SCANLINE LIMIT of 10 SPRITES
     */
    private void drawSprites(int scanline) {
        int lcdc = memory.readByte(LCDC_CONTROL);
        int height = isSet(lcdc, SPRITE_HEIGHT) ? 16 : 8;

        //TODO for GBC mainly the same
        // VRAM Bank #, different palette number

        for (int i = 0; i < 40; ++i){
            int offset = (39 - i) * 4;
            int y = memory.readByte(0xfe00 + offset);
            int x = memory.readByte(0xfe00 + offset + 1);
            int tileNum = memory.readByte(0xfe00 + offset + 2);
            int flags = memory.readByte(0xfe00 + offset + 3);
            if (height == 16) {
                tileNum &= 0xfe;
            }
            if ((scanline >= (y - 16)) && ((y - 17) + height >= scanline)) {
                int address = (tileNum * 16) + 0x8000;
                draw_sprite_line(x - 8, y -16, address,
                                flags, height, scanline);
            }
        }
    }


    /**
     * draws the sprite for current scanline at address
     * to the screen at xPos, yPos (top left corner
     * @param x of LCD screen to display top left
     * @param y of LCD screen to display top left
     * @param address of first byte of sprite data
     * @param height of the sprite (8 or 16)
     * @param flags associated with the sprite
     *
     * TODO scanline LIMIT
     */
    private void draw_sprite_line(int x, int y, int address, int flags, int height, int scanline) {
        boolean horizFlip = isSet(flags, HORIZ_FLIP);
        boolean hasPriority = !isSet(flags, PRIORITY);
        int paletteAddress = isSet(flags, PALETTE_NUM) ? 0xff49 : 0xff48;
        int sprite_line = height - (scanline - y);

        int offset;
        if (!isSet(flags, VERT_FLIP)) {
            offset = 2 * (scanline - y);
        } else {
            offset = 2 * (sprite_line - 1);
        }
        int pixDataA = memory.readByte(address + offset);
        int pixDataB = memory.readByte(address + offset + 1);

        if (gbcMode) {
            paletteAddress = flags & 0x7;
            int bankNum = isSet(flags, 3) ? 1 : 0;
            pixDataA = memory.readVram(address + offset, bankNum);
            pixDataB = memory.readVram(address + offset + 1, bankNum);
        }

        for (int pix = 0; pix < 8; ++pix) {
            int col_index = horizFlip ? 7 - pix : pix;
            int color_num = getPixelColorNum(pixDataA, pixDataB, col_index);
            int color = getColor(color_num, paletteAddress, false);
            if ((x + pix < 160) && (x + pix >= 0) && color_num != 0) {
                if (hasPriority) {
                    drawToBuffer(x + pix, scanline, color);
                } else {
                    //if (bufferToColor(x + pix, scanline) == getColor(0, 0xff47, false)) {
                        drawToBuffer(x + pix, scanline, color);
                    //}
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
     * color RGB int (four byte value)
     *
     * @param pixNum from tile/sprite to translate
     * @param palAddress to interpret the pix number
     * @return a RGB value representing the actual color
     */
    private int getColor(int pixNum, int palAddress, boolean backGround) {
        if (gbcMode && backGround) {
            return memory.getGBCBGPaletteColor(palAddress, pixNum);
        } else if (gbcMode) {
            return memory.getGBCSpritePaletteColor(palAddress, pixNum);
        }

        int palette = memory.readByte(palAddress);
        int colSelect;
        int color;
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
        switch (colSelect & 0x3) {
            case 0: color = 0xffffff;
                    break;
            case 1: color = 0xcccccc;
                    break;
            case 2: color = 0x777777;
                    break;
            case 3: color = 0x000000;
                    break;
            default: color = 0xffffff;
        }
        return color;
    }

}