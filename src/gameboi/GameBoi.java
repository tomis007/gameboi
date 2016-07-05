/*
 * Gameboi
 */
package gameboi;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author tomis007
 */
public class GameBoi {

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {

        Path rom_path = Paths.get(argv[0]);

        GBMem memory = new GBMem(rom_path);

        CPU z80 = new CPU(memory);

        GPU gpu = new GPU(memory, z80);

        z80.setGPU(gpu);
        while (true) {
            int count = 69905;
//            long startTime = System.nanoTime();
            while (count > 0) {
                int cycles;
                cycles = z80.ExecuteOpcode();
                gpu.updateGraphics(cycles);
                count -= cycles;
            }
//            long endTime = System.nanoTime();
//            if (endTime - startTime < (1000000000 / 60)) {
//                System.out.println("Should sleep: " + (endTime - startTime));
//                try {
//                    System.out.println("sleeping");
//                    Thread.sleep((endTime - startTime) / 1000000);
//                } catch (InterruptedException e) {
//                    System.out.println("ugh");
//                }
//            }


//            System.out.println(count);
        }
        //      System.out.println(count);
//        System.exit(1);

    }
}
//}
