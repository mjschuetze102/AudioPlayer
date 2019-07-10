import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Written by Michael Schuetze on 7/6/2019.
 */
public class AudioPlayer {

    private Clip audioClip;
    private AudioInputStream audioFile;

    private boolean audioIsPlaying;
    private Thread currentAudioThread;

    public AudioPlayer(String fileName) {
        try {
            audioFile = AudioSystem.getAudioInputStream(new File(fileName).getAbsoluteFile());
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

    public void play() {
        currentAudioThread = new Thread(() -> {
            audioClip.start();

            audioIsPlaying = true;
            while (audioIsPlaying && audioClip.getMicrosecondPosition() < audioClip.getMicrosecondLength()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            }
            cleanUp();
        });
        currentAudioThread.start();
    }

    public void pause() {
        try {
            audioIsPlaying = false;
            currentAudioThread.interrupt();
            currentAudioThread.join();
        } catch (InterruptedException ignored) {}

        cleanUp();
    }

    public void jumpTo(long timestamp) {}

    /////////////////////////////////////////////////////
    //              Getters and Setters
    /////////////////////////////////////////////////////

    /////////////////////////////////////////////////////
    //               Helper Functions
    /////////////////////////////////////////////////////

    private void cleanUp() {
        audioClip.stop();
        audioClip.close();

        audioIsPlaying = false;
        currentAudioThread = null;

        try {
            audioFile.close();
        } catch (IOException ignored) {}
    }

    /////////////////////////////////////////////////////
    //               Testing Purposes
    /////////////////////////////////////////////////////

    public static void main(String[] args) {
        AudioPlayer audioPlayer = new AudioPlayer("testFile.wav");
        audioPlayer.play();
        try {
            Thread.sleep(3200);
        } catch (InterruptedException ignored) {}
        audioPlayer.pause();
    }
}
