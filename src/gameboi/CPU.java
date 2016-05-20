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

        registers.resetZ();
        registers.resetN();
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
}

