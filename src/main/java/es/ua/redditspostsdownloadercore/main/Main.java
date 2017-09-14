/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.redditspostsdownloadercore.main;

import es.ua.redditspostsdownloadercore.reddit.RedditURL;
import es.ua.redditspostsdownloadercore.utils.RedditToGateConverter;
import gate.util.GateException;
import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.Option;
import io.airlift.airline.ParseException;
import io.airlift.airline.ParseOptionMissingException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;

/**
 *
 * @author shuedo
 */
public class Main {
    
    /**
     *
     * @param args input arguments
     * @description Main method of the application
     * @throws IOException exception that will occur if there is a issue while
     * working with a file
     * @throws Exception exception launched by the method call()
     */
    public static void main(String[] args) throws IOException, Exception {

        CliBuilder<Callable<Void>> builder = Cli.<Callable<Void>>builder("gplsisocialnetworks")
                .withDescription("Social Networks searcher App")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, RedditSearcher.class);

        Cli<Callable<Void>> cli = builder.build();

        try {

            Callable command = cli.parse(args);
            command.call();

        } catch (ParseOptionMissingException ex) {

            System.err.println(ex.getMessage() + ". Please, execute 'help' for more information.");

        } catch (ParseException | FileNotFoundException | UnsupportedEncodingException ex) {

            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    //Command redditsearchposts that will search and print the messages of a given list of urls of posts.
    @Command(name = "redditsearchposts", description = "Search messages from specific url of a post")
    public static class RedditSearcher implements Callable<Void> {


        @Option(name = {"-d", "--directory"}, description = "Folder path to create xml files or file path to create a csv file. If this parameter is not defined the path will be the user's home")
        public String folderOut = System.getProperty("user.home"); //carpeta donde se guardan

        @Option(name = {"-e", "--encoding"}, description = "Encoding of the xml file. If this parameter is not defined the encoding will be windows-1252")
        public String encoding = "windows-1252";

        @Option(name = {"-g", "--gate"}, description = "Path of the Gate Home (required)")
        public String gatehome = null;

        @Option(name = {"-ht", "--hashtag"}, description = "Path of the Gate Home (required)")
        public String hashtag = null;

        @Override
        public Void call() throws IOException, FileNotFoundException, InterruptedException, GateException {

            //Input parameters
            if (hashtag != null) {

                RedditURL ru = new RedditURL();
                List<String> urls = ru.getUrlsByHashtag(hashtag);
                
                RedditToGateConverter converter = new RedditToGateConverter(gatehome, folderOut, encoding);
                converter.initializeGate();
                for(String url : urls)
                {
                    ru.getJSONfromURL(url);
                    if (ru.getMainpost().getId() != null)
                        converter.redditPostAndCommentToGate(ru);
                }
            }

            return null;
        }
    }
}
