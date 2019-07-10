import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Written by Michael Schuetze on 7/6/2019.
 */
public class AudioPlayer {

    /* Audio file and clip which stores the audio to be played */
    private Clip audioClip;
    private String fileName;
    private AudioInputStream audioStream;

    /* Variables that keep track of the progress of the current audio being played */
    private boolean audioIsPlaying;
    private Thread currentAudioThread;
    private long currentTimestamp;

    /**
     * Prepares the audio file so it may be played later
     */
    public AudioPlayer(String fileName) {
        this.fileName = fileName;
        
        // Start the file from the beginning
        currentTimestamp = 0;
    }

    /////////////////////////////////////////////////////
    //              Class Functionality
    /////////////////////////////////////////////////////

    /**
     * Plays the selected audio file
     */
    public void play() {
        // Open the audio stream
        openStream();

        // Play the audio at the last paused position
        audioClip.setMicrosecondPosition(currentTimestamp);

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
            audioIsPlaying = false;
            closeStream();
        });
        currentAudioThread.start();
    }

    /**
     * Pauses the currently playing audio
     */
    public void pause() {
        // Only pause when audio is playing
        if (!audioIsPlaying)
            return;

        // Store the current timestamp so audio can start at same position later
        currentTimestamp = audioClip.getMicrosecondPosition();

        try {
            // Prevent the next iteration of the play loop
            audioIsPlaying = false;
            // Interrupt the thread and wait for it to finish executing
            currentAudioThread.interrupt();
            currentAudioThread.join();
        } catch (InterruptedException ignored) {}
    }

    /**
     * Jump to a specific timestamp in the file
     * @param timestamp - part of the audio stream to jump to
     */
    public void jumpTo(long timestamp) {
        currentTimestamp = timestamp;
    }

    /////////////////////////////////////////////////////
    //              Getters and Setters
    /////////////////////////////////////////////////////

    /////////////////////////////////////////////////////
    //               Helper Functions
    /////////////////////////////////////////////////////

    /**
     * Opens the audio stream so we can use the data from an audio source
     */
    private void openStream() {
        try {
            // Locate the audio file
            audioStream = AudioSystem.getAudioInputStream(new File(fileName).getAbsoluteFile());

            // Open the audio file to be played
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
        } catch (UnsupportedAudioFileException uaf) {
            System.out.println("AudioPlayer() uaf:" + uaf);
        } catch (LineUnavailableException lu) {
            System.out.println("AudioPlayer() lu:" + lu);
        } catch (IOException io) {
            System.out.println("AudioPlayer() io:" + io);
        }
    }

    /**
     * Closes the audio stream so it may be reused later
     */
    private void closeStream() {
        // Stop the audio clip
        audioClip.stop();
        audioClip.close();

        // Close the audio stream
        try {
            audioStream.close();
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
        audioPlayer.jumpTo(2000);
        audioPlayer.play();
    }
}
