/*
 */

package gmailpull;

import com.keywords.KeywordFile;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import com.mongodb.*;
import javax.mail.MessagingException;
import org.apache.http.HttpResponse;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestException;
import java.util.Calendar;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;
/**
 *
 * @author callumj
 */
public class GmailFile extends KeywordFile {
    public String uniqueHash;
    public String awsKey;
    public Message message;

    public static MessageDigest digest;

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
    
    public void commitIfNeeded(AmazonS3Client s3Client, DB targetDB) throws NoSuchAlgorithmException, UnsupportedEncodingException, MessagingException {
        DBCollection targetCollection = targetDB.getCollection(COLLECTIONNAME);

        DBObject insertionRecord = new BasicDBObject();
        insertionRecord.put("message_hash", SHA1(message.getFrom()[0] + message.getSubject() + (message.getSentDate().getTime() / 1000) + message.getSize()));
        insertionRecord.put("service", this.parentID);
        DBObject result = targetCollection.findOne(insertionRecord);

        //check if an update or creation will occur
        if (result != null) {
            
        } else {
            try {
                awsKey = SHA1(((String) insertionRecord.get("message_hash"))  + Calendar.getInstance().getTimeInMillis());
            } catch (UnsupportedEncodingException encodingErr) {
                System.out.println("Encoding error" + encodingErr);
                return;
            } catch (NoSuchAlgorithmException algoErr) {
                System.out.println("Algo error" + algoErr);
                return;
            }

            itemType = "txt";
            filename = message.getSubject();

            insertionRecord.put("file_extension", "mail");
            insertionRecord.put("mime_type", message.getContentType());
            insertionRecord.put("size", message.getSize());
            insertionRecord.put("title", message.getSubject());
            insertionRecord.put("sent_date", message.getSentDate().getTime() / 1000);
            insertionRecord.put("received_date", message.getReceivedDate().getTime() / 1000);
            insertionRecord.put("subject", message.getSubject());
            BasicBSONList addressList = new BasicBSONList();
            if (message.getAllRecipients() != null) {
                for (Address addr : message.getAllRecipients()) {
                    BasicBSONObject person = new BasicBSONObject();
                    if (addr instanceof InternetAddress) {
                        person.put("name", ((InternetAddress) addr).getPersonal());
                        person.put("email", ((InternetAddress) addr).getAddress());
                    } else {
                        person.put("email", addr.toString());
                    }
                    addressList.add(person);
                }
            }


            insertionRecord.put("recipients", addressList);
            insertionRecord.put("aws_key", awsKey);


            //pull in the contents
            StringBuffer messageContents = new StringBuffer();
            try {
                if (message.getContent().getClass() == javax.mail.internet.MimeMultipart.class) {
                    javax.mail.internet.MimeMultipart newMsg = (javax.mail.internet.MimeMultipart) message.getContent();
                    for (int y = 0; y < newMsg.getCount(); y++) {
                        BodyPart bodyContent = newMsg.getBodyPart(y);
                        if (bodyContent.getContentType().toLowerCase().contains("text") || bodyContent.getContentType().toLowerCase().contains("html"))
                            messageContents.append(bodyContent.getContent().toString().replaceAll("\\<.*?>",""));
                    }
                } else {
                    messageContents.append(message.getContent().toString());
                }
            } catch (IOException ioStringErr) {
            
            }

            try {
                //write the inputStream to a FileOutputStream
                File tmpWrite = new File("/tmp/gmailgrab" + awsKey);

                FileWriter out = new FileWriter(tmpWrite);
		out.write(messageContents.toString());
                out.close();

                PutObjectResult putResult = s3Client.putObject(this.BUCKETNAME, awsKey, tmpWrite);
                if (putResult != null) {
                    insertionRecord.put("aws_etag", putResult.getETag());
                    targetCollection.save(insertionRecord);
                    this.myID = (ObjectId) insertionRecord.get("_id");
                    System.out.println("Saved #" + this.myID);

                    this.addToPending(targetDB); //commit
                    tmpWrite.delete();
                }
            } catch (IOException ioErr) {
                System.out.println(ioErr);
                return;
            }
        }

        

        
    }
}
