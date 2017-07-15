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
package main.java.gameboi;

import main.java.gameboi.cpu.CPU;
import main.java.gameboi.gpu.GPU;
import main.java.gameboi.memory.GBMem;
import main.java.gameboi.joypad.JoyPad;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO Better error handling
 * @author tomis007
 */
public class GameBoi {
    private GBMem mem;
    private CPU z80;
    private GPU gpu;
    private Path current_rom;
    private JoyPad joypad;

    //saving/loading info TODO Load from environment variables
    private static Path home = null;
    private static Path saves = null;
    private static Path roms = null;

    //for saving state
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
        //gb.loadRom(Paths.get("/Users/thomas/stuff/tetris.gb"));
        gb.loadGame("test");
        for (int i = 0; i < 200; ++i) {
            gb.renderFrame();
        }
        System.out.println(gb.getRoms().toString());
        System.out.println(gb.getSaves().toString());
        //gb.loadGame("test");
        //gb.saveGame("test");

        //for (int i = 0; i < 200; ++i) {
            //gb.renderFrame();
        //}
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
        if (home == null || roms == null || saves == null) {
            makeHome();
        }
    }

    /**
     * Creates a GameBoi object
     *
     * If makeHome has not been called, calls it
     */
    public GameBoi() {
        mem = new GBMem();
        z80 = new CPU(mem);
        gpu = new GPU(mem, z80);
        joypad = new JoyPad(z80, mem);
        current_rom = null;
        if (home == null || roms == null || saves == null) {
            makeHome();
        }
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
    public static boolean makeHome() {
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
     * saves current state of game
     * to the current rom name "fileName".gbs in the
     * {home | .GBoi}/saves/ directory
     * @param fileName to save as
     */
    public boolean saveGame(String fileName) {
        String saveName;
        if (current_rom == null) {
            System.err.println("Unable to save, no rom loaded");
            return false;
        }

        saveName = saves.toString() + "/" + fileName + ".gbs";
        try {
            FileOutputStream fs = new FileOutputStream(saveName);
            fs.write(z80.saveState());
            fs.write(mem.saveState());
            fs.write(gpu.saveState());
            fs.write(getCurrentRomName());
            fs.close();
        } catch (IOException e) {
            System.err.println("SAVING FAILED: " + saveName + e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    /**
     * Converts the current rom to a byte array
     *
     * @return byte array of current rom name
     */
    private byte[] getCurrentRomName() {
        byte[] romName;
        byte[] buf;
        try {
            romName = current_rom.getFileName().toString().getBytes("UTF-8");
        } catch(UnsupportedEncodingException | NullPointerException e) {
            System.err.println("UTF-8 Not supported, resorting to default " + e.getLocalizedMessage());
            romName = "unknown".getBytes();
        }
        buf = new byte[romName.length];

        System.arraycopy(romName, 0, buf, 0, romName.length);

        return buf;
    }

    /**
     * TODO: What if rom isnt there?
     * Converts the saved RomName from byte array to Path object
     *
     * @param buf the bytes saved into the gbs save
     * @return Path to the rom
     */
    private Path getSavedRomPath(byte[] buf) {
        String romName;
        try {
            romName = new String(buf, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            System.err.println("UTF-8 not supported, resorting to default");
            romName = "unknown";
        }

        return Paths.get(roms.toString() + "/" + romName);
    }

    /**
     * Loads a game save from ~/.GBoi/saves directory (home/saves)
     * Saves have to end in ".gbs"
     *
     * @param name filename of file in ~/.GBoi/saves directory
     * @return true on success, false on failure
     */
    public boolean loadGame(String name) {
        if (!name.endsWith(".gbs")) {
            name += ".gbs";
        }
        String file_path = saves.toString() + "/" + name;
        try {
            byte[] saveData = Files.readAllBytes(new File(file_path).toPath());
            //load the ROM into memory first
            loadRom(getSavedRomPath(Arrays.copyOfRange(saveData, GPU_LAST_BYTE, saveData.length)));
            //Load the state of ROM
            z80.loadState(Arrays.copyOfRange(saveData, CPU_START_BYTE, CPU_LAST_BYTE));
            mem.loadState(Arrays.copyOfRange(saveData, CPU_LAST_BYTE, MEM_LAST_BYTE));
            gpu.loadState(Arrays.copyOfRange(saveData, MEM_LAST_BYTE, GPU_LAST_BYTE));
        } catch(IOException | IndexOutOfBoundsException e) {
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
     * gets all files in dir with endings that are in a
     * List of file endings
     *
     *
     * @param dir to get files in
     * @param fileExt List<String> of file names to accept
     * @return
     */
    private List<String> getFiles(Path dir, List<String> fileExt) {
        List<String> fileList = new ArrayList<>();
        String[] folderContents = dir.toFile().list();
        if (folderContents == null) {
            return null;
        }
        for (String file : folderContents) {
            for (String ext : fileExt) {
                if (file.endsWith(ext)) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    /**
     *
     * Get the current rom files
     *
     * @return A List of Roms in the rom directory
     */
    public List<String> getRoms() {
        List<String> endings = new ArrayList<>();
        endings.add(".gb");
        return getFiles(roms, endings);
    }

    /**
     *
     * Get the current saves
     *
     * @return List of save names in the save save directory
     */
    public List<String> getSaves() {
        List<String> endings = new ArrayList<>();
        endings.add(".gbs");
        return getFiles(saves, endings);
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
}


