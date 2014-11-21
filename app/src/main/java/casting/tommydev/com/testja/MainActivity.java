package casting.tommydev.com.testja;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tommy.dev.dlna.R;
import com.wireme.activity.ContentItem;
import com.wireme.activity.DeviceItem;
import com.wireme.activity.WireUpnpService;
import com.wireme.mediaserver.ContentNode;
import com.wireme.mediaserver.ContentTree;
import com.wireme.mediaserver.MediaServer;
import com.wireme.util.FixedAndroidHandler;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.support.contentdirectory.ui.ContentBrowseActionCallback;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.PersonWithRole;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.ImageItem;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.VideoItem;
import org.teleal.common.logging.LoggingUtil;
import org.teleal.common.util.MimeType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private static final Logger log = Logger.getLogger(MainActivity.class.getName());



    InetAddress LocalIpAddress;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            LocalIpAddress=getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        LoggingUtil.resetRootHandler(new FixedAndroidHandler());
        Logger.getLogger("org.teleal.cling").setLevel(Level.INFO);


        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);


        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp( R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));




        FrameLayout container =(FrameLayout)findViewById(R.id.container);
        RelativeLayout mContent=(RelativeLayout)getLayoutInflater().inflate(R.layout.fragment_main,null);
        container.addView(mContent);









    }
    private int APP_EXIT=2;
    private int APP_POLICY=1;
    private int APP_DEVIES=0;
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(position==APP_EXIT){
            finish();
        }else if(position==APP_DEVIES){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(position + 1,LocalIpAddress,this))
                    .commit();
        }else if(position==APP_POLICY){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PolicyPlaceholderFragment.newInstance(position + 1))
                    .commit();
        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.menu_section1);
                break;
            case 2:
                mTitle = getString(R.string.menu_section2);
                break;
            case 3:
                mTitle = getString(R.string.menu_section3);
                break;

        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener{

        private ListView deviceListView;
        private ListView contentListView;

        private static ArrayAdapter<DeviceItem> deviceListAdapter;
        private ArrayAdapter<ContentItem> contentListAdapter;

        private static DeviceListRegistryListener deviceListRegistryListener;

        private static AndroidUpnpService upnpService;

        private static MediaServer mediaServer;

        private static boolean serverPrepared = false;

        private final static String LOGTAG = "WireMe";
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private ImageView back_floder;

        public static AndroidUpnpService getUpnpService() {
            return upnpService;
        }


        public static DeviceListRegistryListener getDeviceListRegistryListener() {
            return deviceListRegistryListener;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */



        public static InetAddress mInetAddressX;

        public static Activity mActivityX;

        public static PlaceholderFragment newInstance(int sectionNumber,InetAddress mInetAddress,Activity mActivity) {
            mActivityX=mActivity;
            mInetAddressX=mInetAddress;
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            deviceListView = (ListView) rootView.findViewById(R.id.deviceList);
            contentListView = (ListView) rootView.findViewById(R.id.contentList);
            back_floder = (ImageView) rootView.findViewById(R.id.back_floder);
            deviceListAdapter = new ArrayAdapter<DeviceItem>(getActivity(),android.R.layout.simple_list_item_1);
            deviceListRegistryListener = new DeviceListRegistryListener();
            deviceListView.setAdapter(deviceListAdapter);
            deviceListView.setOnItemClickListener(deviceItemClickListener);
            contentListAdapter = new ArrayAdapter<ContentItem>(getActivity(),android.R.layout.simple_list_item_1);
            contentListView.setAdapter(contentListAdapter);
            contentListView.setOnItemClickListener(contentItemClickListener);
            back_floder.setOnClickListener(this);

            getActivity().bindService(new Intent(getActivity(), WireUpnpService.class), serviceConnection,Context.BIND_AUTO_CREATE);
            return rootView;
        }

        @Override
        public void onClick(View view) {
            if(view==back_floder){



                if(!mLinkedList.isEmpty()){
                    mLinkedList.removeLast();

                    if(!mLinkedList.isEmpty()){
                        upnpService.getControlPoint().execute(mLinkedList.getLast());
                    }else{
                        deviceListView.setVisibility(View.VISIBLE);
                        contentListView.setVisibility(View.GONE);
                    }
                }else{
                    deviceListView.setVisibility(View.VISIBLE);
                    contentListView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }





        public class DeviceListRegistryListener extends DefaultRegistryListener {

		/* Discovery performance optimization for very slow Android devices! */

            @Override
            public void remoteDeviceDiscoveryStarted(Registry registry,
                                                     RemoteDevice device) {
            }

            @Override
            public void remoteDeviceDiscoveryFailed(Registry registry,
                                                    final RemoteDevice device, final Exception ex) {
            }

		/*
		 * End of optimization, you can remove the whole block if your Android
		 * handset is fast (>= 600 Mhz)
		 */

            @Override
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

                if (device.getType().getNamespace().equals("schemas-upnp-org")
                        && device.getType().getType().equals("MediaServer")) {
                    final DeviceItem display = new DeviceItem(device, device
                            .getDetails().getFriendlyName(),
                            device.getDisplayString(), "(REMOTE) "
                            + device.getType().getDisplayString());
                    deviceAdded(display);
                }
            }

            @Override
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                final DeviceItem display = new DeviceItem(device,
                        device.getDisplayString());
                deviceRemoved(display);
            }

            @Override
            public void localDeviceAdded(Registry registry, LocalDevice device) {
                final DeviceItem display = new DeviceItem(device, device
                        .getDetails().getFriendlyName(), device.getDisplayString(),
                        "(REMOTE) " + device.getType().getDisplayString());
                deviceAdded(display);
            }

            @Override
            public void localDeviceRemoved(Registry registry, LocalDevice device) {
                final DeviceItem display = new DeviceItem(device,
                        device.getDisplayString());
                deviceRemoved(display);
            }

            public void deviceAdded(final DeviceItem di) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {

                        int position = deviceListAdapter.getPosition(di);
                        if (position >= 0) {
                            // Device already in the list, re-set new value at same
                            // position
                            deviceListAdapter.remove(di);
                            deviceListAdapter.insert(di, position);
                        } else {
                            deviceListAdapter.add(di);
                        }

                        // Sort it?
                        // listAdapter.sort(DISPLAY_COMPARATOR);
                        // listAdapter.notifyDataSetChanged();
                    }
                });
            }

            public void deviceRemoved(final DeviceItem di) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        deviceListAdapter.remove(di);
                    }
                });
            }
        }

        AdapterView.OnItemClickListener deviceItemClickListener = new AdapterView.OnItemClickListener() {

            protected Container createRootContainer(Service service) {
                Container rootContainer = new Container();
                rootContainer.setId("0");
                rootContainer.setTitle("Content Directory on "+ service.getDevice().getDisplayString());
                return rootContainer;
            }

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position,long id) {
                Device device = deviceListAdapter.getItem(position).getDevice();
                Service service = device.findService(new UDAServiceType("ContentDirectory"));

                mLinkedList.add(new ContentBrowseActionCallback(getActivity(), service,createRootContainer(service), contentListAdapter));

                upnpService.getControlPoint().execute( new ContentBrowseActionCallback(getActivity(), service,createRootContainer(service), contentListAdapter));
                deviceListView.setVisibility(View.GONE);

                contentListView.setVisibility(View.VISIBLE);

            }
        };
        LinkedList<ContentBrowseActionCallback> mLinkedList=new LinkedList<ContentBrowseActionCallback>();

        AdapterView.OnItemClickListener contentItemClickListener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {
                // TODO Auto-generated method stub
                ContentItem content = contentListAdapter.getItem(position);
                if (content.isContainer()) {
                    mLinkedList.add(new ContentBrowseActionCallback(getActivity(),content.getService(), content.getContainer(),contentListAdapter));
                    upnpService.getControlPoint().execute(new ContentBrowseActionCallback(getActivity(),content.getService(), content.getContainer(),contentListAdapter));


                } else {
//                    Intent intent = new Intent();
//                    intent.setClass(getActivity(), GPlayer.class);
//                    intent.putExtra("playURI", content.getItem().getFirstResource()
//                            .getValue());
//                    startActivity(intent);
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                  //  File file = new File(content.getItem().getFirstResource().getValue());
                    viewIntent.setDataAndType(Uri.parse(content.getItem().getFirstResource().getValue()), "video/*");
                    startActivity(Intent.createChooser(viewIntent, null));
                }
            }
        };




        private static ServiceConnection serviceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder service) {
                upnpService = (AndroidUpnpService) service;
                Log.v(LOGTAG, "Connected to UPnP Service");
                if (mediaServer == null) {
                    try {
                        mediaServer = new MediaServer(mInetAddressX);
                        upnpService.getRegistry().addDevice(mediaServer.getDevice());
                        prepareMediaServer();

                    } catch (Exception ex) {
                        // TODO: handle exception
                        log.log(Level.SEVERE, "Creating demo device failed", ex);
                        Toast.makeText(mActivityX,
                                R.string.create_demo_failed, Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }

                deviceListAdapter.clear();
                for (@SuppressWarnings("rawtypes") Device device : upnpService.getRegistry().getDevices()) {
                    deviceListRegistryListener.deviceAdded(new DeviceItem(device));
                }

                // Getting ready for future device advertisements
                upnpService.getRegistry().addListener(deviceListRegistryListener);
                // Refresh device list
                upnpService.getControlPoint().search();
            }

            public void onServiceDisconnected(ComponentName className) {
                upnpService = null;
            }
        };


        private static void prepareMediaServer() {

            if (serverPrepared)
                return;

            ContentNode rootNode = ContentTree.getRootNode();
            // Video Container
            Container videoContainer = new Container();
            videoContainer.setClazz(new DIDLObject.Class("object.container"));
            videoContainer.setId(ContentTree.VIDEO_ID);
            videoContainer.setParentID(ContentTree.ROOT_ID);
            videoContainer.setTitle("Videos");
            videoContainer.setRestricted(true);
            videoContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
            videoContainer.setChildCount(0);

            rootNode.getContainer().addContainer(videoContainer);
            rootNode.getContainer().setChildCount(
                    rootNode.getContainer().getChildCount() + 1);
            ContentTree.addNode(ContentTree.VIDEO_ID, new ContentNode(
                    ContentTree.VIDEO_ID, videoContainer));

            Cursor cursor;
            String[] videoColumns = { MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.ARTIST,
                    MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.RESOLUTION };
            cursor =mActivityX.managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoColumns, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String id = ContentTree.VIDEO_PREFIX
                            + cursor.getInt(cursor
                            .getColumnIndex(MediaStore.Video.Media._ID));
                    String title = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String creator = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                    String filePath = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    long size = cursor.getLong(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    long duration = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    String resolution = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION));
                    Res res = new Res(new MimeType(mimeType.substring(0,
                            mimeType.indexOf('/')), mimeType.substring(mimeType
                            .indexOf('/') + 1)), size, "http://"
                            + mediaServer.getAddress() + "/" + id);
                    res.setDuration(duration / (1000 * 60 * 60) + ":"
                            + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                            + (duration % (1000 * 60)) / 1000);
                    res.setResolution(resolution);

                    VideoItem videoItem = new VideoItem(id, ContentTree.VIDEO_ID,
                            title, creator, res);
                    videoContainer.addItem(videoItem);
                    videoContainer
                            .setChildCount(videoContainer.getChildCount() + 1);
                    ContentTree.addNode(id,
                            new ContentNode(id, videoItem, filePath));

                    Log.v(LOGTAG, "added video item " + title + "from " + filePath);
                } while (cursor.moveToNext());
            }

            // Audio Container
            Container audioContainer = new Container(ContentTree.AUDIO_ID,
                    ContentTree.ROOT_ID, "Audios", "GNaP MediaServer",
                    new DIDLObject.Class("object.container"), 0);
            audioContainer.setRestricted(true);
            audioContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
            rootNode.getContainer().addContainer(audioContainer);
            rootNode.getContainer().setChildCount(
                    rootNode.getContainer().getChildCount() + 1);
            ContentTree.addNode(ContentTree.AUDIO_ID, new ContentNode(
                    ContentTree.AUDIO_ID, audioContainer));

            String[] audioColumns = { MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ALBUM };
            cursor =mActivityX. managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    audioColumns, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String id = ContentTree.AUDIO_PREFIX
                            + cursor.getInt(cursor
                            .getColumnIndex(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String creator = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String filePath = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                    long size = cursor.getLong(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    long duration = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    String album = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    Res res = new Res(new MimeType(mimeType.substring(0,
                            mimeType.indexOf('/')), mimeType.substring(mimeType
                            .indexOf('/') + 1)), size, "http://"
                            + mediaServer.getAddress() + "/" + id);
                    res.setDuration(duration / (1000 * 60 * 60) + ":"
                            + (duration % (1000 * 60 * 60)) / (1000 * 60) + ":"
                            + (duration % (1000 * 60)) / 1000);

                    // Music Track must have `artist' with role field, or
                    // DIDLParser().generate(didl) will throw nullpointException
                    MusicTrack musicTrack = new MusicTrack(id,
                            ContentTree.AUDIO_ID, title, creator, album,
                            new PersonWithRole(creator, "Performer"), res);
                    audioContainer.addItem(musicTrack);
                    audioContainer
                            .setChildCount(audioContainer.getChildCount() + 1);
                    ContentTree.addNode(id, new ContentNode(id, musicTrack,
                            filePath));

                    Log.v(LOGTAG, "added audio item " + title + "from " + filePath);
                } while (cursor.moveToNext());
            }

            // Image Container
            Container imageContainer = new Container(ContentTree.IMAGE_ID,
                    ContentTree.ROOT_ID, "Images", "GNaP MediaServer",
                    new DIDLObject.Class("object.container"), 0);
            imageContainer.setRestricted(true);
            imageContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
            rootNode.getContainer().addContainer(imageContainer);
            rootNode.getContainer().setChildCount(
                    rootNode.getContainer().getChildCount() + 1);
            ContentTree.addNode(ContentTree.IMAGE_ID, new ContentNode(
                    ContentTree.IMAGE_ID, imageContainer));

            String[] imageColumns = { MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE };
            cursor = mActivityX.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageColumns, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String id = ContentTree.IMAGE_PREFIX
                            + cursor.getInt(cursor
                            .getColumnIndex(MediaStore.Images.Media._ID));
                    String title = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                    String creator = "unkown";
                    String filePath = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    long size = cursor.getLong(cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));

                    Res res = new Res(new MimeType(mimeType.substring(0,
                            mimeType.indexOf('/')), mimeType.substring(mimeType
                            .indexOf('/') + 1)), size, "http://"
                            + mediaServer.getAddress() + "/" + id);

                    ImageItem imageItem = new ImageItem(id, ContentTree.IMAGE_ID,
                            title, creator, res);
                    imageContainer.addItem(imageItem);
                    imageContainer
                            .setChildCount(imageContainer.getChildCount() + 1);
                    ContentTree.addNode(id,
                            new ContentNode(id, imageItem, filePath));

                    Log.v(LOGTAG, "added image item " + title + "from " + filePath);
                } while (cursor.moveToNext());
            }

            serverPrepared = true;
        }

        // FIXME: now only can get wifi address
        private  InetAddress getLocalIpAddress() throws UnknownHostException {
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return InetAddress.getByName(String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));
        }
    }




    public static class PolicyPlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PolicyPlaceholderFragment newInstance(int sectionNumber) {
            PolicyPlaceholderFragment fragment = new PolicyPlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PolicyPlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_policy, container, false);
            WebView mWebView= (WebView) rootView.findViewById(R.id.webView);
            mWebView.loadUrl("file:///android_asset/index.html");
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (PlaceholderFragment.upnpService != null) {
            PlaceholderFragment.upnpService.getRegistry()
                    .removeListener(PlaceholderFragment.deviceListRegistryListener);
        }
        getApplicationContext().unbindService(PlaceholderFragment.serviceConnection);
    }

    protected void searchNetwork() {
        if (PlaceholderFragment.upnpService == null)
            return;
        Toast.makeText(this, R.string.searching_lan, Toast.LENGTH_SHORT).show();
        PlaceholderFragment.upnpService.getRegistry().removeAllRemoteDevices();
        PlaceholderFragment.upnpService.getControlPoint().search();
    }





    private  InetAddress getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return InetAddress.getByName(String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff)));
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, 0, 0, R.string.search_lan).setIcon(
//                android.R.drawable.ic_menu_search);
//        menu.add(0, 1, 0, R.string.toggle_debug_logging).setIcon(
//                android.R.drawable.ic_menu_info_details);
//        return true;
//    }


    ///// Variable////////


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
