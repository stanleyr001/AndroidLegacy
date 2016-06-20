package com.topicplaces.android;

import Message.MessageDeleter;
import Message.MessageListRetriever;
import Message.MessagePoster;
import Message.MessageRelated.AttributeListRetriever;
import Message.MessageRelated.FollowerListRetriever;
import Message.MessageRelated.LinkMaker;
import Message.MessageRelated.MediaRetriever;
import Message.MessageRelated.MediaUploader;
import Message.MessageRelated.OptionMaker;
import Message.MessageRelated.OptionRetriever;
import Message.MessageRetriever;
import Message.MessageUpdater;
import Message.PrivateMessagePoster;
import Topics.PrivateTopicsListRetriever;
import Topics.TopicCreator;
import Topics.TopicDeleter;
import Topics.TopicRetriever;
import Topics.TopicUpdater;
import Topics.TopicsListRetriever;
import Users.RESTLogin;
import Users.SubscriptionCreator;
import Users.SubscriptionDeleter;
import Users.UserCreator;
import Users.UserDeleter;
import Users.UserInviter;
import Users.UserRetriever;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SNSController
{
  private String ENDPOINT;
  
  public SNSController(String end)
  {
    this.ENDPOINT = end;
  }
  
  private void ensureConnection()
  {
    boolean connected = false;
    while (!connected) {
      try
      {
        URLConnection connection = new URL(this.ENDPOINT).openConnection();
        connection.connect();
        
        connected = true;
      }
      catch (MalformedURLException e)
      {
        throw new IllegalStateException("Bad URL: " + this.ENDPOINT, e);
      }
      catch (IOException e)
      {
        System.out.println("Connection to " + this.ENDPOINT + " lost, attempting to reconnect...");
        try
        {
          Thread.sleep(15000L);
        }
        catch (InterruptedException s)
        {
          throw new IllegalStateException("Sleep interrupted.");
        }
      }
    }
  }
  
  public String acquireKey(String username, String password)
  {
    ensureConnection();
    
    RESTLogin logg = new RESTLogin(this.ENDPOINT);
    return logg.login(username, password);
  }
  
  public String newPublicTopic(String title, String authkey)
  {
    ensureConnection();
    
    TopicCreator tc = new TopicCreator(this.ENDPOINT);
    String topicID = tc.createTopic(title, false, authkey);
    if (topicID.equals(""))
    {
      System.err.println("Unable to create new topic");
      return "";
    }
    return topicID;
  }
  
  public String newPrivateTopic(String title, String authkey)
  {
    ensureConnection();
    
    TopicCreator tc = new TopicCreator(this.ENDPOINT);
    String topicID = tc.createTopic(title, true, authkey);
    if (topicID.equals(""))
    {
      System.err.println("Unable to create new topic");
      return "";
    }
    return topicID;
  }
  
  public void updateTopic(String title, String desc, String media, Boolean isPrivate, String TID, String authkey)
  {
    ensureConnection();
    TopicUpdater tupd = new TopicUpdater(this.ENDPOINT);
    tupd.updateTopic(title, desc, media, isPrivate, TID, authkey);
  }
  
  public String newPublicMessage(String title, String desc, String mediaID, String topicID, String authkey)
  {
    ensureConnection();
    
    MessagePoster tp = new MessagePoster(this.ENDPOINT);
    
    return tp.execute(title, desc, mediaID, authkey, topicID);
  }
  
  public String newPrivateMessage(String title, String desc, String mediaID, String topicID, String authkey)
  {
    ensureConnection();
    
    PrivateMessagePoster tp = new PrivateMessagePoster(this.ENDPOINT);
    String pM = tp.execute(title, desc, mediaID, authkey, topicID);
    
    return pM;
  }
  
  public void updateMessage(String mess, String desc, String mediaID, String GID, String authkey)
  {
    ensureConnection();
    MessageUpdater gupd = new MessageUpdater(this.ENDPOINT);
    gupd.execute(mess, desc, mediaID, GID, authkey);
  }
  
  public String getMessageJSON(String GID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    
    MessageRetriever gret = new MessageRetriever(this.ENDPOINT);
    return gret.getMessageJSON(GID, Boolean.valueOf(isPrivate), authkey);
  }
  
  public String getMessageMedia(String GID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    
    String MID = null;
    if (isPrivate) {
      MID = new MediaRetriever(this.ENDPOINT).getPrivateGameMedia(GID, authkey);
    } else {
      MID = new MediaRetriever(this.ENDPOINT).getGameMedia(GID, authkey);
    }
    if (MID.equals("")) {
      MID = null;
    }
    return MID;
  }
  
  public String getMessageTitle(String GID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    MessageRetriever gret = new MessageRetriever(this.ENDPOINT);
    return gret.getTitleFromJSON(gret.getMessageJSON(GID, Boolean.valueOf(isPrivate), authkey));
  }
  
  public String getMessageDescription(String GID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    MessageRetriever gret = new MessageRetriever(this.ENDPOINT);
    return gret.getDescriptionFromJSON(gret.getMessageJSON(GID, Boolean.valueOf(isPrivate), authkey));
  }
  
  public String getTopicJSON(String TID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    TopicRetriever tRet = new TopicRetriever(this.ENDPOINT);
    return tRet.getTopicInfoJSON(TID, isPrivate, authkey);
  }
  
  public String getTopicMedia(String TID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    
    String MID = new MediaRetriever(this.ENDPOINT).getTopicMedia(TID, Boolean.valueOf(isPrivate), authkey);
    if (MID.equals("")) {
      MID = null;
    }
    return MID;
  }
  
  public String getTopicTitle(String TID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    TopicRetriever tRet = new TopicRetriever(this.ENDPOINT);
    return tRet.getTitleFromJSON(tRet.getTopicInfoJSON(TID, isPrivate, authkey));
  }
  
  public String getTopicDescription(String TID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    TopicRetriever tRet = new TopicRetriever(this.ENDPOINT);
    return tRet.getDescriptionFromJSON(tRet.getTopicInfoJSON(TID, isPrivate, authkey));
  }
  
  public void newLink(String URL, String GID, String authkey)
  {
    LinkMaker lMak = new LinkMaker(this.ENDPOINT);
    lMak.createLinkForGame(URL, GID, authkey);
  }
  
  public void invite(String UID, String PID, String authkey)
  {
    ensureConnection();
    
    UserInviter ui = new UserInviter(this.ENDPOINT);
    ui.inviteUserToPID(UID, PID, authkey);
  }
  
  public void follow(String followerUID, String followeeUID, String authkey)
  {
    SubscriptionCreator screat = new SubscriptionCreator(this.ENDPOINT);
    screat.execute(followerUID, followeeUID, authkey);
  }
  
  public void newMessageAttributes(Map<String, String> attributes, String GID, String authkey)
  {
    ensureConnection();
    
    MessageUpdater gupd = new MessageUpdater(this.ENDPOINT);
    gupd.addAttributes(attributes, GID, authkey);
  }
  
  public Map<String, String> getAttributes(String GID, Boolean isPrivate, String authkey)
  {
    ensureConnection();
    
    AttributeListRetriever alr = new AttributeListRetriever(this.ENDPOINT);
    
    return alr.getList(GID, isPrivate, authkey);
  }
  
  public String newMessageOption(String text, String gameID, String authkey)
  {
    ensureConnection();
    
    OptionMaker oMak = new OptionMaker(this.ENDPOINT);
    String optionID = oMak.createOptionForGame(text, gameID, authkey);
    if (optionID.equals("")) {
      return "";
    }
    return optionID;
  }
  
  public String newUser(String name, String username, String email, String password)
  {
    ensureConnection();
    UserCreator uCreat = new UserCreator(this.ENDPOINT);
    
    return uCreat.createUser(name, username, email, password);
  }
  
  public String verifyEmail(String email)
  {
    ensureConnection();
    
    return verifyUsername(email);
  }
  
  public String verifyUsername(String user)
  {
    ensureConnection();
    UserRetriever uGett = new UserRetriever(this.ENDPOINT);
    
    return uGett.getUserFromIDorEmail(user);
  }
  
  public String getUsernameFromID(String user)
  {
    ensureConnection();
    UserRetriever uGett = new UserRetriever(this.ENDPOINT);
    
    return uGett.getUsernameFromID(user);
  }
  
  public Map<String, String> getPrivateTopicMap(String userID)
  {
    ensureConnection();
    PrivateTopicsListRetriever ptlr = new PrivateTopicsListRetriever(this.ENDPOINT);
    
    return ptlr.getList(userID);
  }
  
  public Map<String, String> getPublicTopicMap(String userID)
  {
    ensureConnection();
    TopicsListRetriever ptlr = new TopicsListRetriever(this.ENDPOINT);
    
    return ptlr.getList(userID);
  }
  
  public Map<String, String> getPrivateMessageMap(String TID, String authkey)
  {
    ensureConnection();
    MessageListRetriever glr = new MessageListRetriever(this.ENDPOINT);
    
    Map<String, String> lis = glr.getList(TID, true, authkey);
    
    return lis;
  }
  
  public Map<String, String> getPublicMessageMap(String TID, String authkey)
  {
    ensureConnection();
    MessageListRetriever glr = new MessageListRetriever(this.ENDPOINT);
    
    Map<String, String> list = glr.getList(TID, false, authkey);
    return list;
  }
  
  public Map<String, String> getFollowerSubMap(String ID)
  {
    ensureConnection();
    
    FollowerListRetriever flr = new FollowerListRetriever(this.ENDPOINT);
    return flr.getUserSubscriptionMap(ID);
  }
  
  public Map<String, String> getFollowerIDMap(String ID)
  {
    ensureConnection();
    
    FollowerListRetriever flr = new FollowerListRetriever(this.ENDPOINT);
    Map<String, String> map = flr.getUserIDMap(ID);
    Map<String, String> nmap = new HashMap();
    
    Iterator<String> iter = map.keySet().iterator();
    while (iter.hasNext())
    {
      String next = (String)iter.next();
      nmap.put(getUsernameFromID(next), next);
    }
    return nmap;
  }
  
  public Map<String, String> getOptionsIDMap(String GID, Boolean isPrivate, String authkey)
  {
    ensureConnection();
    OptionRetriever optRet = new OptionRetriever(this.ENDPOINT);
    return optRet.getIDMap(GID, isPrivate, authkey);
  }
  
  public Map<String, String> getOptionsAnswerMap(String GID, Boolean isPrivate, String authkey)
  {
    ensureConnection();
    OptionRetriever optRet = new OptionRetriever(this.ENDPOINT);
    return optRet.getAnswerMap(GID, isPrivate, authkey);
  }
  
  public void deleteTopic(String id, Boolean isPrivate, String authkey)
  {
    ensureConnection();
    TopicDeleter tdel = new TopicDeleter(this.ENDPOINT);
    tdel.execute(id, isPrivate, authkey);
  }
  
  public void deleteUser(String id, String authkey)
  {
    ensureConnection();
    UserDeleter udel = new UserDeleter(this.ENDPOINT);
    udel.execute(id, authkey);
  }
  
  public void deleteMessage(String GID, String authkey)
  {
    ensureConnection();
    MessageDeleter gd = new MessageDeleter(this.ENDPOINT);
    gd.execute(GID, authkey);
  }
  
  public String newMediaFromLocal(File f, String authkey)
  {
    ensureConnection();
    MediaUploader mUp = new MediaUploader(this.ENDPOINT);
    return mUp.uploadFromLocal(f, authkey);
  }
  
  public String newMediaFromURL(String URL, String authkey)
  {
    ensureConnection();
    MediaUploader mUp = new MediaUploader(this.ENDPOINT);
    return mUp.uploadFromURL(URL, authkey);
  }
  
  public void deleteSubscription(String subID, String authkey)
  {
    ensureConnection();
    
    SubscriptionDeleter sd = new SubscriptionDeleter(this.ENDPOINT);
    sd.execute(subID, authkey);
  }
  
  public void newMessageFromTemplate(String name, Boolean sameName, String fromGID, String toTID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    
    MessageRetriever mRet = new MessageRetriever(this.ENDPOINT);
    OptionRetriever oRet = new OptionRetriever(this.ENDPOINT);
    AttributeListRetriever alr = new AttributeListRetriever(this.ENDPOINT);
    OptionMaker optMak = new OptionMaker(this.ENDPOINT);
    MessageUpdater mUpd = new MessageUpdater(this.ENDPOINT);
    
    String json = mRet.getMessageJSON(fromGID, Boolean.valueOf(isPrivate), authkey);
    
    String title = null;
    if (sameName.booleanValue()) {
      title = mRet.getTitleFromJSON(json);
    } else {
      title = name;
    }
    String desc = mRet.getDescriptionFromJSON(json);
    String media = mRet.getMediaFromJSON(json);
    
    Map<String, String> optMap = oRet.getIDMap(fromGID, Boolean.valueOf(isPrivate), authkey);
    Map<String, String> attsMap = alr.getList(fromGID, Boolean.valueOf(isPrivate), authkey);
    
    String newGame = null;
    if (isPrivate) {
      newGame = newPrivateMessage(title, desc, media, toTID, authkey);
    } else {
      newGame = newPublicMessage(title, desc, media, toTID, authkey);
    }
    mUpd.addAttributes(attsMap, newGame, authkey);
    
    ArrayList<String> newList = new ArrayList();
    Iterator<String> iter = optMap.keySet().iterator();
    while (iter.hasNext()) {
      newList.add(iter.next());
    }
    Collections.sort(newList);
    
    iter = newList.iterator();
    while (iter.hasNext()) {
      optMak.createOptionForGame((String)iter.next(), newGame, authkey);
    }
  }
  
  public void newTopicFromTemplate(String name, Boolean sameName, String fromTID, boolean isPrivate, String authkey)
  {
    ensureConnection();
    
    String title = null;
    if (sameName.booleanValue()) {
      title = getTopicTitle(fromTID, isPrivate, authkey);
    } else {
      title = name;
    }
    String mediaID = getTopicMedia(fromTID, isPrivate, authkey);
    
    String TID = null;
    if (isPrivate) {
      TID = newPrivateTopic(title, authkey);
    } else {
      TID = newPublicTopic(title, authkey);
    }
    if (mediaID != null) {
      updateTopic(null, null, mediaID, Boolean.valueOf(isPrivate), TID, authkey);
    }
    MessageListRetriever glr = new MessageListRetriever(this.ENDPOINT);
    Map<String, String> messageList = glr.getList(fromTID, isPrivate, authkey);
    Iterator<String> it = messageList.keySet().iterator();
    while (it.hasNext())
    {
      String messageName = (String)it.next();
      String messageGID = (String)messageList.get(messageName);
      
      System.out.println("Creating " + messageName + "...");
      
      newMessageFromTemplate(null, Boolean.valueOf(true), messageGID, TID, isPrivate, authkey);
    }
  }
}
