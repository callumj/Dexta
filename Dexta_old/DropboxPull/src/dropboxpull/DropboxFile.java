/*
 */

package dropboxpull;

import com.keywords.KeywordFile;
import com.dropbox.*;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import com.mongodb.*;
import org.apache.http.HttpResponse;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestException;
import java.util.Calendar;
import org.bson.types.ObjectId;
/**
 *
 * @author callumj
 */
public class DropboxFile extends KeywordFile {
    public String path;
    public String internalPath;
    public String mimeType;
    public Long size;
    public Date modified;
    public Long revision;
    public String awsKey;

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

    public void commitIfNeeded(AmazonS3Client s3Client, DropboxClient dboxClient, DB targetDB) {
        DBCollection targetCollection = targetDB.getCollection(COLLECTIONNAME);

        DBObject insertionRecord = new BasicDBObject();
        insertionRecord.put("dropbox_path", path);
        insertionRecord.put("service", this.parentID);
        DBObject result = targetCollection.findOne(insertionRecord);

        //check if an update or creation will occur
        if (result != null) {
            //verify if we need to perform update, otherwise cancel the operation
            Long tempRevision = 0l;
            try {
                tempRevision = (Long) result.get("dropbox_revision");
            } catch (Exception castErr) {
                tempRevision = ((Integer) result.get("dropbox_revision")).longValue();
            }
            
            //perform an overwrite
            insertionRecord = result;

            awsKey = (String) result.get("aws_key");

            if (tempRevision.equals(revision)) {
                return; //quit, nothing to see here
            }
        } else {
            try {
                awsKey = SHA1(path + modified.getTime() + Calendar.getInstance().getTimeInMillis());
            } catch (UnsupportedEncodingException encodingErr) {
                System.out.println("Encoding error" + encodingErr);
                return;
            } catch (NoSuchAlgorithmException algoErr) {
                System.out.println("Algo error" + algoErr);
                return;
            }
        }

        insertionRecord.put("file_extension", itemType);
        insertionRecord.put("mime_type", mimeType);
        insertionRecord.put("size", size);
        insertionRecord.put("modified_date", modified.getTime() / 1000);
        insertionRecord.put("dropbox_revision", revision);
        insertionRecord.put("aws_key", awsKey);

        //write to AWS
        ObjectMetadata awsMeta = new ObjectMetadata();
        awsMeta.setContentType(mimeType);
        awsMeta.setContentLength(size);

        HttpResponse dboxGet;
        try {
            dboxGet = dboxClient.getFile("dropbox", path);
        } catch (DropboxException dbEx) {
            System.out.println(dbEx);
            return;
        }
        
        try {
            InputStream dboxFile = dboxGet.getEntity().getContent();

            //write the inputStream to a FileOutputStream
            File tmpWrite = new File("/tmp/dboxgrab" + awsKey);
            OutputStream out = new FileOutputStream(tmpWrite);

            int read=0;
            byte[] bytes = new byte[1024];

            while((read = dboxFile.read(bytes))!= -1){
                    out.write(bytes, 0, read);
            }

            dboxFile.close();
            out.flush();
            out.close();
            PutObjectResult putResult = s3Client.putObject(this.BUCKETNAME, awsKey, tmpWrite);
            if (putResult != null) {
                insertionRecord.put("aws_etag", putResult.getETag());
                targetCollection.save(insertionRecord);
                this.myID = (ObjectId) insertionRecord.get("_id");
                this.addToPending(targetDB); //commit
                tmpWrite.delete();
            }
        } catch (IOException ioErr) {
            System.out.println(ioErr);
            return;
        }

        
    }
}
