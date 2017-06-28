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
package main.java.gameboi;
import main.java.gameboi.cpu.CPU;
import main.java.gameboi.gpu.GPU;
import main.java.gameboi.memory.GBMem;
import main.java.gameboi.userinterface.FileSelector;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author tomis007
 */
public class GameBoi {
    private GBMem mem;
    private CPU z80;
    private GPU gpu;
    private Path current_rom;

    /**
     * runs the gameboi emulator locally
     * (not configured for server)
     *
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        GameBoi gameboy = new GameBoi(selectRom());

        //Start the Gameboy fetch,decode,execute cycle
        //TODO Lets fix this...
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        startGameBoi(gameboy, executor);
    }

    /**
     * local constructor
     * @param rom path to rom file to load
     */
    public GameBoi(Path rom) {
        mem = new GBMem();
        z80 = new CPU(mem);
        gpu = new GPU(mem, z80, true);
        mem.loadRom(rom);
    }

    /**
     * server constructor
     */
    public GameBoi() {
        mem = new GBMem();
        z80 = new CPU(mem);
        gpu = new GPU(mem, z80, true);
    }

    public void loadRom(Path rom) {
        mem.loadRom(rom);
    }

    /**
     * TODO
     * saves current state of game
     * to currentRomName_"fileName".gbsav
     * @param fileName to add to end of current rom name
     *                 for save
     */
    public void saveGame(String fileName) {
        String saveName = current_rom.getFileName().toString() + fileName + ".gbsav";
        try {
            FileOutputStream fs = new FileOutputStream(saveName);
            fs.write(z80.saveState());
            //fs.write(mem.saveState());
            fs.close();
        } catch (IOException e) {
            System.err.println("SAVING FAILED: " + fileName);
        }
    }


    /**
     * advances gameboy state one frame
     * draws the frame into buffer
     * @param buffer to draw frame into
     *               must be 23040 long
     *
     */
    public void drawFrameToBuffer(ByteBuffer buffer) {
        renderFrame();
        gpu.drawBuffer(buffer);
    }

    /**
     * advances gameboy state one frame
     * draws the frame onto the screen
     */
    public void renderFrame() {
        int count = 0, cycles;
        while (count < 70244) {
            cycles = z80.ExecuteOpcode();
            gpu.updateGraphics(cycles);
            count += cycles;
        }
    }

    /**
     * draws to the LCD Screen
     */
    public void drawToLCD() {
        renderFrame();
        gpu.drawToLCD();
    }


    /**
     * signals to the gameboi that an
     * input key has been pressed
     *
     *       START:   return 7;
     *       SELECT:  return 6;
     *       B:       return 5;
     *       A:       return 4;
     *       DOWN:    return 3;
     *       UP:      return 2;
     *       LEFT:    return 1;
     *       RIGHT:   return 0;
     *
     * @param key_num of joypad key pressed as mapped
     *                above
     */
    public void keyPressed(int key_num) {
        int currentJoyPad = mem.getJoyPadState();

        // not mapped to a joypad key
        if (key_num < 0 || key_num > 7) {
            return;
        }

        // 'press key'
        currentJoyPad = setBit(0, key_num, currentJoyPad);
        mem.updateJoyPadState(currentJoyPad);
        z80.resume();
        z80.requestInterrupt(4);
    }

    /**
     *
     * signals to the gameboi that an
     * input key has been released
     *
     *       START:   return 7;
     *       SELECT:  return 6;
     *       B:       return 5;
     *       A:       return 4;
     *       DOWN:    return 3;
     *       UP:      return 2;
     *       LEFT:    return 1;
     *       RIGHT:   return 0;
     *
     * @param key_num of joypad key released as mapped
     *                above
     */
    public void keyReleased(int key_num) {
        int currentJoyPad = mem.getJoyPadState();

        if (key_num >= 0 && key_num < 8) {
            currentJoyPad = setBit(1, key_num, currentJoyPad);
            mem.updateJoyPadState(currentJoyPad);
        }
    }


    /**
     * sets bit bitNum to val in num
     */
    private int setBit(int val, int bitNum, int num) {
        if (val == 1) {
            return num | 1 << bitNum;
        } else {
            return num & ~(1 << bitNum);
        }
    }


    /**
     * gets Path object to a Rom with a simple GUI
     *
     * @return Path to selected rom
     */
    private static Path selectRom() {
        FileSelector fc = new FileSelector(System.getProperty("user.dir"));

        File rom = fc.selectFile();
        if (rom == null) {
            System.err.println("Sorry, please select a ROM");
            System.exit(1);
        }

        return rom.toPath();
    }

    /**
     * starts the executor executing drawFrame at correct gameboy FPS
     * @param gameboy to start
     * @param executor to use
     */
    private static void startGameBoi(GameBoi gameboy, ScheduledExecutorService executor) {
            //update at correct clock frequency
            executor.scheduleAtFixedRate(() -> gameboy.drawToLCD(), 0, 16, TimeUnit.MILLISECONDS);
    }
}


