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

    private final String gatehome;
    private final String encoding;
    private final String folderOut;

    public RedditToGateConverter(String gatehome, String folderOut, String encoding) {

        this.encoding = encoding;
        this.gatehome = gatehome;
        this.folderOut = folderOut;

        System.setProperty("file.encoding", encoding);
    }

    public void initializeGate() throws GateException {
        
        if (!Gate.isInitialised()) {

            File fgate = new File(gatehome);
            Gate.setGateHome(fgate);
            Gate.init();
        }
    }

    public void redditPostAndCommentToGate(RedditURL ru) {

        String path = folderOut + "/" + ru.getMainpost().getId();

        if (folderOut.charAt(folderOut.length() - 1) == ('/')) {
            path = folderOut + ru.getMainpost().getId();
        }

        createDirectory(path);
        redditPostToGate(ru, path);
        redditCommentsToGate(ru, path);
        
        ru.resetCommentcount();
    }

    private void redditPostToGate(RedditURL ru, String path) {

        try {

            Document document = Factory.newDocument(ru.getMainpost().getText());

            FeatureMap documentFeatures = document.getFeatures();
            documentFeatures.put("ID", ru.getMainpost().getId());
            documentFeatures.put("Title", ru.getMainpost().getTitle());
            documentFeatures.put("Subreddit", ru.getMainpost().getSubreddit());
            documentFeatures.put("Author", ru.getMainpost().getAuthor());
            documentFeatures.put("Numero de respuestas", ru.getMainpost().getNumRespuestas());

            addAnnotationsFeatures(document);

            String name = "Post_" + ru.getMainpost().getId() + ".xml";
            createArchive(document, path, name);
            
        } catch (ResourceInstantiationException ex) {
            Logger.getLogger(RedditToGateConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void redditCommentsToGate(RedditURL ru, String path) {

        for (RedditComment comment : ru.getMainpost().getComments()) {

            try {

                Document document = Factory.newDocument(comment.getText());

                FeatureMap documentFeatures = document.getFeatures();
                documentFeatures.put("ID", comment.getId());
                documentFeatures.put("Post ID", comment.getPostid());
                documentFeatures.put("Parent ID", comment.getParentid());
                documentFeatures.put("Author", comment.getAuthor());

                addAnnotationsFeatures(document);

                String name = "";
                if (comment.getParentid().equals(comment.getPostid()))
                    name = "Comment_" + ru.getCommentcount() + " - ID: " + comment.getId() + ".xml";
                else
                    name = "Comment_" + ru.getCommentcount() + " - ID: " + comment.getId() + " - In reply to: " + comment.getParentid() + ".xml";

                ru.addCommentcount();
                createArchive(document, path, name);
                
            } catch (ResourceInstantiationException ex) {

                Logger.getLogger(RedditToGateConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void addAnnotationsFeatures(Document document) {

        try {

            Long start_offset = gate.Utils.start(document);
            Long end_offset = gate.Utils.end(document);

            SimpleFeatureMapImpl annotationFeatures = new SimpleFeatureMapImpl();

            AnnotationSet annotationSet = document.getAnnotations("original markups");
            annotationFeatures.put("PrimaryMessageType", "");
            annotationFeatures.put("SecondaryMessageType", "");
            annotationFeatures.put("AlertLevel", "");

            annotationSet.add(start_offset, end_offset, "SuicidalClassification", annotationFeatures);

        } catch (InvalidOffsetException ex) {
            Logger.getLogger(RedditToGateConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createDirectory(String path) {

        File dir = new File(path);
        dir.mkdir();
    }
    
    private void createArchive(Document document, String path, String name) {
        
        File outputFile = new File(path, name);

        try (PrintWriter pw = new PrintWriter(outputFile, encoding)) {

            pw.println(document.toXml());

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(RedditToGateConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
