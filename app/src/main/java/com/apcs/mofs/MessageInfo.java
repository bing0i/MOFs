package com.apcs.mofs;

public class MessageInfo {
    private String name = "";
    private String message = "";
    private int imagePath = 0;

    public MessageInfo(String name, String message, int profilePath) {
        this.name = name;
        this.message = message;
        this.imagePath = profilePath;
    }

    public MessageInfo(String key, String s) {
        if (key.equals("username"))
            this.name = s;
        else if (key.equals("message"))
            this.message = s;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getImagePath() {
        return imagePath;
    }

    public void setImagePath(int imagePath) {
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
