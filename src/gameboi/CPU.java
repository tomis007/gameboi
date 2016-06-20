/*
 *  Gameboi
 */
package gameboi;

// for testing
import java.util.Scanner;

/**
 * Z80 Gameboy CPU
 * 
 * Implementation of a gameboy cpu. Eight registers (a,b,c,d,e,f,h,l), a
 * stack pointer, program counter, and timers
 * 
 * @author tomis007
 */
public class CPU {
    // registers
    private final GBRegisters registers;
    
    // stack pointer, program counter
    private int sp;
    private int pc;

    //for debugging    
    private GPU gpu;
    private boolean debug;
    
    //associated memory to use with CPU
    private final GBMem memory;
    private final int clockSpeed;
    private int timerCounter;
    private int divideCounter;
    private boolean interruptsEnabled;
    
    //constants 
    private static final GBRegisters.Reg A = GBRegisters.Reg.A;
    private static final GBRegisters.Reg B = GBRegisters.Reg.B;
    private static final GBRegisters.Reg C = GBRegisters.Reg.C;
    private static final GBRegisters.Reg D = GBRegisters.Reg.D;
    private static final GBRegisters.Reg E = GBRegisters.Reg.E;
    private static final GBRegisters.Reg F = GBRegisters.Reg.F;
    private static final GBRegisters.Reg H = GBRegisters.Reg.H;
    private static final GBRegisters.Reg L = GBRegisters.Reg.L;
    private static final GBRegisters.Reg HL = GBRegisters.Reg.HL;
    private static final GBRegisters.Reg BC = GBRegisters.Reg.BC;
    private static final GBRegisters.Reg DE = GBRegisters.Reg.DE;
    private static final GBRegisters.Reg AF = GBRegisters.Reg.AF;
    
    //flag bitnum constants
    private static final int ZERO_F = 7;
    private static final int SUBTRACT_F = 6;
    private static final int HALFCARRY_F = 5;
    private static final int CARRY_F = 4;
    
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
        clockSpeed = 4194304;
        timerCounter = 1024;
        divideCounter = clockSpeed / 16382;
        interruptsEnabled = false;
        
        debug = false;
    }
    
    /**
     * for debug mode
     * 
     */ 
    public void setGPU(GPU gpu) {
        this.gpu = gpu;
    }
    
    /**
     * returns registers (for testing)
     */ 
    public GBRegisters getReg() {
        return registers;
    }
    
    /**
     * Execute the next opcode in memory, and update the CPU timers
     * 
     * @return clock cycles taken to execute the opcode
     */ 
    public int ExecuteOpcode() {

//        if (pc == 0x36f && !debug) {
//            enterDebugMode();
//        }

//        System.out.println(Integer.toHexString(pc));
        int opcode = memory.readByte(pc);
        pc++;

        int cycles = runInstruction(opcode);
        updateDivideRegister(cycles);
        updateTimers(cycles);
        checkInterrupts();
        
        return cycles;
    }
  
    /**
     * Debug mode for cpu instructions
     * runs one instruction at a time, prints registers
     * 
     */ 
    private void enterDebugMode() {
        debug = true;
        Scanner sc = new Scanner(System.in);
        String input;
        System.out.print(" > ");
        input = sc.nextLine();
        int scanLine = memory.getScanLine();
        while (!"q".equals(input)) {
            if ("p".equals(input)) {
                dumpRegisters();
            } else if ("n".equals(input)) {
                dumpRegisters();
                int cycles = ExecuteOpcode();
                gpu.updateGraphics(cycles);
            } 
            System.out.print(" > ");
            input = sc.nextLine();
        }
    }
    
    /**
     * Opcode Instructions for the Gameboy Z80 Chip.
     * 
     * <p>
     * Runs the instruction associated with the opcode, and returns the 
     * clock cycles taken.
     * 
     * TODO HALT,STOP
     * @param opcode (required) opcode to execute
     * @return number of cycles taken to execute
     */ 
    private int runInstruction(int opcode) { 
        switch (opcode) {
            case 0x0:  return 4; //NOP

            /*****8 BIT LOADS*****/
            // LD nn,n
            case 0x06: return eightBitLdNnN(B);
            case 0x0e: return eightBitLdNnN(C);
            case 0x16: return eightBitLdNnN(D);
            case 0x1e: return eightBitLdNnN(E);
            case 0x26: return eightBitLdNnN(H);
            case 0x2e: return eightBitLdNnN(L);
            //LD r1,r2
            case 0x7f: return eightBitLdR1R2(A, A);
            case 0x78: return eightBitLdR1R2(A, B);
            case 0x79: return eightBitLdR1R2(A, C);
            case 0x7a: return eightBitLdR1R2(A, D);
            case 0x7b: return eightBitLdR1R2(A, E);
            case 0x7c: return eightBitLdR1R2(A, H);
            case 0x7d: return eightBitLdR1R2(A, L);
            case 0x7e: return eightBitLdR1R2(A, HL);
            case 0x40: return eightBitLdR1R2(B, B);
            case 0x41: return eightBitLdR1R2(B, C);
            case 0x42: return eightBitLdR1R2(B, D);
            case 0x43: return eightBitLdR1R2(B, E);
            case 0x44: return eightBitLdR1R2(B, H);
            case 0x45: return eightBitLdR1R2(B, L);
            case 0x46: return eightBitLdR1R2(B, HL);
            case 0x48: return eightBitLdR1R2(C, B);
            case 0x49: return eightBitLdR1R2(C, C);
            case 0x4a: return eightBitLdR1R2(C, D);
            case 0x4b: return eightBitLdR1R2(C, E);
            case 0x4c: return eightBitLdR1R2(C, H);
            case 0x4d: return eightBitLdR1R2(C, L);
            case 0x4e: return eightBitLdR1R2(C, HL);
            case 0x50: return eightBitLdR1R2(D, B);
            case 0x51: return eightBitLdR1R2(D, C);
            case 0x52: return eightBitLdR1R2(D, D);
            case 0x53: return eightBitLdR1R2(D, E);
            case 0x54: return eightBitLdR1R2(D, H);
            case 0x55: return eightBitLdR1R2(D, L);
            case 0x56: return eightBitLdR1R2(D, HL);
            case 0x58: return eightBitLdR1R2(E, B);
            case 0x59: return eightBitLdR1R2(E, C);
            case 0x5a: return eightBitLdR1R2(E, D);
            case 0x5b: return eightBitLdR1R2(E, E);
            case 0x5c: return eightBitLdR1R2(E, H);
            case 0x5d: return eightBitLdR1R2(E, L);
            case 0x5e: return eightBitLdR1R2(E, HL);
            case 0x60: return eightBitLdR1R2(H, B);
            case 0x61: return eightBitLdR1R2(H, C);
            case 0x62: return eightBitLdR1R2(H, D);
            case 0x63: return eightBitLdR1R2(H, E);
            case 0x64: return eightBitLdR1R2(H, H);
            case 0x65: return eightBitLdR1R2(H, L);
            case 0x66: return eightBitLdR1R2(H, HL);
            case 0x68: return eightBitLdR1R2(L, B);
            case 0x69: return eightBitLdR1R2(L, C);
            case 0x6a: return eightBitLdR1R2(L, D);
            case 0x6b: return eightBitLdR1R2(L, E);
            case 0x6c: return eightBitLdR1R2(L, H);
            case 0x6d: return eightBitLdR1R2(L, L);
            case 0x6e: return eightBitLdR1R2(L, HL);
            case 0x70: return eightBitLdR1R2(HL, B);
            case 0x71: return eightBitLdR1R2(HL, C);
            case 0x72: return eightBitLdR1R2(HL, D);
            case 0x73: return eightBitLdR1R2(HL, E);
            case 0x74: return eightBitLdR1R2(HL, H);
            case 0x75: return eightBitLdR1R2(HL, L);
            // special 8 bit load from memory
            case 0x36: return eightBitLoadFromMem();
            // LD A,n
            case 0x0a: return eightBitLdAN(GBRegisters.Reg.BC);
            case 0x1a: return eightBitLdAN(GBRegisters.Reg.DE);
            case 0xfa: return eightBitALoadMem(true);
            case 0x3e: return eightBitALoadMem(false);
            // LD n,A
            case 0x47: return eightBitLdR1R2(GBRegisters.Reg.B, A);
            case 0x4f: return eightBitLdR1R2(GBRegisters.Reg.C, A);
            case 0x57: return eightBitLdR1R2(GBRegisters.Reg.D, A);
            case 0x5f: return eightBitLdR1R2(GBRegisters.Reg.E, A);
            case 0x67: return eightBitLdR1R2(GBRegisters.Reg.H, A);
            case 0x6f: return eightBitLdR1R2(GBRegisters.Reg.L, A);
            case 0x02: return eightBitLdR1R2(GBRegisters.Reg.BC, A);
            case 0x12: return eightBitLdR1R2(GBRegisters.Reg.DE, A);
            case 0x77: return eightBitLdR1R2(GBRegisters.Reg.HL, A);
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
            case 0x87: return addAN(A, false, false);
            case 0x80: return addAN(GBRegisters.Reg.B, false, false);
            case 0x81: return addAN(GBRegisters.Reg.C, false, false);
            case 0x82: return addAN(GBRegisters.Reg.D, false, false);
            case 0x83: return addAN(GBRegisters.Reg.E, false, false);
            case 0x84: return addAN(GBRegisters.Reg.H, false, false);
            case 0x85: return addAN(GBRegisters.Reg.L, false, false);                
            case 0x86: return addAN(GBRegisters.Reg.HL, false, false);                
            case 0xc6: return addAN(A, false, true);                
            //ADC A,n
            case 0x8f: return addAN(A, true, false);
            case 0x88: return addAN(GBRegisters.Reg.B, true, false); 
            case 0x89: return addAN(GBRegisters.Reg.C, true, false);
            case 0x8a: return addAN(GBRegisters.Reg.D, true, false);
            case 0x8b: return addAN(GBRegisters.Reg.E, true, false);
            case 0x8c: return addAN(GBRegisters.Reg.H, true, false);
            case 0x8d: return addAN(GBRegisters.Reg.L, true, false);
            case 0x8e: return addAN(GBRegisters.Reg.HL, true, false);
            case 0xce: return addAN(A, true, true);
            //SUB n
            case 0x97: return subAN(A, false, false);
            case 0x90: return subAN(GBRegisters.Reg.B, false, false);
            case 0x91: return subAN(GBRegisters.Reg.C, false, false);
            case 0x92: return subAN(GBRegisters.Reg.D, false, false);                   
            case 0x93: return subAN(GBRegisters.Reg.E, false, false);            
            case 0x94: return subAN(GBRegisters.Reg.H, false, false);            
            case 0x95: return subAN(GBRegisters.Reg.L, false, false);            
            case 0x96: return subAN(GBRegisters.Reg.HL, false, false);
            case 0xd6: return subAN(A, false, true);
            //SUBC A,n    
            case 0x9f: return subAN(A, true, false); 
            case 0x98: return subAN(GBRegisters.Reg.B, true, false);
            case 0x99: return subAN(GBRegisters.Reg.C, true, false);
            case 0x9a: return subAN(GBRegisters.Reg.D, true, false);
            case 0x9b: return subAN(GBRegisters.Reg.E, true, false);
            case 0x9c: return subAN(GBRegisters.Reg.H, true, false);
            case 0x9d: return subAN(GBRegisters.Reg.L, true, false);
            case 0x9e: return subAN(GBRegisters.Reg.HL, true, false);
            case 0xde: return subAN(A, true, true);
            //AND N
            case 0xa7: return andN(A, false);
            case 0xa0: return andN(GBRegisters.Reg.B, false);
            case 0xa1: return andN(GBRegisters.Reg.C, false);
            case 0xa2: return andN(GBRegisters.Reg.D, false);
            case 0xa3: return andN(GBRegisters.Reg.E, false);
            case 0xa4: return andN(GBRegisters.Reg.H, false);
            case 0xa5: return andN(GBRegisters.Reg.L, false);
            case 0xa6: return andN(GBRegisters.Reg.HL, false);
            case 0xe6: return andN(A, true);
            //OR N
            case 0xb7: return orN(A, false);
            case 0xb0: return orN(GBRegisters.Reg.B, false);            
            case 0xb1: return orN(GBRegisters.Reg.C, false);
            case 0xb2: return orN(GBRegisters.Reg.D, false);            
            case 0xb3: return orN(GBRegisters.Reg.E, false);
            case 0xb4: return orN(GBRegisters.Reg.H, false);            
            case 0xb5: return orN(GBRegisters.Reg.L, false);            
            case 0xb6: return orN(GBRegisters.Reg.HL, false);
            case 0xf6: return orN(A, true);         
            // XOR n
            case 0xaf: return xorN(A, false);
            case 0xa8: return xorN(GBRegisters.Reg.B, false);            
            case 0xa9: return xorN(GBRegisters.Reg.C, false);
            case 0xaa: return xorN(GBRegisters.Reg.D, false);
            case 0xab: return xorN(GBRegisters.Reg.E, false);
            case 0xac: return xorN(GBRegisters.Reg.H, false);
            case 0xad: return xorN(GBRegisters.Reg.L, false);
            case 0xae: return xorN(GBRegisters.Reg.HL, false);
            case 0xee: return xorN(A, true);
            // CP n
            case 0xbf: return cpN(A, false);
            case 0xb8: return cpN(GBRegisters.Reg.B, false);
            case 0xb9: return cpN(GBRegisters.Reg.C, false);
            case 0xba: return cpN(GBRegisters.Reg.D, false);
            case 0xbb: return cpN(GBRegisters.Reg.E, false);    
            case 0xbc: return cpN(GBRegisters.Reg.H, false);
            case 0xbd: return cpN(GBRegisters.Reg.L, false);    
            case 0xbe: return cpN(GBRegisters.Reg.HL, false);    
            case 0xfe: return cpN(A, true);
            // INC n
            case 0x3c: return incN(A);
            case 0x04: return incN(GBRegisters.Reg.B);    
            case 0x0c: return incN(GBRegisters.Reg.C);
            case 0x14: return incN(GBRegisters.Reg.D);            
            case 0x1c: return incN(GBRegisters.Reg.E);
            case 0x24: return incN(GBRegisters.Reg.H);            
            case 0x2c: return incN(GBRegisters.Reg.L);            
            case 0x34: return incN(GBRegisters.Reg.HL);
            // DEC n
            case 0x3d: return decN(A);
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
            // DAA, PROBABLY NOT CORRECT TODO
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
            // JP (HL)
            case 0xe9: return jumpHL();
            //JR n
            case 0x18: return jumpN();
            //JR cc, n
            case 0x20: return jumpCN(opcode);
            case 0x28: return jumpCN(opcode);
            case 0x30: return jumpCN(opcode);
            case 0x38: return jumpCN(opcode);
            
            
            //TODO HALT,STOP
            case 0x76: System.err.println("Halt not implemented"); return 0;
            case 0x10: System.err.println("STOP not implemented"); return 0;
            case 0xf3: return disableInterrupts();
            case 0xfb: return enableInterrupts();

    
            //calls
            case 0xcd: return call();
            case 0xc4: return callC(opcode);
            case 0xcc: return callC(opcode);
            case 0xd4: return callC(opcode);
            case 0xdc: return callC(opcode);
            
            //restarts
            case 0xc7: return restart(0x00);
            case 0xcf: return restart(0x08);
            case 0xd7: return restart(0x10);
            case 0xdf: return restart(0x18);
            case 0xe7: return restart(0x20);
            case 0xef: return restart(0x28);
            case 0xf7: return restart(0x30);
            case 0xff: return restart(0x38);            
            
            
            //RETURNs
            case 0xc9: return ret();
            case 0xc0: return retC(opcode);
            case 0xc8: return retC(opcode);
            case 0xd0: return retC(opcode);
            case 0xd8: return retC(opcode);
            //RETI
            case 0xd9: return retI();
                       
            //ROTATES AND SHIFTS
            //RLCA
            case 0x07: return rlcA();
            //RLA
            case 0x17: return rlA();      
            //RRCA
            case 0x0f: return rrcA();
            case 0x1f: return rrA();
                       
                       
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
            case 0x37: return swapN(A);
            case 0x30: return swapN(GBRegisters.Reg.B);
            case 0x31: return swapN(GBRegisters.Reg.C);
            case 0x32: return swapN(GBRegisters.Reg.D);
            case 0x33: return swapN(GBRegisters.Reg.E);
            case 0x34: return swapN(GBRegisters.Reg.H);
            case 0x35: return swapN(GBRegisters.Reg.L);
            case 0x36: return swapN(GBRegisters.Reg.HL);

            //RLC n
            case 0x07: return rlcN(GBRegisters.Reg.A);
            case 0x00: return rlcN(GBRegisters.Reg.B);
            case 0x01: return rlcN(GBRegisters.Reg.C);
            case 0x02: return rlcN(GBRegisters.Reg.D);
            case 0x03: return rlcN(GBRegisters.Reg.E);
            case 0x04: return rlcN(GBRegisters.Reg.H);
            case 0x05: return rlcN(GBRegisters.Reg.L);
            case 0x06: return rlcN(GBRegisters.Reg.HL);
            //RL n
            case 0x17: return rlN(GBRegisters.Reg.A);
            case 0x10: return rlN(GBRegisters.Reg.B);
            case 0x11: return rlN(GBRegisters.Reg.C);
            case 0x12: return rlN(GBRegisters.Reg.D);
            case 0x13: return rlN(GBRegisters.Reg.E);
            case 0x14: return rlN(GBRegisters.Reg.H);
            case 0x15: return rlN(GBRegisters.Reg.L);
            case 0x16: return rlN(GBRegisters.Reg.HL);
            //RRC n
            case 0x0f: return rrcN(GBRegisters.Reg.A);
            case 0x08: return rrcN(GBRegisters.Reg.B);
            case 0x09: return rrcN(GBRegisters.Reg.C);
            case 0x0a: return rrcN(GBRegisters.Reg.D);
            case 0x0b: return rrcN(GBRegisters.Reg.E);
            case 0x0c: return rrcN(GBRegisters.Reg.H);
            case 0x0d: return rrcN(GBRegisters.Reg.L);
            case 0x0e: return rrcN(GBRegisters.Reg.HL);
            //RR n
            case 0x1f: return rrN(GBRegisters.Reg.A);
            case 0x18: return rrN(GBRegisters.Reg.B);
            case 0x19: return rrN(GBRegisters.Reg.C);
            case 0x1a: return rrN(GBRegisters.Reg.D);
            case 0x1b: return rrN(GBRegisters.Reg.E);
            case 0x1c: return rrN(GBRegisters.Reg.H);
            case 0x1d: return rrN(GBRegisters.Reg.L);
            case 0x1e: return rrN(GBRegisters.Reg.HL);
            //SLA n
            case 0x27: return slAN(GBRegisters.Reg.A);
            case 0x20: return slAN(GBRegisters.Reg.B);
            case 0x21: return slAN(GBRegisters.Reg.C);
            case 0x22: return slAN(GBRegisters.Reg.D);
            case 0x23: return slAN(GBRegisters.Reg.E);
            case 0x24: return slAN(GBRegisters.Reg.H);
            case 0x25: return slAN(GBRegisters.Reg.L);
            case 0x26: return slAN(GBRegisters.Reg.HL);
            
            //SRA n
            case 0x2f: return srAL(GBRegisters.Reg.A, false);
            case 0x28: return srAL(GBRegisters.Reg.B, false);
            case 0x29: return srAL(GBRegisters.Reg.C, false);
            case 0x2a: return srAL(GBRegisters.Reg.D, false);
            case 0x2b: return srAL(GBRegisters.Reg.E, false);
            case 0x2c: return srAL(GBRegisters.Reg.H, false);
            case 0x2d: return srAL(GBRegisters.Reg.L, false);
            case 0x2e: return srAL(GBRegisters.Reg.HL, false);
            //SRL n
            case 0x3f: return srAL(GBRegisters.Reg.A, true);
            case 0x38: return srAL(GBRegisters.Reg.B, true);
            case 0x39: return srAL(GBRegisters.Reg.C, true);
            case 0x3a: return srAL(GBRegisters.Reg.D, true);
            case 0x3b: return srAL(GBRegisters.Reg.E, true);
            case 0x3c: return srAL(GBRegisters.Reg.H, true);
            case 0x3d: return srAL(GBRegisters.Reg.L, true);
            case 0x3e: return srAL(GBRegisters.Reg.HL, true);
            
            //Bit opcodes
            case 0x47: return bitBR(0, GBRegisters.Reg.A);
            case 0x40: return bitBR(0, GBRegisters.Reg.B);
            case 0x41: return bitBR(0, GBRegisters.Reg.C);
            case 0x42: return bitBR(0, GBRegisters.Reg.D);
            case 0x43: return bitBR(0, GBRegisters.Reg.E);
            case 0x44: return bitBR(0, GBRegisters.Reg.H);
            case 0x45: return bitBR(0, GBRegisters.Reg.L);
            case 0x46: return bitBR(0, GBRegisters.Reg.HL);

            case 0x4f: return bitBR(1, GBRegisters.Reg.A);
            case 0x48: return bitBR(1, GBRegisters.Reg.B);
            case 0x49: return bitBR(1, GBRegisters.Reg.C);
            case 0x4a: return bitBR(1, GBRegisters.Reg.D);
            case 0x4b: return bitBR(1, GBRegisters.Reg.E);
            case 0x4c: return bitBR(1, GBRegisters.Reg.H);
            case 0x4d: return bitBR(1, GBRegisters.Reg.L);
            case 0x4e: return bitBR(1, GBRegisters.Reg.HL); 
            
            case 0x57: return bitBR(2, GBRegisters.Reg.A);
            case 0x50: return bitBR(2, GBRegisters.Reg.B);
            case 0x51: return bitBR(2, GBRegisters.Reg.C);
            case 0x52: return bitBR(2, GBRegisters.Reg.D);
            case 0x53: return bitBR(2, GBRegisters.Reg.E);
            case 0x54: return bitBR(2, GBRegisters.Reg.H);
            case 0x55: return bitBR(2, GBRegisters.Reg.L);
            case 0x56: return bitBR(2, GBRegisters.Reg.HL);            

            case 0x5f: return bitBR(3, GBRegisters.Reg.A);
            case 0x58: return bitBR(3, GBRegisters.Reg.B);
            case 0x59: return bitBR(3, GBRegisters.Reg.C);
            case 0x5a: return bitBR(3, GBRegisters.Reg.D);
            case 0x5b: return bitBR(3, GBRegisters.Reg.E);
            case 0x5c: return bitBR(3, GBRegisters.Reg.H);
            case 0x5d: return bitBR(3, GBRegisters.Reg.L);
            case 0x5e: return bitBR(3, GBRegisters.Reg.HL);            

            case 0x67: return bitBR(4, GBRegisters.Reg.A);
            case 0x60: return bitBR(4, GBRegisters.Reg.B);
            case 0x61: return bitBR(4, GBRegisters.Reg.C);
            case 0x62: return bitBR(4, GBRegisters.Reg.D);
            case 0x63: return bitBR(4, GBRegisters.Reg.E);
            case 0x64: return bitBR(4, GBRegisters.Reg.H);
            case 0x65: return bitBR(4, GBRegisters.Reg.L);
            case 0x66: return bitBR(4, GBRegisters.Reg.HL);            

            case 0x6f: return bitBR(5, GBRegisters.Reg.A);
            case 0x68: return bitBR(5, GBRegisters.Reg.B);
            case 0x69: return bitBR(5, GBRegisters.Reg.C);
            case 0x6a: return bitBR(5, GBRegisters.Reg.D);
            case 0x6b: return bitBR(5, GBRegisters.Reg.E);
            case 0x6c: return bitBR(5, GBRegisters.Reg.H);
            case 0x6d: return bitBR(5, GBRegisters.Reg.L);
            case 0x6e: return bitBR(5, GBRegisters.Reg.HL);              
            
            case 0x77: return bitBR(6, GBRegisters.Reg.A);
            case 0x70: return bitBR(6, GBRegisters.Reg.B);
            case 0x71: return bitBR(6, GBRegisters.Reg.C);
            case 0x72: return bitBR(6, GBRegisters.Reg.D);
            case 0x73: return bitBR(6, GBRegisters.Reg.E);
            case 0x74: return bitBR(6, GBRegisters.Reg.H);
            case 0x75: return bitBR(6, GBRegisters.Reg.L);
            case 0x76: return bitBR(6, GBRegisters.Reg.HL);            

            case 0x7f: return bitBR(7, GBRegisters.Reg.A);
            case 0x78: return bitBR(7, GBRegisters.Reg.B);
            case 0x79: return bitBR(7, GBRegisters.Reg.C);
            case 0x7a: return bitBR(7, GBRegisters.Reg.D);
            case 0x7b: return bitBR(7, GBRegisters.Reg.E);
            case 0x7c: return bitBR(7, GBRegisters.Reg.H);
            case 0x7d: return bitBR(7, GBRegisters.Reg.L);
            case 0x7e: return bitBR(7, GBRegisters.Reg.HL);              
            
            case 0xc7: return setBR(1, 0, GBRegisters.Reg.A);
            case 0xc0: return setBR(1, 0, GBRegisters.Reg.B);
            case 0xc1: return setBR(1, 0, GBRegisters.Reg.C);
            case 0xc2: return setBR(1, 0, GBRegisters.Reg.D);
            case 0xc3: return setBR(1, 0, GBRegisters.Reg.E);
            case 0xc4: return setBR(1, 0, GBRegisters.Reg.H);
            case 0xc5: return setBR(1, 0, GBRegisters.Reg.L);
            case 0xc6: return setBR(1, 0, GBRegisters.Reg.HL);

            case 0xcf: return setBR(1, 1, GBRegisters.Reg.A);
            case 0xc8: return setBR(1, 1, GBRegisters.Reg.B);
            case 0xc9: return setBR(1, 1, GBRegisters.Reg.C);
            case 0xca: return setBR(1, 1, GBRegisters.Reg.D);
            case 0xcb: return setBR(1, 1, GBRegisters.Reg.E);
            case 0xcc: return setBR(1, 1, GBRegisters.Reg.H);
            case 0xcd: return setBR(1, 1, GBRegisters.Reg.L);
            case 0xce: return setBR(1, 1, GBRegisters.Reg.HL);
            
            case 0xd7: return setBR(1, 2, GBRegisters.Reg.A);
            case 0xd0: return setBR(1, 2, GBRegisters.Reg.B);
            case 0xd1: return setBR(1, 2, GBRegisters.Reg.C);
            case 0xd2: return setBR(1, 2, GBRegisters.Reg.D);
            case 0xd3: return setBR(1, 2, GBRegisters.Reg.E);
            case 0xd4: return setBR(1, 2, GBRegisters.Reg.H);
            case 0xd5: return setBR(1, 2, GBRegisters.Reg.L);
            case 0xd6: return setBR(1, 2, GBRegisters.Reg.HL);

            case 0xdf: return setBR(1, 3, GBRegisters.Reg.A);
            case 0xd8: return setBR(1, 3, GBRegisters.Reg.B);
            case 0xd9: return setBR(1, 3, GBRegisters.Reg.C);
            case 0xda: return setBR(1, 3, GBRegisters.Reg.D);
            case 0xdb: return setBR(1, 3, GBRegisters.Reg.E);
            case 0xdc: return setBR(1, 3, GBRegisters.Reg.H);
            case 0xdd: return setBR(1, 3, GBRegisters.Reg.L);
            case 0xde: return setBR(1, 3, GBRegisters.Reg.HL);
            
            case 0xe7: return setBR(1, 4, GBRegisters.Reg.A);
            case 0xe0: return setBR(1, 4, GBRegisters.Reg.B);
            case 0xe1: return setBR(1, 4, GBRegisters.Reg.C);
            case 0xe2: return setBR(1, 4, GBRegisters.Reg.D);
            case 0xe3: return setBR(1, 4, GBRegisters.Reg.E);
            case 0xe4: return setBR(1, 4, GBRegisters.Reg.H);
            case 0xe5: return setBR(1, 4, GBRegisters.Reg.L);
            case 0xe6: return setBR(1, 4, GBRegisters.Reg.HL);

            case 0xef: return setBR(1, 5, GBRegisters.Reg.A);
            case 0xe8: return setBR(1, 5, GBRegisters.Reg.B);
            case 0xe9: return setBR(1, 5, GBRegisters.Reg.C);
            case 0xea: return setBR(1, 5, GBRegisters.Reg.D);
            case 0xeb: return setBR(1, 5, GBRegisters.Reg.E);
            case 0xec: return setBR(1, 5, GBRegisters.Reg.H);
            case 0xed: return setBR(1, 5, GBRegisters.Reg.L);
            case 0xee: return setBR(1, 5, GBRegisters.Reg.HL);

            case 0xf7: return setBR(1, 6, GBRegisters.Reg.A);
            case 0xf0: return setBR(1, 6, GBRegisters.Reg.B);
            case 0xf1: return setBR(1, 6, GBRegisters.Reg.C);
            case 0xf2: return setBR(1, 6, GBRegisters.Reg.D);
            case 0xf3: return setBR(1, 6, GBRegisters.Reg.E);
            case 0xf4: return setBR(1, 6, GBRegisters.Reg.H);
            case 0xf5: return setBR(1, 6, GBRegisters.Reg.L);
            case 0xf6: return setBR(1, 6, GBRegisters.Reg.HL);

            case 0xff: return setBR(1, 7, GBRegisters.Reg.A);
            case 0xf8: return setBR(1, 7, GBRegisters.Reg.B);
            case 0xf9: return setBR(1, 7, GBRegisters.Reg.C);
            case 0xfa: return setBR(1, 7, GBRegisters.Reg.D);
            case 0xfb: return setBR(1, 7, GBRegisters.Reg.E);
            case 0xfc: return setBR(1, 7, GBRegisters.Reg.H);
            case 0xfd: return setBR(1, 7, GBRegisters.Reg.L);
            case 0xfe: return setBR(1, 7, GBRegisters.Reg.HL);
            
            case 0x87: return setBR(0, 0, GBRegisters.Reg.A);
            case 0x80: return setBR(0, 0, GBRegisters.Reg.B);
            case 0x81: return setBR(0, 0, GBRegisters.Reg.C);            
            case 0x82: return setBR(0, 0, GBRegisters.Reg.D);
            case 0x83: return setBR(0, 0, GBRegisters.Reg.E);            
            case 0x84: return setBR(0, 0, GBRegisters.Reg.H);
            case 0x85: return setBR(0, 0, GBRegisters.Reg.L);
            case 0x86: return setBR(0, 0, GBRegisters.Reg.HL);            
            
            case 0x8f: return setBR(0, 1, GBRegisters.Reg.A);
            case 0x88: return setBR(0, 1, GBRegisters.Reg.B);
            case 0x89: return setBR(0, 1, GBRegisters.Reg.C);            
            case 0x8a: return setBR(0, 1, GBRegisters.Reg.D);
            case 0x8b: return setBR(0, 1, GBRegisters.Reg.E);            
            case 0x8c: return setBR(0, 1, GBRegisters.Reg.H);
            case 0x8d: return setBR(0, 1, GBRegisters.Reg.L);
            case 0x8e: return setBR(0, 1, GBRegisters.Reg.HL);            

            case 0x97: return setBR(0, 2, GBRegisters.Reg.A);
            case 0x90: return setBR(0, 2, GBRegisters.Reg.B);
            case 0x91: return setBR(0, 2, GBRegisters.Reg.C);            
            case 0x92: return setBR(0, 2, GBRegisters.Reg.D);
            case 0x93: return setBR(0, 2, GBRegisters.Reg.E);            
            case 0x94: return setBR(0, 2, GBRegisters.Reg.H);
            case 0x95: return setBR(0, 2, GBRegisters.Reg.L);
            case 0x96: return setBR(0, 2, GBRegisters.Reg.HL);            
            
            case 0x9f: return setBR(0, 3, GBRegisters.Reg.A);
            case 0x98: return setBR(0, 3, GBRegisters.Reg.B);
            case 0x99: return setBR(0, 3, GBRegisters.Reg.C);            
            case 0x9a: return setBR(0, 3, GBRegisters.Reg.D);
            case 0x9b: return setBR(0, 3, GBRegisters.Reg.E);            
            case 0x9c: return setBR(0, 3, GBRegisters.Reg.H);
            case 0x9d: return setBR(0, 3, GBRegisters.Reg.L);
            case 0x9e: return setBR(0, 3, GBRegisters.Reg.HL);            

            case 0xa7: return setBR(0, 4, GBRegisters.Reg.A);
            case 0xa0: return setBR(0, 4, GBRegisters.Reg.B);
            case 0xa1: return setBR(0, 4, GBRegisters.Reg.C);            
            case 0xa2: return setBR(0, 4, GBRegisters.Reg.D);
            case 0xa3: return setBR(0, 4, GBRegisters.Reg.E);            
            case 0xa4: return setBR(0, 4, GBRegisters.Reg.H);
            case 0xa5: return setBR(0, 4, GBRegisters.Reg.L);
            case 0xa6: return setBR(0, 4, GBRegisters.Reg.HL);            
            
            case 0xaf: return setBR(0, 5, GBRegisters.Reg.A);
            case 0xa8: return setBR(0, 5, GBRegisters.Reg.B);
            case 0xa9: return setBR(0, 5, GBRegisters.Reg.C);            
            case 0xaa: return setBR(0, 5, GBRegisters.Reg.D);
            case 0xab: return setBR(0, 5, GBRegisters.Reg.E);            
            case 0xac: return setBR(0, 5, GBRegisters.Reg.H);
            case 0xad: return setBR(0, 5, GBRegisters.Reg.L);
            case 0xae: return setBR(0, 5, GBRegisters.Reg.HL);            

            case 0xb7: return setBR(0, 6, GBRegisters.Reg.A);
            case 0xb0: return setBR(0, 6, GBRegisters.Reg.B);
            case 0xb1: return setBR(0, 6, GBRegisters.Reg.C);            
            case 0xb2: return setBR(0, 6, GBRegisters.Reg.D);
            case 0xb3: return setBR(0, 6, GBRegisters.Reg.E);            
            case 0xb4: return setBR(0, 6, GBRegisters.Reg.H);
            case 0xb5: return setBR(0, 6, GBRegisters.Reg.L);
            case 0xb6: return setBR(0, 6, GBRegisters.Reg.HL);            
            
            case 0xbf: return setBR(0, 7, GBRegisters.Reg.A);
            case 0xb8: return setBR(0, 7, GBRegisters.Reg.B);
            case 0xb9: return setBR(0, 7, GBRegisters.Reg.C);            
            case 0xba: return setBR(0, 7, GBRegisters.Reg.D);
            case 0xbb: return setBR(0, 7, GBRegisters.Reg.E);            
            case 0xbc: return setBR(0, 7, GBRegisters.Reg.H);
            case 0xbd: return setBR(0, 7, GBRegisters.Reg.L);
            case 0xbe: return setBR(0, 7, GBRegisters.Reg.HL);            
            
            default:
                System.err.println("Unimplemented opcode: 0xcb" + 
                        Integer.toHexString(opcode));
                dumpRegisters();
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
     * Put value r1 into r2.
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
     * LD n,a
     * put value A into n
     * (nn) is two byte immediate value pointing to address
     * to write data
     * 
     * LSB is first
     * Special function for opcode 0xea
     */ 
    private int eightBitLoadToMem() {
        int address = memory.readByte(pc);
        pc++;
        address = address | (memory.readByte(pc) << 8);
        pc++;
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
                     break;
            case DE: registers.setReg(GBRegisters.Reg.DE, data);
                     break;
            case HL: registers.setReg(GBRegisters.Reg.HL, data);
                     break;
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
     *     (immediate value) if true, src ignored
     */ 
    private int addAN(GBRegisters.Reg src, boolean addCarry, boolean readMem) {
        int cycles;
        int regA = registers.getReg(A);
        int toAdd;
        
        if (readMem) {
            toAdd = memory.readByte(pc);
            pc++;
            cycles = 8;
        } else if (src == HL) {
            toAdd = memory.readByte(registers.getReg(src));
            cycles = 8;
        } else {
            toAdd = registers.getReg(src);
            cycles = 4;
        }
        //if adding carry and carry is set
        if (addCarry && isSet(registers.getReg(F), CARRY_F)) {
            toAdd += 1;
        }
        
        //add
        registers.setReg(A, toAdd + regA);
        
        //flags
        registers.resetAll();
        if (registers.getReg(A) == 0) {
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
        int regA = registers.getReg(A);
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
        //if adding carry and carry is set
        if (addCarry && isSet(registers.getReg(F), CARRY_F)) {
            toSub += 1;
        }

        //sub
        registers.setReg(A, regA - toSub);

        //flags
        registers.resetAll();
        if (registers.getReg(A) == 0) {
            registers.setZ();
        }
        registers.setN();
        if ((regA & 0xf) - (toSub & 0xf) >= 0) {
            registers.setH(); //no borrow
        }
        if (toSub < regA) {
            registers.setC(); //no borrow
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
        } else if (src == HL) {
            data = memory.readByte(registers.getReg(src));
            cycles = 8;
        } else {
            data = registers.getReg(src);
            cycles = 4;
        }
        
        int regA = registers.getReg(A);
        registers.setReg(A, data & regA);
        
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
        } else if (src == HL) {
            data = memory.readByte(registers.getReg(src));
            cycles = 8;
        } else {
            data = registers.getReg(src);
            cycles = 4;
        }
        
        int regA = registers.getReg(A);
        registers.setReg(A, data | regA);

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
        } else if (src == HL) {
            data = memory.readByte(registers.getReg(src));
            cycles = 8;
        } else {
            data = registers.getReg(src);
            cycles = 4;
        }
        
        int regA = registers.getReg(A);
        registers.setReg(A, data ^ regA);
        
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
        } else if (src == HL) {
            data = memory.readByte(registers.getReg(src));
            cycles = 8;
        } else {
            data = registers.getReg(src);
            cycles = 4;
        }

        int regA = registers.getReg(A);

        registers.resetAll();
        if (regA == data) {
            registers.setZ();
        }
        registers.setN();
        if ((regA & 0xf) < (data & 0xf)) {
            registers.setH();
        }
        if (regA > data) { //TODO!!! NOTE NOT REALLY SURE??????
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
        int reg;
        if (src == HL) {
            reg = memory.readByte(registers.getReg(src));
            memory.writeByte(registers.getReg(src), reg + 1);
        } else {
            reg = registers.getReg(src);
            registers.setReg(src, reg + 1);
        }
 
        registers.resetZ();
        if (((reg + 1) & 0xff) == 0) {
            registers.setZ();
        }
        registers.resetN();
        registers.resetH();
        if (((reg & 0xf) + 1) > 0xf) {
            registers.setH();
        }
        
        return (src == HL) ? 12 : 4;
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
        int reg;
        if (src == HL) {
            reg = memory.readByte(registers.getReg(src));
            reg = (reg != 0) ? reg - 1 : 0;
            memory.writeByte(registers.getReg(src), reg);
        } else {
            reg = registers.getReg(src);
            reg = (reg != 0) ? reg - 1 : 0;
            registers.setReg(src, reg);
        }
        
        registers.resetZ();
        if (reg == 0) {
            registers.setZ();
        }
        registers.setN();
        registers.resetH();
        if (((reg & 0xf) - 1) >= 0) { //NOTE UNSURE...
            registers.setH();
        }
        
        return (src == HL) ? 12 : 4;
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
        int data;

        if (reg == HL) {
            data = memory.readByte(registers.getReg(reg));
        } else {
            data = registers.getReg(reg);
        }

        int lowNib = data & 0xf;
        int highNib = (data & 0xf0) >> 4;
        data = highNib | (lowNib << 4);
        if (reg == HL) {
            memory.writeByte(registers.getReg(reg), data);
        } else {
            registers.setReg(reg, data);
        }
        registers.resetAll();
        if (data == 0) {
            registers.setZ();
        }
        return (reg == HL) ? 16 : 8;
   }
    
    
    /**
     * Decimal adjust register A
     * 
     * MIGHT NOT BE CORRECT...instructions vague 
     * TODO!!!!
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
        int reg = registers.getReg(A);
        reg = ~reg;
        
        registers.setReg(A, reg & 0xffff);
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
        
        switch (opcode) {
            case 0xca:
                if ((flags & 0x80) == 0x80) {
                    pc = address;
                }  
                break;
            case 0xc2:
                if ((flags & 0x80) == 0x0) {
                    pc = address;
                }  
                break;
            case 0xda:
                if ((flags & 0x10) == 0x10) {
                    pc = address;
                }  
                break;
            case 0xd2:
                if ((flags & 0x10) == 0x0) {
                    pc = address;
                }  
                break;
            default:
                break;
        }
        return 12;
    }
    
    /**
     * JP (HL)
     * 
     * Jump to address contained in HL
     */ 
    private int jumpHL() {
        pc = registers.getReg(GBRegisters.Reg.HL);
        return 4;
    }
    
    /**
     * JR n
     * 
     * add n to current address and jump to it
     */ 
    private int jumpN() {
        pc += (1 +  (byte)memory.readByte(pc));
        return 8;
    }

    
    
    /**
     * JR cc,n
     * 
     * Conditional Jump with immediate offset
     * 
     * @param opcode (required) opcode for jump condition
     */ 
    private int jumpCN(int opcode) {
        int flags = registers.getReg(GBRegisters.Reg.F);
        byte offset = (byte)memory.readByte(pc);
        pc++;
        
        switch (opcode) {
            case 0x28:
                if ((flags & 0x80) == 0x80) {
                    pc += offset;
                }   
                break;
            case 0x20:
                if ((flags & 0x80) == 0x0) {
                    pc += offset;
                }  
                break;
            case 0x38:
                if ((flags & 0x10) == 0x10) {
                    pc += offset;
                }  
                break;
            case 0x30:
                if ((flags & 0x10) == 0x0) {
                    pc += offset;
                }  
                break;
            default:
                break;
        }
        return 8;
    }
    
    
    /**
     * Call nn
     * 
     * push address of next instruction onto stack and jump to address 
     * nn
     * 
     */ 
    private int call() {
        int address = memory.readByte(pc);
        pc++;
        address = address | (memory.readByte(pc) << 8);
        pc++;
        pushWordToStack(pc);
        pc = address;
        return 12;   
    }
    
    /**
     * CALL cc,nn
     * 
     * Call address n if following condition is true
     * Z flag set /reset
     * C flag set/reset
     * @param opcode opcode to check for condition
     */ 
    private int callC(int opcode) {
        int flags = registers.getReg(GBRegisters.Reg.F);
        
        switch(opcode) {
            case 0xcc:
                if ((flags & 0x80) == 0x80) {
                    return call();
                }   
                break;
            case 0xc4:
                if ((flags & 0x80) == 0x0) {
                    return call();
                }   
                break;
            case 0xdc:
                if ((flags & 0x10) == 0x10) {
                    return call();
                }   
                break;
            case 0xd4:
                if ((flags & 0x10) == 0x0) {
                    return call();
                }   
                break;
            default:
                break;
        }
        return 12;
    }
    
    
    /**
     * RET
     * 
     * pop two bytes from stack and jump to that address
     */ 
    private int ret() {
        pc = popWordFromStack();
        return 8;
    }
    
    
    /**
     * RETI
     * 
     * pop two bytes from stack and jump to that address
     * enable interrupts
     */ 
    private int retI() {
        interruptsEnabled = true;
        pc = popWordFromStack();
        return 8;
    }
    
    
    /**
     * Pushes a 16 bit word to the stack
     * MSB pushed first
     */ 
    private void pushWordToStack(int word) {
        sp--;
        memory.writeByte(sp, (word & 0xff00) >> 8);
        sp--;
        memory.writeByte(sp, word & 0xff);
    }


    /**
     * Pop a 16bit word off the stack
     * 
     * LSB is first
     */ 
    private int popWordFromStack() {
        int word = memory.readByte(sp);
        sp++;
        word = word | (memory.readByte(sp) << 8);
        sp++;
        
        return word;
    }
    
    /**
     * RST n
     * 
     * Push present address onto stack, jump to address 0x0000 +n
     * 
     */ 
    private int restart(int offset) {
        pushWordToStack(pc);
        pc = offset;
        return 32;
    }
    

    /**
     * RET C
     * 
     * Return if following condition is true
     * 
     */ 
    private int retC(int opcode) {
        int flags = registers.getReg(GBRegisters.Reg.F);
        
        switch(opcode) {
            case 0xc8:
                if ((flags & 0x80) == 0x80) {
                    return ret();
                } 
                break;
            case 0xc0:
                if ((flags & 0x80) == 0x0) {
                    return ret();
                } 
                break;
            case 0xd8:
                if ((flags & 0x10) == 0x10) {
                    return ret();
                }   
                break;
            case 0xd0:
                if ((flags & 0x10) == 0x0) {
                    return ret();
                }   
                break;
            default:
                break;
        }
        return 8;
    }
    
    
    /**
     * RCLA
     * 
     * Rotate A left, Old bit 7 to Carry flag
     * 
     * Flags
     * Z - set if result is 0
     * H,N - Reset
     * C - Contains old bit 7 data
     * 
     */ 
    private int rlcA() {
        int reg = registers.getReg(GBRegisters.Reg.A);
        int msb = (reg & 0x80) >> 7;
        
        // rotate left
        reg = reg << 1;
        // set lsb to previous msb
        reg |= msb;
        
        registers.resetAll();
        if (msb == 0x1) {
            registers.setC();
        } 
        if ((reg & 0xff) == 0) {
//            registers.setZ();
        }
        
        registers.setReg(GBRegisters.Reg.A, reg);
        return 4;   
    }
    
    /**
     * RLA
     * Rotate A left through Carry Flag
     * 
     * Flags Affected:
     * Z - Set if result is 0 (reset?)
     * N,H - Reset
     * C - contains old bit 7 data
     * 
     */ 
    private int rlA() {
        int reg = registers.getReg(GBRegisters.Reg.A);
        int flags = registers.getReg(GBRegisters.Reg.F);
        
        // rotate left
        reg = reg << 1;
        // set lsb to FLAG C
        reg |= (flags & 0x10) >> 4;
        
        registers.resetAll();
        if ((reg & 0x100) == 0x100) {
            registers.setC();
        } 
        if ((reg & 0xff) == 0) {
//            registers.setZ();
        }
        
        registers.setReg(GBRegisters.Reg.A, reg);
        return 4;   
    }

    /**
     * RRCA
     * 
     * Rotate A right, Old bit 0 to Carry flag
     * 
     * Flags
     * Z - set if result is 0
     * H,N - Reset
     * C - Contains old bit 0 data
     * 
     */ 
    private int rrcA() {
        int reg = registers.getReg(GBRegisters.Reg.A);
        int lsb = reg & 0x1;
        
        // rotate right
        reg = reg >> 1;
        // set msb to previous lsb
        reg |= lsb << 7;
        
        registers.resetAll();
        if (lsb == 1) {
            registers.setC();
        } 
        if ((reg & 0xff) == 0) {
//            registers.setZ();
        }
        
        registers.setReg(GBRegisters.Reg.A, reg);
        return 4;   
    }
    
    
    /**
     * RRA
     * Rotate A right through Carry Flag
     * 
     * Flags Affected:
     * Z - Set if result is 0
     * N,H - Reset
     * C - contains old bit 0 data
     * 
     */ 
    private int rrA() {
        int reg = registers.getReg(GBRegisters.Reg.A);
        int flags = registers.getReg(GBRegisters.Reg.F);
        int lsb = reg & 0x1;
        
        // rotate right
        reg = reg >> 1;
        // set msb to FLAG C
        reg |= (flags & 0x10) << 3;
        
        registers.resetAll();
        if (lsb == 0x1) {
            registers.setC();
        } 
        if ((reg & 0xff) == 0) {
//            registers.setZ();
        }
        
        registers.setReg(GBRegisters.Reg.A, reg);
        return 4;   
    }
    
    /**
     * RLC n
     * 
     * Rotate n left. Old bit 7 to carry flag
     * 
     * Flags:
     * Z - set if result is zero
     * N - Reset
     * H - Reset
     * C - Contains old bit 7 data
     * 
     */ 
    private int rlcN(GBRegisters.Reg src) {
        int data;
        int cycles;
        
        if (src == GBRegisters.Reg.HL) {
            data = memory.readByte(registers.getReg(src));
            cycles = 16;
        } else {
            data = registers.getReg(src);
            cycles = 8;   
        }
        
        int msb = (data & 0x80) >> 7;
        
        data = data << 1;
        data |= msb;
        
        registers.resetAll();
        if (data == 0) {
            registers.setZ();
        }
        if (msb == 1) {
            registers.setC();
        }
        
        if (src == GBRegisters.Reg.HL) {
            memory.writeByte(registers.getReg(src), data);
        } else {
            registers.setReg(src, data);
        }
        
        return cycles;
    }
    
    /**
     * RL n
     * 
     * Rotate n left through carry flag
     * 
     * Flags:
     * Z - set if result is zero
     * N - Reset
     * H - Reset
     * C - Contains old bit 7 data
     */ 
    private int rlN(GBRegisters.Reg src) {
        int data;
        int cycles;
        int flags = registers.getReg(GBRegisters.Reg.F);
        
        if (src == GBRegisters.Reg.HL) {
            data = memory.readByte(registers.getReg(src));
            cycles = 16;
        } else {
            data = registers.getReg(src);
            cycles = 8;   
        }
        
        int msb = (data & 0x80) >> 7;
        
        data = data << 1;
        data |= (flags & 0x10) >> 4;
        
        registers.resetAll();
        if (data == 0) {
            registers.setZ();
        }
        if (msb == 1) {
            registers.setC();
        }
        
        if (src == GBRegisters.Reg.HL) {
            memory.writeByte(registers.getReg(src), data);
        } else {
            registers.setReg(src, data);
        }
        return cycles;
    }

    /**
     * RrCn n
     * 
     * Rotate n Right. Old bit 0 to carry flag
     * 
     * Flags:
     * Z - set if result is zero
     * N - Reset
     * H - Reset
     * C - Contains old bit 0 data
     * 
     */ 
    private int rrcN(GBRegisters.Reg src) {
        int data;
        int cycles;
        
        if (src == GBRegisters.Reg.HL) {
            data = memory.readByte(registers.getReg(src));
            cycles = 16;
        } else {
            data = registers.getReg(src);
            cycles = 8;   
        }
        
        int lsb = data & 0x1;
        
        data = data >> 1;
        data |= lsb << 7;
        
        registers.resetAll();
        if (data == 0) {
            registers.setZ();
        }
        if (lsb == 1) {
            registers.setC();
        }
        
        if (src == GBRegisters.Reg.HL) {
            memory.writeByte(registers.getReg(src), data);
        } else {
            registers.setReg(src, data);
        }
        return cycles;
    }

    /**
     * RR n
     * 
     * Rotate n right through carry flag
     * 
     * Flags:
     * Z - set if result is zero
     * N - Reset
     * H - Reset
     * C - Contains old bit 7 data
     */ 
    private int rrN(GBRegisters.Reg src) {
        int data;
        int cycles;
        int flags = registers.getReg(GBRegisters.Reg.F);
        
        if (src == GBRegisters.Reg.HL) {
            data = memory.readByte(registers.getReg(src));
            cycles = 16;
        } else {
            data = registers.getReg(src);
            cycles = 8;   
        }
        
        int lsb = data & 0x1;
        
        data = data >> 1;
        data |= (flags & 0x10) << 3;
        
        registers.resetAll();
        if (data == 0) {
            registers.setZ();
        }
        if (lsb == 1) {
            registers.setC();
        }
        
        if (src == GBRegisters.Reg.HL) {
            memory.writeByte(registers.getReg(src), data);
        } else {
            registers.setReg(src, data);
        }
        return cycles;
    }
    
    /**
     * Shift n left into Carry, lsb of n set to 0
     * 
     * Flags
     * Z - set if 0
     * HN - reset
     * C - contains old bit 7 data
     */ 
    private int slAN(GBRegisters.Reg reg) {
        int cycles;
        int data;
        
        if (reg == GBRegisters.Reg.HL) {
            data = memory.readByte(registers.getReg(reg));
            cycles = 16;
        } else {
            data = registers.getReg(reg);
            cycles = 8;
        }
        
        data = data << 1;
        
        registers.resetAll();
        if (((data >> 8) & 0x1) == 0) {
            registers.setC();
        }
        if (data == 0) {
            registers.setZ();
        }
        
        if (reg == GBRegisters.Reg.HL) {
            memory.writeByte(registers.getReg(reg), data);
        } else {
            registers.setReg(reg, data);
        }        
        
        return cycles;
    }
        
    /**
     * SRA n/SRL n
     * 
     * Shift n right into carry, sign extended if SRA, unsigned if SRL
     * 
     * Z - set if result is zero
     * N,H - reset
     * C - contains old bit 0 data
     * @param unsignedShift if true SRL, if false SRA
     */ 
    private int srAL(GBRegisters.Reg reg, boolean unsignedShift) {
        int cycles;
        int data;
        
        if (reg == GBRegisters.Reg.HL) {
            data = memory.readByte(registers.getReg(reg));
            cycles = 16;
        } else {
            data = registers.getReg(reg);
            cycles = 8;
        }
        int lsb = data & 0x1;
        
        if (unsignedShift) {
            data = data >>> 1;
        } else {
            data = data >> 1;
        }
        
        registers.resetAll();
        if (data == 0) {
            registers.setZ();
        }
        if (lsb == 1) {
            registers.setC();
        }

        if (reg == GBRegisters.Reg.HL) {
            memory.writeByte(registers.getReg(reg), data);
        } else {
            registers.setReg(reg, data);
        }
        
        return cycles;
    }
    
    /**
     * Prints the contents of the registers to STDOUT
     * 
     * 
     */ 
    public void dumpRegisters() {
        System.out.println("AF: 0x" + Integer.toHexString(registers.getReg(GBRegisters.Reg.AF)));
        System.out.println("BC: 0x" + Integer.toHexString(registers.getReg(GBRegisters.Reg.BC)));
        System.out.println("DE: 0x" + Integer.toHexString(registers.getReg(GBRegisters.Reg.DE)));
        System.out.println("HL: 0x" + Integer.toHexString(registers.getReg(GBRegisters.Reg.HL)));
        System.out.println("SP: 0x" + Integer.toHexString(sp));
        System.out.println("PC: 0x" + Integer.toHexString(pc));
        
    }
    
    
    /**
     * Test bit b in register r
     * 
     * Flags affected:
     * Z - set if bit b of register r is 0
     * N - reset
     * H - set
     * C - not affected
     * 
     * @param bit bit number to check
     * @param reg register to check
     * 
     */ 
    private int bitBR(int bit, GBRegisters.Reg reg) {
        int data;
        int cycles;
        
        if (reg == GBRegisters.Reg.HL) {
            data = memory.readByte(registers.getReg(reg));
            cycles = 16;
        } else {
            data = registers.getReg(reg);
            cycles = 8;
        }
        
        registers.resetZ();
        if (isSet(bit, data)) {
            registers.setZ();
        }
        registers.setH();
        registers.resetN();
        
        return cycles;
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
     * Set bit bitNum in reg to val
     * 
     * @param val value to set
     * @param bitNum bit to set
     * @param reg register to set
     */ 
    private int setBR(int val, int bitNum, GBRegisters.Reg reg) {
        if (reg == GBRegisters.Reg.HL) {
            int data = memory.readByte(registers.getReg(reg));
            data = setBit(val, bitNum, data);
            memory.writeByte(registers.getReg(reg), data);
            return 16;
        } else {
            int data = registers.getReg(reg);
            data = setBit(val, bitNum, data);
            registers.setReg(reg, data);
            return 8;
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
     * updateDivideRegister
     * 
     * updates the divide register 
     * ASSUMES CLOCKSPEED OF 4194304
     * 
     * @param cycles (clock cycles passed this instruction)
     */ 
    private void updateDivideRegister(int cycles) {
        divideCounter += cycles;
        
        if (divideCounter >= 0xff) {
            divideCounter = 0;
            memory.incrementDivider();
        }

    }

    /**
     * updateTimers
     * 
     * updates the CPU timers in memory
     * @param cycles number of cycles that have passed
     */ 
    private void updateTimers(int cycles) {
        //check the enable
        if (!isSet(memory.readByte(0xff07), 2)) {
            return;
        }

        timerCounter -= cycles;
        
        //update the counter in memory
        if (timerCounter <= 0) {
            timerCounter = getCountFrequency();
            
            if (memory.readByte(0xff05) == 0xff) {
                memory.writeByte(0xff05, memory.readByte(0xff06));
                requestInterrupt(0x2);
            } else {
                memory.writeByte(0xff05, memory.readByte(0xff05) + 1);
            }
        }
    }
    
    /**
     * returns the counting frequency of the timer in memory
     * 
     * checks the timer controller register 0xff07
     * and returns the appropriate clock cycle update 
     */ 
    private int getCountFrequency() {
        int freq = memory.readByte(0xff07) & 0x3;
        
        switch (freq) {
            case 0: return clockSpeed / 4096;
            case 1: return clockSpeed / 262144;
            case 2: return clockSpeed / 65526;
            case 3: return clockSpeed / 16384;
            default: return clockSpeed / 4096;
        }
    }
    
    
    /**
     * requests an interrupt to be serviced by the CPU
     * 
     * id can be:
     * 0 - V-Blank interrupt
     * 1 - LCD Timer interrupt
     * 2 - Timer interrupt 
     * 4 - Joypad interrupt
     * 
     * @param id interrupt to request
     */ 
    public void requestInterrupt(int id) {
        int flags = memory.readByte(0xff0f);
        flags = setBit(1, id, flags);
        memory.writeByte(0xff0f, flags);
        System.out.println("requestedInterrupt " + id + "wrote :" + Integer.toBinaryString(flags));
        checkInterrupts(); //TODO???
    }
    
    /**
     * Checks interrupts and services them if required
     * 
     */ 
    private void checkInterrupts() {
        if (!interruptsEnabled) {
//            System.out.println("Interrupts disabled");
            return;
        }
        System.out.println("Interrupts enabled and checking");
        int requests = memory.readByte(0xff0f);
        System.out.println("Requests: 0b" + Integer.toBinaryString(requests));
        if (requests == 0) {
            return; //no interrupts to service
        }
        //service interrupts
        int interruptEnable = memory.readByte(0xffff);
        System.out.println("Enable: 0b" + Integer.toBinaryString(interruptEnable));
        for (int i = 0; i < 5; ++i) {
            if (isSet(requests, i) && isSet(interruptEnable, i)) {
                System.out.println("Interrupting!" + i);
                handleInterrupt(i);
            }
        }
    }
    
    
    
    /**
     * Handles the interrupt
     *
     * id can be:
     * 0 - V-Blank interrupt
     * 1 - LCD Timer interrupt
     * 2 - Timer interrupt 
     * 4 - Joypad interrupt
     * 
     * @param id interrupt to handle
     */ 
    private void handleInterrupt(int id) {
        interruptsEnabled = false;

        int flags = memory.readByte(0xff0f);
        flags = setBit(0, id, flags);
        memory.writeByte(0xff0f, flags);
        pushWordToStack(pc);
        
        switch (id) {
            case 0: pc = 0x40;
                    System.out.println("Vblank interrupt");
                    break;
            case 1: pc = 0x48;
                    break;
            case 2: pc = 0x50;
                    break;
            case 4: pc = 0x60;
                    break;
            default: break;
        }
    }
    
    /**
     * DI
     * 
     * disables interrupts
     */ 
    private int disableInterrupts() {
        interruptsEnabled = false;
        return 4;
    }
    
    /**
     * EI 
     * 
     * enables interrupts
     * 
     * TODO read one more opcode
     */ 
    private int enableInterrupts() {
        System.out.println("Interrupts enabled");
        interruptsEnabled = true;
        return 4;
    }
    
    
    
    
    
}

