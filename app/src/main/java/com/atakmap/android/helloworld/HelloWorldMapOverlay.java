
package com.atakmap.android.helloworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.atakmap.android.helloworld.plugin.R;
//import com.atakmap.android.helloworld.samplelayer.ExampleLayer;
//import com.atakmap.android.helloworld.samplelayer.ExampleMultiLayer;
import com.atakmap.android.hierarchy.HierarchyListFilter;
import com.atakmap.android.hierarchy.HierarchyListItem;
import com.atakmap.android.hierarchy.action.GoTo;
import com.atakmap.android.hierarchy.action.Search;
import com.atakmap.android.hierarchy.action.Visibility;
import com.atakmap.android.hierarchy.action.Visibility2;
import com.atakmap.android.hierarchy.items.AbstractHierarchyListItem2;
import com.atakmap.android.hierarchy.items.MapItemUser;
import com.atakmap.android.maps.DeepMapItemQuery;
import com.atakmap.android.maps.DefaultMapGroup;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.overlay.AbstractMapOverlay2;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Example map overlay that is displayed within Overlay Manager
 */
public class HelloWorldMapOverlay extends AbstractMapOverlay2 {

    private static final String TAG = "HelloWorldMapOverlay";

    private final MapView _mapView;
    private final Context _plugin;
    private final HelloWorldDeepMapItemQuery _query;
    private final DefaultMapGroup _group;

    private HelloWorldListModel _listModel;

    public HelloWorldMapOverlay(MapView mapView, Context plugin) {
        _mapView = mapView;
        _plugin = plugin;
        _query = new HelloWorldDeepMapItemQuery();
        _group = new DefaultMapGroup("Hello World Map Group");
        _group.setMetaBoolean("addToObjList", false);
    }

    @Override
    public String getIdentifier() {
        return TAG;
    }

    @Override
    public String getName() {
        return _plugin.getString(R.string.hello_world);
    }

    @Override
    public MapGroup getRootGroup() {
        return _group;
    }

    @Override
    public DeepMapItemQuery getQueryFunction() {
        return _query;
    }

    @Override
    public HierarchyListItem getListModel(BaseAdapter adapter,
            long capabilities, HierarchyListFilter prefFilter) {
        if (_listModel == null)
            _listModel = new HelloWorldListModel();
        _listModel.refresh(adapter, prefFilter);
        return _listModel;
    }


    public class HelloWorldListModel extends AbstractHierarchyListItem2
            implements Search, Visibility2, View.OnClickListener {

        private final static String TAG = "HelloWorldListModel";

        private View _header, _footer;

        public HelloWorldListModel() {
            this.asyncRefresh = true;
        }

        @Override
        public String getTitle() {
            return HelloWorldMapOverlay.this.getName();
        }

        @Override
        public String getIconUri() {
            return "android.resource://" + _plugin.getPackageName()
                    + "/" + R.drawable.ic_launcher;
        }

        public int getPreferredListIndex() {
            return 5;
        }

        @Override
        public int getDescendantCount() {
            return 0;
        }

        @Override
        public Object getUserObject() {
            return this;
        }

        @Override
        public View getExtraView() {
            return null;
        }

        @Override
        public View getHeaderView() {
            if (_header == null) {
                _header = LayoutInflater.from(_plugin).inflate(
                        R.layout.overlay_header, _mapView, false);
                _header.findViewById(R.id.header_button)
                        .setOnClickListener(this);
            }
            return _header;
        }

        @Override
        public View getFooterView() {
            if (_footer == null) {
                _footer = LayoutInflater.from(_plugin).inflate(
                        R.layout.overlay_footer, _mapView, false);
                _footer.findViewById(R.id.footer_button)
                        .setOnClickListener(this);
            }
            return _footer;
        }

        @Override
        public void refreshImpl() {

        }

        @Override
        public void dispose() {
            disposeChildren();
        }

        @Override
        public boolean hideIfEmpty() {
            return true;
        }

        @Override
        public boolean isMultiSelectSupported() {
            return false;
        }

        @Override
        public boolean setVisible(boolean visible) {
            List<Visibility> actions = getChildActions(Visibility.class);
            boolean ret = !actions.isEmpty();
            for (Visibility del : actions)
                ret &= del.setVisible(visible);
            return ret;
        }

        @Override
        public Set<HierarchyListItem> find(String searchTerms) {
            searchTerms = searchTerms.toLowerCase();
            Set<HierarchyListItem> results = new HashSet<>();
            List<HierarchyListItem> items = getChildren();
            for (HierarchyListItem item : items) {
                if (item.getTitle().toLowerCase().contains(searchTerms))
                    results.add(item);
            }
            return results;
        }

        @Override
        public void onClick(View v) {
            if (v instanceof Button)
                Toast.makeText(_mapView.getContext(),
                        ((Button) v).getText(),
                        Toast.LENGTH_LONG).show();
        }
    }

    private class LayerHierarchyListItem extends AbstractHierarchyListItem2
            implements Visibility, GoTo, MapItemUser {



        @Override
        public String getTitle() {
//            if (_layer != null) {
//                return _layer.getName();
//            }
//            if (_multilayer != null) {
//                return _multilayer.getName();
//            }
            return "";
        }

        @Override
        public String getDescription() {
            return _plugin.getString(R.string.example_layer_description);
        }

        @Override
        public String getIconUri() {
            return "android.resource://" + _mapView.getContext()
                    .getPackageName() + "/"
                    + com.atakmap.app.R.drawable.ic_overlay_gridlines;
        }

        @Override
        public Object getUserObject() {
//            if (_layer != null) {
//                return _layer;
//            }
//            if (_multilayer != null) {
//                return _multilayer;
//            }
            return null;
        }

        @Override
        public boolean isChildSupported() {
            return false;
        }

        @Override
        public int getDescendantCount() {
            return 0;
        }

        @Override
        public void refreshImpl() {
        }

        @Override
        public boolean hideIfEmpty() {
            return false;
        }

        @Override
        public boolean setVisible(boolean visible) {
//            if (_layer != null) {
//                if (visible != _layer.isVisible()) {
//                    _layer.setVisible(visible);
//                    return true;
//                }
//            }
//            if (_multilayer != null) {
//                if (visible != _multilayer.isVisible()) {
//                    _multilayer.setVisible(visible);
//                    return true;
//                }
//            }
            return false;
        }

        @Override
        public boolean isVisible() {
//            if (_layer != null) {
//                return _layer.isVisible();
//            }
//            if (_multilayer != null) {
//                return _multilayer.isVisible();
//            }
            return false;
        }

        @Override
        public MapItem getMapItem() {
//            if (_layer != null) {
//                return _layer.getMetaShape();
//            }
//            if (_multilayer != null) {
//                return _multilayer.getMetaShape();
//            }
            return null;
        }

        @Override
        public boolean goTo(boolean select) {
//            if (_layer != null) {
//                ATAKUtilities.scaleToFit(_mapView, _layer.getPoints(),
//                        _mapView.getWidth(), _mapView.getHeight());
//                if (select) {
//                    MenuLayoutWidget mw = MapMenuReceiver.getMenuWidget();
//                    if (mw != null) {
//                        mw.openMenuOnItem(_layer.getMetaShape());
//                        return true;
//                    }
//                }
//            }
//            if (_multilayer != null) {
//                ATAKUtilities.scaleToFit(_mapView, _multilayer.getPoints(),
//                        _mapView.getWidth(), _mapView.getHeight());
//                if (select) {
//                    MenuLayoutWidget mw = MapMenuReceiver.getMenuWidget();
//                    if (mw != null) {
//                        mw.openMenuOnItem(_multilayer.getMetaShape());
//                        return true;
//                    }
//                }
//            }
            return false;
        }
    }

    private class HelloWorldDeepMapItemQuery implements DeepMapItemQuery {

        @Override
        public MapItem deepFindItem(Map<String, String> metadata) {
            return null;
        }

        @Override
        public List<MapItem> deepFindItems(Map<String, String> metadata) {
            return null;
        }

        @Override
        public MapItem deepFindClosestItem(GeoPoint location, double threshold,
                Map<String, String> metadata) {
            return null;
        }

        @Override
        public Collection<MapItem> deepFindItems(GeoPoint location,
                double radius, Map<String, String> metadata) {
            return null;
        }

        @Override
        public MapItem deepHitTest(int xpos, int ypos, GeoPoint point,
                MapView view) {
//            for (ExampleLayer l : getLayers()) {
//                if (l.isVisible() && l.getBounds().contains(point))
//                    return l.getMetaShape();
//            }
//            for (ExampleMultiLayer l : getMultiLayers()) {
//                if (l.isVisible() && l.getBounds().contains(point))
//                    return l.getMetaShape();
//            }
            return null;
        }

        @Override
        public SortedSet<MapItem> deepHitTestItems(int xpos, int ypos,
                GeoPoint point, MapView view) {
            SortedSet<MapItem> ret = new TreeSet<>(
                    MapItem.ZORDER_HITTEST_COMPARATOR);
//            for (ExampleLayer l : getLayers()) {
//                if (l.isVisible() && l.getBounds().contains(point))
//                    ret.add(l.getMetaShape());
//            }
//            for (ExampleMultiLayer l : getMultiLayers()) {
//                if (l.isVisible() && l.getBounds().contains(point))
//                    ret.add(l.getMetaShape());
//            }
            return ret;
        }

        @Override
        public Collection<MapItem> deepFindItems(GeoBounds bounds,
                Map<String, String> metadata) {
            SortedSet<MapItem> ret = new TreeSet<>(
                    MapItem.ZORDER_HITTEST_COMPARATOR);
//            for (ExampleLayer l : getLayers()) {
//                if (l.isVisible() && l.getBounds().intersects(bounds))
//                    ret.add(l.getMetaShape());
//            }
//            for (ExampleMultiLayer l : getMultiLayers()) {
//                if (l.isVisible() && l.getBounds().intersects(bounds))
//                    ret.add(l.getMetaShape());
//            }
            return ret;
        }
    }
}
