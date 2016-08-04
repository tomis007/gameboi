package gameboi;


import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * @author tomis007
 * Simple GUI to select file
 * probably doing it wrong but it seems to
 * work
 */
public class FileSelector {
    private String workingDir;

    /**
     * Constructor
     *
     * @param workingDir to select files from
     */
    FileSelector(String workingDir) {
        this.workingDir = workingDir;
    }


    /**
     * selects a File with a simple GUI
     *
     *
     * @return File selected (if not selected somehow
     *         this is null)
     */
    public File selectFile() {
        File file;
        JFrame f = new JFrame();
        JFileChooser fc = new JFileChooser(workingDir);
        int returnVal = fc.showOpenDialog(f);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        } else {
            file = null;
        }
        f.dispose();
        return file;
    }
}

