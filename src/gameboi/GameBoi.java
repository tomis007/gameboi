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

import java.nio.file.Path;
import java.io.File;

/**
 *
 * @author tomis007
 */
public class GameBoi {

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        GBMem memory = new GBMem(loadRom());
        CPU z80 = new CPU(memory);
        GPU gpu = new GPU(memory, z80);

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


