package extractocol.common.outputs.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class StdOutputController {
    private static ByteArrayOutputStream baos;
    private static PrintStream previous;
    private static boolean stdOut = true;

    public static void stop() {
        if (!stdOut) return;

        stdOut = false;
        previous = System.out;      
        baos = new ByteArrayOutputStream();

        OutputStream outputStreamCombiner =  new OutputStreamCombiner(baos);
        PrintStream custom = new PrintStream(outputStreamCombiner);

        System.setOut(custom);
        System.setErr(custom);
    }

    public static void start() {
        if (stdOut) return;

        stdOut = true;
        System.setOut(previous);
        System.setErr(previous);

    }
    
    public static boolean getCurrentStatus() { return stdOut;}
    public static void stopOrStart(boolean start) {
    	if(start) start();
    	else stop();
    }
}

class OutputStreamCombiner extends OutputStream {
    private OutputStream outputStream;

    public OutputStreamCombiner(OutputStream outputStreams) {
        this.outputStream = outputStreams;
    }

	public void write(int b) {
		try {
			outputStream.write(b);
		} catch (IOException e) {
			return;
		}
	}

	public void flush() {
		try {
			outputStream.flush();
		} catch (IOException e) {
			return;
		}
	}

	public void close() {
		try {
			outputStream.close();
		} catch (IOException e) {
			return;
		}
	}
}