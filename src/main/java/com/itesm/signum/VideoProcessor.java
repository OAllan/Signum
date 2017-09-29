/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itesm.signum;

import java.awt.Graphics;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import org.json.JSONArray;
import org.json.JSONObject;

public class VideoProcessor extends Thread{
    
    private SignumInterface interfaz;
    private FrameGrab grab;
    private final int PANEL_WIDTH = 388, PANEL_HEIGHT = 307;
    private String filePath;
    private String lastTranslated = "";
    private int fps;
    
    public VideoProcessor(SignumInterface interfaz, String filePath){
        this.interfaz = interfaz;
        this.filePath = filePath;
        fps = 30;
    }
    
    public boolean loadVideo(){
        try {
            File file = new File(filePath);
            grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));
            return true;
        } catch (IOException | JCodecException ex) {
            Logger.getLogger(VideoProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            return false;
        }
    }
    
    public void run(){
        this.loadVideo();
        this.startAnalysis();
    }
    
    public void startAnalysis(){
        try {
            Picture picture;
            
            while (null != (picture = grab.getNativeFrame())) {
                BufferedImage imagen = AWTUtil.toBufferedImage(picture);
                
                BufferedImage newImage = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT,  BufferedImage.TYPE_INT_RGB);
                Graphics g = newImage.createGraphics(); 
                g.drawImage(imagen, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, null);
                g.dispose();
                
                interfaz.showImage(newImage);
                
                if(fps == 30){
                    sendApiRequest(imagen);
                    fps = 0;
                    continue;
                }
                fps++;
                
            }
        } catch (IOException ex) {
            Logger.getLogger(VideoProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    private void sendApiRequest(BufferedImage image){
        HttpClient httpclient = new DefaultHttpClient();
        
        String uriBase = "https://southcentralus.api.cognitive.microsoft.com/customvision/v1.0/Prediction/669f559e-703f-4faf-b761-dfa2e23282cc/image?iterationId=90581bcb-6e7d-44f3-ab1d-6a5ec5b6a8b9";
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
            request.setHeader("Prediction-Key", "d3f194d3f4734d84a4978e2bbf688cd4");

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
                processJSON(json);
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
        BufferedImage bwImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bwImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        File imageFile = new File("temp/image.jpg");
        ImageIO.write(bwImage, "jpg", imageFile); 
        return imageFile;
    }
    
    
    private void processJSON(JSONObject json){
        JSONArray predictions = json.getJSONArray("Predictions");
        JSONObject bestPrediction = predictions.getJSONObject(0);
        String word = bestPrediction.getString("Tag");
        double probability = bestPrediction.getDouble("Probability")*100;
        if(probability>20 && !word.equals(lastTranslated)){
            lastTranslated = word;
            interfaz.setTranslatedText(word);
        }
        
    }
}