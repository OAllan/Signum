/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itesm.signum;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.json.JSONObject;

public class VideoProcessor{
    
    private JLabel video;
    private JTextField texto;
    private FrameGrab grab;
    
    public VideoProcessor(JLabel video, JTextField texto){
        this.video = video;
        this.texto = texto;
    }
    
    public boolean loadVideo(String filePath){
        try {
            File file = new File("/Users/allanruiz/Documents/ITESM/4toSemestre/Redes/Ataquefuerzabrutaportelnet.mp4");
            grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
            return true;
        } catch (IOException | JCodecException ex) {
            Logger.getLogger(VideoProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            return false;
        }
    }
    
    public void startAnalysis(){
        try {
            Picture picture;
            
            while (null != (picture = grab.getNativeFrame())) {
                System.out.println(picture.getData().length);
                BufferedImage imagen = AWTUtil.toBufferedImage(picture);
                
                WritableRaster raster = imagen.getRaster();
                DataBufferByte data   = (DataBufferByte)raster.getDataBuffer();
                byte[] imageData = data.getData();
                
                sendApiRequest(imagen);
                ImageIcon icon = new ImageIcon(imagen);
                
                
                /*video.removeAll();
                video.add(new JLabel(icon));
                video.updateUI();
                */
            }
        } catch (IOException ex) {
            Logger.getLogger(VideoProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    private void sendApiRequest(BufferedImage image){
        HttpClient httpclient = new DefaultHttpClient();
        
        String uriBase = "https://southcentralus.api.cognitive.microsoft.com/customvision/v1.0/Prediction/a775627c-fafd-444e-9e97-9f32eb8d248c/image?iterationId=ef7e50ba-46d6-494e-b948-3d6fccaabad4";
        File imageFile = new File("image.jpg");
        try
        {
            URIBuilder builder = new URIBuilder(uriBase);
            
            imageFile = generateBWImage(image);
            
            // Prepare the URI for the REST API call.
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            
            // Request headers.
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Prediction-Key", "e81add4e982b4523a58130308825fc40");

            // Request body.
            FileEntity reqEntity = new FileEntity(imageFile);
            request.setEntity(reqEntity);

            // Execute the REST API call and get the response entity.
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                // Format and display the JSON response.
                String jsonString = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(jsonString);
                System.out.println("REST Response:\n");
                System.out.println(json.toString(2));
            }
        }
        catch (Exception e)
        {
            // Display error message.
            System.out.println(e.getMessage());
        }
        finally{
            imageFile.deleteOnExit();
        }
    }
    
    
    private File generateBWImage(BufferedImage image) throws IOException{
        BufferedImage bwImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = bwImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        File imageFile = new File("temp/image.jpg");
        ImageIO.write(bwImage, "jpg", imageFile); 
        return imageFile;
    }
}