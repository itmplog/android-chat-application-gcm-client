package top.itmp.chat;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UserFragment extends Fragment {
    ListView list;
    ArrayList<HashMap<String, String>> users = new ArrayList<HashMap<String, String>>();
    Button refresh,logout,chat;
    List<NameValuePair> params;
    SharedPreferences prefs;


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.user_fragment, container, false);
        prefs = getActivity().getSharedPreferences("Chat", 0);

        list = (ListView)view.findViewById(R.id.listView);
        refresh = (Button)view.findViewById(R.id.refresh);
        logout = (Button)view.findViewById(R.id.logout);
        chat = (Button)view.findViewById(R.id.chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("mobno", "11");
                args.putString("name", "11");
                args.putString("msg", "hello from 11");
                Intent chat = new Intent(getActivity(), ChatActivity.class);
                chat.putExtra("INFO", args);
                NotificationCompat.Builder notification = new NotificationCompat.Builder(getActivity());
                notification.setContentTitle("11");
                notification.setContentText("hello from 11");
                notification.setTicker("New Message !");
                notification.setSmallIcon(R.drawable.ic_launcher);

                PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 1000,
                        chat, PendingIntent.FLAG_CANCEL_CURRENT);
                notification.setContentIntent(contentIntent);
                notification.setAutoCancel(true);
                NotificationManager manager =(NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(0, notification.build());
            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new  Logout().execute();

            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.content_frame)).commit();
                Fragment reg = new UserFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, reg);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();

            }
        });
        new Load().execute();

        return view;
    }

    private class Load extends AsyncTask<String, String, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... args) {
            JSONParser json = new JSONParser();
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mobno", prefs.getString("REG_FROM","")));
            JSONArray jAry = json.getJSONArray("http://itmp.top:1010/getuser",params);

            return jAry;
        }
        @Override
        protected void onPostExecute(JSONArray json) {
            if(json != null)
            for(int i = 0; i < json.length(); i++){

                try {
                    JSONObject c =  null;//son.optJSONObject(i);//json.getJSONObject(1);

                   // Log.v("json", c.toString());
                    c = json.getJSONObject(i);
                    String name = c.getString("name");
                    String mobno = c.getString("mobno");
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", name);
                    map.put("mobno", mobno);
                    users.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            ListAdapter adapter = new SimpleAdapter(getActivity(), users,
                    R.layout.user_list_single,
                    new String[] { "name","mobno" }, new int[] {
                    R.id.name, R.id.mobno});
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Bundle args = new Bundle();
                    args.putString("mobno", users.get(position).get("mobno"));
                    Intent chat = new Intent(getActivity(), ChatActivity.class);
                    chat.putExtra("INFO", args);
                    startActivity(chat);
                }
            });
        }
    }
    private class Logout extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser json = new JSONParser();
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("mobno", prefs.getString("REG_FROM","")));
            JSONObject jObj = json.getJSONFromUrl("http://itmp.top:1010/logout",params);

            return jObj;
        }
        @Override
        protected void onPostExecute(JSONObject json) {

            String res = null;
            try {
                res = json.getString("response");
                Toast.makeText(getActivity(),res,Toast.LENGTH_SHORT).show();
                if(res.equals("Removed Sucessfully")) {
                    Fragment reg = new LoginFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, reg);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.addToBackStack(null);
                    ft.commit();
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("REG_FROM", "");
                    edit.commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }
    }

}