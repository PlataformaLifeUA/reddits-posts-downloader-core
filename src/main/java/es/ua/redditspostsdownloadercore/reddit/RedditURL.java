/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.redditspostsdownloadercore.reddit;

import es.ua.gplsi.util.ExternalProcess;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author balmarcha
 */
public class RedditURL {
    
    public static void holamundo() {
        
        System.out.println("hola mundo!");
    }
    
    private RedditPost mainpost;
    private ArrayList<RedditComment> comments;
    private String url;
    private int namecount;

    public RedditURL(String url) {

        comments = new ArrayList<>();
        this.url = url;
        namecount = 1;
    }

    public ArrayList<RedditComment> getComments() {
        return comments;
    }

    public RedditPost getMainpost() {
        return mainpost;
    }

    public void setMainPost(JSONObject post) {

        mainpost = new RedditPost(post.getString("id"), post.getString("title"), post.getString("selftext"), post.getString("subreddit"), post.getString("author"));
    }

    public int getNamecount() {
        return namecount;
    }

    public void addNamecount() {
        this.namecount++;
    }
    
    public void pushComment(JSONObject comment) {

        String id = comment.getString("id");
        String parentid = comment.getString("parent_id");
        String author = comment.getString("author");
        String text = comment.getString("body");
        String postid = this.mainpost.getId();

        RedditComment newcomment = new RedditComment(id, parentid.substring(parentid.indexOf("_") + 1), author, text, postid);
        comments.add(newcomment);
    }
    
    public void manageJSON(JSONArray json) {

        for (int i = 0; i < json.length(); i++) {

            JSONObject objectjson = json.getJSONObject(i);
            JSONObject data = objectjson.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");
            this.getJSONfromChildren(children);
        }
    }

    public void getJSONfromURL() throws IOException, InterruptedException {

        try {
            String command = "curl -X GET -L " + url;
            String resultCurl = "";

            do {
                ExternalProcess ep = new ExternalProcess(command);
                resultCurl = ep.execute(new StringBuilder());

                sleep(500);
                
                System.out.println("es.ua.redditdownloadercore.reddit.RedditURL.getJSONfromURL()");

            } while (resultCurl.isEmpty() || resultCurl.contains("error"));

            JSONArray json = new JSONArray(resultCurl);

            this.manageJSON(json);

        } catch (ExecutionException ex) {
            Logger.getLogger(RedditURL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getJSONfromChildren(JSONArray children) {

        boolean post_empty = false;
        for (int j = 0; j < children.length() && !post_empty; j++) {

            JSONObject child = children.getJSONObject(j);

            switch (child.getString("kind")) {
                case "t1":
                    if (this.mainpost != null) {
                        JSONObject comment = child.getJSONObject("data");
                        this.pushComment(comment);
                        if (comment.has("replies")) {
                            if (comment.get("author").equals(mainpost.getAuthor())) {
                                mainpost.setNumRespuestas();
                            }

                            String rep = comment.get("replies").toString();
                            if (!rep.equals("")) {
                                JSONObject replies = comment.getJSONObject("replies");
                                if (replies.has("data")) {
                                    JSONObject data = replies.getJSONObject("data");
                                    JSONArray babies = data.getJSONArray("children");
                                    this.getJSONfromChildren(babies);
                                }
                            }
                        }
                    }
                    break;
                case "t3":
                    JSONObject post = child.getJSONObject("data");
                    if (!post.getString("selftext").isEmpty()) {
                        this.setMainPost(post);
                    } else {
                        post_empty = true;
                    }

                    break;

                default:
                    break;
            }
        }
    }
    
    /**
     * Obtain url from hashtag
     *
     * @param hashtag
     * @param urls
     * @throws FileNotFoundException
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public static void getUrlsByHashtag(String hashtag, List<String> urls) throws FileNotFoundException, IOException, InterruptedException {

        String url = "http://www.reddit.com/r/" + hashtag + ".json";

        try {
            String command = "curl -X GET -L " + url;

            String resultCurl = null;
            StringBuilder error = new StringBuilder();

            do {
                ExternalProcess ep = new ExternalProcess(command);
                resultCurl = ep.execute(error);
                
                sleep(500);

            } while (resultCurl.isEmpty() || resultCurl.contains("\"error\": 4"));

            if (!error.toString().isEmpty()) {

                extractURL(resultCurl, urls);
            }

        } catch (ExecutionException ex) {
            Logger.getLogger(RedditURL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void extractURL(String resultCurl, List<String> urls) throws JSONException {

        JSONObject jsonObject = new JSONObject(resultCurl);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("children");

        for (int i = 0; i < jsonArray.length(); i++) {

            String urlPost = jsonArray.getJSONObject(i).getJSONObject("data").getString("permalink");
            if (urlPost != null && !urlPost.isEmpty() && !urlPost.contains("www.reddit.com")) {

                String urlPostJson = "http://www.reddit.com" + urlPost.substring(0, urlPost.length() - 1) + ".json";
                urls.add(urlPostJson);
            }
        }
    }
}
