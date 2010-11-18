/*
 */

package gmailpull;

import java.util.List;
import java.util.ArrayList;

import com.mongodb.*;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.gmail.XoauthAuthenticator;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;
import java.util.Calendar;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.search.ReceivedDateTerm;
import net.oauth.OAuthConsumer;

/**
 *
 * @author callumj
 */
public class Main {


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //process users
            Mongo m = new Mongo();
            DB db = m.getDB("keywords");
            DBCollection apiSettings = db.getCollection("apis");
            DBCollection servicesCol = db.getCollection("services");

            //Allocate settings for Gmail
            DBObject gmailSettings = apiSettings.findOne(new BasicDBObject("name", "gmail-imap"));
            String consumerKey = (String) gmailSettings.get("consumer_key");
            String consumerSecret = (String) gmailSettings.get("consumer_secret");
            OAuthConsumer gmailConsumer = new OAuthConsumer(null, consumerKey, consumerSecret, null);

            //Setup AWS
            DBObject awsSettings = apiSettings.findOne(new BasicDBObject("name", "aws"));
            String awsKey = (String) awsSettings.get("consumer_key");
            String awsSecret = (String) awsSettings.get("consumer_secret");
            BasicAWSCredentials awsCred = new BasicAWSCredentials(awsKey, awsSecret);
            AmazonS3Client s3Client = new AmazonS3Client(awsCred);

            XoauthAuthenticator.initialize();

            int limit = 100;
            int count = 0;
            BasicDBObject query = new BasicDBObject("type", "gmail-imap");
            DBCursor findCursor = servicesCol.find(query).limit(limit);
            List obj = findCursor.toArray();
            while (!obj.isEmpty()) {
                for (int i = 0; i < obj.size(); i++) {
                    DBObject item = (DBObject) obj.get(i);
                    String email = (String) item.get("email");
                    String oAuthToken = (String) item.get("token_key");
                    String oAuthSecret = (String) item.get("token_secret");
                    Long lastEmailProcessed = (Long) item.get("last_email_date");
                    org.bson.types.ObjectId thisID = (org.bson.types.ObjectId) item.get("_id");
                    org.bson.types.ObjectId userID = (org.bson.types.ObjectId) item.get("user");

                    if (lastEmailProcessed == null)
                        lastEmailProcessed = 0l; //set to beginning of UNIX time

                    IMAPSSLStore imapSslStore = XoauthAuthenticator.connectToImap("imap.gmail.com",
                                              993,
                                              email,
                                              oAuthToken,
                                              oAuthSecret,
                                              gmailConsumer,
                                              false);
                    Folder inbox = imapSslStore.getFolder("[Gmail]/All Mail");

                    Calendar lastEmailCal = Calendar.getInstance();
                    lastEmailCal.setTimeInMillis(lastEmailProcessed * 1000);
                    ReceivedDateTerm recvDateTerm = new ReceivedDateTerm(ReceivedDateTerm.GT, lastEmailCal.getTime());

                    inbox.open(Folder.READ_ONLY);
                    Message[] inboxMessages = inbox.search(recvDateTerm);
                    System.out.println(inboxMessages.length);

                    long newestTime = 0l;

                    for (Message message : inboxMessages) {
                        try {
                            inbox.getMessageCount();
                            GmailFile newMessage = new GmailFile();
                            newMessage.parentID = thisID;
                            newMessage.userID = userID;
                            newMessage.message = message;
                            newMessage.commitIfNeeded(s3Client, db);

                            long thisTime = message.getReceivedDate().getTime() / 1000;

                            if (thisTime > newestTime)
                                newestTime = thisTime;
                        } catch (Exception msgErr) {
                            System.out.println(msgErr);
                        }
                    }

                    item.put("last_email_date", newestTime);
                    servicesCol.save(item); //update time
                }
                count++;
                obj = servicesCol.find(query).skip(limit * count).limit(limit).toArray();
            }
        } catch (Exception genErr) {
            System.out.println("Error " + genErr);
        }
    }
}
