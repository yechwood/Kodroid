package com.lge.kodroid;
import android.util.Base64;
import java.io.*;import java.net.*;import java.security.*;import javax.crypto.*;import javax.crypto.spec.*;import java.nio.charset.StandardCharsets;
public final class Relay{
 public static final String HOST="https://ntfy.sh", PAIR="kodroid-pair-v1";
 static String b64(byte[] b){return Base64.encodeToString(b,Base64.NO_WRAP|Base64.URL_SAFE);}
 static byte[] unb(String s){return Base64.decode(s,Base64.NO_WRAP|Base64.URL_SAFE);}
 public static KeyPair keys()throws Exception{KeyPairGenerator g=KeyPairGenerator.getInstance("RSA");g.initialize(2048);return g.generateKeyPair();}
 public static String pub(PublicKey k){return b64(k.getEncoded());}
 public static PublicKey publicKey(String s)throws Exception{return KeyFactory.getInstance("RSA").generatePublic(new java.security.spec.X509EncodedKeySpec(unb(s)));}
 public static String encryptKey(byte[] key,PublicKey pub)throws Exception{Cipher c=Cipher.getInstance("RSA/ECB/PKCS1Padding");c.init(Cipher.ENCRYPT_MODE,pub);return b64(c.doFinal(key));}
 public static byte[] decryptKey(String s,PrivateKey priv)throws Exception{Cipher c=Cipher.getInstance("RSA/ECB/PKCS1Padding");c.init(Cipher.DECRYPT_MODE,priv);return c.doFinal(unb(s));}
 public static String enc(String plain,byte[] key)throws Exception{byte[] iv=new byte[12];new SecureRandom().nextBytes(iv);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));byte[] out=c.doFinal(plain.getBytes(StandardCharsets.UTF_8));return b64(iv)+":"+b64(out);}
 public static String dec(String value,byte[] key)throws Exception{String[] p=value.split(":",2);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,unb(p[0])));return new String(c.doFinal(unb(p[1])),StandardCharsets.UTF_8);}
 public static String randomTopic(String prefix){byte[] b=new byte[24];new SecureRandom().nextBytes(b);return prefix+b64(b).replace("=","").replace("/","_").replace("+","-");}
 public static String post(String topic,String body,String... headers)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(HOST+"/"+topic).openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(10000);c.setReadTimeout(10000);c.setRequestProperty("Content-Type","text/plain; charset=utf-8");for(int i=0;i+1<headers.length;i+=2)c.setRequestProperty(headers[i],headers[i+1]);c.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));return read(c);}
 public static String poll(String topic,String since)throws Exception{String u=HOST+"/"+topic+"/json?poll=1"+(since.length()>0?"&since="+URLEncoder.encode(since,"UTF-8"):"");HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setRequestMethod("GET");c.setConnectTimeout(10000);c.setReadTimeout(15000);return read(c);}
 static String read(HttpURLConnection c)throws Exception{int n=c.getResponseCode();InputStream in=n>=400?c.getErrorStream():c.getInputStream();if(in==null)return "";ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] b=new byte[4096];int x;while((x=in.read(b))!=-1)o.write(b,0,x);return o.toString("UTF-8");}
 public static String json(String j,String k){String q="\""+k+"\":";int i=j.indexOf(q);if(i<0)return null;i+=q.length();while(i<j.length()&&Character.isWhitespace(j.charAt(i)))i++;if(i<j.length()&&j.charAt(i)=='\"'){i++;int e=j.indexOf("\"",i);return e<0?null:j.substring(i,e);}int e=i;while(e<j.length()&&",}\n".indexOf(j.charAt(e))<0)e++;return j.substring(i,e).trim();}
}