import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Written by Michael Schuetze on 7/6/2019.
 */
public class AudioPlayer {

    /* Audio file and clip which stores the audio to be played */
    private Clip audioClip;
    private AudioInputStream audioFile;

    /* Variables that keep track of the progress of the current audio being played */
    private boolean audioIsPlaying;
    private Thread currentAudioThread;

    /**
     * Prepares the audio file so it may be played later
     * @param fileName - name of the file with the audio data stored
     */
    public AudioPlayer(String fileName) {
        try {
            // Locate the audio file
            audioFile = AudioSystem.getAudioInputStream(new File(fileName).getAbsoluteFile());
            
            // Open the audio file to be played
            audioClip = AudioSystem.getClip();
            audioClip.open(audioFile);
        } catch (UnsupportedAudioFileException uaf) {
            System.out.println("AudioPlayer() uaf:" + uaf);
        } catch (LineUnavailableException lu) {
            System.out.println("AudioPlayer() lu:" + lu);
        } catch (IOException io) {
            System.out.println("AudioPlayer() io:" + io);
        }
    }

    /////////////////////////////////////////////////////
    //              Class Functionality
    /////////////////////////////////////////////////////

    /**
     * Plays the selected audio file
     */
    public void play() {
        // Create a new thread that keeps track of the progress of the audio clip
        currentAudioThread = new Thread(() -> {
            audioClip.start();

            audioIsPlaying = true;
            while (audioIsPlaying && audioClip.getMicrosecondPosition() < audioClip.getMicrosecondLength()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            }
            
            // Reset variables needed to play audio
            cleanUp();
        });
        currentAudioThread.start();
    }

    /**
     * Pauses the currently playing audio
     */
    public void pause() {
        try {
            // Prevent the next iteration of the play loop
            audioIsPlaying = false;
            // Interrupt the thread and wait for it to finish executing
            currentAudioThread.interrupt();
            currentAudioThread.join();
        } catch (InterruptedException ignored) {}
    }

    public void jumpTo(long timestamp) {}

    /////////////////////////////////////////////////////
    //              Getters and Setters
    /////////////////////////////////////////////////////

    /////////////////////////////////////////////////////
    //               Helper Functions
    /////////////////////////////////////////////////////

    private void cleanUp() {
        // Close the audio stream
        audioClip.stop();
        audioClip.close();

        // Reset variables used to play audio
        audioIsPlaying = false;
        currentAudioThread = null;

        // Close the audio file
        try {
            audioFile.close();
        } catch (IOException ignored) {}
    }

    /////////////////////////////////////////////////////
    //               Testing Purposes
    /////////////////////////////////////////////////////

    public static void main(String[] args) {
        AudioPlayer audioPlayer = new AudioPlayer("testFile.wav");
        audioPlayer.pause();
        audioPlayer.play();
        try {
            Thread.sleep(3200);
        } catch (InterruptedException ignored) {}
        audioPlayer.pause();
    }
}
