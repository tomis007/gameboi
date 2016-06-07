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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tomis007
 */
public class CPUTest {
    private CPU z80;
    private GBMem memory;
    private GBRegisters reg;
    private String gb_testprogram;
    private String gbTestProgramData;
    
    /**
     * 
     * register constants
     * 
     */ 
    private static final GBRegisters.Reg HL = GBRegisters.Reg.HL;
    private static final GBRegisters.Reg BC = GBRegisters.Reg.BC;
    private static final GBRegisters.Reg DE = GBRegisters.Reg.DE;

    
    public CPUTest() {
        

        gb_testprogram = "../all_zeros.gb";
        gbTestProgramData = "../all_zeros.gbd";
        Path rom_path = Paths.get(gb_testprogram);

        GBMem memory = new GBMem(rom_path);
        
        CPU cpu = new CPU(memory);

        GPU gpu = new GPU(memory, cpu);
        
        cpu.setGPU(gpu);
        
        z80 = cpu;
        this.memory = memory;
        reg = z80.getReg();
    }
    

    /**
     * Test of ExecuteOpcode method, of class CPU.
     * tests the execute opcode method against a 
     * gb_testfile and gb_test_data_file
     * 
     */
    @Test
    public void testExecuteOpcode() {
        System.out.println("ExecuteOpcode");
        
        //read number of tests from gb_testfile
        String line;
        try (
            InputStream fis = new FileInputStream(gbTestProgramData);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
        ) {
            while ((line = br.readLine()) != null) {
                z80.ExecuteOpcode();
                
            //dump registers
            //check against gb test program data file
           }
        } catch (java.io.IOException e) {
            System.err.println("Invalid GBTESTFILE FORMAT!");
        }
    }

        
    /**
     * Test of requestInterrupt method, of class CPU.
     */
    @Test
    public void testRequestInterrupt() {
        System.out.println("requestInterrupt");
        // TODO review the generated test code and remove the default call to fail.
    }
    
}
