/*
 *  Copyright (c) 2016, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.sdk.utils;

import android.content.res.AssetManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import org.hisp.dhis.android.sdk.controllers.DhisController;
import org.joda.time.DateTime;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.UUID;

import static android.text.TextUtils.isEmpty;
/**
 * @author Simen Skogly Russnes on 23.02.15.
 */
public class Utils {

    private static final String CLASS_TAG = "Utils";
    public static final String SURVEY = "survey";
    public static final String WORKPLAN = "workplan";
    public static final String randomUUID = DhisController.QUEUED + UUID.randomUUID().toString();

    public static final int getDpPx(int dp, DisplayMetrics displayMetrics) {
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                displayMetrics);
        return px;
    }

    public static String removeTimeFromDateString(String dateTime) {
        if(isEmpty(dateTime)) return null;
        DateTime dt = new DateTime(dateTime);
        return dt.toLocalDate().toString();
    }

    public static String getTempUid() {
        return DhisController.QUEUED + UUID.randomUUID().toString();
    }

    /**
     * Used to determine if a uid for a modifiable data model is local (haven't gotten a UID from
     * server yet) or if it has.
     * @param uid
     * @return
     */
    public static boolean isLocal(String uid) {
        if(uid == null || uid.length() == randomUUID.length())
            return true;
        else return false;
    }

    private static final String IV_PARAM = "0123456789ABCDEF";
    private static final String SECRET_KEY = "MAIDI@2019";

    public static byte[] encrypt(String ivStr, String keyStr, byte[] bytes) throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ivStr.getBytes());
        byte[] ivBytes = md.digest();
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(keyStr.getBytes());
        byte[] keyBytes = sha.digest();
        return encrypt(ivBytes, keyBytes, bytes);
    }
    static byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] bytes) throws Exception{
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(bytes);
    }
    public static byte[] decrypt(String ivStr, String keyStr, byte[] bytes) throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(ivStr.getBytes());
        byte[] ivBytes = md.digest();
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(keyStr.getBytes());
        byte[] keyBytes = sha.digest();
        return decrypt(ivBytes, keyBytes, bytes);
    }
    static byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] bytes)  throws Exception{
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(bytes);
    }
    public static String encryptStrAndToBase64(String enStr) throws Exception{
        byte[] bytes = encrypt(IV_PARAM, SECRET_KEY, enStr.getBytes("UTF-8"));
        return new String(Base64.encode(bytes ,Base64.DEFAULT), "UTF-8");
    }
    public static String decryptStrAndFromBase64(String deStr) throws Exception{
        byte[] bytes = decrypt(IV_PARAM, SECRET_KEY, Base64.decode(deStr.getBytes("UTF-8"),Base64.DEFAULT));
        return new String(bytes, "UTF-8");
    }

    public static String getDataFromAssetFile(AssetManager asset, String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(asset.open(fileName)));
        String str;
        while (true) {
            str = br.readLine();
            if(str != null)
                sb.append(str);
            else
                break;
        }
        br.close();
        return sb.toString();
    }
}
