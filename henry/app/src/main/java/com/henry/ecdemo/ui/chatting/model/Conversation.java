
package com.henry.ecdemo.ui.chatting.model;

import android.database.Cursor;
import android.text.TextUtils;

import com.henry.ecdemo.storage.ContactSqlManager;
import com.henry.ecdemo.ui.contact.ECContacts;

public class Conversation {

    private String sessionId;
    private int msgType;
    private long dateTime;
    private int sendStatus;
    private int unreadCount;
    private String content;
    private String username;
    private String contactId;
    private boolean isNotice;
    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }
    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the msgType
     */
    public int getMsgType() {
        return msgType;
    }
    /**
     * @param msgType the msgType to set
     */
    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
    /**
     * @return the dateTime
     */
    public long getDateTime() {
        return dateTime;
    }
    /**
     * @param dateTime the dateTime to set
     */
    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
    /**
     * @return the sendStatus
     */
    public int getSendStatus() {
        return sendStatus;
    }
    /**
     * @param sendStatus the sendStatus to set
     */
    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }
    /**
     * @return the unreadCount
     */
    public int getUnreadCount() {
        return unreadCount;
    }
    /**
     * @param unreadCount the unreadCount to set
     */
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }
    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public boolean isNotice() {
        return isNotice;
    }

    public void setIsNotice(boolean isNotice) {
        this.isNotice = isNotice;
    }

    public void setCursor(Cursor cursor) {
        this.unreadCount = cursor.getInt(0);
        this.msgType = cursor.getInt(1);
        this.sendStatus = cursor.getInt(2);
        this.dateTime = cursor.getLong(3);
        this.sessionId = cursor.getString(4);
        this.content = cursor.getString(5);
        this.username = cursor.getString(6);

        if(this.sessionId.toLowerCase().startsWith("g")) {
            this.username = cursor.getString(7);
        }
        if(this.username == null && !this.sessionId.toUpperCase().startsWith("G")) {
            ECContacts contacts = ContactSqlManager.getCacheContact(sessionId);
            if(contacts != null) {
                username = contacts.getNickname();
            } else {
                username = sessionId;
            }
        }
        if(TextUtils.isEmpty(this.username)) {
            this.username  = sessionId;
        }
        this.contactId = cursor.getString(8);
        this.isNotice = !(cursor.getInt(9) == 2);
    }

}
