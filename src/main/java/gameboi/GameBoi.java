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
import main.java.gameboi.joypad.JoyPad;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
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
    private JoyPad joypad;

    //saving/loading info TODO Load from environment variables
    private Path home;
    private Path saves;
    private Path roms;

    //for saving state easy modifications
    private static final int CPU_START_BYTE = 0;
    private static final int CPU_LAST_BYTE = CPU_START_BYTE + CPU.byteSaveLength();
    private static final int MEM_LAST_BYTE = CPU_LAST_BYTE + GBMem.byteSaveLength();
    private static final int GPU_LAST_BYTE = MEM_LAST_BYTE + GPU.byteSaveLength();

    /**
     * runs the gameboi emulator locally
     * (not configured for server)
     *
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        //GameBoi gameboy = new GameBoi(selectRom());
        GameBoi gb = new GameBoi();
        gb.loadRom(Paths.get("/Users/thomas/stuff/tetris.gb"));
        //gb.loadGame("test");
        for (int i = 0; i < 200; ++i) {
            gb.renderFrame();
        }
        //gb.loadGame("test");
        //gb.saveGame("test");

        //Start the Gameboy fetch,decode,execute cycle
        //TODO Lets fix this...
        //ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        //startGameBoi(gameboy, executor);
    }

    /**
     * local constructor
     * @param rom path to rom file to load
     */
    public GameBoi(Path rom) {
        mem = new GBMem();
        z80 = new CPU(mem);
        gpu = new GPU(mem, z80);
        mem.loadRom(rom);
        current_rom = null;
        makeHome();
    }

    /**
     * server constructor
     */
    public GameBoi() {
        mem = new GBMem();
        z80 = new CPU(mem);
        //gpu = new GPU(mem, z80, false);
        gpu = new GPU(mem, z80);
        joypad = new JoyPad(z80, mem);
        current_rom = null;
        makeHome();
    }

    public void loadRom(Path rom) {
        current_rom = rom;
        mem.loadRom(rom);
    }


    /**
     * creates gboi home directory if doesnt exist
     *
     *
     * @return true on success, false on error
     */
    private boolean makeHome() {
        String home_path = System.getProperty("user.home");
        home_path += "/.GBoi";
        try {
            home = Files.createDirectories(Paths.get(home_path));
            saves = Files.createDirectories(Paths.get(home_path + "/saves"));
            roms = Files.createDirectories(Paths.get(home_path + "/roms"));
        } catch(IOException e) {
            System.err.print("error creating GBoi home directory");
            return false;
        }
        return true;
    }

    /**
     * TODO ROM ID
     * saves current state of game
     * to the current rom name "fileName".gbsav
     * @param fileName to add to end of current rom name
     *                 for save
     */
    public boolean saveGame(String fileName) {
        String file_name;

        if (current_rom != null) {
            //TODO saving name convention
            //file_name = current_rom.getFileName().toString() + fileName + ".gbs";
            file_name = fileName + ".gbs";
        } else {
            file_name = "unknown.gbs";
        }
        file_name = saves.toString() + "/" + file_name;
        System.out.println("saving as: " + file_name);

        try {
            FileOutputStream fs = new FileOutputStream(file_name);
            fs.write(z80.saveState());
            fs.write(mem.saveState());
            fs.write(gpu.saveState());
            fs.close();
        } catch (IOException e) {
            System.err.println("SAVING FAILED: " + fileName + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     *
     * TODO name saving convention, INDEX OUT OF RANGE!!!
     *
     * @param name filename of file in .Gboi home directory
     * @return true on success, false on failure
     */
    public boolean loadGame(String name) {
        String file_path = saves.toString() + "/" + name + ".gbs";
        try {
            byte[] saveData = Files.readAllBytes(new File(file_path).toPath());
            z80.loadState(Arrays.copyOfRange(saveData, CPU_START_BYTE, CPU_LAST_BYTE));
            mem.loadState(Arrays.copyOfRange(saveData, CPU_LAST_BYTE, MEM_LAST_BYTE));
            gpu.loadState(Arrays.copyOfRange(saveData, MEM_LAST_BYTE, GPU_LAST_BYTE));
        } catch(IOException e) {
            System.err.println("FAILED TO LOAD: " + file_path + " " + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     *
     * interface to keyPressed event in joypad
     * @param key_num key pressed (0-7)
     */
    public void keyPressed(int key_num) {
        joypad.keyPressed(key_num);
    }

    /**
     * interface to keyReleased event in joypad
     * @param key_num key released (0-7)
     */
    public void keyReleased(int key_num) {
        joypad.keyReleased(key_num);
    }

    public int getJoyPadState() {
        return mem.getJoyPadState();
    }

    /**
     * advances gameboy state one frame
     * draws the frame into buffer
     * @param buffer to draw frame into
     *               must be 23040 long
     */
    public void drawFrameToBuffer(ByteBuffer buffer) {
        renderFrame();
        gpu.drawBuffer(buffer);
    }


    /**
     * advances gameboy state one frame
     * draws the frame into buffer
     * @param buffer to draw frame into
     *               must be 23040 long
     * @param count number of frames to render without drawing
     *              Default: 0
     */
    public void drawFrameToBuffer(ByteBuffer buffer, int count) {
        for (int i = 0; i < count + 1; ++i) {
            renderFrame();
        }
        gpu.drawBuffer(buffer);
    }

    /**
     * advances gameboy state one frame
     * draws the frame onto the screen
     */
    private void renderFrame() {
        int count = 0, cycles = 0;
        while (count < 70244) {
            cycles = z80.ExecuteOpcode();
            gpu.updateGraphics(cycles);
            count += cycles;
        }
    }

    /**
     * draws to the LCD Screen
     */
    private void drawToLCD() {
        renderFrame();
        //gpu.drawToLCD();
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


