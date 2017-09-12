/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.redditspostsdownloadercore.utils;

import es.ua.redditspostsdownloadercore.reddit.RedditComment;
import es.ua.redditspostsdownloadercore.reddit.RedditURL;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import gate.util.SimpleFeatureMapImpl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author balmarcha
 */
public class RedditToGateConverter {
    
    public void redditPostAndCommentToGate(RedditURL ru, String folderOut, String encoding, String gatehome) {

        if (!Gate.isInitialised()) {

            try {
                
                File fgate = new File(gatehome);
                Gate.setGateHome(fgate);
                Gate.init();
            }
            catch (GateException ex) {
                
                Logger.getLogger(RedditToGateConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String originalencoding = System.getProperty("file.encoding");
        System.setProperty("file.encoding", encoding);

        String path = folderOut + "/" + ru.getMainpost().getId();

        if (folderOut.charAt(folderOut.length() - 1) == ('/')) {
            path = folderOut + ru.getMainpost().getId();
        }
        
        createDirectory(path);
        printPostXML(ru, path, encoding, gatehome);
        printCommentsXML(ru, path, encoding, gatehome);

        System.setProperty("file.encoding", originalencoding);
    }

    public void printPostXML(RedditURL ru, String folderOut, String encoding, String gatehome) {

        try {
            
            Document newDocument = Factory.newDocument(ru.getMainpost().getText());
            
            AnnotationSet annotationSet = newDocument.getAnnotations("original markups");
            
            Long start_offset = gate.Utils.start(newDocument);
            Long end_offset = gate.Utils.end(newDocument);
            
            FeatureMap documentFeatures = newDocument.getFeatures();
            documentFeatures.put("ID", ru.getMainpost().getId());
            documentFeatures.put("Title", ru.getMainpost().getTitle());
            documentFeatures.put("Subreddit", ru.getMainpost().getSubreddit());
            documentFeatures.put("Author", ru.getMainpost().getAuthor());
            documentFeatures.put("Numero de respuestas", ru.getMainpost().getNumRespuestas());
            
            SimpleFeatureMapImpl annotationFeatures = new SimpleFeatureMapImpl();
            
            annotationFeatures.put("PrimaryMessageType", "");
            annotationFeatures.put("SecondaryMessageType", "");
            annotationFeatures.put("AlertLevel", "");
            
            annotationSet.add(start_offset, end_offset, "SuicidalClassification", annotationFeatures);
            
            String name = ru.getNamecount() + " - " + ru.getMainpost().getId() + " - MainPost" + ".xml";
            ru.addNamecount();
            File outputFile = new File(folderOut, name);
            
            try (PrintWriter pw = new PrintWriter(outputFile, encoding)) {
                
                pw.println(newDocument.toXml());
            }
            catch (FileNotFoundException | UnsupportedEncodingException ex) {
                
                Logger.getLogger(RedditToGateConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        catch (ResourceInstantiationException | InvalidOffsetException ex)
        {
            Logger.getLogger(RedditToGateConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void printCommentsXML(RedditURL ru, String folderOut, String encoding, String gatehome) {

        for (RedditComment comment : ru.getComments()) {

            try {
                
                Document newDocument = Factory.newDocument(comment.getText());
                
                AnnotationSet annotationSet = newDocument.getAnnotations("original markups");
                
                Long start_offset = gate.Utils.start(newDocument);
                Long end_offset = gate.Utils.end(newDocument);
                
                FeatureMap documentFeatures = newDocument.getFeatures();
                documentFeatures.put("ID", comment.getId());
                documentFeatures.put("Post ID", comment.getPostid());
                documentFeatures.put("Parent ID", comment.getParentid());
                documentFeatures.put("Author", comment.getAuthor());
                
                SimpleFeatureMapImpl annotationFeatures = new SimpleFeatureMapImpl();
                
                annotationFeatures.put("PrimaryMessageType", "");
                annotationFeatures.put("SecondaryMessageType", "");
                annotationFeatures.put("SuicidalClassificationType", "");
                
                annotationSet.add(start_offset, end_offset, "SuicidalClassification", annotationFeatures);
                
                String name = ru.getNamecount() + " - Post: " + comment.getPostid() + " - Comment with ID: " + comment.getId() + " - In reply to " + comment.getParentid() + ".xml";
                
                if (comment.getParentid().equals(comment.getPostid())) {
                    name = ru.getNamecount() + " - Post: " + comment.getPostid() + " - Comment with ID: " + comment.getId() + ".xml";
                }
                
                ru.addNamecount();
                
                File outputFile = new File(folderOut, name);
                
                try (PrintWriter pw = new PrintWriter(outputFile, encoding)) {
                    
                    pw.println(newDocument.toXml());
                }
                catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    
                    Logger.getLogger(RedditToGateConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            catch (InvalidOffsetException | ResourceInstantiationException ex) {
                
                Logger.getLogger(RedditToGateConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static void createDirectory(String path) {

        File dir = new File(path);
        dir.mkdir();
    }
}
