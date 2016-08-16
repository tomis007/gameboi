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
import gameboi.cpu.CPU;
import gameboi.gpu.GPU;
import gameboi.memory.GBMem;
import gameboi.userinterface.FileSelector;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Paths;

/**
 *
 * @author tomis007
 */
public class GameBoi {
    private GBMem mem;
    private CPU z80;
    private GPU gpu;
    private Path current_rom;


    public GameBoi() {
        Path tetris = Paths.get("/Users/thomas/Desktop/Links_Awakening.gb");

        mem = new GBMem(tetris);
        z80 = new CPU(mem);
        gpu = new GPU(mem, z80, false);
    }

    public GameBoi(Path rom) {
        current_rom = rom;
        mem = new GBMem(rom);
        z80 = new CPU(mem);
        gpu = new GPU(mem, z80, false);
    }

    /**
     * saves current state of game
     * to currentRomName_"fileName".gbsav
     * @param fileName to add to end of current rom name
     *                 for save
     */
    public void saveGame(String fileName) {
        //TODO
    }

    /**
     * loads a new rom into the gameboi
     *
     *
     * @param rom Path to new rom to load
     */
    public void loadNewGame(Path rom) {
        current_rom = rom;
        mem = new GBMem(rom);
        z80 = new CPU(mem);
        gpu = new GPU(mem, z80, false);
    }



    public void DrawFrame(ByteBuffer buffer) {
        if (mem.getScanLine() == 144) {
            do {
                int cycles;
                cycles = z80.ExecuteOpcode();
                gpu.updateGraphics(cycles);
            } while (mem.getScanLine() == 144);
        }

        while (mem.getScanLine() != 144) {
            int cycles;
            cycles = z80.ExecuteOpcode();
            gpu.updateGraphics(cycles);
        }
        gpu.drawBuffer(buffer);
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
     * @param key_num of joypad key presssed as mapped
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
     * runs the gameboi emulator locally
     * (not configured for server)
     *
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        GBMem memory = new GBMem(loadRom());
        CPU z80 = new CPU(memory);
        GPU gpu = new GPU(memory, z80, true);

        //Start the Gameboy fetch,decode,execute cycle
        while (true) {
            int count = 0;
            long startTime = System.nanoTime();
            while (count < 69905) {
                int cycles;
                cycles = z80.ExecuteOpcode();
                gpu.updateGraphics(cycles);
                count += cycles;
            }
            long sleepTime = 16700000 - (System.nanoTime() - startTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000, (int)sleepTime % 1000000);
                } catch (InterruptedException e) {
                    System.err.println("woops...");
                }
            }
        }
    }


    /**
     * gets Path object to a Rom with a simple GUI
     *
     * @return Path to selected rom
     */
    private static Path loadRom() {
        FileSelector fc = new FileSelector(System.getProperty("user.dir"));

        File rom = fc.selectFile();
        if (rom == null) {
            System.err.println("Sorry, please select a ROM");
            System.exit(1);
        }

        return rom.toPath();
    }

}


