/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itesm.signum;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

/**
 *
 * @author allanruiz
 */
public class VideoPlayer extends Thread {
    
    private SignumInterface interfaz;
    private FrameGrab grab;
    private final int PANEL_WIDTH = 388, PANEL_HEIGHT = 307;
    
    public VideoPlayer(SignumInterface interfaz, FrameGrab grab){
        this.interfaz = interfaz;
        this.grab = grab;
    }
    
    @Override
    public void run(){
        play();
    }
    
    
    private void play(){
        try {
            Picture picture;
            while (null != (picture = grab.getNativeFrame())) {
                BufferedImage imagen = AWTUtil.toBufferedImage(picture);
                BufferedImage newImage = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT,  BufferedImage.TYPE_INT_RGB);
                Graphics g = newImage.createGraphics(); 
                g.drawImage(imagen, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, null);
                g.dispose();
                
                interfaz.showImage(newImage);
            }
        } catch (IOException ex) {
            Logger.getLogger(VideoPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
