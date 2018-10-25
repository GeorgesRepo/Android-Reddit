package com.parmar.amarjot.android_reddit.model.entry;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

@Root(name= "entry", strict = false)
public class Entry implements Serializable {

    @Element(name = "content")
    private String content;

    @Element(required = false, name = "aurthor")
    private String aurthor;

    @Element(name = "id")
    private String id;

    @Element(name = "title")
    private String title;

    @Element(name = "updated")
    private String updated;

    public Entry(String content, String aurthor, String id, String title, String updated) {
        this.content = content;
        this.aurthor = aurthor;
        this.id = id;
        this.title = title;
        this.updated = updated;
    }

    public Entry() {
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAurthor() {
        return aurthor;
    }

    public void setAurthor(String aurthor) {
        this.aurthor = aurthor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "\n \n Entry{" +
                "content='" + content + '\'' +
                ", aurthor='" + aurthor + '\'' +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", updated='" + updated + '\'' +
                '}' + "\n" + "----------------------------------------------------------" + "\n";
    }
}
