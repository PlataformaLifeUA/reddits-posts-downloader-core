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

    private int commentcount;
    private RedditPost mainpost;

    public RedditURL() {

        commentcount = 1;
        mainpost = new RedditPost();
    }

    public RedditPost getMainpost() {
        return mainpost;
    }

    public void setMainPost(JSONObject post) {

        mainpost = new RedditPost(post.getString("id"), post.getString("title"), post.getString("selftext"), post.getString("subreddit"), post.getString("author"));
    }

    public int getCommentcount() {
        return commentcount;
    }

    public void addCommentcount() {
        this.commentcount++;
    }

    public void resetCommentcount() {
        this.commentcount = 1;
    }

    /**
     * Obtain url from hashtag
     *
     * @param hashtag
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public List<String> getUrlsByHashtag(String hashtag) throws FileNotFoundException, IOException, InterruptedException {

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

                return extractURL(resultCurl);
            }

        } catch (ExecutionException ex) {
            Logger.getLogger(RedditURL.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private List<String> extractURL(String resultCurl) throws JSONException {

        JSONObject jsonObject = new JSONObject(resultCurl);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("children");

        List<String> urls = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {

            String urlPost = jsonArray.getJSONObject(i).getJSONObject("data").getString("permalink");
            if (urlPost != null && !urlPost.isEmpty() && !urlPost.contains("www.reddit.com")) {

                String urlPostJson = "http://www.reddit.com" + urlPost.substring(0, urlPost.length() - 1) + ".json";
                urls.add(urlPostJson);
            }
        }

        return urls;
    }

    public void getJSONfromURL(String url) throws IOException, InterruptedException {

        try {
            String command = "curl -X GET -L " + url;
            String resultCurl = "";

            do {
                ExternalProcess ep = new ExternalProcess(command);
                resultCurl = ep.execute(new StringBuilder());

                sleep(500);

            } while (resultCurl.isEmpty() || resultCurl.contains("\"error\": 4"));

            manageJSON(new JSONArray(resultCurl));

        } catch (ExecutionException ex) {
            Logger.getLogger(RedditURL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void manageJSON(JSONArray json) {

        if (!((JSONObject) json.getJSONObject(0).getJSONObject("data").getJSONArray("children").getJSONObject(0).get("data")).isNull("selftext_html")) {
            
            for (int i = 0; i < json.length(); i++) {

                JSONObject objectjson = json.getJSONObject(i);
                JSONObject data = objectjson.getJSONObject("data");
                JSONArray children = data.getJSONArray("children");

                this.getJSONfromChildren(children);
            }
        }
    }

    public void getJSONfromChildren(JSONArray children) {

        for (int j = 0; j < children.length(); j++) {

            JSONObject child = children.getJSONObject(j);

            switch (child.getString("kind")) {
                case "t1":
                    if (this.mainpost != null) {
                        JSONObject comment = child.getJSONObject("data");
                        mainpost.addComment(comment, this);
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
                    this.setMainPost(post);
                    break;
                default:
                    break;
            }
        }
    }
}
