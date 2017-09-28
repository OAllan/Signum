/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itesm.signum;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import javax.swing.ImageIcon;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
/**
 *
 * @author allanruiz
 */
public class Signum {
    public static void main(String[] args) {
        Signum.setup();
        Signum.start();
        
        
    }
    
    public static void setup(){
        File tempDir = new File("temp");
        tempDir.mkdir();
        tempDir.deleteOnExit();
    }
    
    public static void start(){
        SignumInterface.main(null);
    }
}
