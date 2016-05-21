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

            /*****8 BIT LOADS*****/
            // LD nn,n
            case 0x06: return eightBitLdNnN(GBRegisters.Reg.B);
            case 0x0e: return eightBitLdNnN(GBRegisters.Reg.C);
            case 0x16: return eightBitLdNnN(GBRegisters.Reg.D);
            case 0x1e: return eightBitLdNnN(GBRegisters.Reg.E);
            case 0x26: return eightBitLdNnN(GBRegisters.Reg.H);
            case 0x2e: return eightBitLdNnN(GBRegisters.Reg.L);
            //LD r1,r2
            case 0x7f: return eightBitLdR1R2(GBRegisters.Reg.A, GBRegisters.Reg.A);
            case 0x78: return eightBitLdR1R2(GBRegisters.Reg.A, GBRegisters.Reg.B);
            case 0x79: return eightBitLdR1R2(GBRegisters.Reg.A, GBRegisters.Reg.C);
            case 0x7a: return eightBitLdR1R2(GBRegisters.Reg.A, GBRegisters.Reg.D);
            case 0x7b: return eightBitLdR1R2(GBRegisters.Reg.A, GBRegisters.Reg.E);
            case 0x7c: return eightBitLdR1R2(GBRegisters.Reg.A, GBRegisters.Reg.H);
            case 0x7d: return eightBitLdR1R2(GBRegisters.Reg.A, GBRegisters.Reg.L);
            case 0x7e: return eightBitLdR1R2(GBRegisters.Reg.A, GBRegisters.Reg.HL);
            case 0x40: return eightBitLdR1R2(GBRegisters.Reg.B, GBRegisters.Reg.B);
            case 0x41: return eightBitLdR1R2(GBRegisters.Reg.B, GBRegisters.Reg.C);
            case 0x42: return eightBitLdR1R2(GBRegisters.Reg.B, GBRegisters.Reg.D);
            case 0x43: return eightBitLdR1R2(GBRegisters.Reg.B, GBRegisters.Reg.E);
            case 0x44: return eightBitLdR1R2(GBRegisters.Reg.B, GBRegisters.Reg.H);
            case 0x45: return eightBitLdR1R2(GBRegisters.Reg.B, GBRegisters.Reg.L);
            case 0x46: return eightBitLdR1R2(GBRegisters.Reg.B, GBRegisters.Reg.HL);
            case 0x48: return eightBitLdR1R2(GBRegisters.Reg.C, GBRegisters.Reg.B);
            case 0x49: return eightBitLdR1R2(GBRegisters.Reg.C, GBRegisters.Reg.C);
            case 0x4a: return eightBitLdR1R2(GBRegisters.Reg.C, GBRegisters.Reg.D);
            case 0x4b: return eightBitLdR1R2(GBRegisters.Reg.C, GBRegisters.Reg.E);
            case 0x4c: return eightBitLdR1R2(GBRegisters.Reg.C, GBRegisters.Reg.H);
            case 0x4d: return eightBitLdR1R2(GBRegisters.Reg.C, GBRegisters.Reg.L);
            case 0x4e: return eightBitLdR1R2(GBRegisters.Reg.C, GBRegisters.Reg.HL);
            case 0x50: return eightBitLdR1R2(GBRegisters.Reg.D, GBRegisters.Reg.B);
            case 0x51: return eightBitLdR1R2(GBRegisters.Reg.D, GBRegisters.Reg.C);
            case 0x52: return eightBitLdR1R2(GBRegisters.Reg.D, GBRegisters.Reg.D);
            case 0x53: return eightBitLdR1R2(GBRegisters.Reg.D, GBRegisters.Reg.E);
            case 0x54: return eightBitLdR1R2(GBRegisters.Reg.D, GBRegisters.Reg.H);
            case 0x55: return eightBitLdR1R2(GBRegisters.Reg.D, GBRegisters.Reg.L);
            case 0x56: return eightBitLdR1R2(GBRegisters.Reg.D, GBRegisters.Reg.HL);
            case 0x58: return eightBitLdR1R2(GBRegisters.Reg.E, GBRegisters.Reg.B);
            case 0x59: return eightBitLdR1R2(GBRegisters.Reg.E, GBRegisters.Reg.C);
            case 0x5a: return eightBitLdR1R2(GBRegisters.Reg.E, GBRegisters.Reg.D);
            case 0x5b: return eightBitLdR1R2(GBRegisters.Reg.E, GBRegisters.Reg.E);
            case 0x5c: return eightBitLdR1R2(GBRegisters.Reg.E, GBRegisters.Reg.H);
            case 0x5d: return eightBitLdR1R2(GBRegisters.Reg.E, GBRegisters.Reg.L);
            case 0x5e: return eightBitLdR1R2(GBRegisters.Reg.E, GBRegisters.Reg.HL);
            case 0x60: return eightBitLdR1R2(GBRegisters.Reg.H, GBRegisters.Reg.B);
            case 0x61: return eightBitLdR1R2(GBRegisters.Reg.H, GBRegisters.Reg.C);
            case 0x62: return eightBitLdR1R2(GBRegisters.Reg.H, GBRegisters.Reg.D);
            case 0x63: return eightBitLdR1R2(GBRegisters.Reg.H, GBRegisters.Reg.E);
            case 0x64: return eightBitLdR1R2(GBRegisters.Reg.H, GBRegisters.Reg.H);
            case 0x65: return eightBitLdR1R2(GBRegisters.Reg.H, GBRegisters.Reg.L);
            case 0x66: return eightBitLdR1R2(GBRegisters.Reg.H, GBRegisters.Reg.HL);
            case 0x68: return eightBitLdR1R2(GBRegisters.Reg.L, GBRegisters.Reg.B);
            case 0x69: return eightBitLdR1R2(GBRegisters.Reg.L, GBRegisters.Reg.C);
            case 0x6a: return eightBitLdR1R2(GBRegisters.Reg.L, GBRegisters.Reg.D);
            case 0x6b: return eightBitLdR1R2(GBRegisters.Reg.L, GBRegisters.Reg.E);
            case 0x6c: return eightBitLdR1R2(GBRegisters.Reg.L, GBRegisters.Reg.H);
            case 0x6d: return eightBitLdR1R2(GBRegisters.Reg.L, GBRegisters.Reg.L);
            case 0x6e: return eightBitLdR1R2(GBRegisters.Reg.L, GBRegisters.Reg.HL);
            case 0x70: return eightBitLdR1R2(GBRegisters.Reg.HL, GBRegisters.Reg.B);
            case 0x71: return eightBitLdR1R2(GBRegisters.Reg.HL, GBRegisters.Reg.C);
            case 0x72: return eightBitLdR1R2(GBRegisters.Reg.HL, GBRegisters.Reg.D);
            case 0x73: return eightBitLdR1R2(GBRegisters.Reg.HL, GBRegisters.Reg.E);
            case 0x74: return eightBitLdR1R2(GBRegisters.Reg.HL, GBRegisters.Reg.H);
            case 0x75: return eightBitLdR1R2(GBRegisters.Reg.HL, GBRegisters.Reg.L);
            // special 8 bit load from memory
            case 0x36: return eightBitLoadFromMem();
            // LD A,n
            case 0x0a: return eightBitLdAN(GBRegisters.Reg.BC);
            case 0x1a: return eightBitLdAN(GBRegisters.Reg.DE);
            case 0xfa: return eightBitALoadMem(true);
            case 0x3e: return eightBitALoadMem(false);
            // LD n,A
            case 0x47: return eightBitLdR1R2(GBRegisters.Reg.B, GBRegisters.Reg.A);
            case 0x4f: return eightBitLdR1R2(GBRegisters.Reg.C, GBRegisters.Reg.A);
            case 0x57: return eightBitLdR1R2(GBRegisters.Reg.D, GBRegisters.Reg.A);
            case 0x5f: return eightBitLdR1R2(GBRegisters.Reg.E, GBRegisters.Reg.A);
            case 0x67: return eightBitLdR1R2(GBRegisters.Reg.H, GBRegisters.Reg.A);
            case 0x6f: return eightBitLdR1R2(GBRegisters.Reg.L, GBRegisters.Reg.A);
            case 0x02: return eightBitLdR1R2(GBRegisters.Reg.BC, GBRegisters.Reg.A);
            case 0x12: return eightBitLdR1R2(GBRegisters.Reg.DE, GBRegisters.Reg.A);
            case 0x77: return eightBitLdR1R2(GBRegisters.Reg.HL, GBRegisters.Reg.A);
            case 0xea: return eightBitLoadToMem();
            // LD A, (C)
            case 0xf2: return eightBitLDfromAC();
            case 0xe2: return eightBitLDtoAC();
            // LDD A,(HL)
            case 0x3a: return eightBitLDAHl();
            // LDD (HL), A
            case 0x32: return eightBitStoreHL();
            // LDI (HL), A
            case 0x2a: return eightBitLDIA();
            // LDI (HL), A
            case 0x22: return eightBitLDIHLA();
            // LDH (n), A, LDH A,(n)
            case 0xe0: return eightBitLdhA(true);
            case 0xf0: return eightBitLdhA(false);
            
            /*****16 BIT LOADS*****/
            //LD n, nn
            case 0x01: return sixteenBitLdNNn(GBRegisters.Reg.BC);
            case 0x11: return sixteenBitLdNNn(GBRegisters.Reg.DE);
            case 0x21: return sixteenBitLdNNn(GBRegisters.Reg.HL);
            case 0x31: return sixteenBitLdNNnSP();
            //LD SP,HL
            case 0xf9: return sixteenBitLdSpHl();
            case 0xf8: return sixteenBitLdHlSp();
            //LD (nn), SP
            case 0x08: return sixteenBitLdNnSp();
            //Push nn to stack
            case 0xf5: return pushNN(GBRegisters.Reg.AF);
            case 0xc5: return pushNN(GBRegisters.Reg.BC);
            case 0xd5: return pushNN(GBRegisters.Reg.DE);
            case 0xe5: return pushNN(GBRegisters.Reg.HL);
            //POP nn off stack
            case 0xf1: return popNN(GBRegisters.Reg.AF);
            case 0xc1: return popNN(GBRegisters.Reg.BC);
            case 0xd1: return popNN(GBRegisters.Reg.DE);
            case 0xe1: return popNN(GBRegisters.Reg.HL);
            
            
            /******8-BIT ALU*****/
            //ADD A,n
            case 0x87: return addAN(GBRegisters.Reg.A, false, false);
            case 0x80: return addAN(GBRegisters.Reg.B, false, false);
            case 0x81: return addAN(GBRegisters.Reg.C, false, false);
            case 0x82: return addAN(GBRegisters.Reg.D, false, false);
            case 0x83: return addAN(GBRegisters.Reg.E, false, false);
            case 0x84: return addAN(GBRegisters.Reg.H, false, false);
            case 0x85: return addAN(GBRegisters.Reg.L, false, false);                
            case 0x86: return addAN(GBRegisters.Reg.HL, false, false);                
            case 0xc6: return addAN(GBRegisters.Reg.A, false, true);                
            //ADC A,n
            case 0x8f: return addAN(GBRegisters.Reg.A, true, false);
            case 0x88: return addAN(GBRegisters.Reg.B, true, false); 
            case 0x89: return addAN(GBRegisters.Reg.C, true, false);
            case 0x8a: return addAN(GBRegisters.Reg.D, true, false);
            case 0x8b: return addAN(GBRegisters.Reg.E, true, false);
            case 0x8c: return addAN(GBRegisters.Reg.H, true, false);
            case 0x8d: return addAN(GBRegisters.Reg.L, true, false);
            case 0x8e: return addAN(GBRegisters.Reg.HL, true, false);
            case 0xce: return addAN(GBRegisters.Reg.A, true, true);
            //SUB n
            case 0x97: return subAN(GBRegisters.Reg.A, false, false);
            case 0x90: return subAN(GBRegisters.Reg.B, false, false);
            case 0x91: return subAN(GBRegisters.Reg.C, false, false);
            case 0x92: return subAN(GBRegisters.Reg.D, false, false);                   
            case 0x93: return subAN(GBRegisters.Reg.E, false, false);            
            case 0x94: return subAN(GBRegisters.Reg.H, false, false);            
            case 0x95: return subAN(GBRegisters.Reg.L, false, false);            
            case 0x96: return subAN(GBRegisters.Reg.HL, false, false);
            case 0xd6: return subAN(GBRegisters.Reg.A, false, true);
            //SUBC A,n    
            case 0x9f: return subAN(GBRegisters.Reg.A, true, false); 
            case 0x98: return subAN(GBRegisters.Reg.B, true, false);
            case 0x99: return subAN(GBRegisters.Reg.C, true, false);
            case 0x9a: return subAN(GBRegisters.Reg.D, true, false);
            case 0x9b: return subAN(GBRegisters.Reg.E, true, false);
            case 0x9c: return subAN(GBRegisters.Reg.H, true, false);
            case 0x9d: return subAN(GBRegisters.Reg.L, true, false);
            case 0x9e: return subAN(GBRegisters.Reg.HL, true, false);
            case 0xde: return subAN(GBRegisters.Reg.A, true, true);
            //AND N
            case 0xa7: return andN(GBRegisters.Reg.A, false);
            case 0xa0: return andN(GBRegisters.Reg.B, false);
            case 0xa1: return andN(GBRegisters.Reg.C, false);
            case 0xa2: return andN(GBRegisters.Reg.D, false);
            case 0xa3: return andN(GBRegisters.Reg.E, false);
            case 0xa4: return andN(GBRegisters.Reg.H, false);
            case 0xa5: return andN(GBRegisters.Reg.L, false);
            case 0xa6: return andN(GBRegisters.Reg.HL, false);
            case 0xe6: return andN(GBRegisters.Reg.A, true);
            //OR N
            case 0xb7: return orN(GBRegisters.Reg.A, false);
            case 0xb0: return orN(GBRegisters.Reg.B, false);            
            case 0xb1: return orN(GBRegisters.Reg.C, false);
            case 0xb2: return orN(GBRegisters.Reg.D, false);            
            case 0xb3: return orN(GBRegisters.Reg.E, false);
            case 0xb4: return orN(GBRegisters.Reg.H, false);            
            case 0xb5: return orN(GBRegisters.Reg.L, false);            
            case 0xb6: return orN(GBRegisters.Reg.HL, false);
            case 0xf6: return orN(GBRegisters.Reg.A, true);         
            // XOR n
            case 0xaf: return xorN(GBRegisters.Reg.A, false);
            case 0xa8: return xorN(GBRegisters.Reg.B, false);            
            case 0xa9: return xorN(GBRegisters.Reg.C, false);
            case 0xaa: return xorN(GBRegisters.Reg.D, false);
            case 0xab: return xorN(GBRegisters.Reg.E, false);
            case 0xac: return xorN(GBRegisters.Reg.H, false);
            case 0xad: return xorN(GBRegisters.Reg.L, false);
            case 0xae: return xorN(GBRegisters.Reg.HL, false);
            case 0xee: return xorN(GBRegisters.Reg.A, true);
            // CP n
            case 0xbf: return cpN(GBRegisters.Reg.A, false);
            case 0xb8: return cpN(GBRegisters.Reg.B, false);
            case 0xb9: return cpN(GBRegisters.Reg.C, false);
            case 0xba: return cpN(GBRegisters.Reg.D, false);
            case 0xbb: return cpN(GBRegisters.Reg.E, false);    
            case 0xbc: return cpN(GBRegisters.Reg.H, false);
            case 0xbd: return cpN(GBRegisters.Reg.L, false);    
            case 0xbe: return cpN(GBRegisters.Reg.HL, false);    
            case 0xfe: return cpN(GBRegisters.Reg.A, false);
            // INC n
            case 0x3c: return incN(GBRegisters.Reg.A);
            case 0x04: return incN(GBRegisters.Reg.B);    
            case 0x0c: return incN(GBRegisters.Reg.C);
            case 0x14: return incN(GBRegisters.Reg.D);            
            case 0x1c: return incN(GBRegisters.Reg.E);
            case 0x24: return incN(GBRegisters.Reg.H);            
            case 0x2c: return incN(GBRegisters.Reg.L);            
            case 0x34: return incN(GBRegisters.Reg.HL);
            // DEC n
            case 0x3d: return decN(GBRegisters.Reg.A);
            case 0x05: return decN(GBRegisters.Reg.B);
            case 0x0d: return decN(GBRegisters.Reg.C);            
            case 0x15: return decN(GBRegisters.Reg.D);            
            case 0x1d: return decN(GBRegisters.Reg.E);
            case 0x25: return decN(GBRegisters.Reg.H);            
            case 0x2d: return decN(GBRegisters.Reg.L);
            case 0x35: return decN(GBRegisters.Reg.HL);            
            //ADD HL,n
            case 0x09: return sixteenBitAdd(GBRegisters.Reg.BC, false);
            case 0x19: return sixteenBitAdd(GBRegisters.Reg.DE, false);
            case 0x29: return sixteenBitAdd(GBRegisters.Reg.HL, false);
            case 0x39: return sixteenBitAdd(GBRegisters.Reg.BC, true);
            //ADD SP,n
            case 0xe8: return addSPN();
            //INC nn
            case 0x03: return incNN(GBRegisters.Reg.BC, false);
            case 0x13: return incNN(GBRegisters.Reg.DE, false);
            case 0x23: return incNN(GBRegisters.Reg.HL, false);
            case 0x33: return incNN(GBRegisters.Reg.BC, true);
            //DEC nn
            case 0x0B: return decNN(GBRegisters.Reg.BC, false);
            case 0x1B: return decNN(GBRegisters.Reg.DE, false);
            case 0x2B: return decNN(GBRegisters.Reg.HL, false);
            case 0x3B: return decNN(GBRegisters.Reg.BC, true);

            // extended
            case 0xcb: return extendedOpcode();
            // DAA, PROBABLY NOT CORRECT
            case 0x27: return decAdjust();
            //CPL
            case 0x2f: return cplRegA();
            //CCF
            case 0x3f: return ccf();
            //SCF
            case 0x37: return scf();
            
            
            //Jumps
            case 0xc3: return jump();
            // conditional jump
            case 0xc2: return jumpC(opcode);
            case 0xca: return jumpC(opcode);
            case 0xd2: return jumpC(opcode);
            case 0xda: return jumpC(opcode);
            
            //TODO HALT,STOP, EI,DI
            case 0x76:
            case 0x10:
            case 0xf3:
            case 0xfb: System.err.println("TODO!!!!");
                       System.exit(1);
            
            
                       
                       
                       
            //TODO SHIFTS/ROTATES, BIT OPCODES           
            default:
                System.err.println("Unimplemented opcode: 0x" + 
                        Integer.toHexString(opcode));
                System.exit(1);
        }    
        return 0;
    }
    
    
    
    private int extendedOpcode() {
        int opcode = memory.readByte(pc);
        pc++;        
        
        switch(opcode) {
            //SWAP N
            case 0x37: return swapN(GBRegisters.Reg.A);
            case 0x30: return swapN(GBRegisters.Reg.B);
            case 0x31: return swapN(GBRegisters.Reg.C);
            case 0x32: return swapN(GBRegisters.Reg.D);
            case 0x33: return swapN(GBRegisters.Reg.E);
            case 0x34: return swapN(GBRegisters.Reg.H);
            case 0x35: return swapN(GBRegisters.Reg.L);
            case 0x36: return swapN(GBRegisters.Reg.HL);

           
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
    
    
    /**
     * Put value r2 into r2.
     * 
     * <p> Use with: r1,r2 = A,B,C,D,E,H,L,(HL)
     * 
     * @param dest destination register
     * @param scr  source register
     */ 
    private int eightBitLdR1R2(GBRegisters.Reg dest, GBRegisters.Reg src) {
        if (src == GBRegisters.Reg.HL) {
            int data = memory.readByte(registers.getReg(GBRegisters.Reg.HL));
            registers.setReg(dest, data);
            return 8;
        } else if ((dest == GBRegisters.Reg.HL) || (dest == GBRegisters.Reg.BC) ||
                   (dest == GBRegisters.Reg.DE)) {
            memory.writeByte(registers.getReg(dest), registers.getReg(src));
            return 8;
        } else {
            registers.setReg(dest, registers.getReg(src));
            return 4;
        }
    }
    
    /**
     * Special function for opcode 0x36
     * 
     * LD (HL), n
     */ 
    private int eightBitLoadFromMem() {
        int data = memory.readByte(pc);
        pc++;
        memory.writeByte(registers.getReg(GBRegisters.Reg.HL), data);
        return 12;
    }
    
    
    /**
     * Special function for opcode 0xea
     * 
     */ 
    private int eightBitLoadToMem() {
        int address = memory.readByte(pc);
        pc++;
        address = address | (memory.readByte(pc) << 8);
        memory.writeByte(address, registers.getReg(GBRegisters.Reg.A));
        return 16;
    }
    
    /**
     * LD A,n
     * 
     * Put value n into A. For opcodes 0x0a, 0x1a
     * 
     * 
     * @param src value n 
     */ 
    private int eightBitLdAN(GBRegisters.Reg src) {
            int data = memory.readByte(registers.getReg(src));
            registers.setReg(GBRegisters.Reg.A, data);
            return 8;
    }

    /**
     * LD A,n (where n is located in rom, or an address in rom)
     * 
     * For opcodes: 0xfa, 0x3e
     * 
     * @param isPointer If true, next two bytes are address in memory to load
     *     from. If false, eight bit immediate value is loaded.
     */ 
    private int eightBitALoadMem(boolean isPointer) {
        if (isPointer) {
            int address = memory.readByte(pc);
            pc++;
            address = address | (memory.readByte(pc) << 8);
            pc++;
            registers.setReg(GBRegisters.Reg.A, memory.readByte(address));
            return 16;   
        } else {
            int data = memory.readByte(pc);
            pc++;
            registers.setReg(GBRegisters.Reg.A, data);
            return 8;
        }
    }
    
    
    /**
     * LD A, (C)
     * 
     * put value at address $FF00 + register C into A
     * Same as: LD A, ($FF00 + C)
     */ 
    private int eightBitLDfromAC() {
        int data = memory.readByte(registers.getReg(GBRegisters.Reg.C) + 0xff00);
        registers.setReg(GBRegisters.Reg.A, data);
        return 8;
    }
    
    
    /**
     * LD (C), A
     * 
     * Put A into address $FF00 + register C
     * 
     */ 
    private int eightBitLDtoAC() {
        int address = registers.getReg(GBRegisters.Reg.C);
        int data = registers.getReg(GBRegisters.Reg.A);
        memory.writeByte(address + 0xff00, data);
        return 8;
    }
    
    
    
    /** LDD A, (HL)
     * 
     * Put value at address HL into A, decrement HL
     * 
     */ 
    private int eightBitLDAHl() {
        int address = registers.getReg(GBRegisters.Reg.HL);
        registers.setReg(GBRegisters.Reg.A, memory.readByte(address));
        registers.setReg(GBRegisters.Reg.HL, address - 1);
        return 8;
    }
    
    
    /**
     * LDD (HL), A
     * Put A into memory address HL, Decrement HL
     * 
     */ 
    private int eightBitStoreHL() {
        int address = registers.getReg(GBRegisters.Reg.HL);
        int data = registers.getReg(GBRegisters.Reg.A);
        
        memory.writeByte(address, data);
        registers.setReg(GBRegisters.Reg.HL, address - 1);
        return 8;
    }
    
    
    /**
     * Put value at address HL into A, increment HL
     * 
     */ 
    private int eightBitLDIA() {
        int address = registers.getReg(GBRegisters.Reg.HL);
        registers.setReg(GBRegisters.Reg.A, memory.readByte(address));
        registers.setReg(GBRegisters.Reg.HL, address + 1);
        return 8;
    }
    
    
    /**
     * Put A into memory at address HL. Increment HL
     * 
     */ 
    private int eightBitLDIHLA() {
        int address = registers.getReg(GBRegisters.Reg.HL);
        int data = registers.getReg(GBRegisters.Reg.A);
        
        memory.writeByte(address, data);
        registers.setReg(GBRegisters.Reg.HL, address + 1);
        return 8;
    }
    
    
    /**
     * LDH (n), A and LDH A, (n)
     * 
     * LDH (n), A - Put A into memory address $FF00 + n
     * LDH A,(n)  - Put memory address $FF00+n into A
     * 
     * @param writeToMem (required) if true LDH (n),A. if false LDH A,(n)
     * 
     */ 
    private int eightBitLdhA(boolean writeToMem) {
        int offset = memory.readByte(pc);
        pc++;
        if (writeToMem) {
            int data = registers.getReg(GBRegisters.Reg.A);
            memory.writeByte(0xff00 + offset, data);
        } else {
            int data = memory.readByte(0xff00 + offset);
            registers.setReg(GBRegisters.Reg.A, data);
        }
        return 12;   
    }
    
    
    /**
     * LD n, nn
     * 
     * Put value nn into n
     * 
     * nn - 16 Bit immediate value, n = BC, DE, HL
     * 
     */ 
    private int sixteenBitLdNNn(GBRegisters.Reg reg) {
        // read two byte data from memory LSB first
        int data = memory.readByte(pc);
        pc++;
        data = data | (memory.readByte(pc) << 8);
        pc++;
        switch(reg) {
            case BC: registers.setReg(GBRegisters.Reg.BC, data);
            case DE: registers.setReg(GBRegisters.Reg.DE, data);
            case HL: registers.setReg(GBRegisters.Reg.HL, data);
        }        
        return 12;
    }
    
    /**
     * LD n, nn
     * Put value nn into SP
     * 
     */ 
    private int sixteenBitLdNNnSP() {
        int data = memory.readByte(pc);
        pc++;
        data = data | (memory.readByte(pc) << 8);
        pc++;
        
        sp = data;
        return 12;
    }
    
    /**
     * LD SP,HL
     * 
     * Put HL into SP
     */ 
    private int sixteenBitLdSpHl() {
        sp = registers.getReg(GBRegisters.Reg.HL);
        return 8;
    }
    
    
    /**
     * LDHL SP,n
     * 
     * Put SP+n effective address into HL
     * 
     * <p>n = one byte signed immediate value
     * Z - reset
     * N - reset
     * H - set or reset according to operation
     * C - set or reset according to operation
     * 
     */ 
    private int sixteenBitLdHlSp() {
        int offset = memory.readByte(pc);
        pc++;

        byte data = (byte)memory.readByte((sp + offset) & 0xffff);
        registers.setReg(GBRegisters.Reg.HL, data);

        registers.resetAll();
        // NOT REALLY SURE HERE, CPU DOCUMENTATION NOT EXACT
        if (((sp + offset) & 0x1f) > 0xf) {
            registers.setH();
        }
        if ((sp + offset) > 0xffff) {
            registers.setC();
        }
        return 12;
    }
    
    
    /**
     * Put SP at address n (2 byte immediate address)
     * stored little endian
     */ 
    private int sixteenBitLdNnSp() {
        int address = memory.readByte(pc);
        pc++;
        address = address | (memory.readByte(pc) << 8);
        pc++;

        memory.writeByte(address, sp & 0xff);
        memory.writeByte(address + 1, ((sp & 0xff00) >> 8));
        
        return 20;
    }
    
    /**
     * Push Register Pair value to stack
     * 
     * @param src (required) register pair to push to stack
     */ 
    private int pushNN(GBRegisters.Reg src) {
        int registerPair = registers.getReg(src);
        sp--;
        memory.writeByte(sp, (registerPair & 0xff00) >> 8);
        sp--;
        memory.writeByte(sp, (registerPair & 0xff));
        return 16;
    }
    
    
    /**
     * pop value off stack to a register pair
     * 
     * @param dest (required) register to store data in
     */ 
    private int popNN(GBRegisters.Reg dest) {
        int data = memory.readByte(sp);
        sp++;
        data = data | (memory.readByte(sp) << 8);
        sp++;
        registers.setReg(dest, data);
        return 12;
    }
    
    
    /**
     * ADD A,n
     * 
     * Add n to A
     * 
     * Flags:
     * Z - set if result is zero
     * N - Reset
     * H - set if carry from bit 3
     * C - set if carry from bit 7
     * 
     * @param src (source to add from)
     * @param addCarry true if adding carry
     * @param readMem true if reading immediate value from memory
     *     if true, src ignored
     */ 
    private int addAN(GBRegisters.Reg src, boolean addCarry, boolean readMem) {
        int cycles;
        int regA = registers.getReg(GBRegisters.Reg.A);
        int toAdd;
        
        if (readMem) {
            toAdd = memory.readByte(pc);
            pc++;
            cycles = 8;
        } else if (src == GBRegisters.Reg.HL) {
            toAdd = memory.readByte(registers.getReg(src));
            cycles = 8;
        } else {
            toAdd = registers.getReg(src);
            cycles = 4;
        }
        toAdd += (addCarry) ? 1 : 0;

        //add
        registers.setReg(GBRegisters.Reg.A, toAdd + regA);
        
        //flags
        registers.resetAll();
        if (registers.getReg(GBRegisters.Reg.A) == 0) {
            registers.setZ();
        }
        if ((regA & 0xf) + (toAdd & 0xf) > 0xf) {
            registers.setH();
        }
        if (toAdd + regA > 0xff) {
            registers.setC();
        }
        
        return cycles;
    }

    
    /**
     * SUB n, SUBC A,n
     * Subtract N from A
     * Z- Set if result is zero
     * N - Set
     * H - set if no borrow from bit 4
     * C - set if no borrow
     * 
     */ 
    private int subAN(GBRegisters.Reg src, boolean addCarry, boolean readMem) {
        int cycles;
        int regA = registers.getReg(GBRegisters.Reg.A);
        int toSub;
        
        if (readMem) {
            toSub = memory.readByte(pc);
            pc++;
            cycles = 8;
        } else if (src == GBRegisters.Reg.HL) {
            toSub = memory.readByte(registers.getReg(src));
            cycles = 8;
        } else {
            toSub = registers.getReg(src);
            cycles = 4;
        }
        toSub += (addCarry) ? 1 : 0;
        //sub
        registers.setReg(GBRegisters.Reg.A, regA - toSub);

        //flags
        registers.resetAll();
        if (registers.getReg(GBRegisters.Reg.A) == 0) {
            registers.setZ();
        }
        registers.setN();
        if ((regA & 0xf) - (toSub & 0xf) < 0) {
            registers.setH();
        }
        if (regA < toSub) {
            registers.setC();
        }
        return cycles;
    }
    
    /**
     * And N with A, result in A
     * 
     * FLAGS:
     * Z - Set if result is 0
     * N - Reset
     * H - Set
     * C - Reset
     * 
     * @param src (required) N to and
     * @param readMem (required) true if reading immediate value from
     *     memory (if true, src is ignored)
     */ 
    private int andN(GBRegisters.Reg src, boolean readMem) {
        int cycles;
        int data;
        
        if (readMem) {
            data = memory.readByte(pc);
            pc++;
            cycles = 8;
        } else if (src == GBRegisters.Reg.HL) {
            data = registers.getReg(src);
            cycles = 8;
        } else {
            data = registers.getReg(src);
            cycles = 4;
        }
        
        int regA = registers.getReg(GBRegisters.Reg.A);
        registers.setReg(GBRegisters.Reg.A, data & regA);
        
        registers.resetAll();
        if ((data & regA) == 0) {
            registers.setZ();
        }    
        registers.setH();
        
        return cycles;
    }
    
    /**
     * OR N with A, result in A
     * 
     * FLAGS:
     * Z - Set if result is 0
     * N - Reset
     * H - Reset
     * C - Reset
     * 
     * @param src (required) N to and
     * @param readMem (required) true if reading immediate value from
     *     memory (if true, src is ignored)
     */ 
    private int orN(GBRegisters.Reg src, boolean readMem) {
        int cycles;
        int data;
        
        if (readMem) {
            data = memory.readByte(pc);
            pc++;
            cycles = 8;
        } else if (src == GBRegisters.Reg.HL) {
            data = registers.getReg(src);
            cycles = 8;
        } else {
            data = registers.getReg(src);
            cycles = 4;
        }
        
        int regA = registers.getReg(GBRegisters.Reg.A);
        registers.setReg(GBRegisters.Reg.A, data | regA);

        registers.resetAll();
        if ((data | regA) == 0) {
            registers.setZ();
        }    
        
        return cycles;
    }

    /**
     * XOR n
     * 
     * Logical XOR n, with A, result in A
     * 
     * FLAGS
     * Z - set if result is 0
     * N, H, C = Reset
     * @param src (required) src register
     * @param readMem (required) true if reading immediate value from
     *     memory (if true src ignored)
     * @return clock cycles taken
     */ 
    private int xorN(GBRegisters.Reg src, boolean readMem) {
        int cycles;
        int data;
        
        if (readMem) {
            data = memory.readByte(pc);
            pc++;
            cycles = 8;
        } else if (src == GBRegisters.Reg.HL) {
            data = registers.getReg(src);
            cycles = 8;
        } else {
            data = registers.getReg(src);
            cycles = 4;
        }
        
        int regA = registers.getReg(GBRegisters.Reg.A);
        registers.setReg(GBRegisters.Reg.A, data ^ regA);
        
        registers.resetAll();
        if ((data ^ regA) == 0) {
            registers.setZ();
        }    
        
        return cycles;
    }
    
    /**
     * CP n
     * 
     * Compare A with n. Basically A - n subtraction but 
     * results are thrown away
     * 
     * FLAGS
     * Z - set if result is 0 (if A == n)
     * N - set
     * H - Set if no borrow from bit 4
     * C = set if no morrow (Set if A is less than n)
     * 
     * @param src (required) src register
     * @param readMem (required) true if reading immediate value from
     *     memory (if true src ignored)
     * @return clock cycles taken
     */ 
    private int cpN(GBRegisters.Reg src, boolean readMem) {
        int cycles;
        int data;
        
        if (readMem) {
            data = memory.readByte(pc);
            pc++;
            cycles = 8;
        } else if (src == GBRegisters.Reg.HL) {
            data = registers.getReg(src);
            cycles = 8;
        } else {
            data = registers.getReg(src);
            cycles = 4;
        }

        int regA = registers.getReg(GBRegisters.Reg.A);

        registers.resetAll();
        if (regA == data) {
            registers.setZ();
        }
        registers.setN();
        if ((regA & 0xf) - (data & 0xf) < 0) {
            registers.setH();
        }
        if (regA < data) {
            registers.setC();
        }
        return cycles;
    }
    

    /**
     * INC n
     * 
     * Increment register n.
     * 
     * FLAGS:
     * Z - Set if result is 0
     * N - Reset
     * H - Set if carry from bit 3
     * C - Not affected
     * @param src (required) register to increment
     */ 
    private int incN(GBRegisters.Reg src) {
        int reg = registers.getReg(src);
        registers.setReg(src, reg + 1);
        
        
        registers.resetZ();
        if (registers.getReg(src) == 0) {
            registers.setZ();
        }
        registers.resetN();
        registers.resetH();
        if (((reg & 0xf) + 1) > 0xf) {
            registers.setH();
        }
        
        return (src == GBRegisters.Reg.HL) ? 12 : 4;
    }
    
    /**
     * DEC n
     * 
     * Decrement register n.
     * 
     * FLAGS:
     * Z - Set if result is 0
     * N - Set
     * H - Set if no borrow from bit 4
     * C - Not affected
     * @param src (required) register to decrement
     */ 
    private int decN(GBRegisters.Reg src) {
        int reg = registers.getReg(src);
        registers.setReg(src, reg - 1);
        
        registers.resetZ();
        if (registers.getReg(src) == 0) {
            registers.setZ();
        }
        registers.setN();
        registers.resetH();
        if (((reg & 0xf) - 1) < 0) {
            registers.setH();
        }
        
        return (src == GBRegisters.Reg.HL) ? 12 : 4;
    }
    
    
    
    /**
     * ADD HL,n
     * 
     * Add n to HL
     * 
     * n = BC,DE,HL,SP
     * 
     * Flags
     * Z - Not affected
     * N - Reset
     * H - Set if carry from bit 11
     * C - Set if carry from bit 15
     * 
     * @param src source register to add
     * @param addSP boolean (if true, adds stackpointer instead of register
     *     to HL, ignores src)
     * @return clock cycles taken
     */ 
    private int sixteenBitAdd(GBRegisters.Reg src, boolean addSP) {
        int toAdd;
        int regVal = registers.getReg(GBRegisters.Reg.HL);
        
        if (addSP) {
            toAdd = sp;
        } else {
            toAdd = registers.getReg(src);
        }
        
        registers.setReg(GBRegisters.Reg.HL, regVal + toAdd);
        
        //flags
        registers.resetN();
        registers.resetH();
        if ((regVal & 0xfff) + (toAdd & 0xfff) > 0xfff) {
            registers.setH();
        }
        registers.resetC();
        if ((regVal + toAdd) > 0xffff) {
            registers.setC();
        }
        return 8;
    }
    
    
    /**
     * ADD SP,n
     * Add n to sp
     * 
     * Flags:
     * Z, N - Reset
     * H, C - Set/reset according to operation????
     */ 
    private int addSPN() {
        byte offset = (byte)memory.readByte(pc);
        pc++;
        
        sp += offset;
        
        registers.resetAll();
        if (((sp + offset) & 0x1f) > 0xf) {
            registers.setH();
        }
        if (((sp & 0xffff) + offset) > 0xffff) {
            registers.setC();
        }
        return 16;
    }
    
    
    /**
     * INC nn
     * 
     * Increment register nn
     * 
     * Affects NO FLAGS
     * 
     * @param reg register to increment (ignored if incSP is true)
     * @param incSP boolean if true, ignore reg and increment sp
     */ 
    private int incNN(GBRegisters.Reg reg, boolean incSP) {
        if (incSP) {
            sp++;
        } else {
            int value = registers.getReg(reg);
            registers.setReg(reg, value + 1);
        }
        return 8;
    }
    
    /**
     * DEC nn
     * 
     * Decrement register nn
     * 
     * no flags affected
     * 
     * @param reg register to increment (ignored if incSP is true)
     * @param decSP boolean if true, ignore reg and increment sp
     */
    private int decNN(GBRegisters.Reg reg, boolean decSP) {
        if (decSP) {
            sp--;
        } else {
            int value = registers.getReg(reg);
            registers.setReg(reg, value - 1);
        }    
        return 8;
    }
    
    /**
     * Swap N
     * 
     * swap upper and lower nibbles of n
     * 
     * Flags Affected: 
     * Z - set if result is 0
     * NHC - reset
     * 
     * @param reg (required) register to swap
     */ 
    private int swapN(GBRegisters.Reg reg) {
        int data = registers.getReg(reg);
        
        registers.resetAll();
        if (reg == GBRegisters.Reg.HL) {
            int lowNib = data & 0xf;
            int highNib = (data & 0xf000) >> 12;
            int midByte = (data & 0x0ff0) >> 4;
            
            data = (lowNib << 12) | (midByte << 4) | highNib;
            registers.setReg(reg, data);
            
            if (data == 0) {
                registers.setZ();
            }
            return 16;
        } else {
            int lowNib = data & 0xf;
            int highNib = (data & 0xf0) >> 4;
            registers.setReg(reg, highNib | (lowNib << 4));
            
            if ((highNib | (lowNib << 4)) == 0) {
                registers.setZ();
            }
            return 8;
        }
    }
    
    
    /**
     * Decimal adjust register A
     * 
     * MIGHT NOT BE CORRECT...instructions vague 
     * 
     * Flags:
     * z - Set if A is zero
     * N - Not affected
     * H - reset
     * C - set or reset according to operation
     */ 
    private int decAdjust() {
        int flags = registers.getReg(GBRegisters.Reg.F);
        int reg = registers.getReg(GBRegisters.Reg.A);
        registers.resetC();
        
        
        if (((flags & 0x20) == 0x20) || ((reg & 0xf) > 0x9)) {
            reg += 0x6;
            registers.setC(); //?????
        }
        
        if (((flags & 0x10) == 0x10) || ((reg & 0xf0) >> 4) > 0x9) {
            reg += 0x60;
        }
        
        registers.setReg(GBRegisters.Reg.A, reg);
        
        registers.resetZ();
        if (reg == 0) {
            registers.setZ();
        }
        registers.resetH();
        
        return 4;
    }    
    
    /**
     * Complement register A
     * 
     * (toggles all bits)
     */ 
    private int cplRegA() {
        int reg = registers.getReg(GBRegisters.Reg.A);
        reg = ~reg;
        
        registers.setReg(GBRegisters.Reg.A, reg & 0xffff);
        return 4;
    }
    
    /**
     * Complement carry flag
     * 
     * FLAGS:
     * Z - not affected
     * H, N - reset
     * C - Complemented
     */ 
    private int ccf() {
        registers.toggleC();
        registers.resetN();
        registers.resetH();
        return 4;
    }    
    
    /** 
     * Set carry flag
     * Flags:
     * Z - Not affected
     * N,H - reset
     * C - Set
     */ 
    private int scf() {
        registers.resetH();
        registers.resetN();
        registers.setC();
        return 4;
    }
    
    
    /**
     * Jump to address
     * 
     */ 
    private int jump() {
        int address = memory.readByte(pc);
        pc++;
        address = address | (memory.readByte(pc) << 8);
        pc = address;
        return 12;
    }
    
    /**
     * Conditional jump
     * 
     */ 
    private int jumpC(int opcode) {
        int flags = registers.getReg(GBRegisters.Reg.F);
        int address = memory.readByte(pc);
        pc++;
        address = address | (memory.readByte(pc) << 8);
        pc++;
        
        if (opcode == 0xca) {
            if ((flags & 0x80) == 0x80) {
                pc = address;
            }
        } else if (opcode == 0xc2) {
            if ((flags & 0x80) == 0x0) {
                pc = address;
            }
        } else if (opcode == 0xda) {
            if ((flags & 0x10) == 0x10) {
                pc = address;
            }
        } else if (opcode == 0xd2) {
            if ((flags & 0x10) == 0x0) {
                pc = address;
            }
        }
        
        return 12;
    }
}

