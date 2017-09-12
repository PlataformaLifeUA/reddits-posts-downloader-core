/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.redditspostsdownloadercore.reddit;

/**
 *
 * @author balmarcha
 */
public class RedditComment {
    
    private String id;
    private String parentid;
    private String author;
    private String text;
    private String postid;

    public RedditComment(String id, String parentid, String author, String text, String postid) {
       
        this.id = id;
        this.parentid = parentid;
        this.author = author;
        this.text = text;
        this.postid = postid;
    }
    
    public String getId() {
        return id;
    }

    public String getParentid() {
        return parentid;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public String getPostid() {
        return postid;
    }
}
