/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameboi;

// for testing
import java.util.Random;


/**
 * Z80 Gameboy CPU
 * 
 * Implementation of a gameboy cpu. Eight registers (a,b,c,d,e,f,h,l), a
 * stack pointer, program counter
 * 
 * @author tomis007
 */
public class CPU {
    // registers
    private final GBRegisters registers;
    
    // stack pointer, program counter
    private int sp;
    private int pc;

    //associated memory to use with CPU
    private final GBMem memory;

    
    /**
     * Constructor for gameboy z80 CPU
     * 
     * @param memory GBMem object to associate with this cpu
     */ 
    public CPU(GBMem memory) {
        pc = 0x100;
        sp = 0xfffe;
        this.memory = memory;
        registers = new GBRegisters();
    }
    
    /**
     * Execute the next opcode in memory
     * 
     * @return clock cycles taken to execute the opcode
     */ 
    public int ExecuteOpcode() {
        
        int opcode = memory.readByte(pc);
        pc++;
        return runInstruction(opcode);
    }
    
    
    
    /**
     * Opcode Instructions for the Gameboy Z80 Chip.
     * 
     * <p>
     * Runs the instruction associated with the opcode, and returns the 
     * clock cycles taken.
     * 
     * @param opcode (required) opcode to execute
     * @return number of cycles taken to execute
     */ 
    private int runInstruction(int opcode) { 
        switch (opcode) {
            case 0x0:  return 4; //NOP
            case 0x06: return eightBitLdNnN(GBRegisters.Reg.B);
            case 0x0e: return eightBitLdNnN(GBRegisters.Reg.C);
            case 0x16: return eightBitLdNnN(GBRegisters.Reg.D);
            case 0x1e: return eightBitLdNnN(GBRegisters.Reg.E);
            case 0x26: return eightBitLdNnN(GBRegisters.Reg.H);
            case 0x2e: return eightBitLdNnN(GBRegisters.Reg.L);
            default:
                System.err.println("Unimplemented opcode: 0x" + 
                        Integer.toHexString(opcode));
                System.exit(1);
        }    
        return 0;
    }
    
    
    /**
     * LD nn,n. Put value nn into n.
     * 
     * <p>nn = B,C,D,E,H,L,BC,DE,HL,SP
     * n = 8 bit immediate value
     * 
     * @param register (required) register (nn) to load to
     */ 
    private int eightBitLdNnN(GBRegisters.Reg register) {
        int data = memory.readByte(pc);
        pc++;
        registers.setReg(register, data);
        return 8;   
    }
    
    /**
     * Testing function for EightBitLdNnN
     * 
     * Tests the eightBitLdNnN function with random input data
     * to make sure the function works correctly
     */ 
    public void testEightBitLdNnN() {
//        Random rand = new Random();
//        for (int i = 0; i < 2000; ++i) {
//            int data = rand.nextInt(256);
//            for (int j = 0; j < 200; ++j) {
//                eightBitLdNnN(GBRegisters.Reg.E, data);
//                if (registers.getReg(GBRegisters.Reg.E)!= data) {
//                    System.err.println("Error failed register E test");
//                }
//                data = rand.nextInt(256);
//                eightBitLdNnN(GBRegisters.Reg.B, data);
//                if (registers.getReg(GBRegisters.Reg.B)!= data) {
//                    System.err.println("Error failed register B test");
//                }
//                data = rand.nextInt(256);
//                eightBitLdNnN(GBRegisters.Reg.C, data);
//                if (registers.getReg(GBRegisters.Reg.C) != data) {
//                    System.err.println("Error failed register C test");
//                }
//                data = rand.nextInt(256);
//                eightBitLdNnN(GBRegisters.Reg.D, data);
//                if (registers.getReg(GBRegisters.Reg.D) != data) {
//                    System.err.println("Error failed register D test");
//                }
//                data = rand.nextInt(256);
//                eightBitLdNnN(GBRegisters.Reg.H, data);
//                if (registers.getReg(GBRegisters.Reg.H) != data) {
//                    System.err.println("Error failed register H test");
//                }
//                data = rand.nextInt(256);
//                eightBitLdNnN(GBRegisters.Reg.L, data);
//                if (registers.getReg(GBRegisters.Reg.L) != data) {
//                    System.err.println("Error failed register L test");
//                }
//            }
//        }
//        System.out.println("Finished and reported all error messages");
    }
}

