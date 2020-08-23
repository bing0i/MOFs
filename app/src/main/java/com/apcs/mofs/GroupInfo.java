package com.apcs.mofs;

public class GroupInfo {
    private int _imageAva = R.drawable.ic_group;
    private String _textViewGroupName;
    private String keyGroup = "";

    public GroupInfo(int imageAva, String textViewGroupName, String key) {
        _imageAva = imageAva;
        _textViewGroupName = textViewGroupName;
        keyGroup = key;
    }

    public GroupInfo(String tv, String key) {
        _textViewGroupName = tv;
        keyGroup = key;
    }

    public GroupInfo(String key) {
        keyGroup = key;
    }

    public int get_imageAva() {
        return _imageAva;
    }

    public void set_imageAva(int _imageAva) {
        this._imageAva = _imageAva;
    }

    public String get_textViewGroupName() {
        return _textViewGroupName;
    }

    public void set_textViewGroupName(String _textViewGroupName) {
        this._textViewGroupName = _textViewGroupName;
    }

    public String getKeyGroup() {
        return keyGroup;
    }

    public void setKeyGroup(String keyGroup) {
        this.keyGroup = keyGroup;
    }
}
