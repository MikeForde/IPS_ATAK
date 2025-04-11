
package com.atakmap.android.helloworld;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ClipboardManager;
import android.view.View;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.TextView;
import com.atak.plugins.impl.PluginLayoutInflater;
import android.graphics.Color;
import android.view.Gravity;

import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;
import com.atakmap.android.util.AbstractMapItemSelectionTool;
import com.atakmap.comms.CotServiceRemote;
import com.atakmap.comms.CotStreamListener;
import com.atakmap.comms.app.CotPortListActivity;
import com.atakmap.comms.app.CotPortListActivity.CotPort;
import com.atakmap.android.chat.ChatManagerMapComponent;
import com.atakmap.comms.CommsMapComponent;
import android.os.Bundle;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.android.toolbar.widgets.TextContainer;
import android.widget.Toast;
import com.atakmap.comms.NetConnectString;
import com.atakmap.android.contact.Connector;
import com.atakmap.android.contact.IpConnector;
import com.atakmap.android.contact.Contacts;
import com.atakmap.android.contact.Contact;
import com.atakmap.android.contact.IndividualContact;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.helloworld.plugin.R;

import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.atakmap.coremap.log.Log;

import android.app.Activity;

import java.lang.*;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.text.Html;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.Base64;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.webkit.WebView;
import android.widget.Toast;

import android.hardware.SensorManager;

public class HelloWorldDropDownReceiver extends DropDownReceiver implements
        OnStateListener {

//    private final NotificationManager nm;

    public static final String TAG = "HelloWorldDropDownReceiver";

    public static final String SHOW_HELLO_WORLD = "com.atakmap.android.helloworld.SHOW_HELLO_WORLD";
    public static final String CHAT_HELLO_WORLD = "com.atakmap.android.helloworld.CHAT_HELLO_WORLD";
    public static final String SEND_HELLO_WORLD = "com.atakmap.android.helloworld.SEND_HELLO_WORLD";
    public static final String LAYER_DELETE = "com.atakmap.android.helloworld.LAYER_DELETE";
    public static final String LAYER_VISIBILITY = "com.atakmap.android.helloworld.LAYER_VISIBILITY";
    private final View helloView;

    private final Context pluginContext;

    // example menu factory

    // inspection map selector
    final InspectionMapItemSelectionTool imis;

    private Timer issTimer = null;

//    private JoystickListener _joystickView;

    private double currWidth = HALF_WIDTH;
    private double currHeight = HALF_HEIGHT;

    private final CotServiceRemote csr;
    private boolean connected = false;

    final CotServiceRemote.ConnectionListener cl = new CotServiceRemote.ConnectionListener() {
        @Override
        public void onCotServiceConnected(Bundle fullServiceState) {
            Log.d(TAG, "onCotServiceConnected: ");
            connected = true;
        }

        @Override
        public void onCotServiceDisconnected() {
            Log.d(TAG, "onCotServiceDisconnected: ");
            connected = false;
        }

    };

    final CotStreamListener csl;
    final CotServiceRemote.OutputsChangedListener _outputsChangedListener = new CotServiceRemote.OutputsChangedListener() {
        @Override
        public void onCotOutputRemoved(Bundle descBundle) {
            Log.d(TAG, "stream removed");
        }

        @Override
        public void onCotOutputUpdated(Bundle descBundle) {
            Log.v(TAG,
                    "Received ADD message for "
                            + descBundle
                                    .getString(CotPort.DESCRIPTION_KEY)
                            + ": enabled="
                            + descBundle.getBoolean(
                                    CotPort.ENABLED_KEY, true)
                            + ": connected="
                            + descBundle.getBoolean(
                                    CotPort.CONNECTED_KEY, false));
        }
    };

    /**************************** CONSTRUCTOR *****************************/

    public HelloWorldDropDownReceiver(final MapView mapView,
            final Context context, HelloWorldMapOverlay overlay) {
        super(mapView);
        this.pluginContext = context;
//        this.mapOverlay = overlay;
        final Activity parentActivity = (Activity) mapView.getContext();

//        _joystickView = new JoystickListener();

        csr = new CotServiceRemote();
        csr.setOutputsChangedListener(_outputsChangedListener);

        csr.connect(cl);

        imis = new InspectionMapItemSelectionTool();

        csl = new CotStreamListener(mapView.getContext(), TAG, null) {
            @Override
            public void onCotOutputRemoved(Bundle bundle) {
                Log.d(TAG, "stream outputremoved");
            }

            @Override
            protected void enabled(CotPortListActivity.CotPort port,
                    boolean enabled) {
                Log.d(TAG, "stream enabled");
            }

            @Override
            protected void connected(CotPortListActivity.CotPort port,
                    boolean connected) {
                Log.d(TAG, "stream connected");
            }

            @Override
            public void onCotOutputUpdated(Bundle descBundle) {
                Log.d(TAG, "stream added/updated");
            }

        };

        printNetworks();

        // If you are using a custom layout you need to make use of the PluginLayoutInflator to clear
        // out the layout cache so that the plugin can be properly unloaded and reloaded.
        helloView = PluginLayoutInflater.inflate(pluginContext,
                R.layout.hello_world_layout, null);

        //Find buttons by id and implement code for long click
        View.OnLongClickListener longClickListener = new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                int id = v.getId();
                if (id == R.id.itemInspect) {
                    toast(context.getString(R.string.itemInspect));
                } else if (id == R.id.webView) {
                    toast(context.getString(R.string.webView));
                }
                return true;
            }
        };

        final Button itemInspect = helloView
                .findViewById(R.id.itemInspect);
        itemInspect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set formatting mode to pretty JSON.
                useCustomFormat = false;
                boolean val = itemInspect.isSelected();
                if (val) {
                    imis.requestEndTool();
                } else {

                    AtakBroadcast.getInstance().registerReceiver(
                            inspectionReceiver,
                            new AtakBroadcast.DocumentedIntentFilter(
                                    "com.atakmap.android.helloworld.InspectionMapItemSelectionTool.Finished"));
                    Bundle extras = new Bundle();
                    ToolManagerBroadcastReceiver.getInstance().startTool(
                            "com.atakmap.android.helloworld.InspectionMapItemSelectionTool",
                            extras);

                }
                itemInspect.setSelected(!val);
            }
        });

        // New button for custom formatting.
        final Button customType = helloView.findViewById(R.id.customType);
        customType.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set formatting mode to custom (human-friendly) mode.
                useCustomFormat = true;
                boolean val = customType.isSelected();
                if (val) {
                    imis.requestEndTool();
                } else {
                    AtakBroadcast.getInstance().registerReceiver(
                            inspectionReceiver,
                            new AtakBroadcast.DocumentedIntentFilter(
                                    "com.atakmap.android.helloworld.InspectionMapItemSelectionTool.Finished"));
                    Bundle extras = new Bundle();
                    ToolManagerBroadcastReceiver.getInstance().startTool(
                            "com.atakmap.android.helloworld.InspectionMapItemSelectionTool",
                            extras);
                }
                customType.setSelected(!val);
            }
        });

        final Button webView = helloView
                .findViewById(R.id.webView);
        webView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setRetain(true);
                Intent webViewIntent = new Intent();

                webViewIntent
                        .setAction(WebViewDropDownReceiver.SHOW_WEBVIEW);
                AtakBroadcast.getInstance().sendBroadcast(webViewIntent);

            }
        });

        final Button mapScreenshot = helloView.findViewById(R.id.mapScreenshot);
        mapScreenshot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Register the screenshot receiver which will process the selected item.
                AtakBroadcast.getInstance().registerReceiver(
                        screenshotReceiver,
                        new AtakBroadcast.DocumentedIntentFilter("com.atakmap.android.helloworld.InspectionMapItemSelectionTool.Finished")
                );
                Bundle extras = new Bundle();
                ToolManagerBroadcastReceiver.getInstance().startTool(
                        "com.atakmap.android.helloworld.InspectionMapItemSelectionTool",
                        extras
                );
                // Optionally, mark the button as selected.
                mapScreenshot.setSelected(true);
            }
        });

        itemInspect.setOnLongClickListener(longClickListener);

    }

    /**
     * This class makes use of a compact class to aid with the selection of map items.   Prior to
     * 3.12, this all had to be manually done playing with the dispatcher and listening for map
     * events.
     */
    public class InspectionMapItemSelectionTool
            extends AbstractMapItemSelectionTool {
        public InspectionMapItemSelectionTool() {
            super(getMapView(),
                    "com.atakmap.android.helloworld.InspectionMapItemSelectionTool",
                    "com.atakmap.android.helloworld.InspectionMapItemSelectionTool.Finished",
                    "Select Map Item on the screen",
                    "Invalid Selection");
        }

        @Override
        protected boolean isItem(MapItem mi) {
            return true;
        }

    }

    final BroadcastReceiver screenshotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Unregister to avoid duplicate handling.
            AtakBroadcast.getInstance().unregisterReceiver(this);

            // Retrieve the UID from the finished tool selection.
            String uid = intent.getStringExtra("uid");
            if (uid == null) {
                Toast.makeText(getMapView().getContext(), "No UID provided", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the selected map item.
            MapItem mi = getMapView().getMapItem(uid);
            if (mi == null) {
                Toast.makeText(getMapView().getContext(), "Map item not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert the map item into a CoT string.
            final CotEvent cotEvent = CotEventFactory.createCotEvent(mi);
            String cotString;
            if (cotEvent != null)
                cotString = cotEvent.toString();
            else
                cotString = "";

            // Try to extract our custom <ipsData> element first;
            // fall back to <remarks> if needed.
            String ipsDataContent = extractIpsData(cotString);
            if (ipsDataContent == null) {
                ipsDataContent = extractRemarks(cotString);
            }

            if (ipsDataContent == null) {
                Toast.makeText(getMapView().getContext(), "No IPS data found in the selected item", Toast.LENGTH_SHORT).show();
                return;
            }

            // Decode HTML entities (if any).
            String decodedData = Html.fromHtml(ipsDataContent, Html.FROM_HTML_MODE_LEGACY).toString().trim();

            // If the decoded data does not look like plain JSON (doesn't start with '{' or '['),
            // then assume it is gzipped Base64 and decompress it.
            if (!decodedData.startsWith("{") && !decodedData.startsWith("[")) {
                String decompressed = decompressGzipBase64(decodedData);
                if (decompressed != null) {
                    decodedData = decompressed;
                } else {
                    Log.e(TAG, "Decompression failed; using original data");
                }
            }

            // At this point, decodedData should contain plain JSON.
            try {
                JSONObject ipsJson = new JSONObject(decodedData);
                // Extract packageUUID (adjust the key if needed).
                String packageUUID = ipsJson.optString("packageUUID", null);
                if (packageUUID == null || packageUUID.isEmpty()) {
                    Toast.makeText(getMapView().getContext(), "No packageUUID found in IPS data", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Construct the URL from the packageUUID.
                final String urlString = "http://localhost:5000/tak/browser/" + packageUUID;
                Log.d(TAG, "Using URL: " + urlString);

                // Perform an HTTP GET to the URL on a background thread.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(urlString);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setConnectTimeout(5000);
                            connection.setReadTimeout(5000);
                            int responseCode = connection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = in.readLine()) != null) {
                                    response.append(line);
                                }
                                in.close();
                                final String htmlPayload = response.toString();

                                // Post the HTML payload to the UI thread.
                                getMapView().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Create and configure the WebView
                                        WebView webView = new WebView(getMapView().getContext());
                                        webView.getSettings().setLoadWithOverviewMode(true);
                                        webView.getSettings().setUseWideViewPort(true);
                                        webView.getSettings().setBuiltInZoomControls(false);
                                        webView.getSettings().setDisplayZoomControls(false);
                                        webView.getSettings().setTextZoom(100);
                                        webView.setInitialScale(100);

                                        // Load the HTML payload
                                        webView.loadDataWithBaseURL(null, htmlPayload, "text/html", "UTF-8", null);

                                        // Create a custom title view with reduced font size and lower padding.
                                        TextView customTitle = new TextView(getMapView().getContext());
                                        customTitle.setText("IPS Record for: " + packageUUID);
                                        customTitle.setTextSize(16);  // Adjust font size as needed (e.g., 16sp instead of the default)
                                        customTitle.setPadding(16, 8, 16, 8); // Adjust padding to reduce overall title height
                                        // Optionally, set text color, background, gravity, etc.
                                        customTitle.setTextColor(Color.WHITE);
                                        customTitle.setGravity(Gravity.CENTER_VERTICAL);
                                        customTitle.setBackgroundColor(Color.parseColor("#0D6EFC"));


                                        // Build and display the AlertDialog with custom title.
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getMapView().getContext());
                                        builder.setCustomTitle(customTitle);
                                        builder.setView(webView);
                                        builder.setPositiveButton("Close", null);
                                        builder.show();
                                    }
                                });
                            } else {
                                getMapView().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getMapView().getContext(),
                                                "Error fetching IPS Record. HTTP code: " + responseCode,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error fetching IPS Record HTML", e);
                            getMapView().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getMapView().getContext(),
                                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing IPS JSON", e);
                Toast.makeText(getMapView().getContext(), "Error parsing IPS data", Toast.LENGTH_SHORT).show();
            }
        }
    };


    final BroadcastReceiver inspectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AtakBroadcast.getInstance().unregisterReceiver(this);
            final Button itemInspect = helloView.findViewById(R.id.itemInspect);
            final Button customType = helloView.findViewById(R.id.customType);
            itemInspect.setSelected(false);
            customType.setSelected(false);

            String uid = intent.getStringExtra("uid");
            if (uid == null)
                return;

            MapItem mi = getMapView().getMapItem(uid);
            if (mi == null)
                return;

            Log.d(TAG, "class: " + mi.getClass());
            Log.d(TAG, "type: " + mi.getType());

            final CotEvent cotEvent = CotEventFactory.createCotEvent(mi);
            String val;
            if (cotEvent != null)
                val = cotEvent.toString();
            else if (mi.hasMetaValue("nevercot"))
                val = "map item set to never persist (nevercot)";
            else
                val = "error turning a map item into CoT";

            // Try to extract our custom <ipsData> element first.
            String dataContent = extractIpsData(val);
            if (dataContent == null) {
                // Fallback: Try to extract <remarks> if <ipsData> doesn't exist.
                dataContent = extractRemarks(val); // assuming extractRemarks() is your preexisting helper
            }

            if (dataContent != null) {
                // HTML-decode (using the API 26 version)
                String decodedData = Html.fromHtml(dataContent, Html.FROM_HTML_MODE_LEGACY).toString().trim();

                // If it doesn't start with a { or [, assume it's compressed.
                if (!decodedData.startsWith("{") && !decodedData.startsWith("[")) {
                    String decompressed = decompressGzipBase64(decodedData);
                    if (decompressed != null) {
                        decodedData = decompressed;
                    } else {
                        Log.e(TAG, "Decompression failed; using original data.");
                    }
                }

                // Now, decodedData should be plain JSON.
                if (decodedData.startsWith("{") || decodedData.startsWith("[")) {
                    try {
                        JSONObject ipsJson = new JSONObject(decodedData);
                        if (useCustomFormat) {
                            String customFormatted = customFormatIPS(ipsJson);
                            val = "Custom Formatted IPS Record:\n" + customFormatted;
                        } else {
                            String formattedIps = ipsJson.toString(4);
                            val = "Formatted IPS Record:\n" + formattedIps;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing IPS JSON", e);
                    }
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getMapView().getContext());
            TextView showText = new TextView(getMapView().getContext());
            showText.setText(val);
            showText.setTextIsSelectable(true);
            showText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager manager = (ClipboardManager) getMapView().getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    manager.setText(((TextView) v).getText());
                    Toast.makeText(v.getContext(), "copied the data", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            builder.setTitle("Resulting CoT");
            builder.setView(showText);
            builder.show();
        }
    };



    /**************************** PUBLIC METHODS *****************************/

    @Override
    public void disposeImpl() {

//        _joystickView.dispose();

        if (issTimer != null) {
            issTimer.cancel();
            issTimer.purge();
            issTimer = null;
        }



        SensorManager sensorManager = (SensorManager) getMapView().getContext()
                .getSystemService(Context.SENSOR_SERVICE);

        TextContainer.getTopInstance().closePrompt();

        imis.dispose();

    }

    /**************************** INHERITED METHODS *****************************/

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "showing hello world drop down");

        final String action = intent.getAction();
        if (action == null)
            return;

        // Show drop-down
        switch (action) {
            case SHOW_HELLO_WORLD:
                if (!isClosed()) {
                    Log.d(TAG, "the drop down is already open");
                    unhideDropDown();
                    return;
                }

                showDropDown(helloView, HALF_WIDTH, FULL_HEIGHT,
                        FULL_WIDTH, HALF_HEIGHT, false, this);
                setAssociationKey("helloWorldPreference");
                List<Contact> allContacts = Contacts.getInstance()
                        .getAllContacts();
                for (Contact c : allContacts) {
                    if (c instanceof IndividualContact)
                        Log.d(TAG, "Contact IP address: "
                                + getIpAddress((IndividualContact) c));
                }

                break;

            // Chat message sent to Hello World contact
            case CHAT_HELLO_WORLD:
                Bundle cotMessage = intent.getBundleExtra(
                        ChatManagerMapComponent.PLUGIN_SEND_MESSAGE_EXTRA);

                String msg = cotMessage.getString("message");

//                if (!FileSystemUtils.isEmpty(msg)) {
//                    // Display toast to show the message was received
//                    toast(helloContact.getName() + " received: " + msg);
//                }
                break;

            // Sending CoT to Hello World contact
            case SEND_HELLO_WORLD:
                // Map item UID
                String uid = intent.getStringExtra("targetUID");
                MapItem mapItem = getMapView().getRootGroup().deepFindUID(uid);
//                if (mapItem != null) {
//                    // Display toast to show the CoT was received
//                    toast(helloContact.getName() + " received request to send: "
//                            + ATAKUtilities.getDisplayName(mapItem));
//                }
                break;
        }
    }

    public NetConnectString getIpAddress(IndividualContact ic) {
        Connector ipConnector = ic.getConnector(IpConnector.CONNECTOR_TYPE);
        if (ipConnector != null) {
            String connectString = ipConnector.getConnectionString();
            return NetConnectString.fromString(connectString);
        } else {
            return null;
        }

    }

    @Override
    protected void onStateRequested(int state) {
        if (state == DROPDOWN_STATE_FULLSCREEN) {
            if (!isPortrait()) {
                if (Double.compare(currWidth, HALF_WIDTH) == 0) {
                    resize(FULL_WIDTH - HANDLE_THICKNESS_LANDSCAPE,
                            FULL_HEIGHT);
                }
            } else {
                if (Double.compare(currHeight, HALF_HEIGHT) == 0) {
                    resize(FULL_WIDTH, FULL_HEIGHT - HANDLE_THICKNESS_PORTRAIT);
                }
            }
        } else if (state == DROPDOWN_STATE_NORMAL) {
            if (!isPortrait()) {
                resize(HALF_WIDTH, FULL_HEIGHT);
            } else {
                resize(FULL_WIDTH, HALF_HEIGHT);
            }
        }
    }

    @Override
    public void onDropDownSelectionRemoved() {
    }

    @Override
    public void onDropDownVisible(boolean v) {
    }

    @Override
    public void onDropDownSizeChanged(double width, double height) {
        currWidth = width;
        currHeight = height;
    }

    @Override
    public void onDropDownClose() {

        // make sure that if the Map Item inspector is running
        // turn off the map item inspector
        final Button itemInspect = helloView
                .findViewById(R.id.itemInspect);
        boolean val = itemInspect.isSelected();
        if (val) {
            itemInspect.setSelected(false);
            imis.requestEndTool();

        }

    }

    /************************* Helper Methods *************************/

//    final BroadcastReceiver fordReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getMapView().getContext(),
//                    "Ford Tow Truck Application", Toast.LENGTH_SHORT).show();
//        }
//    };

    private String decompressGzipBase64(String base64Encoded) {
        try {
            // Decode Base64 to get the compressed byte array.
            byte[] compressedBytes = Base64.getDecoder().decode(base64Encoded);
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedBytes);
            GZIPInputStream gis = new GZIPInputStream(bis);
            InputStreamReader reader = new InputStreamReader(gis, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line).append("\n");
            }
            in.close();
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error decompressing gzip Base64 string", e);
            return null;
        }
    }

    // Flag to control which formatting mode to use.
    private boolean useCustomFormat = false;

    // Helper method to recursively transform the IPS JSONObject into a more user-friendly string.
// This example omits keys named "_id". You can adjust the logic to remove quote marks or other characters.
    private String customFormatIPS(JSONObject json) throws JSONException {
        StringBuilder sb = new StringBuilder();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            // Optionally skip keys you don't want to display, such as those containing _id.
            if (key.equals("_id")) continue;
            Object value = json.get(key);
            // Format nested objects recursively
            if (value instanceof JSONObject) {
                sb.append(key).append(":\n");
                sb.append(customFormatIPS((JSONObject) value)).append("\n");
            }
            // Handle arrays by iterating over their elements
            else if (value instanceof org.json.JSONArray) {
                sb.append(key).append(":\n");
                org.json.JSONArray arr = (org.json.JSONArray) value;
                for (int i = 0; i < arr.length(); i++) {
                    Object elem = arr.get(i);
                    if (elem instanceof JSONObject) {
                        sb.append(customFormatIPS((JSONObject) elem)).append("\n");
                    } else {
                        sb.append(elem.toString()).append("\n");
                    }
                }
            } else {
                // Append key and value without quotes.
                sb.append(key).append(": ").append(value.toString()).append("\n");
            }
        }
        return sb.toString();
    }


    private String extractRemarks(String cotMessage) {
        // Use a regex pattern to capture content between <remarks> and </remarks>
        Pattern pattern = Pattern.compile("<remarks>(.*?)</remarks>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cotMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractIpsData(String cotMessage) {
        Pattern pattern = Pattern.compile("<ipsData[^>]*>(.*?)</ipsData>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cotMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private void toast(String str) {
        Toast.makeText(getMapView().getContext(), str,
                Toast.LENGTH_LONG).show();
    }

    void printNetworks() {
        /*
         *    CotPort.DESCRIPTION_KEY
         *    CotPort.ENABLED_KEY
         *    CotPort.CONNECTED_KEY
         *    CotPort.CONNECT_STRING_KEY
         */
        Bundle b = CommsMapComponent.getInstance().getAllPortsBundle();
        Bundle[] streams = (Bundle[]) b.getParcelableArray("streams");
        Bundle[] outputs = (Bundle[]) b.getParcelableArray("outputs");
        Bundle[] inputs = (Bundle[]) b.getParcelableArray("inputs");
        if (inputs != null) {
            for (Bundle input : inputs)
                Log.d(TAG, "input " + input.getString(CotPort.DESCRIPTION_KEY)
                        + ": " + input.getString(CotPort.CONNECT_STRING_KEY));
        }
        if (outputs != null) {
            for (Bundle output : outputs)
                Log.d(TAG, "output " + output.getString(CotPort.DESCRIPTION_KEY)
                        + ": " + output.getString(CotPort.CONNECT_STRING_KEY));
        }
        if (streams != null) {
            for (Bundle stream : streams)
                Log.d(TAG, "stream " + stream.getString(CotPort.DESCRIPTION_KEY)
                        + ": " + stream.getString(CotPort.CONNECT_STRING_KEY));
        }
    }


    public void removeContact(Contact contact) {
        Contacts.getInstance().removeContact(contact);
    }


}
