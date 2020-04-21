package com.chess.gui;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;

public class SoundUtils {
    private final static String defaultSoundPath="/art/sounds/";

    private SoundUtils(){
    }

    public static void playMoveSound() {
        try{
            Clip clip = AudioSystem.getClip();
            AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(SoundUtils.class.getResourceAsStream(defaultSoundPath+"move2.wav")));
            clip.open(ais);
            clip.start();
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void playCaptureSound() {
        try{
            Clip clip = AudioSystem.getClip();
            AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(SoundUtils.class.getResourceAsStream(defaultSoundPath+"Capture.wav")));
            clip.open(ais);
            clip.start();
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void playGameOverSound() {
        try{
            Clip clip = AudioSystem.getClip();
            AudioInputStream ais = AudioSystem.getAudioInputStream(
                    new BufferedInputStream(SoundUtils.class.getResourceAsStream(defaultSoundPath+"gameOver.wav")));
            clip.open(ais);
            clip.start();
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void playAnalysisDoneSound(final boolean soundMuted) {
        if(!soundMuted){
            try{
                Clip clip = AudioSystem.getClip();
                AudioInputStream ais = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(SoundUtils.class.getResourceAsStream(defaultSoundPath+"analysisDone.wav")));
                clip.open(ais);
                clip.start();
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
