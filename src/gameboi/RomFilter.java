package gameboi;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * @author tomis007
 */
public class RomFilter extends FileFilter {

    /**
     * selects .gb files
     *
     * @param f file
     * @return boolean if ends in .gb
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String fileName = f.getName();
        String extension;

        int i = fileName.lastIndexOf('.');

        extension = (i > 0) ? fileName.substring(i + 1) : "";

        return extension.equals("gb");
    }


    public String getDescription() {
        return ".gb Files";
    }

}
