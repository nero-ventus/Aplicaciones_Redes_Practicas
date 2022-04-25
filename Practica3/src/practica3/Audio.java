package practica3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Audio {
    // record duration, in milliseconds
	static final long RECORD_TIME = 5000;	// 5 seconds
        AudioFormat audioFormat;
        // path of the wav file
	//File wavFile = new File("E:/Test/RecordAudio.wav");
        File wavFile = new File("Audio.wav");

	// format of audio file
	AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

	// the line from which audio data is captured
	TargetDataLine line;
        AudioInputStream ais=null;
        
        
        private final int BUFFER_SIZE = 228000;
        private File soundFile;
        private AudioInputStream audioStream;
        //private AudioFormat audioFormat;
        private SourceDataLine sourceLine;
        

	/**
	 * Defines an audio format
	 */
	AudioFormat getAudioFormat() {
		float sampleRate = 8000.0F;//muestras x segundo (ya sea 1 o 2 canales. 8000 muestras x canal)
		int sampleSizeInBits = 16; //#bits usados para almacenar c/muestra (8 o 16 bits valores típicos)
		int channels = 2; //1=mono, 2=stereo
		boolean signed = true; //indica si los datos de la muestra van con signo/sin signo
		boolean bigEndian = false;  //indica el orden de bits(0=little-endian, 1=Big-endian)//importante x tam muestra(1 o 2 bytes)
                /*construye un formato de audio con codificación lineal PCM() con el tamaño de trama especificado al # de bits requeridos para una muestra x canal*/
		AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);//codificación PCM(modulación por pulsos codificados)
		return format;
	}

	/**
	 * Captures the sound and record into a WAV file
	 */
	void start(int m) {
		try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    /**********************************************/
                    //Get and display a list of
                    // available mixers.
                  Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
                  //Get everything set up for capture
                  AudioFormat format = getAudioFormat();

                  DataLine.Info info = new DataLine.Info(TargetDataLine.class,format);

                  //Select one of the available
                  // mixers.
                  Mixer mixer = AudioSystem.getMixer(mixerInfo[m]);//3,5
                  //TargetDataLine line;                  
                  // checks if system supports the data line
                  if (!AudioSystem.isLineSupported(info)) {
                        System.out.println("Line not supported");
                        System.exit(0);
                   }//if
                  //Get a TargetDataLine on the selected
                  // mixer.
                  line = (TargetDataLine)mixer.getLine(info);
                  //Prepare the line for use.
			line.open(format);   
			line.start();	// start capturing
//           
			System.out.println("Start capturing...");

			/*AudioInputStream*/ ais = new AudioInputStream(line);
			System.out.println("Start recording...");

			// start recording
			AudioSystem.write(ais, fileType, wavFile);
                        br.close();
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Closes the target data line to finish capturing and recording
	 */
	void finish() {
            try{
		line.stop();
		line.close();
                ais.close();
		System.out.println("Finished");
            }catch(Exception e){e.printStackTrace();}
	}

	/**
	 * Entry to run the program
	 */
	void Graba(){
		final Audio recorder = new Audio();
              try{
                  //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                                      //Get and display a list of
                    // available mixers.
                  Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
                  System.out.println("Mezcladores disponibles:");
                  for(int cnt = 0; cnt < mixerInfo.length;cnt++){
                    System.out.println("["+cnt+"]->"+mixerInfo[cnt].getName());
                  }//end for loop
                  System.out.print("\nElige el mezclador de entrada (microfono) de tu eleccion:");
                  int micro = Integer.parseInt("2");//br.readLine());
		// creates a new thread that waits for a specified
		// of time before stopping
		Thread stopper = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(RECORD_TIME);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				recorder.finish();
			}
		});

		stopper.start();

		// start recording
		recorder.start(micro);
                //br.close();
              }catch(Exception e){
                  e.printStackTrace();
              }//catch
	}
        
        
        /*void reproduce(){
            String filename="Audio.wav"; //fill in file name here

            int EXTERNAL_BUFFER_SIZE = 524288;

            File soundFile = new File(filename);
            Mixer mixer=null;

            if (!soundFile.exists())
            {
             System.err.println("Wave file not found: " + filename);
             return;
            }

             //BufferedReader br=null;
              try{
                  //br = new BufferedReader(new InputStreamReader(System.in));
                  //Get and display a list of
                  // available mixers.
                  Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
                  System.out.println("Mezcladores disponibles:");
                  for(int cnt = 0; cnt < mixerInfo.length;cnt++){
                    System.out.println("["+cnt+"]->"+mixerInfo[cnt].getName());
                  }//end for loop
                  System.out.print("\nElige el mezclador de salida (bocinas) de tu eleccion:");
                  int bocina = Integer.parseInt("0");//br.readLine());
                  mixer = AudioSystem.getMixer(mixerInfo[bocina]);//3
              }catch(Exception e){e.printStackTrace();
              }//catch

            AudioInputStream audioInputStream = null;
            try
            {
             audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            }
            catch(Exception e)
            {
             e.printStackTrace();
             return;
            }

            AudioFormat format = audioInputStream.getFormat();

            SourceDataLine auline = null;
            try
            {
            //Describe a desired line
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            auline = (SourceDataLine)mixer.getLine(info);

             //auline = (SourceDataLine) AudioSystem.getLine(info);

             //Opens the line with the specified format,
             //causing the line to acquire any required
             //system resources and become operational.
             auline.open(format);
            }
            catch(Exception e)
            {
             e.printStackTrace();
             return;
            }

             //Allows a line to engage in data I/O
            auline.start();

            int nBytesRead = 0;
            byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

            try
            {
             while (nBytesRead != -1)
             {
              nBytesRead = audioInputStream.read(abData, 0, abData.length);
              if (nBytesRead >= 0)
              {
               //Writes audio data to the mixer via this source data line
               //NOTE : A mixer is an audio device with one or more lines
               auline.write(abData, 0, nBytesRead);
              }
             }
             audioInputStream.close();
            }catch(Exception e)
            {
             e.printStackTrace();
             return;
            }
            finally
            {
             //Drains queued data from the line
             //by continuing data I/O until the
             //data line's internal buffer has been emptied
             auline.drain();

             //Closes the line, indicating that any system
             //resources in use by the line can be released
             auline.close();
            }
        }*/
        
        public void playSound(){

        String strFilename = "Audio.wav";

        try {
            soundFile = new File(strFilename);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            audioStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        audioFormat = audioStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        sourceLine.start();

        int nBytesRead = 0;
        byte[] abData = new byte[BUFFER_SIZE];
        while (nBytesRead != -1) {
            try {
                nBytesRead = audioStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
                @SuppressWarnings("unused")
                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
            }
        }

        sourceLine.drain();
        sourceLine.close();
    }
}