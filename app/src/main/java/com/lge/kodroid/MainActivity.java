package com.lge.kodroid;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity {
  EditText server,key,name; TextView status;
  android.content.SharedPreferences sp;
  @Override public void onCreate(Bundle b){super.onCreate(b);
    sp=getSharedPreferences("cfg",0);
    LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,24,24,24);
    TextView title=new TextView(this); title.setText("Kodroid"); title.setTextSize(28); title.setTextColor(Color.BLACK); root.addView(title);
    status=new TextView(this); status.setText("Ready"); root.addView(status);
    name=field("Device name",sp.getString("name","Android device")); root.addView(name);
    server=field("Server URL",sp.getString("server","https://YOUR-SERVER")); root.addView(server);
    key=field("OwnDroid API key",sp.getString("key","")); root.addView(key);
    Button save=new Button(this); save.setText("Save settings"); save.setOnClickListener(v->save()); root.addView(save);
    Button poll=new Button(this); poll.setText("Check server now"); poll.setOnClickListener(v->new Thread(()->pollOnce()).start()); root.addView(poll);
    Button own=new Button(this); own.setText("Test OwnDroid connection"); own.setOnClickListener(v->sendOwn("LOCK",null)); root.addView(own);
    Button apk=new Button(this); apk.setText("Open APK installer"); apk.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_VIEW); i.setDataAndType(Uri.parse("content://com.lge.kodroid.invalid"),"application/vnd.android.package-archive"); startActivity(i);}); root.addView(apk);
    setContentView(root);
  }
  EditText field(String hint,String val){EditText e=new EditText(this);e.setHint(hint);e.setText(val);e.setSingleLine(true);return e;}
  void save(){sp.edit().putString("name",name.getText().toString()).putString("server",server.getText().toString()).putString("key",key.getText().toString()).apply(); status.setText("Saved");}
  void pollOnce(){try{
    String base=server.getText().toString().replaceAll("/+$","");
    URL u=new URL(base+"/v1/devices/poll?device="+URLEncoder.encode(name.getText().toString(),"UTF-8"));
    HttpURLConnection c=(HttpURLConnection)u.openConnection(); c.setRequestMethod("GET"); c.setConnectTimeout(8000); c.setReadTimeout(8000);
    c.setRequestProperty("Authorization","Bearer "+sp.getString("deviceToken",""));
    int code=c.getResponseCode(); final String s="Server response: "+code;
    runOnUiThread(()->status.setText(s)); c.disconnect();
  }catch(Exception e){runOnUiThread(()->status.setText("Server error: "+e.getClass().getSimpleName()));}}
  void sendOwn(String action,String pkg){try{
    Intent i=new Intent("com.bintianqi.owndroid.action."+action).setComponent(new ComponentName("com.bintianqi.owndroid","com.bintianqi.owndroid.ApiReceiver")).putExtra("key",key.getText().toString());
    if(pkg!=null)i.putExtra("package",pkg); sendBroadcast(i); runOnUiThread(()->status.setText("OwnDroid request sent: "+action));
  }catch(Exception e){runOnUiThread(()->status.setText("OwnDroid error: "+e.getMessage()));}}
}
