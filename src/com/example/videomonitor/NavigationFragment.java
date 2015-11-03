package com.example.videomonitor;

import java.util.ArrayList;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.navi.demo.util.AMapUtil;
import com.amap.navi.demo.util.ToastUtil;
import com.amap.navi.demo.util.Utils;
public class NavigationFragment extends DialogFragment implements LocationSource, AMapLocationListener,
OnGeocodeSearchListener,OnClickListener, AMapNaviListener
{
	
	private MapView mapView;
	private AMap aMap;
	private Marker marker;// 定位雷达小图标
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;
	private EditText endpoint;
	// 驾车线路：路线规划、模拟导航、实时导航按钮
	private Button mDriveRouteButton;
	private Button mDriveEmulatorButton;
	private Button mDriveNaviButton;
	// 步行线路：路线规划、模拟导航、实时导航按钮
	private Button mFootRouteButton;
	private Button mFootEmulatorButton;
	private Button mFootNaviButton;
	private Button ok;
	private AMapNavi mAMapNavi;
	private GeocodeSearch geocoderSearch;
	private String addressName;
	private NaviLatLng mStartPoint = new NaviLatLng();
	private LatLonPoint latLonPoint ;

	// 起点终点坐标
	//private NaviLatLng mNaviStart = new NaviLatLng(39.989614, 116.481763);
	private NaviLatLng mNaviEnd ;
	//= new NaviLatLng(34.45, 108.56);
	private LatLng latlng=new LatLng(34.45, 108.56);
	// 起点终点列表
	private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
	private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
	// 规划线路
	private RouteOverLay mRouteOverLay;
	// 是否驾车和是否计算成功的标志
	private boolean mIsDriveMode = true;
	private boolean mIsCalculateRouteSuccess = false;
	private boolean mIsGetGPS = false;// 记录GPS定位是否成功
	private Marker mGPSMarker;

	private Marker geoMarker;
	private Marker regeoMarker;
	//定位
		private LocationManagerProxy mLocationManger;
		
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View navigationView = inflater.inflate(R.layout.navigation_layout,container, false);
	    /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
	    //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置

		mAMapNavi = AMapNavi.getInstance(getActivity());
		mAMapNavi.setAMapNaviListener(this);
		geocoderSearch = new GeocodeSearch(getActivity());
		geocoderSearch.setOnGeocodeSearchListener(this);
		mapView = (MapView) navigationView.findViewById(R.id.simple_route_map);
		endpoint=(EditText)navigationView.findViewById(R.id.end_position_textview);
		

		mDriveNaviButton = (Button)navigationView. findViewById(R.id.car_navi_navi);
		mDriveEmulatorButton = (Button) navigationView.findViewById(R.id.car_navi_emulator);
		mDriveRouteButton = (Button)navigationView. findViewById(R.id.car_navi_route);
		ok = (Button)navigationView. findViewById(R.id.button1);

		mFootRouteButton = (Button) navigationView.findViewById(R.id.foot_navi_route);
		mFootEmulatorButton = (Button)navigationView. findViewById(R.id.foot_navi_emulator);
		mFootNaviButton = (Button) navigationView.findViewById(R.id.foot_navi_navi);

		mDriveNaviButton.setOnClickListener(this);
		mDriveEmulatorButton.setOnClickListener(this);
		mDriveRouteButton.setOnClickListener(this);
		ok.setOnClickListener(this);

		mFootRouteButton.setOnClickListener(this);
		mFootEmulatorButton.setOnClickListener(this);
		mFootNaviButton.setOnClickListener(this);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
		//aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15));
		mRouteOverLay = new RouteOverLay(aMap, null);
		
		return navigationView;
	}
	
	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}

	
	
	//计算驾车路线
		private void calculateDriveRoute() {
			
			boolean isSuccess = mAMapNavi.calculateDriveRoute(mStartPoints,
					mEndPoints, null, AMapNavi.DrivingDefault);
			if (!isSuccess) {
				showToast("路线计算失败,检查参数情况");
			}

		}
		//计算步行路线
		private void calculateFootRoute() {
			
			boolean isSuccess = mAMapNavi.calculateWalkRoute(mStartPoint, mNaviEnd);
			if (!isSuccess) {
				showToast("路线计算失败,检查参数情况");
			}
		}
		
		private void showToast(String message) {
			Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
		}

		private void startEmulatorNavi(boolean isDrive) {
			if ((isDrive && mIsDriveMode && mIsCalculateRouteSuccess)
					|| (!isDrive && !mIsDriveMode && mIsCalculateRouteSuccess)) {
				Intent emulatorIntent = new Intent(getActivity().getApplicationContext(),SimpleNaviActivity.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Utils.ISEMULATOR, true);
				bundle.putInt(Utils.ACTIVITYINDEX, Utils.SIMPLEROUTENAVI);
				emulatorIntent.putExtras(bundle);
				emulatorIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				getActivity().startActivity(emulatorIntent);

			} else {
				showToast("请先进行相对应的路径规划，再进行导航");
			}
		}

		private void startGPSNavi(boolean isDrive) {
			if ((isDrive && mIsDriveMode && mIsCalculateRouteSuccess)
					|| (!isDrive && !mIsDriveMode && mIsCalculateRouteSuccess)) {
				Intent gpsIntent = new Intent(getActivity()
						.getApplicationContext(),SimpleNaviActivity.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean(Utils.ISEMULATOR, false);
				bundle.putInt(Utils.ACTIVITYINDEX, Utils.SIMPLEROUTENAVI);
				gpsIntent.putExtras(bundle);
				gpsIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				getActivity().startActivity(gpsIntent);
			} else {
				showToast("请先进行相对应的路径规划，再进行导航");
			}
		}
	//-------------------------Button点击事件和返回键监听事件---------------------------------
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button1:
				addressName=endpoint.getText().toString().trim();
				getLatlon(addressName);
				break;
			case R.id.car_navi_route:
				mIsCalculateRouteSuccess = false;
				mIsDriveMode = true;
				
				calculateDriveRoute();
				break;
			case R.id.car_navi_emulator:
				startEmulatorNavi(true);
				break;
			case R.id.car_navi_navi:
				startGPSNavi(true);
				break;
			case R.id.foot_navi_route:
				mIsCalculateRouteSuccess = false;
				mIsDriveMode = false;
				calculateFootRoute();
				break;
			case R.id.foot_navi_emulator:
				startEmulatorNavi(false);
				break;
			case R.id.foot_navi_navi:
				startGPSNavi(false);
				break;

			}

		}
		
		//--------------------导航监听回调事件-----------------------------
		@Override
		public void onArriveDestination() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onArrivedWayPoint(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCalculateRouteFailure(int arg0) {
			showToast("路径规划出错" + arg0);
			mIsCalculateRouteSuccess = false;
		}

		@Override
		public void onCalculateRouteSuccess() {
			AMapNaviPath naviPath = mAMapNavi.getNaviPath();
			if (naviPath == null) {
				return;
			}
			// 获取路径规划线路，显示到地图上
			mRouteOverLay.setRouteInfo(naviPath);
			mRouteOverLay.addToMap();
			mIsCalculateRouteSuccess = true;
		}

		@Override
		public void onEndEmulatorNavi() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetNavigationText(int arg0, String arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGpsOpenStatus(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onInitNaviFailure() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onInitNaviSuccess() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLocationChange(AMapNaviLocation arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onNaviInfoUpdated(AMapNaviInfo arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReCalculateRouteForTrafficJam() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReCalculateRouteForYaw() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStartNavi(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTrafficStatusUpdate() {
			// TODO Auto-generated method stub

		}

	
	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point1));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point2));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point3));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point4));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point5));
		giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.point6));
		marker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.icons(giflist).period(50));
		
		// 自定义系统定位小蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.location_marker));// 设置小蓝点的图标
		myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
		myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
		// myLocationStyle.anchor(int,int)//设置小蓝点的锚点
		myLocationStyle.strokeWidth(0.1f);// 设置圆形的边框粗细
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setMyLocationRotateAngle(180);
		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		//设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种 
		aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,10));
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null && aLocation != null) {
			mListener.onLocationChanged(aLocation);// 显示系统小蓝点
			mStartPoint = new NaviLatLng(aLocation.getLatitude(),
					aLocation.getLongitude());
			marker.setPosition(new LatLng(aLocation.getLatitude(), aLocation
					.getLongitude()));// 定位雷达小图标
			mStartPoints.clear();
			mStartPoints.add(mStartPoint);
			latLonPoint=new LatLonPoint(aLocation.getLatitude(), aLocation
					.getLongitude());
			//getAddress(latLonPoint);
			
			float bearing = aMap.getCameraPosition().bearing;
			aMap.setMyLocationRotateAngle(bearing);// 设置小蓝点旋转角度
		}
	}
	

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(getActivity());
			/*
			 * mAMapLocManager.setGpsEnable(false);
			 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
			 * API定位采用GPS和网络混合定位方式
			 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
			 */
			mAMapLocationManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mAMapLocationManager != null) {
			mAMapLocationManager.removeUpdates(this);
			mAMapLocationManager.destory();
		}
		mAMapLocationManager = null;
	}

	
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNaviInfoUpdate(NaviInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 地理编码查询回调
	 */
	@Override
	public void onGeocodeSearched(GeocodeResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getGeocodeAddressList() != null
					&& result.getGeocodeAddressList().size() > 0) {
				GeocodeAddress address = result.getGeocodeAddressList().get(0);
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
				//geoMarker.setPosition(AMapUtil.convertToLatLng(address.getLatLonPoint()));
				mNaviEnd = new NaviLatLng(address.getLatLonPoint().getLatitude(), address.getLatLonPoint().getLongitude());
				mEndPoints.clear();
				mEndPoints.add(mNaviEnd);
				addressName = "经纬度值:" + address.getLatLonPoint() + "\n位置描述:"
						+ address.getFormatAddress();
				ToastUtil.show(getActivity().getApplicationContext(), addressName);
			} else {
				ToastUtil.show(getActivity().getApplicationContext(), "没有结果");
			}
		} else if (rCode == 27) {
			ToastUtil.show(getActivity().getApplicationContext(), "没有结果");
		} else if (rCode == 32) {
			ToastUtil.show(getActivity().getApplicationContext(), "没有结果");
		} else {
			ToastUtil.show(getActivity().getApplicationContext(),
					"没有结果");
		}
	}

	/**
	 * 逆地理编码回调
	 */
	@Override
	public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getRegeocodeAddress() != null
					&& result.getRegeocodeAddress().getFormatAddress() != null) {
				addressName = result.getRegeocodeAddress().getFormatAddress()
						+ "附近";
				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						AMapUtil.convertToLatLng(latLonPoint), 15));
				//regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
				ToastUtil.show(getActivity().getApplicationContext(), addressName);
			} else {
				ToastUtil.show(getActivity().getApplicationContext(), "没有结果");
			}
		} else if (rCode == 27) {
			ToastUtil.show(getActivity().getApplicationContext(), "没有结果");
		} else if (rCode == 32) {
			ToastUtil.show(getActivity().getApplicationContext(), "没有结果");
		} else {
			ToastUtil.show(getActivity().getApplicationContext(),
					"没有结果");
		}
	}
	/**
	 * 响应地理编码
	 */
	public void getLatlon(final String name) {
		GeocodeQuery query = new GeocodeQuery(name, "西安");// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
		geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
	}

	/**
	 * 响应逆地理编码
	 */
	public void getAddress(final LatLonPoint latLonPoint) {
		RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
				GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
		geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
	}
	
}